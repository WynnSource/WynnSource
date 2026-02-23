package fyw.fyi.wynnsource.datagen.lang

import fyw.fyi.wynnsource.datagen.lang.LangUtils.toEnUd
import net.minecraft.text.MutableText
import net.minecraft.text.Text

@Suppress("FunctionName")
data class Translatable(val key: String, val cn: String, val en: String) {
    fun zh_cn() = cn
    fun en_us() = en
    fun en_ud() = en.toEnUd() // why not

    fun toComponent(): MutableText {
        return Text.translatable(key)
    }
}
