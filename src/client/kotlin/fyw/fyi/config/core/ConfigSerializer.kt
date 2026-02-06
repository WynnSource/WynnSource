package fyw.fyi.config.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import fyw.fyi.WynnSource
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Handles JSON serialization and deserialization of config pages.
 * Each config page is stored as a separate JSON file in the config/wynnsource/ directory.
 *
 * ## Behavior:
 * - **First launch (no file):** Generates a complete JSON with all default values and saves it.
 * - **Subsequent loads:** Merges the on-disk JSON with the current entry definitions:
 *   - Existing keys that still exist in code: preserved (loaded from file).
 *   - New keys added in code: filled with default values.
 *   - Stale keys removed from code: dropped from the file.
 *   - The merged result is written back to disk to keep the file up-to-date.
 */
object ConfigSerializer {
    private val logger = WynnSource.logger

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

    private val configDir by lazy {
        FabricLoader.getInstance().configDir.resolve("wynnsource").also {
            if (!it.exists()) {
                Files.createDirectories(it)
            }
        }
    }

    /**
     * Save a config page to its JSON file.
     * Writes to a temporary file first, then renames for atomicity.
     */
    @Suppress("TooGenericExceptionCaught")
    fun save(page: ConfigPage) {
        try {
            val jsonObject = buildJsonFromEntries(page)
            val file = configDir.resolve("${page.id}.json")
            val tmpFile = configDir.resolve("${page.id}.json.tmp")

            tmpFile.writeText(gson.toJson(jsonObject))
            Files.move(
                tmpFile,
                file,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE
            )
            logger.debug("Saved config page '{}' to {}", page.id, file)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            // Fallback: non-atomic write if filesystem doesn't support atomic move
            try {
                val jsonObject = buildJsonFromEntries(page)
                val file = configDir.resolve("${page.id}.json")
                file.writeText(gson.toJson(jsonObject))
                logger.debug("Saved config page '{}' to {} (non-atomic)", page.id, file)
            } catch (e: Exception) {
                logger.error("Failed to save config page '${page.id}'", e)
            }
        } catch (e: Exception) {
            logger.error("Failed to save config page '${page.id}'", e)
        }
    }

    /**
     * Load a config page from its JSON file.
     *
     * If the file does not exist, generates a complete default config and saves it.
     * If the file exists, merges it with current definitions:
     * - Loads values for keys that exist both in file and in code.
     * - Adds default values for new keys not present in the file.
     * - Removes stale keys that are in the file but no longer in code.
     * - Writes the merged result back to disk.
     */
    fun load(page: ConfigPage) {
        val file = configDir.resolve("${page.id}.json")

        if (!file.exists()) {
            // generate defaults
            logger.info("Config file for '{}' does not exist, generating defaults", page.id)
            save(page)
            return
        }

        try {
            val jsonObject = gson.fromJson(file.readText(), JsonObject::class.java)
                ?: run {
                    logger.warn("Config file for '{}' is empty or invalid, regenerating defaults", page.id)
                    save(page)
                    return
                }

            val currentKeys = page.allEntries().map { it.key }.toSet()
            val fileKeys = jsonObject.keySet().toSet()

            // Load values for entries that exist in both code and file
            var needsResave = false
            page.allEntries().forEach { entry ->
                val element = jsonObject.get(entry.key)
                if (element != null) {
                    loadValue(entry, element)
                } else {
                    // New key in code, not in file - use default (already set)
                    logger.info(
                        "New config key '{}' in page '{}', using default value",
                        entry.key, page.id
                    )
                    needsResave = true
                }
            }

            // Check for stale keys in file that no longer exist in code
            val staleKeys = fileKeys - currentKeys
            if (staleKeys.isNotEmpty()) {
                logger.info(
                    "Removing stale config keys from page '{}': {}",
                    page.id, staleKeys.joinToString(", ")
                )
                needsResave = true
            }

            // Re-save if there were changes (new or removed keys)
            if (needsResave) {
                save(page)
            }

            logger.debug("Loaded config page '{}' from {}", page.id, file)
        } catch (e: Exception) {
            logger.error("Failed to load config page '${page.id}', regenerating defaults", e)
            // On corruption, regenerate defaults
            save(page)
        }
    }

    /**
     * Build a JsonObject from all entries' committed values.
     */
    private fun buildJsonFromEntries(page: ConfigPage): JsonObject {
        val jsonObject = JsonObject()
        page.allEntries().forEach { entry ->
            val value = entry.getCommitted()
            // For enums, serialize as the name string
            if (entry.type.java.isEnum) {
                jsonObject.addProperty(entry.key, (value as Enum<*>).name)
            } else {
                jsonObject.add(entry.key, gson.toJsonTree(value))
            }
        }
        return jsonObject
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> loadValue(entry: ConfigEntry<T>, element: JsonElement) {
        try {
            val value: Any = when (entry.type) {
                Boolean::class -> element.asBoolean
                Int::class -> element.asInt
                Long::class -> element.asLong
                Float::class -> element.asFloat
                Double::class -> element.asDouble
                String::class -> element.asString
                else -> {
                    if (entry.type.java.isEnum) {
                        loadEnumValue(entry, element)
                    } else {
                        logger.warn("Unsupported config type: ${entry.type} for key '${entry.key}', using default")
                        return
                    }
                }
            }
            entry.setCommitted(value as T)
        } catch (e: Exception) {
            logger.warn("Failed to load config value for '${entry.key}', using default", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> loadEnumValue(entry: ConfigEntry<T>, element: JsonElement): T {
        val enumConstants = entry.type.java.enumConstants as Array<Enum<*>>
        val name = element.asString
        return enumConstants.find { it.name == name } as? T
            ?: run {
                logger.warn("Unknown enum value '$name' for '${entry.key}', using default")
                entry.default
            }
    }
}
