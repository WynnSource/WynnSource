package fyw.fyi.wynnsource.data.transformer

import com.wynntils.core.components.Models
import com.wynntils.models.spells.type.SpellType
import com.wynntils.models.stats.type.StatType
import fyw.fyi.wynnsource.schema.WynnSourceItemOuterClass
import fyw.fyi.wynnsource.schema.common.Components
import fyw.fyi.wynnsource.schema.common.Enums
import fyw.fyi.wynnsource.schema.common.craftedEffect
import fyw.fyi.wynnsource.schema.common.damageRange
import fyw.fyi.wynnsource.schema.common.defense
import fyw.fyi.wynnsource.schema.common.identification
import fyw.fyi.wynnsource.schema.common.powderSlot
import fyw.fyi.wynnsource.schema.common.requirements
import fyw.fyi.wynnsource.schema.item.GearOuterClass
import fyw.fyi.wynnsource.schema.item.IngredientOuterClass
import fyw.fyi.wynnsource.schema.item.armorStats
import fyw.fyi.wynnsource.schema.item.copy
import fyw.fyi.wynnsource.schema.item.gear
import fyw.fyi.wynnsource.schema.item.ingredient
import fyw.fyi.wynnsource.schema.item.modifier
import fyw.fyi.wynnsource.schema.item.modifierPos
import fyw.fyi.wynnsource.schema.item.reqModifier
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
        var parsedSet: String? = null

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
                    if (parsedSet == null) {
                        parsedSet = extractSet(line)
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

        if (parsedMajorId.all { it.startsWith('7') }) {
            // in market
            parsedMajorId.replaceAll { it.drop(1) }
        }

        return wynnSourceItem {
            name = itemName
            level = parsedLevel ?: error("Level not found in lore")
            rarity = parsedRarity ?: error("Rarity not found in lore")

            gear = gear {
                type = finalGearType
                requirements = parsedRequirements ?: error("Requirements not found in lore")
                majorId = parsedMajorId.joinToString(" ")
                set = parsedSet ?: ""

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

    fun serializePowderPatch(item: ItemStack): WynnSourceItemOuterClass.WynnSourceItem {
        val itemName = ID_NAME_PATTERN.matcher(getItemStackName(item)).takeIf { it.find() }?.group(1)
            ?: error("Item type not supported yet")
        var parsedRarity: Enums.Rarity? = null
        var parsedGearType: GearOuterClass.GearType? = null
        val itemLore = getItemStackLore(item)
        var section = 0
        var parsedPowderSlots: Int? = null

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
                }

                1, 2 -> { // If there is the set, the powder slots info can be in the thrid section
                    if (isPowder(line) && parsedPowderSlots == null) {
                        parsedPowderSlots = extractPowder(line)
                    }
                }

                else -> {
                    // Ignore other sections for now
                }
            }
        }

        return wynnSourceItem {
            name = itemName
            rarity = parsedRarity ?: error("Rarity not found in lore")
            gear = gear {
                type = parsedGearType ?: error("GearType not found in lore")
                powders.addAll(
                    List(parsedPowderSlots ?: error("Powder slots not found in lore")) {
                        powderSlot {

                        }
                    }
                )
            }
        }
    }

    fun serializeIngredient(item: ItemStack): WynnSourceItemOuterClass.WynnSourceItem {
        val itemName = ID_NAME_PATTERN.matcher(getItemStackName(item)).takeIf { it.find() }?.group(1)
            ?: error("Item type not supported yet")
        val itemLore = getItemStackLore(item)
        var isIngredient = false
        var section = 0
        var parsedIngredientTier: Enums.IngredientRarity? = null
        val parsedProfessions = mutableListOf<Enums.Profession>()
        var parsedLevel: Int? = null
        var parsedDurability: Int? = null
        var parsedCharge: Int? = null
        var parsedDuration: Int? = null
        var parsedMinReq: IngredientOuterClass.ReqModifier = reqModifier {}
        val parsedCraftedEffects = mutableListOf<Components.CraftedEffect>()
        var modifierStart = false
        var curEffectY = -2
        val parsedModifiers = mutableListOf<IngredientOuterClass.Modifier>()

        for (line in itemLore) {
            if (isDivider(line)) {
                section++
                continue
            }

            when (section) {
                0 -> {
                    // tier, type, level, profession
                    if (isIngredient(line)) {
                        isIngredient = true
                    }
                    parsedIngredientTier = parsedIngredientTier ?: extractIngreTier(line)

                    parsedProfessions.addAll(extractProfessions(line))
                    parsedLevel =
                        parsedLevel ?: CRAFTING_LEVEL_PATTERN.matcher(line.string).takeIf { it.find() }?.group(1)
                            ?.toIntOrNull()
                }

                1, 2 -> {
                    // durability, charge, duration, min req
                    // identifications
                    // effectiveness sometime
                    parsedDurability =
                        parsedDurability ?: DURABILITY_PATTERN.matcher(line.string).takeIf { it.find() }?.group(1)
                            ?.toIntOrNull()
                    parsedCharge =
                        parsedCharge ?: CHARGE_PTTERN.matcher(line.string).takeIf { it.find() }?.group(1)
                            ?.toIntOrNull()
                    parsedDuration =
                        parsedDuration ?: DURATION_PATTERN.matcher(line.string).takeIf { it.find() }?.group(1)
                            ?.toIntOrNull()

                    val minReqMatch = MIN_REQ_PATTERN.matcher(line.string).takeIf { it.find() }
                    if (minReqMatch != null) {
                        val statName = minReqMatch.group(1)
                        val statValue = minReqMatch.group(2).toIntOrNull() ?: 0
                        when (statName) {
                            "Strength" -> parsedMinReq = parsedMinReq.copy { strengthReq = statValue }
                            "Dexterity" -> parsedMinReq = parsedMinReq.copy { dexterityReq = statValue }
                            "Intelligence" -> parsedMinReq = parsedMinReq.copy { intelligenceReq = statValue }
                            "Defense" -> parsedMinReq = parsedMinReq.copy { defenseReq = statValue }
                            "Agility" -> parsedMinReq = parsedMinReq.copy { agilityReq = statValue }
                        }
                    }
                    extractCraftedEffect(line)?.let { parsedCraftedEffects.add(it) }

                    if ("EFFECTIVENESS" in FontUtils.fromBannerFiltered(line.string)) {
                        modifierStart = true
                    }

                    if (modifierStart) {
                        // effectiveness
                        val effectivenessMatch = EFFECTIVENESS_PATTERN.findAll(line.string)
                        var curEffectX = -1
                        effectivenessMatch.forEach {
                            val valueStr = it.value
                            val value = valueStr.replace("%", "").toIntOrNull() ?: return@forEach
                            parsedModifiers.add(
                                modifier {
                                    pos = modifierPos {
                                        x = curEffectX++
                                        y = curEffectY
                                    }
                                    this.value = value
                                }
                            )
                            // For the middle line, we skip the second column as it's the center
                            if (curEffectY == 0) {
                                curEffectX++
                            }
                        }.also {
                            if (curEffectX != -1) {
                                curEffectY++
                            }
                        }
                    }
                }

                else -> {
                    // Ignore other sections for now
                }
            }
        }

        require(isIngredient) { "Not an ingredient" }

        return wynnSourceItem {
            name = itemName
            level = parsedLevel ?: error("Level not found in lore")

            ingredient = ingredient {
                parsedDurability?.let { durability = it }
                parsedCharge?.let { charges = it }
                parsedDuration?.let { duration = it }
                rarity = parsedIngredientTier ?: error("Ingredient tier not found in lore")
                reqModifier = parsedMinReq
                effects.addAll(parsedCraftedEffects)
                professions.addAll(parsedProfessions)
                modifiers.addAll(parsedModifiers)
            }
        }
    }

    override fun deserialize(item: WynnSourceItemOuterClass.WynnSourceItem): ItemStack {
        TODO("Not yet implemented")
    }

    // ========================================================================
    // Shared Helpers
    // ========================================================================


    private val ID_NAME_PATTERN = Pattern.compile("^\uDAFC\uDC00(.+)À?\uDAFC\uDC00$")

    // \uE0008 is the lock in the name
    private val UNID_NAME_PATTERN = Pattern.compile("^\uDAFC\uDC00\uE008\uDB00\uDC02(.*?)À?\uDAFC\uDC00$")
    private val LEVEL_PATTERN = Pattern.compile("^.*?Combat Level.*?(\\d+)$")
    private val CRAFTING_LEVEL_PATTERN = Pattern.compile("^.*?(\\d+) Crafting Level.*?$")
    private val HEALTH_PATTERN = Pattern.compile("^.*?([+-][\\d,]+) Health$")
    private val ID_PATTERN = Pattern.compile(
        "^.*?7?([\\w\\s]+).*?([+-][\\d,]+)(/\\ds|\\stier|%)?(?:\\sto\\s([+-][\\d,]+)(?:/\\ds|\\stier|%)?)?$"
    )
    private val ATTACK_SPEED_PATTERN = Pattern.compile("^.*?\uE007\\s([\\w\\s]+) \\(.*$")
    private val DURABILITY_PATTERN = Pattern.compile("^.*?Durability.*?([+-]\\d+)$")
    private val DURATION_PATTERN = Pattern.compile("^.*?Duration.*?([+-]\\d+)s$")
    private val CHARGE_PTTERN = Pattern.compile("^.*?Charges.*?([+-]\\d+)$")
    private val MIN_REQ_PATTERN = Pattern.compile("^.*?Min\\.\\s(\\w+).*?([+-]\\d+)$")
    private val EFFECTIVENESS_PATTERN = "([+-]?\\d+%)".toRegex()


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
            when (FontUtils.fromBannerFiltered((text.content as PlainTextContent).string())) {
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
            when (FontUtils.fromBannerFiltered((text.content as PlainTextContent).string())) {
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

    private val ID_MAPPING = mapOf(
        "Combat Experience" to "xpBonus",
        "Critical Damage" to "criticalDamageBonus",
        "Loot" to "lootBonus"
    )

    private fun processIdenfitications(text: Text): Triple<StatType, Int?, Int?>? {
        val idMatch = ID_PATTERN.matcher(text.string).takeIf { it.find() }
            ?: return null
        val idDisplayName = idMatch.group(1)
        val idValue = idMatch.group(2) // e.g. "+10", "-1,000"
        val idValue2 = idMatch.group(4) // Optional second value for range-based stats.
        val unit = idMatch.group(3) ?: ""

        var idStats: StatType? = ID_MAPPING[idDisplayName]?.let { apiName ->
            Models.Stat.allStatTypes.firstOrNull { it.apiName == apiName }
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

        return Triple(
            idStats,
            idValue.replace(",", "").replace("%", "").toIntOrNull(),
            idValue2?.replace(",", "")?.replace("%", "")?.toIntOrNull()
        )
    }

    private fun extractIdentifications(text: Text): Components.Identification? {
        val idStatsTriple = processIdenfitications(text) ?: return null
        val idStats = idStatsTriple.first
        val idValue = idStatsTriple.second ?: return null
        val idValue2 = idStatsTriple.third

        val base = if (idValue2 == null) {
            idValue
        } else {
            // For range-based stats, if it is positive, 0.3 - 1.3
            // If it is negative, 1.3 - 0.7
            // Base is always 1.0, and should be an integer
            // The current algorithm might not be perfect, the result may vary in 1-2.
            if (idValue == idValue2) {
                idValue
            } else if (idValue < 0 != idStats.calculateAsInverted()) {
                // 1.3 - 0.7
                ((idValue + idValue2) / 2.0).roundToInt()
            } else {
                // 0.3 - 1.3
                ((idValue + idValue2) / 1.6).roundToInt()
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
        val str = text.string.filter { it.isDigit() || it == '-' || it in '\ue000'..'\ue005' }.dropWhile { it == '7' }
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
            .dropWhile { it == '7' }
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

    private val POWDER_REGEX = Pattern.compile("^(\\d+)/(\\d+)$")
    private fun isPowder(text: Text): Boolean {
        if (text.content != null && text.style.font == FONT_BANNER_BOX) {
            return FontUtils.filterAscii(
                FontUtils.fromBanner(
                    (text.content as PlainTextContent).string()
                )
            ) == "POWDERSOCKETS"
        }
        return text.siblings.any { isPowder(it) }
    }

    private fun extractPowder(text: Text): Int? {
        if (text.content != null && text.style.font == FONT_LANGUAGE_WYNNCRAFT) {
            val match =
                POWDER_REGEX.matcher(
                    (text.content as PlainTextContent).string()
                ).takeIf { it.find() } ?: return null
            val current = match.group(1).toIntOrNull() ?: return null
            val max = match.group(2).toIntOrNull() ?: return null
            return max;
        }
        return text.siblings.firstNotNullOfOrNull { extractPowder(it) }
    }

    private val SET_REGEX = Pattern.compile("^(\\w+)SET$")
    private fun extractSet(text: Text): String? {
        if (text.content != null && text.style.font == FONT_BANNER_BOX) {
            val match =
                SET_REGEX.matcher(
                    FontUtils.filterAscii(
                        FontUtils.fromBanner(
                            (text.content as PlainTextContent).string()
                        )
                    )
                ).takeIf { it.find() }
                    ?: return null
            return match.group(1)
        }
        return text.siblings.firstNotNullOfOrNull { extractSet(it) }
    }

    private fun isIngredient(text: Text): Boolean {
        if (text.content != null && text.style.font == FONT_BANNER_BOX) {
            return FontUtils.fromBannerFiltered((text.content as PlainTextContent).string()) == "INGREDIENT"
        }
        return text.siblings.any { isIngredient(it) }
    }

    private val INGRE_TIER_REGEX = Pattern.compile("^(\uE000+)\uDB00\uDC02$")
    private val FONT_BANNER_SIMBOL = StyleSpriteSource.Font(Identifier.of("minecraft:banner/symbol"))
    private fun extractIngreTier(text: Text): Enums.IngredientRarity? {
        if (text.content != null && text.style.font == FONT_BANNER_SIMBOL && text.siblings.isNotEmpty()) {
            val sib = text.siblings.first()
            val content = (sib.content as PlainTextContent).string()
            val match = INGRE_TIER_REGEX.matcher(content).takeIf { it.find() } ?: return null
            val tierStr = match.group(1)
            return when (tierStr.length) {
                1 -> Enums.IngredientRarity.INGREDIENT_RARITY_1
                2 -> Enums.IngredientRarity.INGREDIENT_RARITY_2
                3 -> when (sib.style.color?.name) {
                    "black" -> Enums.IngredientRarity.INGREDIENT_RARITY_0
                    else -> Enums.IngredientRarity.INGREDIENT_RARITY_3
                }

                else -> null
            }

        }
        return text.siblings.firstNotNullOfOrNull { extractIngreTier(it) }
    }

    val FONT_PROFESSION = StyleSpriteSource.Font(Identifier.of("minecraft:profession"))
    private fun extractProfessions(text: Text): List<Enums.Profession> {
        val professions = mutableListOf<Enums.Profession>()
        if (text.content != null && text.style.font == FONT_PROFESSION) {
            when ((text.content as PlainTextContent).string().filter {
                it in '\uE004'..'\uE00B'
            }) {
                "\uE004" -> professions.add(Enums.Profession.PROFESSION_ALCHEMISM)
                "\uE005" -> professions.add(Enums.Profession.PROFESSION_ARMOURING)
                "\uE006" -> professions.add(Enums.Profession.PROFESSION_COOKING)
                "\uE007" -> professions.add(Enums.Profession.PROFESSION_JEWELING)
                "\uE008" -> professions.add(Enums.Profession.PROFESSION_SCRIBING)
                "\uE009" -> professions.add(Enums.Profession.PROFESSION_TAILORING)
                "\uE00A" -> professions.add(Enums.Profession.PROFESSION_WEAPONSMITHING)
                "\uE00B" -> professions.add(Enums.Profession.PROFESSION_WOODWORKING)
            }
        }
        text.siblings.forEach { professions.addAll(extractProfessions(it)) }
        return professions
    }

    private fun extractCraftedEffect(text: Text): Components.CraftedEffect? {
        // Crafted effects are similar to identifications, but use min-max range for the value.
        val idStatsTriple = processIdenfitications(text) ?: return null
        val idStats = idStatsTriple.first
        val idValue = idStatsTriple.second ?: return null
        val idValue2 = idStatsTriple.third

        return craftedEffect {
            id = IdentificationMappingRepo.fromApiName(idStats.apiName)?.id ?: return null
            minVal = idValue
            maxVal = idValue2 ?: idValue
        }
    }

    // ========================================================================
    // Shared Vanilla Helpers
    // ========================================================================

    private

    fun getItemStackName(itemStack: ItemStack) =
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
