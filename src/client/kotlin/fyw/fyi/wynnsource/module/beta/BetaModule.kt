package fyw.fyi.wynnsource.module.beta

import fyw.fyi.wynnsource.coroutine.WCSCoroutineScope
import fyw.fyi.wynnsource.data.transformer.ItemDatabase
import fyw.fyi.wynnsource.data.transformer.NativeItemTransformer
import fyw.fyi.wynnsource.event.EventBus
import fyw.fyi.wynnsource.event.ItemTooltipDrawEvent
import fyw.fyi.wynnsource.module.BaseModule
import net.minecraft.item.ItemStack

object BetaModule : BaseModule() {
    override val name = "Beta"
    override val config = BetaConfigPage

    init {
        this.dataCollection += BetaItemSubmissionCollection
        this.dataCollection += BetaItemPowderPatchCollection
        this.repo += BetaItemRepo
    }

    override fun subscribeEvents() {
        EventBus.subscribe(WCSCoroutineScope.Main, ::itemTooltipDrawListener)
    }

    private fun itemTooltipDrawListener(event: ItemTooltipDrawEvent) {
        if (!this.config.enableNewItemCollection) return
        val itemStack = event.itemStack ?: return

        handleNewItemSubmission(itemStack)
        handleNewPowderPatchSubmission(itemStack)
    }

    private fun handleNewItemSubmission(itemStack: ItemStack) {
        val item = runCatching {
            NativeItemTransformer.serialize(itemStack)
        }.onFailure {
            logger.debug(
                "Failed to serialize item ${itemStack.name.string}, skipping. Error: ${it.message}"
            )
            return
        }.getOrNull() ?: return

        // Check non-beta database
        if (ItemDatabase.getOrNull()?.contains(item.name) ?: return) return
        // Check beta items
//        if (BetaItemRepo.getOrNull()?.contains(item.name) ?: return) return
        // Check local cache
        if (BetaItemSubmissionCollection.entries.any { it.key == item.name }) return
        logger.info("Found new item ${item.name}.")
        BetaItemSubmissionCollection.addEntry(item.name, item)
    }

    private fun handleNewPowderPatchSubmission(itemStack: ItemStack) {
        val item = runCatching {
            NativeItemTransformer.serializePowderPatch(itemStack)
        }.onFailure {
            return
        }.getOrNull() ?: return

        // Check non-beta database
        if (ItemDatabase.getOrNull()?.contains(item.name) ?: return) return
        // Check local cache
        if (BetaItemPowderPatchCollection.entries.any { it.key == item.name }) return
        BetaItemPowderPatchCollection.addEntry(item.name, item)
    }
}
