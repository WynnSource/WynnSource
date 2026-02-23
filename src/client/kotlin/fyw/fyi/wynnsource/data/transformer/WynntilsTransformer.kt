package fyw.fyi.wynnsource.data.transformer

// Wynntils types

// WynnSource schema types
import com.wynntils.core.components.Models
import com.wynntils.models.character.type.ClassType
import com.wynntils.models.elements.type.Element
import com.wynntils.models.elements.type.PotionType
import com.wynntils.models.elements.type.Skill
import com.wynntils.models.emeralds.type.EmeraldUnits
import com.wynntils.models.gear.type.ConsumableType
import com.wynntils.models.gear.type.GearAttackSpeed
import com.wynntils.models.gear.type.GearRequirements
import com.wynntils.models.gear.type.GearTier
import com.wynntils.models.gear.type.GearType
import com.wynntils.models.ingredients.type.IngredientPosition
import com.wynntils.models.items.WynnItem
import com.wynntils.models.items.items.game.AmplifierItem
import com.wynntils.models.items.items.game.AspectItem
import com.wynntils.models.items.items.game.CharmItem
import com.wynntils.models.items.items.game.CraftedConsumableItem
import com.wynntils.models.items.items.game.CraftedGearItem
import com.wynntils.models.items.items.game.CrafterBagItem
import com.wynntils.models.items.items.game.DungeonKeyItem
import com.wynntils.models.items.items.game.EmeraldItem
import com.wynntils.models.items.items.game.EmeraldPouchItem
import com.wynntils.models.items.items.game.GearBoxItem
import com.wynntils.models.items.items.game.GearItem
import com.wynntils.models.items.items.game.HorseItem
import com.wynntils.models.items.items.game.IngredientItem
import com.wynntils.models.items.items.game.InsulatorItem
import com.wynntils.models.items.items.game.MaterialItem
import com.wynntils.models.items.items.game.PotionItem
import com.wynntils.models.items.items.game.RuneItem
import com.wynntils.models.items.items.game.SimulatorItem
import com.wynntils.models.items.items.game.TeleportScrollItem
import com.wynntils.models.items.items.game.TomeItem
import com.wynntils.models.items.items.game.TrinketItem
import com.wynntils.models.items.items.game.UnknownGearItem
import com.wynntils.models.profession.type.MaterialProfile
import com.wynntils.models.profession.type.ProfessionType
import com.wynntils.models.rewards.type.TomeType
import com.wynntils.models.stats.type.DamageType
import com.wynntils.models.stats.type.FixedStats
import com.wynntils.models.stats.type.StatActualValue
import com.wynntils.models.stats.type.StatPossibleValues
import com.wynntils.utils.mc.LoreUtils
import fyw.fyi.wynnsource.mixin.client.wynntils.ShinyStatTypesAccessor
import fyw.fyi.wynnsource.schema.WynnSourceItemKt
import fyw.fyi.wynnsource.schema.WynnSourceItemOuterClass
import fyw.fyi.wynnsource.schema.common.Components
import fyw.fyi.wynnsource.schema.common.Enums
import fyw.fyi.wynnsource.schema.common.craftedEffect
import fyw.fyi.wynnsource.schema.common.craftedIdentification
import fyw.fyi.wynnsource.schema.common.damageRange
import fyw.fyi.wynnsource.schema.common.defense
import fyw.fyi.wynnsource.schema.common.durability
import fyw.fyi.wynnsource.schema.common.identification
import fyw.fyi.wynnsource.schema.common.levelRange
import fyw.fyi.wynnsource.schema.common.powder
import fyw.fyi.wynnsource.schema.common.powderSlot
import fyw.fyi.wynnsource.schema.common.requirements
import fyw.fyi.wynnsource.schema.common.shiny
import fyw.fyi.wynnsource.schema.item.ConsumableOuterClass
import fyw.fyi.wynnsource.schema.item.CorkianModifierOuterClass
import fyw.fyi.wynnsource.schema.item.EmeraldOuterClass
import fyw.fyi.wynnsource.schema.item.GearOuterClass
import fyw.fyi.wynnsource.schema.item.IngredientOuterClass
import fyw.fyi.wynnsource.schema.item.MaterialOuterClass
import fyw.fyi.wynnsource.schema.item.MountOuterClass
import fyw.fyi.wynnsource.schema.item.TomeOuterClass
import fyw.fyi.wynnsource.schema.item.armorStats
import fyw.fyi.wynnsource.schema.item.aspect
import fyw.fyi.wynnsource.schema.item.charm
import fyw.fyi.wynnsource.schema.item.consumable
import fyw.fyi.wynnsource.schema.item.consumableCharge
import fyw.fyi.wynnsource.schema.item.corkianModifier
import fyw.fyi.wynnsource.schema.item.craftedGear
import fyw.fyi.wynnsource.schema.item.crafterBag
import fyw.fyi.wynnsource.schema.item.dungeonKey
import fyw.fyi.wynnsource.schema.item.emerald
import fyw.fyi.wynnsource.schema.item.emeraldPouch
import fyw.fyi.wynnsource.schema.item.gear
import fyw.fyi.wynnsource.schema.item.healingPotion
import fyw.fyi.wynnsource.schema.item.identifiedGear
import fyw.fyi.wynnsource.schema.item.ingredient
import fyw.fyi.wynnsource.schema.item.material
import fyw.fyi.wynnsource.schema.item.modifier
import fyw.fyi.wynnsource.schema.item.mount
import fyw.fyi.wynnsource.schema.item.mountXp
import fyw.fyi.wynnsource.schema.item.named
import fyw.fyi.wynnsource.schema.item.rune
import fyw.fyi.wynnsource.schema.item.statPotion
import fyw.fyi.wynnsource.schema.item.teleportScroll
import fyw.fyi.wynnsource.schema.item.tome
import fyw.fyi.wynnsource.schema.item.trinket
import fyw.fyi.wynnsource.schema.item.unidentifiedGear
import fyw.fyi.wynnsource.schema.item.unidentifiedGearBox
import fyw.fyi.wynnsource.schema.item.weaponStats
import fyw.fyi.wynnsource.schema.wynnSourceItem
import net.minecraft.item.ItemStack
import com.wynntils.models.elements.type.Powder as WynntilsPowder

object WynntilsTransformer : ItemTransformer<WynnItem>() {

    @Suppress("CyclomaticComplexMethod")
    override fun serialize(item: WynnItem): WynnSourceItemOuterClass.WynnSourceItem {
        return when (item) {
            // Gear items (order matters: CraftedGearItem before GearItem)
            is CraftedGearItem -> serializeCraftedGear(item)
            is GearItem -> serializeGear(item)
            is UnknownGearItem -> serializeUnknownGear(item)
            is GearBoxItem -> serializeGearBox(item)

            // Consumable items
            is PotionItem -> serializePotion(item)
            is CraftedConsumableItem -> serializeCraftedConsumable(item)

            // Identifiable reward items
            is TomeItem -> serializeTome(item)
            is CharmItem -> serializeCharm(item)

            // Crafting items
            is IngredientItem -> serializeIngredient(item)
            is MaterialItem -> serializeMaterial(item)

            // Other typed items
            is AspectItem -> serializeAspect(item)
            is EmeraldItem -> serializeEmerald(item)
            is CrafterBagItem -> serializeCrafterBag(item)
            is HorseItem -> serializeHorse(item)

            // Corkian modifier items
            is AmplifierItem -> serializeCorkianModifier(
                item,
                CorkianModifierOuterClass.CorkianModifierType.CORKIAN_MODIFIER_TYPE_AMPLIFIER
            )

            is SimulatorItem -> serializeCorkianModifier(
                item,
                CorkianModifierOuterClass.CorkianModifierType.CORKIAN_MODIFIER_TYPE_SIMULATOR
            )

            is InsulatorItem -> serializeCorkianModifier(
                item,
                CorkianModifierOuterClass.CorkianModifierType.CORKIAN_MODIFIER_TYPE_INSULATOR
            )

            // Marker-only items (schema has no fields for these)
            is RuneItem -> serializeSimple(item) { rune = rune {} }
            is DungeonKeyItem -> serializeSimple(item) { dungeonKey = dungeonKey {} }
            is TrinketItem -> serializeSimple(item) { trinket = trinket {} }
            is TeleportScrollItem -> serializeSimple(item) { teleportScroll = teleportScroll {} }
            is EmeraldPouchItem -> serializeSimple(item) { emeraldPouch = emeraldPouch {} }

            // Fallback for all other item types (PowderItem, MiscItem, GuiItems, etc.)
            else -> serializeFallback(item)
        }
    }

    override fun deserialize(item: WynnSourceItemOuterClass.WynnSourceItem): WynnItem {
        TODO("Not yet implemented")
    }

    // ========================================================================
    // Gear Serialization
    // ========================================================================

    private fun serializeGear(item: GearItem): WynnSourceItemOuterClass.WynnSourceItem {
        val gearInfo = item.itemInfo
        val fixedStats = gearInfo.fixedStats()

        return wynnSourceItem {
            name = item.name
            count = 1
            level = item.level
            rarity = item.gearTier.toWCS()

            gear = gear {
                type = item.gearType.toWCS()
                requirements = serializeGearRequirements(gearInfo.requirements())

                // Identified vs unidentified state
                if (item.isUnidentified) {
                    unidentified = serializeUnidentifiedGear(item)
                } else {
                    identified = identifiedGear {
                        identifications.addAll(
                            serializeIdentifications(item.identifications, item.possibleValues)
                        )
                        powders.addAll(serializePowders(item.powders, item.powderSlots))
                        rerolls = item.rerollCount

                        item.shinyStat.ifPresent { stat ->
                            shiny = shiny {
                                id = ShinyMappingRepo.fromApiName(stat.statType().key)?.id
                                    ?: error("Unknown shiny stat type: ${stat.statType().key}")
                                value = stat.value().toInt()
                                roll = stat.shinyRerolls()
                            }
                        }
                    }
                }

                // Base stats (weapon damages or armor health/defenses)
                if (item.gearType.isWeapon) {
                    weaponStats = serializeWeaponStats(fixedStats)
                } else if (item.gearType.isArmor) {
                    armorStats = serializeArmorStats(fixedStats)
                }
            }
        }
    }

    private fun serializeUnknownGear(item: UnknownGearItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = item.name
            count = 1
            level = item.level
            rarity = item.gearTier.toWCS()

            gear = gear {
                type = item.gearType.toWCS()
                requirements = serializeGearRequirements(item.requirements)

                if (item.isUnidentified) {
                    unidentified = unidentifiedGear {}
                } else {
                    identified = identifiedGear {
                        identifications.addAll(
                            serializeIdentifications(item.identifications, emptyList())
                        )
                        powders.addAll(serializePowders(item.powders, item.powderSlots))
                        rerolls = item.rerollCount

                        item.shinyStat.ifPresent { stat ->
                            shiny = shiny {
                                id = ShinyMappingRepo.fromApiName(stat.statType().key)?.id
                                    ?: error("Unknown shiny stat type: ${stat.statType().key}")
                                value = stat.value().toInt()
                                roll = stat.shinyRerolls()
                            }
                        }
                    }
                }

                // Base stats from direct accessors
                if (item.gearType.isWeapon) {
                    weaponStats = weaponStats {
                        attackSpeed = item.attackSpeed.toWCS()
                        for (dmg in item.damages) {
                            damages += damageRange {
                                element = dmg.key().toWCS()
                                min = dmg.value().low()
                                max = dmg.value().high()
                            }
                        }
                    }
                } else if (item.gearType.isArmor) {
                    armorStats = armorStats {
                        health = item.health
                        for (def in item.defences) {
                            defenses += defense {
                                element = def.key().toWCS()
                                value = def.value()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun serializeCraftedGear(item: CraftedGearItem): WynnSourceItemOuterClass.WynnSourceItem {
        val possibleMap = item.possibleValues.associateBy { it.statType() }

        return wynnSourceItem {
            name = item.name
            count = 1
            level = item.level
            rarity = Enums.Rarity.RARITY_CRAFTED

            gear = gear {
                type = item.gearType.toWCS()

                requirements = serializeGearRequirements(item.requirements)

                crafted = craftedGear {
                    durability = durability {
                        current = item.durability.current()
                        max = item.durability.max()
                    }

                    for (id in item.identifications) {
                        val possible = possibleMap[id.statType()]
                        identifications += craftedIdentification {
                            this.id = IdentificationMappingRepo.fromApiName(id.statType().apiName)?.id
                                ?: error("Unknown identification stat type: ${id.statType().apiName}")
                            currentVal = id.value()
                            maxVal = possible?.range()?.high() ?: id.value()
                        }
                    }

                    powders.addAll(serializePowders(item.powders, item.powderSlots))
                }

                // Base stats
                if (item.gearType.isWeapon) {
                    weaponStats = weaponStats {
                        item.attackSpeed.ifPresent { attackSpeed = it.toWCS() }
                        for (dmg in item.damages) {
                            damages += damageRange {
                                element = dmg.key().toWCS()
                                min = dmg.value().low()
                                max = dmg.value().high()
                            }
                        }
                    }
                } else if (item.gearType.isArmor) {
                    armorStats = armorStats {
                        health = item.health
                        for (def in item.defences) {
                            defenses += defense {
                                element = def.key().toWCS()
                                value = def.value()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun serializeUnidentifiedGear(item: GearItem): GearOuterClass.UnidentifiedGear {
        return unidentifiedGear {
            identifications.addAll(serializedUnidentifiedIdentifications(item.possibleValues))
            val extractedShiny = extractShinyFromUnidentified(item)
            if (extractedShiny != null) {
                shiny = extractedShiny
            }
        }
    }

    private fun serializeGearBox(item: GearBoxItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = getItemStackName(item)
            count = 1
            level = item.level

            unidentifiedGearBox = unidentifiedGearBox {
                type = item.gearType.toWCS()
                levelRange = levelRange {
                    min = item.levelRange.low
                    max = item.levelRange.high
                }
            }
        }
    }

    // ========================================================================
    // Consumable Serialization
    // ========================================================================

    private fun serializePotion(item: PotionItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = getItemStackName(item)
            count = 1
            level = item.level

            consumable = consumable {
                charge = consumableCharge {
                    current = item.uses.current()
                    max = item.uses.max()
                }

                when (item.type) {
                    PotionType.HEALING -> healingPotion = healingPotion {}
                    PotionType.MANA -> manaPotion = fyw.fyi.wynnsource.schema.item.manaPotion {}
                    PotionType.XP -> xpPotion = fyw.fyi.wynnsource.schema.item.xpPotion {}
                    else -> statPotion = statPotion {
                        element = item.type.toWCSElement()
                    }
                }
            }
        }
    }

    private fun serializeCraftedConsumable(item: CraftedConsumableItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = item.name
            count = 1
            level = item.level
            rarity = Enums.Rarity.RARITY_CRAFTED

            consumable = consumable {
                duration = item.duration

                charge = consumableCharge {
                    current = item.uses.current()
                    max = item.uses.max()
                }

                crafted = fyw.fyi.wynnsource.schema.item.craftedConsumable {
                    craftedConsumableType = item.consumableType.toWCS()
                }
            }
        }
    }

    // ========================================================================
    // Reward Items (Tome, Charm)
    // ========================================================================

    private fun serializeTome(item: TomeItem): WynnSourceItemOuterClass.WynnSourceItem {
        val tomeInfo = item.itemInfo

        return wynnSourceItem {
            name = item.name
            count = 1
            level = item.level
            rarity = item.gearTier.toWCS()

            tome = tome {
                slot = tomeInfo.type().toWCS()
                identifications.addAll(
                    serializeIdentifications(item.identifications, item.possibleValues)
                )
            }
        }
    }

    private fun serializeCharm(item: CharmItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = item.name
            count = 1
            level = item.level
            rarity = item.gearTier.toWCS()

            charm = charm {
                identifications.addAll(
                    serializeIdentifications(item.identifications, item.possibleValues)
                )
            }
        }
    }

    // ========================================================================
    // Crafting Items (Ingredient, Material)
    // ========================================================================

    private fun serializeIngredient(item: IngredientItem): WynnSourceItemOuterClass.WynnSourceItem {
        val info = item.ingredientInfo

        return wynnSourceItem {
            name = item.name
            count = 1
            level = item.level

            ingredient = ingredient {
                durability = info.durabilityModifier()
                duration = info.duration()
                charges = info.charges()
                rarity = info.tier().toIngredientRarity()

                // Crafting effects from variable stats
                for (stat in info.variableStats()) {
                    effects += craftedEffect {
                        id = stat.key().apiName.hashCode()
                        minVal = stat.value().low()
                        maxVal = stat.value().high()
                    }
                }

                // Applicable professions
                for (prof in info.professions()) {
                    professions += prof.toWCS()
                }

                // Position modifiers
                for ((pos, value) in info.positionModifiers()) {
                    modifiers += modifier {
                        this.pos = pos.toWCS()
                        this.value = value
                    }
                }
            }
        }
    }

    private fun serializeMaterial(item: MaterialItem): WynnSourceItemOuterClass.WynnSourceItem {
        val profile = item.materialProfile

        return wynnSourceItem {
            name = getItemStackName(item)
            count = getItemStackCount(item)
            level = item.level

            material = material {
                type = profile.resourceType.toWCS()
            }
        }
    }

    // ========================================================================
    // Other Typed Items
    // ========================================================================

    private fun serializeAspect(item: AspectItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = item.name
            count = 1
            rarity = item.gearTier.toWCS()

            aspect = aspect {
                classType = item.requiredClass.toWCS()
            }
        }
    }

    private fun serializeEmerald(item: EmeraldItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = getItemStackName(item)
            count = item.amount

            emerald = emerald {
                type = item.unit.toWCS()
            }
        }
    }

    private fun serializeCrafterBag(item: CrafterBagItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = getItemStackName(item)
            count = 1
            rarity = item.gearTier.toWCS()

            crafterBag = crafterBag {}
        }
    }

    private fun serializeHorse(item: HorseItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = item.name.orElse("Horse")
            count = 1

            mount = mount {
                mountType = MountOuterClass.MountType.MOUNT_TYPE_HORSE
                xp = mountXp {
                    currentXp = item.xp.current()
                    currentLvl = item.level.current()
                    maxLvl = item.level.max()
                }
            }
        }
    }

    private fun serializeCorkianModifier(
        item: WynnItem,
        modType: CorkianModifierOuterClass.CorkianModifierType
    ): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = getItemStackName(item)
            count = 1

            corkianModifier = corkianModifier {
                type = modType
            }
        }
    }

    // ========================================================================
    // Simple & Fallback Serialization
    // ========================================================================

    private inline fun serializeSimple(
        item: WynnItem,
        crossinline setData: WynnSourceItemKt.Dsl.() -> Unit
    ): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = getItemStackName(item)
            count = getItemStackCount(item)
            setData()
        }
    }

    private fun serializeFallback(item: WynnItem): WynnSourceItemOuterClass.WynnSourceItem {
        return wynnSourceItem {
            name = getItemStackName(item)
            count = getItemStackCount(item)
            named = named {}
        }
    }

    // ========================================================================
    // Shared Helpers
    // ========================================================================

    private fun getItemStackName(item: WynnItem): String {
        val itemStack: ItemStack? = item.data.get("itemstack")
        return itemStack?.customName?.string ?: itemStack?.name?.string ?: ""
    }

    private fun getItemStackCount(item: WynnItem): Int {
        val itemStack: ItemStack? = item.data.get("itemstack")
        return itemStack?.count ?: 1
    }

    private fun serializeGearRequirements(reqs: GearRequirements): Components.Requirements {
        return requirements {
            level = reqs.level()
            reqs.classType().ifPresent { classReq = it.toWCS() }

            for (skill in reqs.skills()) {
                when (skill.key()) {
                    Skill.STRENGTH -> strengthReq = skill.value()
                    Skill.DEXTERITY -> dexterityReq = skill.value()
                    Skill.INTELLIGENCE -> intelligenceReq = skill.value()
                    Skill.DEFENCE -> defenseReq = skill.value()
                    Skill.AGILITY -> agilityReq = skill.value()
                }
            }
        }
    }

    private fun serializeIdentifications(
        identifications: List<StatActualValue>,
        possibleValues: List<StatPossibleValues>
    ): List<Components.Identification> {
        val possibleMap = possibleValues.associateBy { it.statType() }

        return identifications.map { actual ->
            val possible = possibleMap[actual.statType()]
            identification {
                id = IdentificationMappingRepo.fromApiName(actual.statType().apiName)?.id
                    ?: error("Unknown identification stat type: ${actual.statType().apiName}")
                baseVal = possible?.baseValue() ?: actual.value()
                roll10K = calculateRoll10k(actual, possible)
            }
        }
    }

    private fun serializedUnidentifiedIdentifications(
        possibleValues: List<StatPossibleValues>
    ): List<Components.Identification> {
        possibleValues.associateBy { it.statType() }

        return possibleValues.map { possible ->
            identification {
                id = IdentificationMappingRepo.fromApiName(possible.statType().apiName)?.id
                    ?: error("Unknown identification stat type: ${possible.statType().apiName}")
                baseVal = possible.baseValue()
            }
        }
    }

    private fun calculateRoll10k(actual: StatActualValue, possible: StatPossibleValues?): Int {
        if (possible == null) return 10000
        val range = possible.range()
        if (range.high() == range.low()) return 10000 // Pre-identified / fixed stat

        val percentage = (actual.value().toDouble() - range.low()) / (range.high() - range.low())
        return (percentage * 10000).toInt().coerceIn(0, 13000)
    }

    private fun serializePowders(
        powders: List<WynntilsPowder>,
        totalSlots: Int
    ): List<Components.PowderSlot> {
        val result = mutableListOf<Components.PowderSlot>()

        // Applied powders
        for (p in powders) {
            result += powderSlot {
                powder = powder {
                    element = p.element.toWCS()
                }
            }
        }

        // Empty slots
        repeat((totalSlots - powders.size).coerceAtLeast(0)) {
            result += powderSlot {}
        }

        return result
    }

    private fun serializeWeaponStats(stats: FixedStats): GearOuterClass.WeaponStats {
        return weaponStats {
            stats.attackSpeed().ifPresent { attackSpeed = it.toWCS() }
            for (dmg in stats.damages()) {
                damages += damageRange {
                    element = dmg.key().toWCS()
                    min = dmg.value().low()
                    max = dmg.value().high()
                }
            }
        }
    }

    private fun serializeArmorStats(stats: FixedStats): GearOuterClass.ArmorStats {
        return armorStats {
            health = stats.healthBuff()
            for (def in stats.defences()) {
                defenses += defense {
                    element = def.key().toWCS()
                    value = def.value()
                }
            }
        }
    }

    private fun extractShinyFromUnidentified(item: GearItem): Components.Shiny? {
        if (!item.isUnidentified) return null

        val itemStack: ItemStack? = item.data.get("itemstack")

        if (itemStack?.customName?.string?.contains("Shiny") != true) {
            return null
        }

        val lore = LoreUtils.getTooltipLines(itemStack)
        val shinyStatType =
            (Models.Shiny as ShinyStatTypesAccessor).shinyStatTypes.values.sortedByDescending {
                it.displayName().length
            }.firstOrNull { shinyStatType ->
                lore.any { text -> text.string.contains(shinyStatType.displayName) }
            } ?: return null

        return shiny {
            id = shinyStatType.id()
            value = 0 // Cannot determine actual value from unidentified item
            roll = 0 // Cannot determine roll from unidentified item
        }
    }

    // ========================================================================
    // Enum Conversion Extensions
    // ========================================================================

    fun GearTier.toWCS(): Enums.Rarity {
        return when (this) {
            GearTier.NORMAL -> Enums.Rarity.RARITY_COMMON
            GearTier.UNIQUE -> Enums.Rarity.RARITY_UNIQUE
            GearTier.RARE -> Enums.Rarity.RARITY_RARE
            GearTier.SET -> Enums.Rarity.RARITY_SET
            GearTier.LEGENDARY -> Enums.Rarity.RARITY_LEGENDARY
            GearTier.FABLED -> Enums.Rarity.RARITY_FABLED
            GearTier.MYTHIC -> Enums.Rarity.RARITY_MYTHIC
            GearTier.CRAFTED -> Enums.Rarity.RARITY_CRAFTED
        }
    }

    fun GearType.toWCS(): GearOuterClass.GearType {
        return when (this) {
            GearType.SPEAR -> GearOuterClass.GearType.GEAR_TYPE_SPEAR
            GearType.BOW -> GearOuterClass.GearType.GEAR_TYPE_BOW
            GearType.WAND -> GearOuterClass.GearType.GEAR_TYPE_WAND
            GearType.DAGGER -> GearOuterClass.GearType.GEAR_TYPE_DAGGER
            GearType.RELIK -> GearOuterClass.GearType.GEAR_TYPE_RELIK
            GearType.HELMET -> GearOuterClass.GearType.GEAR_TYPE_HELMET
            GearType.CHESTPLATE -> GearOuterClass.GearType.GEAR_TYPE_CHESTPLATE
            GearType.LEGGINGS -> GearOuterClass.GearType.GEAR_TYPE_LEGGINGS
            GearType.BOOTS -> GearOuterClass.GearType.GEAR_TYPE_BOOTS
            GearType.RING -> GearOuterClass.GearType.GEAR_TYPE_RING
            GearType.BRACELET -> GearOuterClass.GearType.GEAR_TYPE_BRACELET
            GearType.NECKLACE -> GearOuterClass.GearType.GEAR_TYPE_NECKLACE
            else -> GearOuterClass.GearType.GEAR_TYPE_UNSPECIFIED
        }
    }

    fun ClassType.toWCS(): Enums.ClassType {
        return when (this) {
            ClassType.MAGE -> Enums.ClassType.CLASS_TYPE_MAGE
            ClassType.ARCHER -> Enums.ClassType.CLASS_TYPE_ARCHER
            ClassType.WARRIOR -> Enums.ClassType.CLASS_TYPE_WARRIOR
            ClassType.ASSASSIN -> Enums.ClassType.CLASS_TYPE_ASSASSIN
            ClassType.SHAMAN -> Enums.ClassType.CLASS_TYPE_SHAMAN
            ClassType.NONE -> Enums.ClassType.CLASS_TYPE_ANY
        }
    }

    fun GearAttackSpeed.toWCS(): GearOuterClass.AttackSpeed {
        return when (this) {
            GearAttackSpeed.SUPER_FAST -> GearOuterClass.AttackSpeed.ATTACK_SPEED_SUPER_FAST
            GearAttackSpeed.VERY_FAST -> GearOuterClass.AttackSpeed.ATTACK_SPEED_VERY_FAST
            GearAttackSpeed.FAST -> GearOuterClass.AttackSpeed.ATTACK_SPEED_FAST
            GearAttackSpeed.NORMAL -> GearOuterClass.AttackSpeed.ATTACK_SPEED_NORMAL
            GearAttackSpeed.SLOW -> GearOuterClass.AttackSpeed.ATTACK_SPEED_SLOW
            GearAttackSpeed.VERY_SLOW -> GearOuterClass.AttackSpeed.ATTACK_SPEED_VERY_SLOW
            GearAttackSpeed.SUPER_SLOW -> GearOuterClass.AttackSpeed.ATTACK_SPEED_SUPER_SLOW
        }
    }

    fun DamageType.toWCS(): Enums.Element {
        return when (this) {
            DamageType.NEUTRAL -> Enums.Element.ELEMENT_NEUTRAL
            DamageType.EARTH -> Enums.Element.ELEMENT_EARTH
            DamageType.THUNDER -> Enums.Element.ELEMENT_THUNDER
            DamageType.WATER -> Enums.Element.ELEMENT_WATER
            DamageType.FIRE -> Enums.Element.ELEMENT_FIRE
            DamageType.AIR -> Enums.Element.ELEMENT_AIR
            else -> Enums.Element.ELEMENT_UNSPECIFIED
        }
    }

    fun Element.toWCS(): Enums.Element {
        return when (this) {
            Element.EARTH -> Enums.Element.ELEMENT_EARTH
            Element.THUNDER -> Enums.Element.ELEMENT_THUNDER
            Element.WATER -> Enums.Element.ELEMENT_WATER
            Element.FIRE -> Enums.Element.ELEMENT_FIRE
            Element.AIR -> Enums.Element.ELEMENT_AIR
        }
    }

    fun PotionType.toWCSElement(): Enums.Element {
        return when (this) {
            PotionType.STRENGTH -> Enums.Element.ELEMENT_EARTH
            PotionType.DEXTERITY -> Enums.Element.ELEMENT_THUNDER
            PotionType.INTELLIGENCE -> Enums.Element.ELEMENT_WATER
            PotionType.DEFENCE -> Enums.Element.ELEMENT_FIRE
            PotionType.AGILITY -> Enums.Element.ELEMENT_AIR
            else -> Enums.Element.ELEMENT_UNSPECIFIED
        }
    }

    fun TomeType.toWCS(): TomeOuterClass.TomeSlot {
        return when (this) {
            TomeType.ARMOUR_TOME -> TomeOuterClass.TomeSlot.TOME_SLOT_ARMOUR
            TomeType.WEAPON_TOME -> TomeOuterClass.TomeSlot.TOME_SLOT_WEAPON
            TomeType.EXPERTISE_TOME -> TomeOuterClass.TomeSlot.TOME_SLOT_EXPERTISE
            TomeType.MARATHON_TOME -> TomeOuterClass.TomeSlot.TOME_SLOT_MARATHON
            TomeType.MYSTICISM_TOME -> TomeOuterClass.TomeSlot.TOME_SLOT_MYSTICISM
            TomeType.LOOTRUN_TOME -> TomeOuterClass.TomeSlot.TOME_SLOT_LOOTRUN
            TomeType.GUILD_TOME -> TomeOuterClass.TomeSlot.TOME_SLOT_GUILD
        }
    }

    fun ConsumableType.toWCS(): ConsumableOuterClass.CraftedConsumableType {
        return when (this) {
            ConsumableType.POTION ->
                ConsumableOuterClass.CraftedConsumableType.CRAFTED_CONSUMABLE_TYPE_POTION

            ConsumableType.FOOD ->
                ConsumableOuterClass.CraftedConsumableType.CRAFTED_CONSUMABLE_TYPE_FOOD

            ConsumableType.SCROLL ->
                ConsumableOuterClass.CraftedConsumableType.CRAFTED_CONSUMABLE_TYPE_SCROLL

            else ->
                ConsumableOuterClass.CraftedConsumableType.CRAFTED_CONSUMABLE_TYPE_UNSPECIFIED
        }
    }

    fun EmeraldUnits.toWCS(): EmeraldOuterClass.EmeraldType {
        return when (this) {
            EmeraldUnits.EMERALD -> EmeraldOuterClass.EmeraldType.EMERALD_TYPE_EMERALD
            EmeraldUnits.EMERALD_BLOCK -> EmeraldOuterClass.EmeraldType.EMERALD_TYPE_BLOCK
            else -> EmeraldOuterClass.EmeraldType.EMERALD_TYPE_LIQUID
        }
    }

    fun MaterialProfile.ResourceType.toWCS(): MaterialOuterClass.MaterialType {
        return when (this) {
            MaterialProfile.ResourceType.INGOT -> MaterialOuterClass.MaterialType.MATERIAL_TYPE_INGOT
            MaterialProfile.ResourceType.GEM -> MaterialOuterClass.MaterialType.MATERIAL_TYPE_GEM
            MaterialProfile.ResourceType.WOOD -> MaterialOuterClass.MaterialType.MATERIAL_TYPE_WOOD
            MaterialProfile.ResourceType.PAPER -> MaterialOuterClass.MaterialType.MATERIAL_TYPE_PAPER
            MaterialProfile.ResourceType.STRING -> MaterialOuterClass.MaterialType.MATERIAL_TYPE_STRING
            MaterialProfile.ResourceType.GRAINS -> MaterialOuterClass.MaterialType.MATERIAL_TYPE_GRAIN
            MaterialProfile.ResourceType.OIL -> MaterialOuterClass.MaterialType.MATERIAL_TYPE_OIL
            MaterialProfile.ResourceType.MEAT -> MaterialOuterClass.MaterialType.MATERIAL_TYPE_MEAT
        }
    }

    fun ProfessionType.toWCS(): Enums.Profession {
        return when (this) {
            ProfessionType.ARMOURING -> Enums.Profession.PROFESSION_ARMOURING
            ProfessionType.TAILORING -> Enums.Profession.PROFESSION_TAILORING
            ProfessionType.JEWELING -> Enums.Profession.PROFESSION_JEWELING
            ProfessionType.WEAPONSMITHING -> Enums.Profession.PROFESSION_WEAPONSMITHING
            ProfessionType.WOODWORKING -> Enums.Profession.PROFESSION_WOODWORKING
            ProfessionType.ALCHEMISM -> Enums.Profession.PROFESSION_ALCHEMISM
            ProfessionType.SCRIBING -> Enums.Profession.PROFESSION_SCRIBING
            ProfessionType.COOKING -> Enums.Profession.PROFESSION_COOKING
            // Gathering professions have no WCS equivalent
            else -> Enums.Profession.PROFESSION_UNSPECIFIED
        }
    }

    fun IngredientPosition.toWCS(): IngredientOuterClass.ModifierPos {
        return when (this) {
            IngredientPosition.LEFT -> IngredientOuterClass.ModifierPos.MODIFIER_POS_LEFT
            IngredientPosition.RIGHT -> IngredientOuterClass.ModifierPos.MODIFIER_POS_RIGHT
            IngredientPosition.ABOVE -> IngredientOuterClass.ModifierPos.MODIFIER_POS_UP
            IngredientPosition.UNDER -> IngredientOuterClass.ModifierPos.MODIFIER_POS_DOWN
            IngredientPosition.TOUCHING -> IngredientOuterClass.ModifierPos.MODIFIER_POS_TOUCH
            IngredientPosition.NOT_TOUCHING -> IngredientOuterClass.ModifierPos.MODIFIER_POS_NO_TOUCH
        }
    }

    private fun Int.toIngredientRarity(): Enums.IngredientRarity {
        return when (this) {
            0 -> Enums.IngredientRarity.INGREDIENT_RARITY_0
            1 -> Enums.IngredientRarity.INGREDIENT_RARITY_1
            2 -> Enums.IngredientRarity.INGREDIENT_RARITY_2
            3 -> Enums.IngredientRarity.INGREDIENT_RARITY_3
            else -> Enums.IngredientRarity.INGREDIENT_RARITY_UNSPECIFIED
        }
    }
}
