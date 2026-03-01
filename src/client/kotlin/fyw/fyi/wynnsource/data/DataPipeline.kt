package fyw.fyi.wynnsource.data

import fyw.fyi.wynnsource.WynnSource
import fyw.fyi.wynnsource.WynnSourceClient
import fyw.fyi.wynnsource.config.GlobalConfigPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object DataPipeline {
    val collections: MutableList<DataCollection<*>> = mutableListOf()

    private var flushJob: Job? = null

    fun start(scope: CoroutineScope) {
        if (WynnSourceClient.isDataGenMode) return
        flushJob = scope.launch {
            while (this.isActive) {
                submitAll()
                delay(GlobalConfigPage.reporting.reportInterval * 1000L)
            }
        }
    }

    fun registerCollection(collection: DataCollection<*>) {
        require(!collections.any { it.moduleId == collection.moduleId }) {
            "Collection with moduleId ${collection.moduleId} is already registered."
        }
        collections.add(collection)
    }

    suspend fun submitAll() {
        collections.forEach {
            if (it.dirty) {
                try {
                    WynnSource.logger.info(
                        "Submitting collection ${it.moduleId} with ${it.dirtyEntries.size} dirty entries..."
                    )
                    it.submit()
                    it.entries.forEach { entry -> entry.dirty = false }
                } catch (e: Exception) {
                    // We use general exception here to prevent one failed collection from blocking the entire pipeline.
                    WynnSource.logger.error("Error submitting collection ${it.moduleId}: ${e.message}")
                }
            }
        }
    }
}
