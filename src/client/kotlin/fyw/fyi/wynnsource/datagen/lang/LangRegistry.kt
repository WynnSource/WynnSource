package fyw.fyi.wynnsource.datagen.lang

import fyw.fyi.wynnsource.datagen.lang.LangUtils.trimAllLineStart
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object LangRegistry {
    private val LANG = Object2ObjectOpenHashMap<String, Translatable>()
    private val SUPPORTED_LANGS = mapOf(
        "zh_cn" to Translatable::zh_cn,
        "zh_tw" to Translatable::zh_cn, // No traditional Chinese translations yet
        "en_us" to Translatable::en_us,
        "en_ud" to Translatable::en_ud,
    )

    fun translatable(key: String, cn: String, en: String): Translatable {
        val translatable = Translatable(key, cn.trimAllLineStart(), en.trimAllLineStart())
        LANG[key] = translatable
        return translatable
    }

    fun getTranslations(languageCode: String): Map<String, String> {
        val extractor = SUPPORTED_LANGS[languageCode]
            ?: Translatable::en_us // Default to en_us if unsupported language code
        return LANG.mapValues { (_, translatable) -> extractor(translatable) }
    }

    fun runDataGen(pack: FabricDataGenerator.Pack) {
        SUPPORTED_LANGS.keys.forEach { langCode ->
            pack.addProvider { output, lookupFuture ->
                LangProvider(
                    output,
                    langCode,
                    lookupFuture
                )
            }
        }
    }
}
