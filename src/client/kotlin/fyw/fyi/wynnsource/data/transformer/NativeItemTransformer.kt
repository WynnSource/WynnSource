package fyw.fyi.wynnsource.data.transformer

import com.wynntils.core.components.Models
import com.wynntils.models.spells.type.SpellType
import com.wynntils.models.stats.type.StatType
import fyw.fyi.wynnsource.schema.WynnSourceItemOuterClass
import fyw.fyi.wynnsource.schema.common.Components
import fyw.fyi.wynnsource.schema.common.Enums
import fyw.fyi.wynnsource.schema.common.damageRange
import fyw.fyi.wynnsource.schema.common.defense
import fyw.fyi.wynnsource.schema.common.identification
import fyw.fyi.wynnsource.schema.common.requirements
import fyw.fyi.wynnsource.schema.item.GearOuterClass
import fyw.fyi.wynnsource.schema.item.armorStats
import fyw.fyi.wynnsource.schema.item.gear
import fyw.fyi.wynnsource.schema.item.unidentifiedGear
import fyw.fyi.wynnsource.schema.item.weaponStats
import fyw.fyi.wynnsource.schema.wynnSourceItem
import fyw.fyi.wynnsource.utils.FontUtils
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.ItemStack
import net.minecraft.text.PlainTextContent
import net.minecraft.text.StyleSpriteSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.regex.Pattern
import kotlin.math.roundToInt

@Suppress("UNUSED")
object NativeItemTransformer : ItemTransformer<ItemStack>() {
    override fun serialize(item: ItemStack): WynnSourceItemOuterClass.WynnSourceItem {
        // TODO Currently only unidentified gear is supported for beta
        val itemName = UNID_NAME_PATTERN.matcher(getItemStackName(item)).takeIf { it.find() }?.group(1)
            ?: error("Item type not supported yet")
        val itemLore = getItemStackLore(item)

        var parsedLevel: Int? = null
        var parsedRarity: Enums.Rarity? = null
        var parsedGearType: GearOuterClass.GearType? = null
        var parsedRequirements: Components.Requirements? = null
        var parsedAttackSpeed: GearOuterClass.AttackSpeed? = null
        var parsedHealth: Int? = null

        val parsedIdentifications = mutableListOf<Components.Identification>()
        val parsedDamages = mutableListOf<Components.DamageRange>()
        val parsedDefenses = mutableListOf<Components.Defense>()
        var section = 0
        var isMajorSection = false
        val parsedMajorId: MutableList<String> = mutableListOf()

        for (line in itemLore) {
            if (isDivider(line)) {
                section++
                continue
            }

            when (section) {
                0 -> {
                    // First section, contains name, rarity, gear type, weapon/armor stats
                    if (parsedRarity == null) {
                        parsedRarity = toRarity(line)
                    }
                    if (parsedGearType == null) {
                        parsedGearType = toGearType(line)
                    }
                    if (parsedHealth == null) {
                        parsedHealth = extractHealth(line)
                    }
                    extractDamage(line)?.let { parsedDamages.addAll(it) }
                    extractDef(line)?.let { parsedDefenses.addAll(it) }
                    if (parsedAttackSpeed == null) {
                        parsedAttackSpeed = toAttackSpeed(line)
                    }
                }

                1 -> {
                    // Second section, contains requirements, level
                    if (parsedRequirements == null) {
                        parsedRequirements = extractRequirements(line)
                    }
                    if (parsedLevel == null) {
                        LEVEL_PATTERN.matcher(line.string).takeIf { it.find() }?.group(1)?.toIntOrNull()
                            ?.let { parsedLevel = it }
                    }
                }

                2 -> {
                    // Third section, only identifications
                    extractIdentifications(line)?.let { parsedIdentifications.add(it) }
                    if (isMajor(line)) {
                        isMajorSection = true
                    }
                    if (isPager(line)) {
                        isMajorSection = false
                    }
                    if (isMajorSection) {
                        parsedMajorId.add(FontUtils.filterAscii(line.string))
                    }
                }

                else -> {
                    // Ignore other sections for now
                }
            }
        }

        val finalGearType = parsedGearType ?: error("GearType not found in lore")

        return wynnSourceItem {
            name = itemName
            level = parsedLevel ?: error("Level not found in lore")
            rarity = parsedRarity ?: error("Rarity not found in lore")

            gear = gear {
                type = finalGearType
                requirements = parsedRequirements ?: error("Requirements not found in lore")
                majorId = parsedMajorId.joinToString(" ")

                unidentified = unidentifiedGear {
                    identifications += parsedIdentifications.ifEmpty { error("Identifications not found in lore") }
                }

                if (finalGearType.isWeapon()) {
                    weaponStats = weaponStats {
                        attackSpeed = parsedAttackSpeed ?: error("AttackSpeed not found in lore")
                        damages += parsedDamages.ifEmpty { error("Damage not found in lore") }
                    }
                } else {
                    armorStats = armorStats {
                        health = parsedHealth ?: error("Health not found in lore")
                        defenses += parsedDefenses.ifEmpty { error("Defense not found in lore") }
                    }
                }
            }
        }
    }

    override fun deserialize(item: WynnSourceItemOuterClass.WynnSourceItem): ItemStack {
        TODO("Not yet implemented")
    }

    // ========================================================================
    // Shared Helpers
    // ========================================================================

    // \uE0008 is the lock in the name
    private val UNID_NAME_PATTERN = Pattern.compile("^\uDAFC\uDC00\uE008\uDB00\uDC02(.*?)À?\uDAFC\uDC00$")
    private val LEVEL_PATTERN = Pattern.compile("^.*?Combat Level.*?(\\d+)$")
    private val HEALTH_PATTERN = Pattern.compile("^.([+-][\\d,]+) Health$")
    private val ID_PATTERN = Pattern.compile(
        "^([\\w\\s]+).*?([+-][\\d,]+)(?:/\\ds|\\stier|%)?(?:\\sto\\s([+-][\\d,]+)(/\\ds|\\stier|%)?)?$"
    )
    private val ATTACK_SPEED_PATTERN = Pattern.compile("^.*?\uE007\\s([\\w\\s]+) \\(.*$")

    private enum class TooltipElement(val char: Char, val wcs: Enums.Element) {
        EARTH('\uE000', Enums.Element.ELEMENT_EARTH),
        THUNDER('\uE001', Enums.Element.ELEMENT_THUNDER),
        WATER('\uE002', Enums.Element.ELEMENT_WATER),
        FIRE('\uE003', Enums.Element.ELEMENT_FIRE),
        AIR('\uE004', Enums.Element.ELEMENT_AIR),
        NEUTRAL('\uE005', Enums.Element.ELEMENT_NEUTRAL);

        companion object {
            fun fromChar(char: Char): Enums.Element? {
                return entries.find { it.char == char }?.wcs
            }
        }
    }

    private val FONT_BANNER_BOX = StyleSpriteSource.Font(Identifier.of("minecraft:banner/box"))

    private fun toRarity(text: Text): Enums.Rarity? {
        // Recursive find all text with style={font=Font[id=minecraft:banner/box]}
        if (text.content != null && text.style.font == FONT_BANNER_BOX) {
            when (FontUtils.filterAscii(FontUtils.fromBanner((text.content as PlainTextContent).string()))) {
                "COMMON" -> return Enums.Rarity.RARITY_COMMON
                "UNIQUE" -> return Enums.Rarity.RARITY_UNIQUE
                "RARE" -> return Enums.Rarity.RARITY_RARE
                "FABLED" -> return Enums.Rarity.RARITY_FABLED
                "LEGENDARY" -> return Enums.Rarity.RARITY_LEGENDARY
                "MYTHIC" -> return Enums.Rarity.RARITY_MYTHIC
            }
        }

        return text.siblings.firstNotNullOfOrNull { toRarity(it) }
    }

    private fun toGearType(text: Text): GearOuterClass.GearType? {
        // Recursive find all text with style={font=Font[id=minecraft:banner/box]}
        if (text.content != null && text.style.font == FONT_BANNER_BOX) {
            when (FontUtils.filterAscii(FontUtils.fromBanner((text.content as PlainTextContent).string()))) {
                "SPEAR" -> return GearOuterClass.GearType.GEAR_TYPE_SPEAR
                "BOW" -> return GearOuterClass.GearType.GEAR_TYPE_BOW
                "WAND" -> return GearOuterClass.GearType.GEAR_TYPE_WAND
                "DAGGER" -> return GearOuterClass.GearType.GEAR_TYPE_DAGGER
                "RELIK" -> return GearOuterClass.GearType.GEAR_TYPE_RELIK
                "HELMET" -> return GearOuterClass.GearType.GEAR_TYPE_HELMET
                "CHESTPLATE" -> return GearOuterClass.GearType.GEAR_TYPE_CHESTPLATE
                "LEGGINGS" -> return GearOuterClass.GearType.GEAR_TYPE_LEGGINGS
                "BOOTS" -> return GearOuterClass.GearType.GEAR_TYPE_BOOTS
                "RING" -> return GearOuterClass.GearType.GEAR_TYPE_RING
                "BRACELET" -> return GearOuterClass.GearType.GEAR_TYPE_BRACELET
                "NECKLACE" -> return GearOuterClass.GearType.GEAR_TYPE_NECKLACE
            }
        }

        return text.siblings.firstNotNullOfOrNull { toGearType(it) }
    }

    private fun toAttackSpeed(text: Text): GearOuterClass.AttackSpeed? {
        val match = ATTACK_SPEED_PATTERN.matcher(text.string).takeIf { it.find() } ?: return null
        return when (match.group(1)) {
            "Super Slow" -> GearOuterClass.AttackSpeed.ATTACK_SPEED_SUPER_SLOW
            "Very Slow" -> GearOuterClass.AttackSpeed.ATTACK_SPEED_VERY_SLOW
            "Slow" -> GearOuterClass.AttackSpeed.ATTACK_SPEED_SLOW
            "Normal" -> GearOuterClass.AttackSpeed.ATTACK_SPEED_NORMAL
            "Fast" -> GearOuterClass.AttackSpeed.ATTACK_SPEED_FAST
            "Very Fast" -> GearOuterClass.AttackSpeed.ATTACK_SPEED_VERY_FAST
            "Super Fast" -> GearOuterClass.AttackSpeed.ATTACK_SPEED_SUPER_FAST
            else -> null
        }
    }

    private fun extractIdentifications(text: Text): Components.Identification? {
        val idMatch = ID_PATTERN.matcher(text.string).takeIf { it.find() }
            ?: return null
        val idDisplayName = idMatch.group(1)
        val idValue = idMatch.group(2) // e.g. "+10", "-1,000"
        val idValue2 = idMatch.group(3) // Optional second value for range-based stats.
        val unit = idMatch.group(4) ?: ""

        var idStats: StatType? = null

        if (idDisplayName == "Combat Experience") {
            idStats = Models.Stat.allStatTypes.firstOrNull {
                it.apiName == "xpBonus"
            }
        }

        if (idDisplayName.endsWith(" Cost")) {
            val spellType = SpellType.fromName(idDisplayName)
            val genericName = when (spellType?.spellNumber) {
                1 -> "1st"
                2 -> "2nd"
                3 -> "3rd"
                4 -> "4th"
                else -> null
            }

            if (spellType != null) {
                idStats = if ("%" in unit) {
                    Models.Stat.allStatTypes.firstOrNull {
                        it.apiName == "${genericName}SpellCost"
                    }
                } else {
                    Models.Stat.allStatTypes.firstOrNull {
                        it.apiName == "raw${genericName}SpellCost"
                    }
                }
            }
        }

        idStats = idStats ?: Models.Stat.allStatTypes.firstOrNull {
            it.displayName == idDisplayName
        } ?: return null

        if ("damage" in idStats.displayName.lowercase()) {
            if ("%" !in unit) {
                // For damage withouth percentage, we take it as rawxxxx
                idStats = Models.Stat.allStatTypes.firstOrNull {
                    it.apiName == "raw${idStats.apiName[0].uppercase()}${idStats.apiName.substring(1)}"
                } ?: return null
            }
        }

        if (idDisplayName == "Health Regen") {
            if ("%" !in unit) {
                // For health regen without percentage, we take it as raw health regen
                idStats = Models.Stat.allStatTypes.firstOrNull {
                    it.apiName == "healthRegenRaw"
                } ?: return null
            }
        }

        val base = if (idValue2 == null || idValue2.isEmpty()) {
            idValue.replace(",", "").replace("%", "").toIntOrNull() ?: 0
        } else {
            val id1 = idValue.replace(",", "").replace("%", "").toIntOrNull() ?: 0
            val id2 = idValue2.replace(",", "").replace("%", "").toIntOrNull() ?: 0
            // For range-based stats, if it is positive, 0.3 - 1.3
            // If it is negative, 1.3 - 0.7
            // Base is always 1.0, and should be an integer
            // The current algorithm might not be perfect, the result may vary in 1-2.
            if (id1 == id2) {
                id1
            } else if (id1 < 0 != idStats.calculateAsInverted()) {
                // 1.3 - 0.7
                ((id1 + id2) / 2.0).roundToInt()
            } else {
                // 0.3 - 1.3
                ((id1 + id2) / 1.6).roundToInt()
            }
        }

        return identification {
            id = IdentificationMappingRepo.fromApiName(idStats.apiName)?.id
                ?: return null
            baseVal = base
        }
    }

    val FONT_LANGUAGE_WYNNCRAFT = StyleSpriteSource.Font(Identifier.of("minecraft:language/wynncraft"))
    val FONT_TOOLTIP_REQUIREMENT = StyleSpriteSource.Font(Identifier.of("minecraft:tooltip/requirement/sprite"))
    private fun extractRequirements(text: Text): Components.Requirements? {
        val stats = mutableListOf<Int>()
        var hasSprite = false
        fun extract(text: Text) {
            if (text.content == null) {
                text.siblings.forEach { extract(it) }
                return
            }
            when (text.style.font) {
                FONT_TOOLTIP_REQUIREMENT if (text.content as PlainTextContent).string()[0] in '\ue005'..'\ue007'
                    -> {
                    hasSprite = true
                }

                FONT_LANGUAGE_WYNNCRAFT -> {
                    (text.content as PlainTextContent).string().toIntOrNull()?.let { stats.add(it) }
                }

                else -> {
                    text.siblings.forEach { extract(it) }
                }
            }
        }

        extract(text)

        if (stats.isNotEmpty() && stats.size == 5 && hasSprite) {
            return requirements {
                strengthReq = stats[0]
                dexterityReq = stats[1]
                intelligenceReq = stats[2]
                defenseReq = stats[3]
                agilityReq = stats[4]
            }
        }
        return null
    }

    private fun extractDamage(text: Text): List<Components.DamageRange>? {
        val str = text.string.filter { it.isDigit() || it == '-' || it in '\ue000'..'\ue005' }
        // Now str should be in the format of \uE00010-20\uE0015-10, split by the element symbol and parse
        val parts = str.split(Regex("(?=[\uE000-\uE005])")).filter { it.isNotBlank() }
        // Parse each pair of element and damage range
        val damageRanges = mutableListOf<Components.DamageRange>()
        for (part in parts) {
            val elementChar = part[0]
            val element = TooltipElement.fromChar(elementChar) ?: return null
            val rangeParts = part.substring(1).split('-')
            if (rangeParts.size != 2) {
                continue
            }
            val min = rangeParts[0].toIntOrNull() ?: return null
            val max = rangeParts[1].toIntOrNull() ?: return null
            damageRanges.add(
                damageRange {
                    this.element = element
                    this.min = min
                    this.max = max
                }
            )
        }
        return damageRanges.ifEmpty { null }
    }

    private fun extractDef(text: Text): List<Components.Defense>? {
        val str = text.string.filter { it.isDigit() || it == '-' || it == '+' || it in '\ue000'..'\ue005' }
        // Now str should be in the format of \uE000+/-10\uE001+/-20, split by the element symbol and parse
        val parts = str.split(Regex("(?=[\uE000-\uE005])")).filter { it.isNotBlank() }
        // Parse each pair of element and defense value
        val defenses = mutableListOf<Components.Defense>()
        for (part in parts) {
            val elementChar = part[0]
            val element = TooltipElement.fromChar(elementChar) ?: return null
            val value = part.substring(1).toIntOrNull() ?: return null
            defenses.add(
                defense {
                    this.element = element
                    this.value = value
                }
            )
        }
        return defenses.ifEmpty { null }
    }

    private fun extractHealth(text: Text): Int? {
        val match = HEALTH_PATTERN.matcher(text.string).takeIf { it.find() } ?: return null
        return match.group(1).replace(",", "").toIntOrNull()
    }

    private fun GearOuterClass.GearType.isWeapon(): Boolean {
        return when (this) {
            GearOuterClass.GearType.GEAR_TYPE_SPEAR,
            GearOuterClass.GearType.GEAR_TYPE_BOW,
            GearOuterClass.GearType.GEAR_TYPE_WAND,
            GearOuterClass.GearType.GEAR_TYPE_DAGGER,
            GearOuterClass.GearType.GEAR_TYPE_RELIK -> true

            else -> false
        }
    }

    private val FONT_DIVIDER = StyleSpriteSource.Font(Identifier.of("minecraft:tooltip/divider"))
    private fun isDivider(text: Text): Boolean {
        if (text.content != null && text.style.font == FONT_DIVIDER) {
            return true
        }
        return text.siblings.any { isDivider(it) }
    }

    private val FONT_MAJOR_ID = StyleSpriteSource.Font(Identifier.of("minecraft:tooltip/identification/major"))
    private fun isMajor(text: Text): Boolean {
        // Major identifications have a specific start
        if ((text.content as PlainTextContent).string().startsWith('\uE000') && text.style.font == FONT_MAJOR_ID) {
            return true
        }
        return text.siblings.any { isMajor(it) }
    }

    private val FONT_PAGER = StyleSpriteSource.Font(Identifier.of("minecraft:tooltip/page"))
    private fun isPager(text: Text): Boolean {
        if (text.content != null && text.style.font == FONT_PAGER) {
            return true
        }
        return text.siblings.any { isPager(it) }
    }

    // ========================================================================
    // Shared Vanilla Helpers
    // ========================================================================

    private fun getItemStackName(itemStack: ItemStack) =
        itemStack.customName?.string ?: itemStack.name?.string.orEmpty()

    private fun getItemStackLore(itemStack: ItemStack): List<Text> {
        return (
                itemStack.getOrDefault(
                    DataComponentTypes.LORE,
                    LoreComponent.DEFAULT
                ) as LoreComponent
                ).lines()
    }
}
