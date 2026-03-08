package fyw.fyi.wynnsource

import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.wynntils.models.worlds.type.WorldState
import fyw.fyi.wynnsource.config.ExampleConfigPage
import fyw.fyi.wynnsource.config.GlobalConfigPage
import fyw.fyi.wynnsource.config.core.ConfigRegistry
import fyw.fyi.wynnsource.coroutine.WCSCoroutineScope
import fyw.fyi.wynnsource.data.DataPipeline
import fyw.fyi.wynnsource.data.repository.RepositoryRegistry
import fyw.fyi.wynnsource.event.EventBus
import fyw.fyi.wynnsource.event.WorldStateChangeEvent
import fyw.fyi.wynnsource.module.ModuleRegistry
import fyw.fyi.wynnsource.module.beta.BetaModule
import fyw.fyi.wynnsource.module.pool.PoolModule
import fyw.fyi.wynnsource.utils.ChatLogger
import fyw.fyi.wynnsource.utils.NetUtils
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import org.slf4j.LoggerFactory
import java.net.URI

object WynnSourceClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("WynnSource")

    val isDataGenMode: Boolean
        get() = System.getProperty("fabric-api.datagen") != null

    val isDev: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun onInitializeClient() {
        logger.info("Initializing WynnSource client...")

        registerModules()
        initModules()

        // Config must be initialized after modules, and before data pipeline and repositories
        initConfig()

        initDataPipeline()
        initRepositoryRegistry()

        initUpdateCheck()

        if (isDev) {
            devInit()
        }

        logger.info("WynnSource client initialized!")
    }

    fun initConfig() {
        ConfigRegistry.registerGlobal(GlobalConfigPage)

        if (isDev) {
            ConfigRegistry.register(ExampleConfigPage)
        }

        ConfigRegistry.loadAll()

        logger.info("Config system initialized with ${ConfigRegistry.getPages().size} page(s)")
    }

    fun registerModules() {
        ModuleRegistry.register(PoolModule)
        ModuleRegistry.register(BetaModule)
    }

    fun initModules() {
        val modules = ModuleRegistry.getModules()

        modules.forEach { module ->
            logger.info("Initializing module: ${module.name}")
            module.config?.let {
                ConfigRegistry.register(it)
            }
            module.dataCollection.forEach {
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

    fun initUpdateCheck() {
        WCSCoroutineScope.IO.launch {
            val newRelease = NetUtils.checkUpdate()
            if (newRelease != null) {
                EventBus.subscribe(
                    WCSCoroutineScope.Main
                ) { event: WorldStateChangeEvent ->
                    if (event.newState == WorldState.WORLD && event.isFirstJoinWorld) {
                        ChatLogger.log(
                            Text.literal("A new version of WynnSource is available: ").append(
                                Text.literal(newRelease.name).styled { style ->
                                    style.withClickEvent(
                                        ClickEvent.OpenUrl(URI(newRelease.htmlUrl))
                                    ).withHoverEvent(
                                        HoverEvent.ShowText(Text.literal("Click to view the release on GitHub"))
                                    ).withUnderline(true)
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    fun devInit() {
        // Add any development-only initialization logic here
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, access, environment ->
            dispatcher.register(
                literal<ServerCommandSource>("wynnsource_dev")
                    .then(
                        literal<ServerCommandSource>("test")
                            .executes {
                                logger.info("WynnSource dev command executed!")
                                ChatLogger.log(Text.literal("WynnSource dev command executed!"))
                                1
                            })
            )
        })
    }
}
