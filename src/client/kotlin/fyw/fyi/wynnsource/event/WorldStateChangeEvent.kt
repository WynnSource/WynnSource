package fyw.fyi.wynnsource.event

import com.wynntils.models.worlds.type.WorldState

data class WorldStateChangeEvent(
    val newState: WorldState,
    val oldState: WorldState,
    val newWorldName: String,
    val isFirstJoinWorld: Boolean
) : BaseEvent()
