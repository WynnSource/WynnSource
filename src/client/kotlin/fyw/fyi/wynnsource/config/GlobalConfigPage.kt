package fyw.fyi.wynnsource.config

import fyw.fyi.wynnsource.config.core.ConfigGroup
import fyw.fyi.wynnsource.config.core.ConfigPage
import fyw.fyi.wynnsource.config.delegate.range
import fyw.fyi.wynnsource.config.delegate.url
import fyw.fyi.wynnsource.datagen.lang.LangRegistry
import fyw.fyi.wynnsource.utils.NetUtils

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
            )
    }

    val reporting = group(
        Reporting
    )

    object Advanced : ConfigGroup(
        name = translatable("config.global.advanced", "高级设置", "Advanced Settings"),
        expanded = false
    ) {
        var logLevel by config(default = LogLevel.INFO)
            .name(translatable("config.advanced.loglevel", "日志级别", "Log Level"))
            .description(
                translatable(
                    "config.advanced.loglevel.desc",
                    "模组的日志输出级别",
                    "Log output level for the mod"
                )
            )

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
            )
    }

    val advanced = group(Advanced)

    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}
