package fyw.fyi.wynnsource

import fyw.fyi.wynnsource.config.GlobalConfigPage
import fyw.fyi.wynnsource.config.core.ConfigRegistry
import fyw.fyi.wynnsource.coroutine.WCSCoroutineScope
import fyw.fyi.wynnsource.data.DataPipeline
import fyw.fyi.wynnsource.data.repository.RepositoryRegistry
import fyw.fyi.wynnsource.module.ModuleRegistry
import fyw.fyi.wynnsource.module.pool.PoolModule
import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object WynnSourceClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("WynnSource")

    override fun onInitializeClient() {
        logger.info("Initializing WynnSource client...")

        registerModules()
        initModules()

        // Config must be initialized after modules, and before data pipeline and repositories
        initConfig()

        initDataPipeline()
        initRepositoryRegistry()

        logger.info("WynnSource client initialized!")
    }

    fun initConfig() {
        ConfigRegistry.registerGlobal(GlobalConfigPage)

        ConfigRegistry.loadAll()

        logger.info("Config system initialized with ${ConfigRegistry.getPages().size} page(s)")
    }

    fun registerModules() {
        ModuleRegistry.register(PoolModule)
    }

    fun initModules() {
        val modules = ModuleRegistry.getModules()

        modules.forEach { module ->
            logger.info("Initializing module: ${module.name}")
            module.config?.let {
                ConfigRegistry.register(it)
            }
            module.dataCollection?.let {
                DataPipeline.registerCollection(it)
            }
            module.repo.forEach {
                RepositoryRegistry.register(it)
            }
            module.subscribeEvents()
        }
    }

    fun initDataPipeline() {
        DataPipeline.start(WCSCoroutineScope.IO)
    }

    fun initRepositoryRegistry() {
        RepositoryRegistry.start(WCSCoroutineScope.IO)
    }
}
