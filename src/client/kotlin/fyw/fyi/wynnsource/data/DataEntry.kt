package fyw.fyi.wynnsource.data

data class DataEntry<T>(
    val key: String,
    val timestamp: Long,
    val value: T,
    var dirty: Boolean = false
)
