package fyw.fyi.wynnsource

import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object WynnSource : ModInitializer {
    const val MOD_ID = "wynnsource"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)
    val version
        get() = getModVersion()

    override fun onInitialize() {
        logger.info("WynnSource initialized.")
    }

    private fun getModVersion(): String? {
        val modContainerOptional = FabricLoader.getInstance().getModContainer(MOD_ID)
        if (modContainerOptional.isPresent) {
            val metadata: ModMetadata = modContainerOptional.get().metadata
            return metadata.version.friendlyString
        }
        return "Unknown"
    }
}
