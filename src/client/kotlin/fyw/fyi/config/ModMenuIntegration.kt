package fyw.fyi.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import fyw.fyi.config.ui.ConfigScreen

/**
 * ModMenu integration for WynnSource.
 * Provides a config screen factory that opens our custom ConfigScreen.
 */
class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent -> ConfigScreen(parent) }
    }
}
