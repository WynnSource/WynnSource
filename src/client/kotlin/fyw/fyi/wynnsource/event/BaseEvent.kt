package fyw.fyi.wynnsource.event

sealed class BaseEvent {
    val timestamp: Long = System.currentTimeMillis()
}
