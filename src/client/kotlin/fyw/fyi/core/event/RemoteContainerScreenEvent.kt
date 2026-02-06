package fyw.fyi.core.event

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen

data class RemoteContainerScreenEvent(val screen: GenericContainerScreen) : BaseEvent()
