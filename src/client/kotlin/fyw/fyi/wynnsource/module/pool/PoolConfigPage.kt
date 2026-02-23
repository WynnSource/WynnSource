package fyw.fyi.wynnsource.module.pool

import fyw.fyi.wynnsource.config.core.ConfigPage
import fyw.fyi.wynnsource.datagen.lang.LangRegistry

object PoolConfigPage : ConfigPage(
    id = "pool",
    name = LangRegistry.translatable(
        "config.wynnsource.pool",
        "奖励池配置",
        "Pool Config"
    )
) {
    var enabled by config(
        true,
        translatable(
            "config.wynnsource.pool.enabled",
            "启用奖励池收集",
            "Enable Pool Data Collection"
        )
    )
}
