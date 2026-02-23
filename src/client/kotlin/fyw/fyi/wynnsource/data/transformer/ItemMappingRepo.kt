package fyw.fyi.wynnsource.data.transformer

import fyw.fyi.wynnsource.data.repository.BaseRepository
import fyw.fyi.wynnsource.server.apis.MiscApi
import fyw.fyi.wynnsource.server.models.MappingType
import fyw.fyi.wynnsource.utils.NetUtils
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MappingEntry(
    val id: Int,
    val key: String,
)

@Serializable
data class IdentificationMapping(
    val data: List<MappingEntry>,
    val lastUpdated: Instant
)

object IdentificationMappingRepo : BaseRepository<IdentificationMapping>() {
    override val id: String = "item_identification_mapping"
    override val ttlSeconds: Long = 60 * 60 // 1 hour
    override val dataSerializer = IdentificationMapping.serializer()

    val apiClient = MiscApi(
        NetUtils.baseUrl,
        NetUtils.sharedEngine,
        NetUtils.sharedConfig
    )

    override suspend fun fetch(): IdentificationMapping {
        val response = apiClient.getMappings(MappingType.identification)
        if (response.success) {
            return IdentificationMapping(
                data = response.body().data.map { MappingEntry(it.id, it.key) },
                lastUpdated = response.body().lastUpdated
            )
        } else {
            error("Failed to fetch identification mapping: HTTP ${response.status}")
        }
    }

    fun fromApiName(apiName: String): MappingEntry? {
        return getOrNull()?.data?.find { it.key == apiName }
    }
}

@Serializable
data class ShinyMapping(
    val data: List<MappingEntry>,
    val lastUpdated: Instant
)

object ShinyMappingRepo : BaseRepository<ShinyMapping>() {
    override val id: String = "item_shiny_mapping"
    override val ttlSeconds: Long = 60 * 60 // 1 hour
    override val dataSerializer = ShinyMapping.serializer()

    val apiClient = MiscApi(
        NetUtils.baseUrl,
        NetUtils.sharedEngine,
        NetUtils.sharedConfig
    )

    override suspend fun fetch(): ShinyMapping {
        val response = apiClient.getMappings(MappingType.shiny)
        if (response.success) {
            return ShinyMapping(
                data = response.body().data.map { MappingEntry(it.id, it.key) },
                lastUpdated = response.body().lastUpdated
            )
        } else {
            error("Failed to fetch identification mapping: HTTP ${response.status}")
        }
    }

    fun fromApiName(apiName: String): MappingEntry? {
        return IdentificationMappingRepo.getOrNull()?.data?.find { it.key == apiName }
    }
}
