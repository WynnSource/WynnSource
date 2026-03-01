package fyw.fyi.wynnsource.config.core

import fyw.fyi.wynnsource.config.constraint.ValidationResult
import fyw.fyi.wynnsource.config.delegate.ConfigDelegate
import fyw.fyi.wynnsource.datagen.lang.LangRegistry
import fyw.fyi.wynnsource.datagen.lang.Translatable
import io.wispforest.owo.ui.core.UIComponent
import net.minecraft.item.Item

/**
 * Base class for config pages.
 * Each config page represents a tab in the config screen and is stored as a separate JSON file.
 *
 * @param id Unique identifier for this config page (used for file name)
 * @param name Display name of the config page
 * @param icon Optional item icon to display in the tab
 */
@Suppress("UNUSED")
abstract class ConfigPage(
    val id: String,
    val name: Translatable,
    val icon: Item? = null,
) {
    // Entries not in any group
    @PublishedApi
    internal val entries = mutableListOf<ConfigEntry<*>>()

    // Foldable group containing entries
    @PublishedApi
    internal val groups = mutableListOf<ConfigGroup>()

    /**
     * Helper to create a Translatable via LangRegistry.
     */
    protected fun translatable(key: String, cn: String, en: String): Translatable {
        return LangRegistry.translatable(key, cn, en)
    }

    /**
     * Register an entry to the ungrouped list.
     */
    @PublishedApi
    internal fun registerEntry(entry: ConfigEntry<*>) {
        entries.add(entry)
    }

    /**
     * Create a config entry with the given default value.
     * The entry is automatically added to the current group or the ungrouped list.
     */
    protected inline fun <reified T : Any> config(
        default: T,
        name: Translatable? = null,
        description: Translatable? = null
    ): ConfigDelegate<T> {
        val entry = ConfigEntry(
            key = "", // Will be set by delegate using property name
            default = default,
            type = T::class,
            name = name,
            description = description
        )

        registerEntry(entry)

        return ConfigDelegate(entry)
    }

    /**
     * Register a group.
     */
    protected fun <G : ConfigGroup> group(group: G): G {
        groups.add(group)
        return group
    }

    open fun prePage(): UIComponent? = null
    open fun postPage(): UIComponent? = null

    /**
     * Get all config entries, including those in groups.
     */
    fun allEntries(): List<ConfigEntry<*>> {
        return entries + groups.flatMap { it.entries }
    }

    /**
     * Validate all config entries and return any validation errors.
     */
    fun validate(): List<ValidationResult> {
        return allEntries()
            .map { it.validate() }
            .filter { !it.valid }
    }

    /**
     * Apply all pending values (commit them).
     */
    fun apply() {
        allEntries().forEach { it.commit() }
    }

    /**
     * Save this config page to file.
     */
    fun save() {
        ConfigSerializer.save(this)
    }

    /**
     * Load this config page from file.
     */
    fun load() {
        ConfigSerializer.load(this)
    }

    /**
     * Reset all entries to their default values.
     */
    fun resetToDefaults() {
        allEntries().forEach { it.resetToDefault() }
    }

    /**
     * Discard all pending changes.
     */
    fun discardChanges() {
        allEntries().forEach { it.discardPending() }
    }

    /**
     * Check if there are any unsaved changes.
     */
    fun hasUnsavedChanges(): Boolean {
        return allEntries().any { it.hasChanges() }
    }

    /**
     * Get ungrouped entries.
     */
    fun getUngroupedEntries(): List<ConfigEntry<*>> = entries.toList()

    /**
     * Get all groups.
     */
    fun getAllGroups(): List<ConfigGroup> = groups.toList()
}
