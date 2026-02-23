package fyw.fyi.wynnsource.event

import fyw.fyi.wynnsource.WynnSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object EventBus {
    private val _events = MutableSharedFlow<BaseEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events: SharedFlow<BaseEvent> = _events.asSharedFlow()

    suspend fun emit(event: BaseEvent) {
        _events.emit(event)
    }

    @JvmStatic
    fun emitSync(event: BaseEvent) {
        _events.tryEmit(event)
    }

    inline fun <reified T : BaseEvent> subscribe(
        scope: CoroutineScope,
        crossinline handler: suspend (T) -> Unit
    ) {
        events.filterIsInstance<T>()
            .onEach {
                try {
                    handler(it)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    WynnSource.logger.error("Error handling event ${it::class.simpleName}", e)
                }
            }
            .launchIn(scope)
    }
}
