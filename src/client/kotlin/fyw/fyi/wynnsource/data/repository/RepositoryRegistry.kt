package fyw.fyi.wynnsource.data.repository

import fyw.fyi.wynnsource.data.transformer.IdentificationMappingRepo
import fyw.fyi.wynnsource.data.transformer.ShinyMappingRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

object RepositoryRegistry {
    private val logger = LoggerFactory.getLogger("WynnSource-RepoManager")
    private val repositories = mutableListOf<BaseRepository<*>>()
    private var refreshJob: Job? = null

    init {
        register(IdentificationMappingRepo)
        register(ShinyMappingRepo)
    }

    fun register(repository: BaseRepository<*>) {
        require(repositories.none { it.id == repository.id }) {
            "Repository with id '${repository.id}' is already registered."
        }
        repositories.add(repository)
        logger.debug("Registered repository: {}", repository.id)
    }

    fun getRepositories(): List<BaseRepository<*>> = repositories.toList()
    fun get(id: String): BaseRepository<*>? = repositories.find { it.id == id }

    fun start(scope: CoroutineScope) {
        refreshJob = scope.launch {
            while (this.isActive) {
                refreshAll(this)
                delay(60 * 60 * 1000L) // Auto-refresh only once per hour.
            }
        }
    }

    suspend fun refreshAll(scope: CoroutineScope) {
        logger.info("Checking updates for {} repositories...", repositories.size)
        repositories.map { repo ->
            scope.async {
                repo.id to repo.refresh()
            }
        }.awaitAll()

        logger.info("Repository update check complete.")
    }

    suspend fun forceRefreshAll(scope: CoroutineScope) {
        logger.info("Force-refreshing all {} repositories...", repositories.size)
        repositories.map { repo ->
            scope.async {
                repo.id to repo.forceRefresh()
            }
        }.awaitAll()
    }

    suspend fun forceRefresh(id: String) {
        val repo = repositories.find { it.id == id }
        repo?.forceRefresh()
    }

    fun invalidateAll() {
        repositories.forEach { it.invalidate() }
    }
}
