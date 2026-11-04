package come.asg.lemudroid.lib.saves

import come.asg.lemudroid.common.kotlin.runCatchingWithRetry
import come.asg.lemudroid.common.kotlin.safeBasename
import come.asg.lemudroid.common.kotlin.stableGameIdentity
import come.asg.lemudroid.common.kotlin.writeBytesAtomic
import come.asg.lemudroid.lib.library.SystemCoreConfig
import come.asg.lemudroid.lib.library.db.entity.Game
import come.asg.lemudroid.lib.saves.migrators.getSavesMigrator
import come.asg.lemudroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SavesManager(private val directoriesManager: DirectoriesManager) {
    suspend fun getSaveRAM(
        game: Game,
        systemCoreConfig: SystemCoreConfig,
    ): ByteArray? {
        return withContext(Dispatchers.IO) {
            migrateLegacyFile(game)
            runCatchingWithRetry(FILE_ACCESS_RETRIES) {
                val saveFile = saveFile(game)
                if (saveFile.isFile && saveFile.length() > 0) {
                    saveFile.readBytes()
                } else {
                    systemCoreConfig.getSavesMigrator()?.loadPreviousSaveForGame(game, directoriesManager)
                }
            }.getOrThrow()
        }
    }

    suspend fun setSaveRAM(
        game: Game,
        data: ByteArray,
    ) {
        withContext(Dispatchers.IO) {
            migrateLegacyFile(game)
            runCatchingWithRetry(FILE_ACCESS_RETRIES) {
                if (data.isEmpty()) return@runCatchingWithRetry
                saveFile(game).writeBytesAtomic(data)
            }.getOrThrow()
        }
    }

    suspend fun getSaveRAMInfo(game: Game): SaveInfo {
        return withContext(Dispatchers.IO) {
            migrateLegacyFile(game)
            val file = saveFile(game)
            SaveInfo(file.isFile && file.length() > 0, file.lastModified())
        }
    }

    private fun migrateLegacyFile(game: Game) {
        val baseName = game.fileName.substringBeforeLast(".").safeBasename("game")
        val hashedBaseName = "$baseName-${stableGameIdentity(game.fileUri, game.systemId)}"
        SaveStoragePaths.migrateNewestIfMissing(
            listOf(
                File(directoriesManager.getSavesDirectory(), "$baseName.srm"),
                File(directoriesManager.getSavesDirectory(), "$hashedBaseName.srm"),
            ),
            saveFile(game),
        )
    }

    private fun saveDirectory(game: Game): File =
        SaveStoragePaths.gameDirectory(
            directoriesManager.getSavesDirectory(),
            game,
            "save-ram",
        )

    private fun saveFile(game: Game): File = File(saveDirectory(game), "save.srm")

    companion object {
        private const val FILE_ACCESS_RETRIES = 3
    }
}
