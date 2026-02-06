package fyw.fyi.wynnsource

import fyw.fyi.wynnsource.config.GlobalConfigPage
import fyw.fyi.wynnsource.config.core.ConfigRegistry
import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object WynnSourceClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("WynnSource")

    override fun onInitializeClient() {
        logger.info("Initializing WynnSource client...")

        initConfig()

        logger.info("WynnSource client initialized!")
    }

    private fun initConfig() {
        ConfigRegistry.registerGlobal(GlobalConfigPage)

        ConfigRegistry.loadAll()

        logger.info("Config system initialized with ${ConfigRegistry.getPages().size} page(s)")
    }
}
