package fyw.fyi.wynnsource.module.pool

import com.wynntils.core.components.Models
import fyw.fyi.wynnsource.coroutine.WCSCoroutineScope
import fyw.fyi.wynnsource.data.transformer.WynntilsTransformer
import fyw.fyi.wynnsource.event.EventBus
import fyw.fyi.wynnsource.event.RemoteContainerScreenEvent
import fyw.fyi.wynnsource.module.BaseModule
import fyw.fyi.wynnsource.utils.StringUtils.splitByCodePoint
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

object PoolModule : BaseModule() {
    override val name = "Pool"
    override val config = PoolConfigPage
    override val dataCollection = PoolDataCollection

    // TODO add repositories for loot and raid pools, and a custom screen to view them.

    override fun subscribeEvents() {
        EventBus.subscribe(WCSCoroutineScope.Main, ::chestOpenListener)
    }

    private fun chestOpenListener(event: RemoteContainerScreenEvent) {
        if (!this.config.enabled) return

        val screenHandler = event.screen.screenHandler
        val inventory = screenHandler.inventory
        val (poolType, region) = determineInventory(event.screen.title)
        if (poolType == null || region == null) {
            return
        }

        val page = determinePage(inventory, poolType)
        var key = "$poolType:$region:$page"

        val items = extractItemList(inventory, poolType).map { item ->
            Models.Item.getWynnItem(item).get()
        }.mapNotNull {
            try {
                WynntilsTransformer.serialize(it)
            } catch (e: Exception) {
                logger.warn(
                    "Failed to serialize item ${
                        it.data.get<ItemStack>("itemstack").name
                    }, skipping. Error: ${e.message}"
                )
                null
            }
        }

        when (poolType) {
            RewardPoolType.Loot -> dataCollection.addEntry(key, items)
            RewardPoolType.Raid -> {
                key = "Raid_items:$region:$page"
                dataCollection.addEntry(key, items.filter { !it.hasAspect() })
                key = "Raid_aspects:$region:$page"
                dataCollection.addEntry(key, items.filter { it.hasAspect() })
            }
        }

        logger.debug("Collected reward pool data for $key with ${items.size} items")
    }

    private const val LOOT_REWARD_START_SLOT = 18
    private const val RAID_REWARD_START_SLOT = 27
    private const val NEXT_PAGE_TEXT = "§7Next Page"
    private const val PREVIOUS_PAGE_TEXT = "§7Previous Page"

    private fun extractItemList(inventory: Inventory, poolType: RewardPoolType): List<ItemStack> {
        return when (poolType) {
            RewardPoolType.Loot -> {
                (LOOT_REWARD_START_SLOT..<inventory.size()).mapNotNull {
                    val item = inventory.getStack(it)
                    if (item.isEmpty) {
                        null
                    } else {
                        item
                    }
                }
            }

            RewardPoolType.Raid -> {
                (RAID_REWARD_START_SLOT..<inventory.size()).mapNotNull {
                    val item = inventory.getStack(it)
                    if (item.isEmpty) {
                        null
                    } else {
                        item
                    }
                }
            }
        }
    }

    private fun determineInventory(title: Text): Pair<RewardPoolType?, String?> {
        val cps = title.string.splitByCodePoint()
        if (cps.size != 4) {
            return Pair(null, null)
        }

        val invType = when (cps[1]) {
            RewardPoolType.Loot.char -> RewardPoolType.Loot
            RewardPoolType.Raid.char -> RewardPoolType.Raid
            else -> null
        }

        val region = when (invType) {
            RewardPoolType.Loot -> LootPool.entries.find { it.char == cps[3] }?.name.orEmpty()
            RewardPoolType.Raid -> RaidPool.entries.find { it.char == cps[3] }?.name.orEmpty()
            else -> null
        }

        return Pair(invType, region)
    }

    private fun determinePage(inventory: Inventory, poolType: RewardPoolType): Int {
        // Look for the previous, next page button
        val items = when (poolType) {
            RewardPoolType.Loot -> {
                (0..<LOOT_REWARD_START_SLOT).map { inventory.getStack(it) }
            }

            RewardPoolType.Raid -> {
                (0..<RAID_REWARD_START_SLOT).map { inventory.getStack(it) }
            }
        }
        val previousPageExist = items.any { !it.isEmpty && it.customName?.string == PREVIOUS_PAGE_TEXT }
        val nextPageExist = items.any { !it.isEmpty && it.customName?.string == NEXT_PAGE_TEXT }
        return when (poolType) {
            RewardPoolType.Loot -> {
                check(!(previousPageExist && nextPageExist)) {
                    "Both previous and next page buttons cannot exist in Loot Pool, invalid state"
                }

                if (previousPageExist) {
                    2 // Only the previous button exists, we are on the second page
                } else if (nextPageExist) {
                    1 // Only the next button exists, we are on the first page
                } else {
                    error("No page buttons found in Loot Pool, cannot determine page")
                }
            }

            RewardPoolType.Raid -> {
                if (previousPageExist && nextPageExist) {
                    2 // Both buttons exist, we are on the second page
                } else if (previousPageExist) {
                    3 // Only the previous button exists, we are on the third page
                } else if (nextPageExist) {
                    1 // Only the next button exists, we are on the first page
                } else {
                    error("No page buttons found, cannot determine page")
                }
            }
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
