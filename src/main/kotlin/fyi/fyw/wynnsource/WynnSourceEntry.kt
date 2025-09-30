package fyi.fyw.wynnsource


import fyi.fyw.wynnsource.config.WynnSourceConfig
import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

@Suppress("UNUSED")
object WynnSourceEntry : ClientModInitializer {
    const val MOD_ID = "wynnsource"

    val CONFIG: WynnSourceConfig = WynnSourceConfig.createAndLoad()
    val LOGGER = LoggerFactory.getLogger(WynnSourceEntry::class.java)

    override fun onInitializeClient() {
    }
}