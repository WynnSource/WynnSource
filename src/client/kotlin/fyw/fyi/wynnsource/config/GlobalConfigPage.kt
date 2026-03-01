package fyw.fyi.wynnsource.config

import fyw.fyi.wynnsource.config.core.ConfigGroup
import fyw.fyi.wynnsource.config.core.ConfigPage
import fyw.fyi.wynnsource.config.delegate.range
import fyw.fyi.wynnsource.config.delegate.url
import fyw.fyi.wynnsource.config.ui.VanillaButtonRenderer
import fyw.fyi.wynnsource.coroutine.WCSCoroutineScope
import fyw.fyi.wynnsource.datagen.lang.LangRegistry
import fyw.fyi.wynnsource.utils.HashUtils
import fyw.fyi.wynnsource.utils.NetUtils
import io.wispforest.owo.ui.component.UIComponents
import io.wispforest.owo.ui.container.UIContainers
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient

@Suppress("unused")
object GlobalConfigPage : ConfigPage(
    id = "global",
    name = LangRegistry.translatable(
        "config.wynnsource.global",
        "全局配置",
        "Global Config"
    )
) {
    object Reporting : ConfigGroup(
        name = translatable("config.global.reporting", "数据上报", "Reporting"),
        expanded = true
    ) {
        var reportInterval by config(default = 15L)
            .name(translatable("config.reporting.interval", "上报间隔(秒)", "Report Interval (sec)"))
            .description(
                translatable(
                    "config.reporting.interval.desc",
                    "向服务器上报数据的最小时间间隔",
                    "Minimum time interval between reports to the server"
                )
            )
            .range(
                5L,
                30L,
                translatable(
                    "config.reporting.interval.error",
                    "间隔必须在 5-30 秒之间",
                    "Interval must be between 5-30 seconds"
                )
            )

        var apiEndpoint by config(default = "")
            .name(translatable("config.reporting.endpoint", "API 地址", "API Endpoint"))
            .description(
                translatable(
                    "config.reporting.endpoint.desc",
                    "用于上传数据的服务器 API 接口地址",
                    "Server API endpoint for uploading data"
                )
            )
            .url(
                translatable(
                    "config.reporting.endpoint.error",
                    "请输入有效的 URL (以 http(s):// 开头)",
                    "Please enter a valid URL (starting with http(s)://)"
                )
            ).onChange { _, new -> NetUtils.updateBaseUrl(new) }
            .onLoad { new -> NetUtils.updateBaseUrl(new) }

        var apiKey by config(default = "")
            .name(translatable("config.reporting.apikey", "API 密钥", "API Key"))
            .description(
                translatable(
                    "config.reporting.apikey.desc",
                    "用于身份验证的 API 密钥",
                    "API key for authentication"
                )
            ).postEntryComponent { entry ->
                UIContainers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                    gap(2)
                    verticalAlignment(VerticalAlignment.CENTER)

                    child(
                        UIComponents.label(
                            registerText.toComponent()
                        )
                    )

                    child(
                        VanillaButtonRenderer.vanillaButton(
                            registerBtn.toComponent()
                        ) {
                            // For now we just use the uuid hashed with md5 and a random salt as the token.
                            val uuid = MinecraftClient.getInstance().session.uuidOrNull?.toString()
                                ?: return@vanillaButton
                            val salt = List(16) { (0..255).random().toByte() }.toByteArray()
                            val token = HashUtils.md5Hash("$uuid:${salt.decodeToString()}")
                            WCSCoroutineScope.IO.launch {
                                val response = NetUtils.managementClient.registerUser(
                                    token = token
                                )
                                if (response.success) {
                                    entry.setPending(token)
                                    entry.setCommitted(token)
                                }
                            }
                        }.tooltip(
                            registerBtn.toComponent()
                        )
                    )
                }
            }
    }

    val reporting = group(
        Reporting
    )

    object Advanced : ConfigGroup(
        name = translatable("config.global.advanced", "高级设置", "Advanced Settings"),
        expanded = false
    ) {
        var debugMode by config(
            default = false,
            name = translatable("config.advanced.debug", "调试模式", "Debug Mode"),
            description = translatable(
                "config.advanced.debug.desc",
                "启用额外的调试信息输出",
                "Enable additional debug information output"
            )
        )

        var experimentalFlag by config(default = 0)
            .name(
                translatable(
                    "config.advanced.experimental",
                    "实验性功能",
                    "Experimental Features"
                )
            ).postEntryComponent {
                UIComponents.label(
                    experimentalFlagWarning.toComponent()
                ).color(Color.RED).margins(Insets.of(4, 0, 0, 0))
            }
    }

    val advanced = group(Advanced)

    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    val registerText = translatable(
        "config.reporting.apikey.register",
        "没有 API 密钥？点击这里注册",
        "Don't have an API key? Click here to register"
    )
    val registerBtn = translatable(
        "config.reporting.apikey.register.button",
        "注册",
        "Register"
    )
    val registerBtnWarning = translatable(
        "config.reporting.apikey.register.warning",
        """注册后文本框内会出现一个新的 API 密钥
            |请勿重复点击
            |重复点击将导致密钥被覆盖且无法进行注册""".trimMargin(),
        """A new API key will appear in the text box after registration.
            |Please do not click repeatedly.
            |Repeated clicks will overwrite the key and prevent further registration.""".trimMargin()
    )
    val experimentalFlagWarning = translatable(
        "config.advanced.experimental.warning",
        "启用后可能会导致不稳定！",
        "May cause instability when enabled!"
    )
}
