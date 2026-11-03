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
            runCatchingWithRetry(FILE_ACCESS_RETRIES) {
                val legacyFile = getSaveFile(getSaveRAMFileName(game))
                val modernFile = getSaveFile(getHashedSaveRAMFileName(game))
                val saveFile =
                    listOf(legacyFile, modernFile)
                        .filter { it.isFile && it.length() > 0 }
                        .maxByOrNull(File::lastModified)
                if (saveFile != null) {
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
            runCatchingWithRetry(FILE_ACCESS_RETRIES) {
                if (data.isEmpty()) return@runCatchingWithRetry
                getSaveFile(getSaveRAMFileName(game)).writeBytesAtomic(data)
            }.getOrThrow()
        }
    }

    suspend fun getSaveRAMInfo(game: Game): SaveInfo {
        return withContext(Dispatchers.IO) {
            val candidates =
                listOf(getSaveRAMFileName(game), getHashedSaveRAMFileName(game))
                    .map { getSaveFile(it) }
            val existing = candidates.filter { it.isFile && it.length() > 0 }.maxByOrNull(File::lastModified)
            SaveInfo(existing != null, existing?.lastModified() ?: 0L)
        }
    }

    private suspend fun getSaveFile(fileName: String): File {
        return withContext(Dispatchers.IO) {
            File(directoriesManager.getSavesDirectory(), fileName)
        }
    }

    /** Prefer the historical basename for RetroArch and move-stable compatibility. */
    private fun getSaveRAMFileName(game: Game): String = "${game.fileName.substringBeforeLast(".").safeBasename()}.srm"

    /** Newer URI-keyed names remain readable for installations created by newer builds. */
    private fun getHashedSaveRAMFileName(game: Game): String {
        val baseName = game.fileName.substringBeforeLast(".").safeBasename()
        return "$baseName-${stableGameIdentity(game.fileUri, game.systemId)}.srm"
    }

    companion object {
        private const val FILE_ACCESS_RETRIES = 3
    }
}
