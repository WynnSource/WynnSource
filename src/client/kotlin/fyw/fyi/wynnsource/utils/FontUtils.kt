package fyw.fyi.wynnsource.utils

object FontUtils {

    const val TEXT_SEQUENCE =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ?[]/%&0123456789!()<=>"

    // Map from U+E000 to U+E02F
    // Map from U+E030 to U+E05F
    fun fromBanner(text: String): String {
        val sb = StringBuilder()
        for (char in text) {
            if (char in '\uE000'..'\uE02F') {
                sb.append(TEXT_SEQUENCE[char - '\uE000'])
            } else if (char in '\uE030'..'\uE05F') {
                sb.append(TEXT_SEQUENCE[char - '\uE030'])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    fun filterAscii(text: String): String {
        return text.filter { it in '\u0000'..'\u007F' }
    }
}
