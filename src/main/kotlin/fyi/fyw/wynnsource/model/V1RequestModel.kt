package fyi.fyw.wynnsource.model;


import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable;

@Serializable
data class RequestShinyData(
    val item: String,
    val tracker: String
)

@Serializable
data class RequestItemData(
    @SerialName("Mythic") val mythic: MutableSet<String> = mutableSetOf(),
    @SerialName("Fabled") val fabled: MutableSet<String> = mutableSetOf(),
    @SerialName("Legendary") val legendary: MutableSet<String> = mutableSetOf(),
    @SerialName("Rare") val rare: MutableSet<String> = mutableSetOf(),
    @SerialName("Unique") val unique: MutableSet<String> = mutableSetOf()
)

enum class LootPoolType() {
    @SerialName("lr_item_pool")
    LR(),

    @SerialName("raid_aspect_pool")
    RAID_ASPECT(),

    @SerialName("raid_tome_pool")
    RAID_TOME(),
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class CrowdSourceLootPoolData(
    var shiny: RequestShinyData? = null,

    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val items: RequestItemData = RequestItemData(),
    val type: LootPoolType,
    val location: String,
    val page: Int
)

