package fyw.fyi.wynnsource.module.pool

import fyw.fyi.wynnsource.WynnSource
import fyw.fyi.wynnsource.config.GlobalConfigPage
import fyw.fyi.wynnsource.data.DataCollection
import fyw.fyi.wynnsource.data.DataEntry
import fyw.fyi.wynnsource.schema.WynnSourceItemOuterClass
import fyw.fyi.wynnsource.server.models.PoolSubmissionSchema
import fyw.fyi.wynnsource.server.models.PoolType
import fyw.fyi.wynnsource.utils.NetUtils
import java.util.*
import kotlin.time.Instant

object PoolDataCollection : DataCollection<List<WynnSourceItemOuterClass.WynnSourceItem>>() {
    override val moduleId = PoolModule.name

    val b64encoder: Base64.Encoder = Base64.getEncoder()

    override suspend fun submit() {
        NetUtils.poolClient.setApiKey(GlobalConfigPage.reporting.apiKey)
        NetUtils.poolClient.submitPoolData(
            dirtyEntries.map {
                entryToSchema(it)
            }
        )
    }

    fun entryToSchema(entry: DataEntry<List<WynnSourceItemOuterClass.WynnSourceItem>>): PoolSubmissionSchema {
        val keys = entry.key.split(':')
        val poolType = when (keys[0]) {
            "Loot" -> PoolType.lr_item_pool
            "Raid_items" -> PoolType.raid_item_pool
            "Raid_aspects" -> PoolType.raid_aspect_pool
            else -> throw IllegalArgumentException("Invalid pool type: ${keys[0]}")
        }

        val region = keys[1]
        val page = keys[2].toIntOrNull() ?: throw IllegalArgumentException("Invalid page number: ${keys[2]}")

        return PoolSubmissionSchema(
            poolType = poolType,
            region = region,
            page = page,
            clientTimestamp = Instant.fromEpochMilliseconds(entry.timestamp),
            modVersion = WynnSource.version ?: "unknown",
            items = entry.value.map {
                b64encoder.encodeToString(it.toByteArray())
            }
        )
    }
}
