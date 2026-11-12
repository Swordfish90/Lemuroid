package come.asg.ominoy.lib.saves

import come.asg.ominoy.common.kotlin.safeBasename
import come.asg.ominoy.common.kotlin.stableGameIdentity
import come.asg.ominoy.lib.library.db.entity.Game
import java.io.File

/** Shared filesystem layout for per-ROM saves, states, and previews. */
internal object SaveStoragePaths {
    fun gameFolderName(
        game: Game,
        discriminator: String,
    ): String {
        val baseName = game.fileName.substringBeforeLast(".").safeBasename("game")
        val identity = stableGameIdentity(game.fileUri, game.systemId, discriminator)
        return "${baseName}__${identity}"
    }

    fun gameDirectory(
        root: File,
        game: Game,
        discriminator: String,
    ): File = File(root, gameFolderName(game, discriminator)).apply { mkdirs() }

    /**
     * Copies the newest existing legacy candidate into [destination] if the new file does not exist.
     * The old flat file is intentionally retained so rollback and older app versions remain safe.
     */
    fun migrateNewestIfMissing(
        candidates: List<File>,
        destination: File,
    ) {
        if (destination.exists()) return
        val source = candidates.filter { it.isFile && it.length() > 0 }.maxByOrNull(File::lastModified) ?: return
        destination.parentFile?.mkdirs()
        val temporary = File(destination.parentFile, ".${destination.name}.migration")
        runCatching {
            source.copyTo(temporary, overwrite = true)
            if (!destination.exists() && !temporary.renameTo(destination)) {
                source.copyTo(destination, overwrite = false)
                temporary.delete()
            }
        }.onFailure {
            temporary.delete()
            throw it
        }
    }
}
