package fyw.fyi.wynnsource.utils


object StringUtils {
    fun String.splitByCodePoint(): Array<String> {
        return this.codePoints().toArray().let { codepoints ->
            Array(codepoints.size) { index -> String(codepoints, index, 1) }
        }
    }
}
