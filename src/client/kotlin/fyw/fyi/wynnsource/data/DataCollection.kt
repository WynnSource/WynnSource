package fyw.fyi.wynnsource.data

abstract class DataCollection<T> {
    abstract val moduleId: String
    val entries: MutableList<DataEntry<T>> = mutableListOf()

    fun addEntry(key: String, value: T) {
        addEntry(DataEntry(key, System.currentTimeMillis(), value, true))
    }

    fun addEntry(entry: DataEntry<T>) {
        val index = entries.indexOfFirst { it.key == entry.key }

        when {
            index != -1 && entries[index].value == entry.value -> return
            index != -1 -> entries[index] = entry
            else -> entries.add(entry)
        }

        entry.dirty = true
    }

    val dirty
        get() = entries.any { it.dirty }
    val dirtyEntries get() = entries.filter { it.dirty }

    abstract suspend fun submit()
}
