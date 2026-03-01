package fyw.fyi.wynnsource.module.beta

import fyw.fyi.wynnsource.WynnSource
import fyw.fyi.wynnsource.data.DataCollection
import fyw.fyi.wynnsource.schema.WynnSourceItemOuterClass
import fyw.fyi.wynnsource.server.models.NewItemSubmission
import fyw.fyi.wynnsource.utils.NetUtils
import java.util.*
import kotlin.time.Instant

object BetaDataCollection : DataCollection<WynnSourceItemOuterClass.WynnSourceItem>() {
    override val moduleId = BetaModule.name

    val b64encoder: Base64.Encoder = Base64.getEncoder()

    override suspend fun submit() {
        NetUtils.betaClient.submitBetaItem(
            NewItemSubmission(
                clientTimestamp = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                modVersion = WynnSource.version ?: "unknown",
                items = dirtyEntries.map { b64encoder.encodeToString(it.value.toByteArray()) }
            )
        )
    }
}
