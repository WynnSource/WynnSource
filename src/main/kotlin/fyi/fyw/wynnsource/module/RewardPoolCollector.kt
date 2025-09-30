package fyi.fyw.wynnsource.module

import com.wynntils.core.components.Models
import com.wynntils.models.gear.type.GearTier
import com.wynntils.models.items.WynnItem
import com.wynntils.models.items.WynnItemData
import com.wynntils.models.items.items.game.*
import com.wynntils.models.stats.type.ShinyStatType
import com.wynntils.utils.mc.LoreUtils
import fyi.fyw.wynnsource.WynnSourceEntry
import fyi.fyw.wynnsource.mixins.AspectInfoAccessor
import fyi.fyw.wynnsource.mixins.ShinyStatTypesAccessor
import fyi.fyw.wynnsource.model.CrowdSourceLootPoolData
import fyi.fyw.wynnsource.model.LootPoolType
import fyi.fyw.wynnsource.model.RequestShinyData
import fyi.fyw.wynnsource.utils.JsonUtils
import fyi.fyw.wynnsource.utils.NetUtils
import fyi.fyw.wynnsource.utils.SecurityKit
import fyi.fyw.wynnsource.utils.StringUtils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import kotlin.concurrent.thread


object RewardPoolCollector {
    // Temporary magic number marking the starting slot of reward
    private const val LOOT_REWARD_START_SLOT = 18
    private const val RAID_REWARD_START_SLOT = 27
    private const val NEXT_PAGE_TEXT = "§7Next Page"
    private const val PREVIOUS_PAGE_TEXT = "§7Previous Page"

    private var crowdSourceLootPoolData: MutableSet<CrowdSourceLootPoolData> = mutableSetOf()
    private var dirty = false

    init {
        // Create scheduler for uploading data periodically
        if (WynnSourceEntry.CONFIG.reportToServer()) {
            try {
                thread {
                    while (true) {
                        if (dirty) {
                            dirty = false
                            try {
                                WynnSourceEntry.LOGGER.info("Uploading ${crowdSourceLootPoolData.size} crowdsource loot pool data entries to the server")
                                NetUtils.post(
                                    "${WynnSourceEntry.CONFIG.reportApiEndpoint()}/pool/crowdsource",
                                    JsonUtils.serializeToJson(crowdSourceLootPoolData),
                                    mapOf(
                                        "X-API-KEY" to WynnSourceEntry.CONFIG.reportApiKey(),
                                        "X-UUID-HASHED" to SecurityKit.sha256(MinecraftClient.getInstance().session.uuidOrNull.toString())
                                    )
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        Thread.sleep(WynnSourceEntry.CONFIG.reportInterval())
                    }
                }.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun onContainerScreen(screen: GenericContainerScreen) {
        if (!WynnSourceEntry.CONFIG.enabled() || !WynnSourceEntry.CONFIG.collectRewardPool()) return;

        val screenHandler = screen.screenHandler
        val inventory = screenHandler.inventory
        val (invType, invId) = determineInventory(screen.title)
        if (invType == null || invId == null) {
            return
        }

//        println("Inventory Type: $invType, Inventory ID: $invId")

        val items = extractItemList(inventory, invType)
        val page = determinePage(inventory, invType)

//        println("Page: $page, InvId: $invId, InvType: $invType, Items: ${items.size}")

        val collected: MutableList<CrowdSourceLootPoolData> = mutableListOf()

        when (invType) {
            RewardPoolType.Loot -> {

                val lrCollect = CrowdSourceLootPoolData(
                    type = LootPoolType.LR,
                    location = invId,
                    page = page,
                )


                items.asSequence().filterNotNull().forEach { wynnItem ->
                    when (wynnItem) {
                        is GearItem -> {
                            when (wynnItem.gearTier) {
                                GearTier.MYTHIC -> {
                                    val itemStack: ItemStack = wynnItem.data.get(WynnItemData.ITEMSTACK_KEY)
                                    val shinyStatType = extractShiny(itemStack)
                                    if (shinyStatType != null) {
                                        lrCollect.shiny = RequestShinyData(
                                            wynnItem.name,
                                            shinyStatType.displayName
                                        )
                                    } else {
                                        lrCollect.items.mythic.add(wynnItem.name)
                                    }
                                }

                                GearTier.FABLED -> lrCollect.items.fabled.add(wynnItem.name)

                                GearTier.LEGENDARY -> lrCollect.items.legendary.add(wynnItem.name)

                                GearTier.RARE -> lrCollect.items.rare.add(wynnItem.name)

                                GearTier.UNIQUE -> lrCollect.items.unique.add(wynnItem.name)

                                else -> throw IllegalStateException("Unreachable when case")
                            }
                        }

                        is SimulatorItem -> {
                            lrCollect.items.mythic.add("Corkian Simulator")
                        }

                        is InsulatorItem -> {
                            lrCollect.items.mythic.add("Corkian Insulator")
                        }

                        else -> {}
                    }

                    collected.add(lrCollect)
                }
            }

            RewardPoolType.Raid -> {
                val aspectCollect = CrowdSourceLootPoolData(
                    type = LootPoolType.RAID_ASPECT,
                    location = invId,
                    page = page,
                )
                val tomeCollect = CrowdSourceLootPoolData(
                    type = LootPoolType.RAID_TOME,
                    location = invId,
                    page = page,
                )

                items.asSequence().filterNotNull().forEach { wynnItem ->
                    when (wynnItem) {
                        is AspectItem -> {
                            when (wynnItem.gearTier) {
                                GearTier.MYTHIC -> aspectCollect.items.mythic.add((wynnItem as AspectInfoAccessor).aspectInfo.name)

                                GearTier.FABLED -> aspectCollect.items.fabled.add((wynnItem as AspectInfoAccessor).aspectInfo.name)

                                GearTier.LEGENDARY -> aspectCollect.items.legendary.add((wynnItem as AspectInfoAccessor).aspectInfo.name)

                                else -> throw IllegalStateException("Unreachable when case")
                            }
                        }

                        is TomeItem -> {
                            when (wynnItem.gearTier) {
                                GearTier.MYTHIC -> tomeCollect.items.mythic.add(wynnItem.name)

                                GearTier.FABLED -> tomeCollect.items.fabled.add(wynnItem.name)

                                GearTier.LEGENDARY -> tomeCollect.items.legendary.add(wynnItem.name)

                                else -> throw IllegalStateException("Unreachable when case")
                            }
                        }

                        else -> {}
                    }

                }

                collected.add(aspectCollect)
                collected.add(tomeCollect)
            }
        }

        val prevCrowdSourceLootPoolData = crowdSourceLootPoolData.toSet()
        collected.forEach { it ->
            // Replace existing data if it matches the type, location and page
            crowdSourceLootPoolData.firstOrNull { existing ->
                existing.type == it.type && existing.location == it.location && existing.page == it.page
            }?.let { it -> crowdSourceLootPoolData.remove(it) }

            crowdSourceLootPoolData.add(it)
        }

        if (crowdSourceLootPoolData != prevCrowdSourceLootPoolData) {
            dirty = true
        }

        JsonUtils.writeToFile(
            FabricLoader.getInstance().gameDir.resolve("${WynnSourceEntry.MOD_ID}/data.json"),
            crowdSourceLootPoolData
        )
    }

    private fun extractItemList(inventory: Inventory, invType: RewardPoolType): List<WynnItem?> {
        return when (invType) {
            RewardPoolType.Loot -> {
                (LOOT_REWARD_START_SLOT..<inventory.size()).map {
                    val item = inventory.getStack(it)
                    if (item.isEmpty) {
                        null
                    } else {
                        Models.Item.getWynnItem(item).get()
                    }
                }
            }

            RewardPoolType.Raid -> {
                (RAID_REWARD_START_SLOT..<inventory.size()).map {
                    val item = inventory.getStack(it)
                    if (item.isEmpty) {
                        null
                    } else {
                        Models.Item.getWynnItem(item).get()
                    }
                }
            }
        }
    }

    private fun determineInventory(title: Text): Pair<RewardPoolType?, String?> {
        val cps = StringUtils.splitByCodePoint(title.string)
        if (cps.size != 4) {
            return Pair(null, null)
        }

        val invType = when (cps[1]) {
            RewardPoolType.Loot.char -> RewardPoolType.Loot
            RewardPoolType.Raid.char -> RewardPoolType.Raid
            else -> null
        }

        val invId = when (invType) {
            RewardPoolType.Loot -> LootPool.entries.find { it.char == cps[3] }?.name ?: ""
            RewardPoolType.Raid -> RaidPool.entries.find { it.char == cps[3] }?.name ?: ""
            else -> null
        }

        return Pair(invType, invId)
    }

    private fun determinePage(inventory: Inventory, invType: RewardPoolType): Int {
        // Look for the previous, next page button
        val items = when (invType) {
            RewardPoolType.Loot -> {
                (0..<LOOT_REWARD_START_SLOT).map { inventory.getStack(it) }
            }

            RewardPoolType.Raid -> {
                (0..<RAID_REWARD_START_SLOT).map { inventory.getStack(it) }
            }
        }
        val previousPageExist = items.any { !it.isEmpty && it.customName?.string == PREVIOUS_PAGE_TEXT }
        val nextPageExist = items.any { !it.isEmpty && it.customName?.string == NEXT_PAGE_TEXT }
        return when (invType) {
            RewardPoolType.Loot -> {
                if (previousPageExist && nextPageExist) {
                    throw IllegalStateException("Both previous and next page buttons exist, not supported")
                } else if (previousPageExist) {
                    2 // Only the previous button exists, we are on the second page
                } else if (nextPageExist) {
                    1 // Only the next button exists, we are on the first page
                } else {
                    throw IllegalStateException("No page buttons found, cannot determine page")
                }
            }

            RewardPoolType.Raid -> {
                if (previousPageExist && nextPageExist) {
                    2 // Both buttons exist, we are on the first page
                } else if (previousPageExist) {
                    3 // Only the previous button exists, we are on the second page
                } else if (nextPageExist) {
                    1 // Only the next button exists, we are on the first page
                } else {
                    throw IllegalStateException("No page buttons found, cannot determine page")
                }
            }
        }
    }


    private fun extractShiny(item: ItemStack): ShinyStatType? {
        if (item.customName?.string?.contains("Shiny") != true) {
            return null;
        }

        val lore = LoreUtils.getTooltipLines(item)
        return (Models.Shiny as ShinyStatTypesAccessor).shinyStatTypes.values.sortedByDescending { it.displayName().length }
            .firstOrNull { shinyStatType ->
                lore.any { text -> text.string.contains(shinyStatType.displayName) }
            }

    }


    enum class RewardPoolType(val char: String) {
        Loot("\uE00A"),
        Raid("\uE00D"),
    }

    enum class LootPool(val char: String) {
        Canyon(char = "\uF006"),
        Corkus(char = "\uF007"),
        Molten(char = "\uF008"),
        Sky(char = "\uF009"),
        SE(char = "\uF00A"),
    }

    enum class RaidPool(val char: String) {
        NOTG(char = "\uF00B"),
        NOL(char = "\uF00C"),
        TCC(char = "\uF00D"),
        TNA(char = "\uF00E"),
    }
}
