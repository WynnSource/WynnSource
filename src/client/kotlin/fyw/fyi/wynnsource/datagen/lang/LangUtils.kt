package fyw.fyi.wynnsource.datagen.lang

object LangUtils {
    // From https://github.com/The-Fireplace/MC-en-UD-Generator/blob/5c3456262be80bf5e56180b04b726c9dbf35e57a/src/main/kotlin/thefireplace/en2ud/EN2UD.kt#L40
    private val map =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()-_[]{};':\",.<>/?§+|= "
            .zip("ⱯᗺƆᗡƎℲ⅁HIՐʞꞀWNOԀΌᴚS⟘∩ᴧMX⅄Zɐqɔpǝɟᵷɥᴉɾʞꞁɯuodbɹsʇnʌʍxʎz⥝ᘔƐᔭ59Ɫ860¡@#$%^⅋*)(-‾][}{؛,:„'˙></¿§+|= ")
            .toMap()
    
    fun String.toEnUd(): String = this.map { map[it] ?: it }.joinToString("")

    fun String.trimAllLineStart(): String = this.lines().joinToString("\n") { it.trimStart() }
}
