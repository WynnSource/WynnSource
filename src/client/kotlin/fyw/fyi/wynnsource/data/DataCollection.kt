package fyw.fyi.wynnsource.data

abstract class DataCollection<T> {
    abstract val moduleId: String
    val entries: MutableList<DataEntry<T>> = mutableListOf()

    fun addEntry(key: String, value: T) {
        addEntry(DataEntry(key, System.currentTimeMillis(), value, true))
    }

    fun addEntry(entry: DataEntry<T>) {
        entry.dirty = true
        if (entries.any { it.key == entry.key }) {
            entries.replaceAll { if (it.key == entry.key) entry else it }
        } else {
            entries.add(entry)
        }
    }

    val dirty
        get() = entries.any { it.dirty }
    val dirtyEntries get() = entries.filter { it.dirty }

    abstract suspend fun submit()
}
