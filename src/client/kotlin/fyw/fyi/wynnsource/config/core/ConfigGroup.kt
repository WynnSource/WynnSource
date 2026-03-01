package fyw.fyi.wynnsource.config.core

import fyw.fyi.wynnsource.config.delegate.ConfigDelegate
import fyw.fyi.wynnsource.datagen.lang.LangRegistry
import fyw.fyi.wynnsource.datagen.lang.Translatable
import io.wispforest.owo.ui.core.UIComponent

/**
 * Represents a group of related config entries.
 * Groups can be collapsed/expanded in the UI.
 *
 * @param name The display name of the group
 * @param expanded Whether the group is expanded by default
 */
abstract class ConfigGroup(
    val name: Translatable,
    var expanded: Boolean = true,
) {
    internal val entries = mutableListOf<ConfigEntry<*>>()

    internal inline fun <reified T : Any> config(
        default: T,
        name: Translatable? = null,
        description: Translatable? = null
    ): ConfigDelegate<T> {
        val entry = ConfigEntry(
            key = "",
            default = default,
            type = T::class,
            name = name,
            description = description
        )
        entries.add(entry)
        return ConfigDelegate(entry)
    }

    open fun preGroup(): UIComponent? = null
    open fun postGroup(): UIComponent? = null

    protected fun translatable(key: String, cn: String, en: String): Translatable {
        return LangRegistry.translatable(key, cn, en)
    }

    fun getEntries(): List<ConfigEntry<*>> = entries.toList()
}
