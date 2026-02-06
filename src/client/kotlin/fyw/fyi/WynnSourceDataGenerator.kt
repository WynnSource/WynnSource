package fyw.fyi

import fyw.fyi.data.lang.LangHandler
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object WynnSourceDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        // TODO do module registration first
        // module.addLang()...

        // Language data generation
        LangHandler.translatable("wynnsource.example", "示例", "Example")
        LangHandler.runDataGen(pack)
    }
}
