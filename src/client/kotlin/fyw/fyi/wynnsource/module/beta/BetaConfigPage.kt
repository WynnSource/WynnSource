package fyw.fyi.wynnsource.module.beta

import fyw.fyi.wynnsource.config.core.ConfigPage
import fyw.fyi.wynnsource.datagen.lang.LangRegistry
import fyw.fyi.wynnsource.datagen.lang.LangUtils.trimAllLineStart

object BetaConfigPage : ConfigPage(
    id = "beta",
    name = LangRegistry.translatable(
        "config.wynnsource.beta",
        "Hero Beta 配置",
        "Hero Beta Config"
    )
) {
    var enableNewItemCollection by config(
        true,
        LangRegistry.translatable(
            "config.wynnsource.beta.enable_new_item_collection",
            "启用新物品收集",
            "Enable new item collecting"
        ),
        LangRegistry.translatable(
            "config.wynnsource.beta.enable_new_item_collection.desc",
            "启用后，WynnSource 将尝试收集游戏中未在现有数据库中记录的物品数据。有助于完善数据库。",
            """Enable this to allow WynnSource to attempt collecting data on items
                not already recorded in the existing database.
                This helps improve the database completeness.
            """.trimAllLineStart()
        )
    )
}
