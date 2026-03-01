package fyw.fyi.wynnsource.config.ui

import fyw.fyi.wynnsource.config.core.ConfigEntry
import io.wispforest.owo.ui.core.UIComponent

typealias ConfigEntryComponentBuilder<T> = (entry: ConfigEntry<T>) -> UIComponent
