package fyw.fyi.wynnsource.config

import fyw.fyi.wynnsource.config.core.ConfigGroup
import fyw.fyi.wynnsource.config.core.ConfigPage
import fyw.fyi.wynnsource.config.delegate.range
import fyw.fyi.wynnsource.config.delegate.url
import fyw.fyi.wynnsource.datagen.lang.LangRegistry
import fyw.fyi.wynnsource.datagen.lang.Translatable
import io.wispforest.owo.ui.component.UIComponents
import io.wispforest.owo.ui.container.UIContainers
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.UIComponent
import net.minecraft.text.Text

@Suppress("UNUSED")
object ExampleConfigPage : ConfigPage(
    id = "demo",
    name = LangRegistry.translatable("wynnsource.example", "示例", "Example")
) {
    override fun prePage(): UIComponent {
        // You can add a description or other components before the config entries
        return UIComponents.label(Text.of("This is the pre-page component."))
    }

    fun nonTranslatedConfig(key: String): Translatable = LangRegistry.nonRegisteredTranslatable(key)

    val exampleBoolean by config(
        default = true,
        name = nonTranslatedConfig(
            "config.wynnsource.example.boolean",
        ),
        description = nonTranslatedConfig(
            "config.wynnsource.example.boolean.desc",
        )
    )

    val exampleString by config(
        default = "https://example.com",
        name = nonTranslatedConfig(
            "config.wynnsource.example.string",
        ),
        description = nonTranslatedConfig(
            "config.wynnsource.example.string.desc",
        )
    ).url()

    val exampleInt by config(
        default = 42,
        name = nonTranslatedConfig(
            "config.wynnsource.example.int",
        ),
        description = nonTranslatedConfig(
            "config.wynnsource.example.int.desc",
        )
    )

    val exampleDouble by config(
        default = 3.14,
        name = nonTranslatedConfig(
            "config.wynnsource.example.double",
        ),
        description = nonTranslatedConfig(
            "config.wynnsource.example.double.desc",
        )
    )

    val exampleRange by config(
        default = 10,
        name = nonTranslatedConfig(
            "config.wynnsource.example.range",
        ),
        description = nonTranslatedConfig(
            "config.wynnsource.example.range.desc",
        )
    ).range(
        min = 0,
        max = 100,
        errorMessage = nonTranslatedConfig(
            "config.wynnsource.example.range.error",
        )
    )

    val exampleEntryWithCustomComponent by config(
        default = "Custom Component",
        name = nonTranslatedConfig(
            "config.wynnsource.example.customcomponent",
        ),
        description = nonTranslatedConfig(
            "config.wynnsource.example.customcomponent.desc",
        )
    ).preEntryComponent {
        UIComponents.label(Text.of("This is a custom pre-entry component for ${it.key}"))
    }.postEntryComponent {
        UIComponents.label(Text.of("This is a custom post-entry component for ${it.key}"))
    }.overrideEntryComponent { entry ->
        // This completely replaces the default input component with a custom one
        UIContainers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
            child(UIComponents.label(Text.of("Custom Component for ${entry.key}: ")))
            child(
                UIComponents.textBox(Sizing.fill()).apply {
                    text(entry.getPending())
                    onChanged().subscribe { text ->
                        entry.setPending("$text (edited)")
                    }
                }
            )
        }
    }

    object ExampleGroup : ConfigGroup(
        name = nonTranslatedConfig("config.global.advanced"),
        expanded = false
    ) {
        override fun preGroup(): UIComponent {
            return UIComponents.label(Text.of("This is the pre-group component."))
        }

        val groupEntry by config(
            default = "Group Entry",
            name = nonTranslatedConfig(
                "config.wynnsource.example.group.entry",
            ),
            description = nonTranslatedConfig(
                "config.wynnsource.example.group.entry.desc",
            )
        )

        override fun postGroup(): UIComponent {
            return UIComponents.label(Text.of("This is the post-group component."))
        }
    }

    val exampleGroup = group(ExampleGroup)

    override fun postPage(): UIComponent {
        // You can add components after the config entries as well
        return UIComponents.label(Text.of("This is the post-page component."))
    }
}
