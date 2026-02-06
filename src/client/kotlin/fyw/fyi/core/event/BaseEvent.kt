package fyw.fyi.core.event

sealed class BaseEvent {
    val timestamp: Long = System.currentTimeMillis()
}
