package fyw.fyi.wynnsource.config.core

/**
 * Global registry for all config pages.
 * Manages registration, loading, and saving of config pages.
 */
@Suppress("unused")
object ConfigRegistry {
    private val pages = mutableListOf<ConfigPage>()
    private var globalPage: ConfigPage? = null

    /**
     * Register a config page.
     * Pages are displayed in the order they are registered.
     */
    fun register(page: ConfigPage) {
        pages.add(page)
    }

    /**
     * Register a config page as the global/main config.
     * The global page is always displayed first.
     */
    fun registerGlobal(page: ConfigPage) {
        globalPage = page
        // Insert at the beginning
        pages.add(0, page)
    }

    fun getPages(): List<ConfigPage> = pages.toList()
    fun getGlobalPage(): ConfigPage? = globalPage
    fun getPage(id: String): ConfigPage? = pages.find { it.id == id }

    /**
     * Load all registered config pages from their files.
     */
    fun loadAll() {
        pages.forEach { it.load() }
    }

    /**
     * Save all registered config pages to their files.
     */
    fun saveAll() {
        pages.forEach { it.save() }
    }

    /**
     * Check if any page has unsaved changes.
     */
    fun hasAnyUnsavedChanges(): Boolean {
        return pages.any { it.hasUnsavedChanges() }
    }

    /**
     * Discard all unsaved changes across all pages.
     */
    fun discardAllChanges() {
        pages.forEach { it.discardChanges() }
    }

    internal fun clear() {
        pages.clear()
        globalPage = null
    }
}
