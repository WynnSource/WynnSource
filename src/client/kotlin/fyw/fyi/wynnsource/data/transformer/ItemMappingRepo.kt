package fyw.fyi.wynnsource.data.transformer

import fyw.fyi.wynnsource.data.repository.BaseRepository
import fyw.fyi.wynnsource.server.models.MappingType
import fyw.fyi.wynnsource.utils.NetUtils
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
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

    override suspend fun fetch(): IdentificationMapping {
        val response = NetUtils.miscClient.getMappings(MappingType.identification)
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

    override suspend fun fetch(): ShinyMapping {
        val response = NetUtils.miscClient.getMappings(MappingType.shiny)
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

// This is currently only a partial item mapping for wynncraft official api
@Serializable
data class Item(
    val internalName: String,
    val type: String
)

object ItemDatabase : BaseRepository<Map<String, Item>>() {
    override val id: String = "item_database"
    override val ttlSeconds: Long = 24 * 60 * 60 // 24 hours
    override val dataSerializer: KSerializer<Map<String, Item>> =
        MapSerializer(String.serializer(), Item.serializer())

    const val url = "https://api.wynncraft.com/v3/item/database?fullResult"

    val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun fetch(): Map<String, Item> {
        val response = NetUtils.httpClient.get(url)
        if (response.status.value in 200..299) {
            val responseBody = response.bodyAsText()
            return json.decodeFromString(dataSerializer, responseBody)
        } else {
            error("Failed to fetch item database: HTTP ${response.status}")
        }
    }
}
