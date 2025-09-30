package fyi.fyw.wynnsource.utils

import fyi.fyw.wynnsource.WynnSourceEntry
import java.net.URI

object NetUtils {
    fun post(url: String, data: String, headers: Map<String, String> = mapOf()): String? {
        return try {
            val connection = URI.create(url).toURL().openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            // Set custom headers
            for ((key, value) in headers) {
                connection.setRequestProperty(key, value)
            }
            connection.outputStream.use { it.write(data.toByteArray()) }
            if (connection.responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                WynnSourceEntry.LOGGER.warn(
                    "Failed to post data to $url: ${connection.responseCode} ${connection.responseMessage}"
                )
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}