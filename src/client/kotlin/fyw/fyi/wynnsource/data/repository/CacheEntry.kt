package fyw.fyi.wynnsource.data.repository

import kotlinx.serialization.Serializable

@Serializable
data class CacheEntry<T>(
    val data: T,
    val metadata: CacheMetadata
) {
    @Serializable
    data class CacheMetadata(
        val lastUpdated: Long,
        val ttlSeconds: Long,
    ) {
        val isExpired: Boolean
            get() = System.currentTimeMillis() - lastUpdated > ttlSeconds * 1000L
    }
}
