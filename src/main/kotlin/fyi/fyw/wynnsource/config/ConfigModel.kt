package fyi.fyw.wynnsource.config

import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Modmenu


@Modmenu(modId = "wynnsource")
@Config(name = "wynnsource", wrapperName = "WynnSourceConfig")
class ConfigModel {
    @JvmField
    public var enabled: Boolean = false

    @JvmField
    public var collectRewardPool: Boolean = false

    @JvmField
    public var reportToServer: Boolean = false

    @JvmField
    public var reportApiEndpoint: String = ""

    @JvmField
    public var reportApiKey: String = ""

    @JvmField
    public var reportInterval: Long = 15000
}