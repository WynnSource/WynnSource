package fyw.fyi.wynnsource.utils

import fyw.fyi.wynnsource.WynnSource.logger
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.writeText

object FileUtils {

    fun safeSave(content: String, dir: Path, fileName: String) {
        try {
            val file = dir.resolve("$fileName.json")
            val tmpFile = dir.resolve("$fileName.json.tmp")

            tmpFile.writeText(content)
            Files.move(
                tmpFile,
                file,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
            logger.debug("Saved JSON to {}", file)
        } catch (_: AtomicMoveNotSupportedException) {
            // Fallback: non-atomic write if filesystem doesn't support atomic move
            try {
                val file = dir.resolve("$fileName.json")
                file.writeText(content)
            } catch (e: Exception) {
                logger.error("Could not write file {}", dir.resolve("$fileName.json"), e)
            }
        } catch (e: Exception) {
            logger.error("Could not write file {}", dir.resolve("$fileName.json"), e)
        }
    }
}
