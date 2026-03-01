package fyw.fyi.wynnsource.module.beta

import fyw.fyi.wynnsource.data.repository.BaseRepository
import fyw.fyi.wynnsource.server.models.ItemReturnType
import fyw.fyi.wynnsource.utils.NetUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.jsonPrimitive

object BetaItemRepo : BaseRepository<List<String>>() {
    override val id: String = "${BetaModule.name}_items"
    override val ttlSeconds: Long = 5 * 60 // shorter TTL since beta items may change more frequently
    override val dataSerializer: KSerializer<List<String>>
        get() = ListSerializer(String.serializer())

    override suspend fun fetch(): List<String> {
        val response = NetUtils.betaClient.listBetaItems(ItemReturnType.name_only)
        if (response.success) {
            return response.body().data.items.map { it.jsonPrimitive.content }
        } else {
            error("Failed to fetch beta items: HTTP ${response.status}")
        }
    }
}

object BetaOtherRepo {
    // TODO
}
