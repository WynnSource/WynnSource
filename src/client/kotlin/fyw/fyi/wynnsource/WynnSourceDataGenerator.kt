package fyw.fyi.wynnsource

import fyw.fyi.wynnsource.config.ui.ConfigScreen
import fyw.fyi.wynnsource.data.lang.LangRegistry
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object WynnSourceDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        // TODO do module registration first
        // module.addLang()...

        // Language data generation
        LangRegistry.translatable("wynnsource.example", "示例", "Example")
        ConfigScreen.initLang()
        LangRegistry.runDataGen(pack)
    }
}
