package fyw.fyi

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object WynnSource : ModInitializer {
    const val MOD_ID = "wynnsource"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        WynnSource.logger.info("WynnSource initialized.")
    }
}
