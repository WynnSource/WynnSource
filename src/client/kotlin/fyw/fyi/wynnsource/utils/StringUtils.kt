package fyw.fyi.wynnsource.utils

object StringUtils {
    fun String.splitByCodePoint(): Array<String> {
        return this.codePoints().toArray().let { codepoints ->
            Array(codepoints.size) { index -> String(codepoints, index, 1) }
        }
    }

    infix fun String.isNewerThan(other: String?): Boolean {
        val v1 = this.split(".").map { it.toIntOrNull() ?: 0 }
        if (other.isNullOrEmpty()) return true
        val v2 = other.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLen = maxOf(v1.size, v2.size)

        for (i in 0 until maxLen) {
            val part1 = v1.getOrElse(i) { 0 }
            val part2 = v2.getOrElse(i) { 0 }
            if (part1 > part2) return true
            if (part1 < part2) return false
        }
        return false
    }
}
