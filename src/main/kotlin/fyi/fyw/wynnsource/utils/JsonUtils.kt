package fyi.fyw.wynnsource.utils

import kotlinx.serialization.json.Json
import java.nio.file.Path

object JsonUtils {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    inline fun <reified T> readFromFile(path: Path): T {
        return json.decodeFromString(
            FileUtils.readFromFile(path) ?: throw IllegalArgumentException("File not found: $path")
        )
    }

    inline fun <reified T> writeToFile(path: Path, data: T) {
        FileUtils.writeToFile(path, json.encodeToString(data))
    }

    inline fun <reified T> parseJson(jsonString: String): T {
        return json.decodeFromString(jsonString)
    }

    inline fun <reified T> serializeToJson(data: T): String {
        return json.encodeToString(data)
    }
}