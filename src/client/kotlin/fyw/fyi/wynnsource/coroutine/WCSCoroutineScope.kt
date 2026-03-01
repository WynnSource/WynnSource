package fyw.fyi.wynnsource.coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

object WCSCoroutineScope {
    private val supervisor = SupervisorJob()

    private fun namedThreadFactory(namePrefix: String): ThreadFactory {
        return object : ThreadFactory {
            private val count = AtomicInteger(1)
            override fun newThread(r: Runnable): Thread {
                return Thread(r, "$namePrefix-${count.getAndIncrement()}")
            }
        }
    }

    private val mainDispatcher = Executors.newSingleThreadExecutor(namedThreadFactory("WCS-Main"))
        .asCoroutineDispatcher()

    private val ioDispatcher = Executors.newSingleThreadExecutor(namedThreadFactory("WCS-IO"))
        .asCoroutineDispatcher()

    val Main = CoroutineScope(
        mainDispatcher + supervisor + CoroutineName("WCS-Main")
    )

    val IO = CoroutineScope(
        ioDispatcher + supervisor + CoroutineName("WCS-IO")
    )

    fun shutdown() {
        supervisor.cancel()
    }
}
