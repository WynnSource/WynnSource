package fyw.fyi.wynnsource.module

import fyw.fyi.wynnsource.config.core.ConfigPage
import fyw.fyi.wynnsource.data.DataCollection
import fyw.fyi.wynnsource.data.repository.BaseRepository
import org.slf4j.LoggerFactory

abstract class BaseModule {
    abstract val name: String
    abstract val config: ConfigPage?
    abstract val dataCollection: DataCollection<*>?
    abstract val repo: List<BaseRepository<*>>

    abstract fun subscribeEvents()

    val logger: org.slf4j.Logger
        get() = LoggerFactory.getLogger("WynnSource-Module-$name")
}
