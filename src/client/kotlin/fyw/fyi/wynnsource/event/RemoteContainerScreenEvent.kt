package fyw.fyi.wynnsource.event

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen

data class RemoteContainerScreenEvent(val screen: GenericContainerScreen) : BaseEvent()
