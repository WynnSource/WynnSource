package fyw.fyi.wynnsource.config.core

import fyw.fyi.wynnsource.data.lang.Translatable

/**
 * Represents a group of related config entries.
 * Groups can be collapsed/expanded in the UI.
 *
 * @param name The display name of the group
 * @param expanded Whether the group is expanded by default
 */
data class ConfigGroup(
    val name: Translatable,
    var expanded: Boolean = true
) {
    internal val entries = mutableListOf<ConfigEntry<*>>()

    /**
     * Get all config entries in this group.
     */
    fun getEntries(): List<ConfigEntry<*>> = entries.toList()
}
