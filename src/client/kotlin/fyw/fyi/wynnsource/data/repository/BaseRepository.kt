package fyw.fyi.wynnsource.data.repository

import kotlinx.serialization.KSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseRepository<T : Any> {
    abstract val id: String
    abstract val ttlSeconds: Long
    abstract val dataSerializer: KSerializer<T>

    protected val logger: Logger by lazy { LoggerFactory.getLogger("WynnSource-Repo-$id") }

    @Volatile
    private var memoryCache: CacheEntry<T>? = null

    abstract suspend fun fetch(): T

    open fun shouldUpdate(cached: CacheEntry<T>?): Boolean {
        return cached == null || cached.metadata.isExpired
    }

    @Throws(IllegalStateException::class)
    suspend fun get(): T {
        memoryCache?.let { return it.data }

        val diskCache = loadFromDisk()
        if (diskCache != null) {
            memoryCache = diskCache
            return diskCache.data
        }

        return forceRefresh().let {
            memoryCache?.data ?: error("Failed to fetch data for repository '$id'")
        }
    }

    fun getOrNull(): T? {
        memoryCache?.let { return it.data }
        val diskCache = loadFromDisk()
        if (diskCache != null) {
            memoryCache = diskCache
            return diskCache.data
        }
        return null
    }

    suspend fun refresh() {
        val cached = memoryCache ?: loadFromDisk()
        memoryCache = cached

        if (!shouldUpdate(cached)) {
            logger.debug("Cache for '{}' is still valid, skipping refresh", id)
        }

        try {
            val data = fetch()
            val entry = CacheEntry(
                data = data,
                metadata = CacheEntry.CacheMetadata(
                    lastUpdated = System.currentTimeMillis(),
                    ttlSeconds = ttlSeconds
                )
            )
            saveToDisk(entry)
            memoryCache = entry
            logger.info("Repository '{}' updated successfully", id)
        } catch (e: Exception) {
            logger.error("Failed to refresh repository '{}': {}", id, e.message)
        }
    }


    suspend fun forceRefresh() {
        return try {
            val data = fetch()
            val entry = CacheEntry(
                data = data,
                metadata = CacheEntry.CacheMetadata(
                    lastUpdated = System.currentTimeMillis(),
                    ttlSeconds = ttlSeconds
                )
            )
            saveToDisk(entry)
            memoryCache = entry
            logger.info("Repository '{}' force-refreshed successfully", id)
        } catch (e: Exception) {
            logger.error("Failed to force-refresh repository '{}': {}", id, e.message)
        }
    }

    fun invalidate() {
        memoryCache = null
        CacheStore.delete(id)
        logger.info("Repository '{}' cache invalidated", id)
    }

    private fun loadFromDisk(): CacheEntry<T>? {
        return CacheStore.load(id, cacheEntrySerializer())
    }

    private fun saveToDisk(entry: CacheEntry<T>) {
        CacheStore.save(id, entry, cacheEntrySerializer())
    }

    private fun cacheEntrySerializer(): KSerializer<CacheEntry<T>> {
        return CacheEntry.serializer(dataSerializer)
    }
}
