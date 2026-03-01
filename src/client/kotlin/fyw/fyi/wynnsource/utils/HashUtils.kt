package fyw.fyi.wynnsource.utils

import java.security.MessageDigest

object HashUtils {
    val md = MessageDigest.getInstance("MD5")

    fun md5Hash(input: String): String {
        val hashBytes = md.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
