package fyw.fyi.wynnsource.data.repository

import fyw.fyi.wynnsource.utils.FileUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path

object CacheStore {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val cacheDir: Path by lazy {
        FabricLoader.getInstance().gameDir.resolve("wynnsource").resolve("cache").also {
            if (!Files.exists(it)) Files.createDirectories(it)
        }
    }

    fun <T> save(id: String, entry: CacheEntry<T>, serializer: KSerializer<CacheEntry<T>>) {
        FileUtils.safeSave(json.encodeToString(serializer, entry), cacheDir, id)
    }

    fun <T> load(id: String, serializer: KSerializer<CacheEntry<T>>): CacheEntry<T>? {
        val file = cacheDir.resolve("$id.json")
        if (!Files.exists(file)) return null
        return try {
            json.decodeFromString(serializer, file.toFile().readText())
        } catch (_: Exception) {
            null
        }
    }

    fun delete(id: String) {
        val file = cacheDir.resolve("$id.json")
        Files.deleteIfExists(file)
    }
}
