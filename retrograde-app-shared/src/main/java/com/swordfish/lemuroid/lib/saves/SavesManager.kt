package com.swordfish.lemuroid.lib.saves

import com.swordfish.lemuroid.common.kotlin.runCatchingWithRetry
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.migrators.getSavesMigrator
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SavesManager(private val directoriesManager: DirectoriesManager) {
    suspend fun getSaveRAM(
        game: Game,
        systemCoreConfig: SystemCoreConfig,
    ): ByteArray? {
        return withContext(Dispatchers.IO) {
            val result =
                runCatchingWithRetry(FILE_ACCESS_RETRIES) {
                    val saveFile = getSaveFile(getSaveRAMFileName(game))
                    if (saveFile.exists() && saveFile.length() > 0) {
                        saveFile.readBytes()
                    } else {
                        val savesMigrator = systemCoreConfig.getSavesMigrator()
                        savesMigrator?.loadPreviousSaveForGame(game, directoriesManager)
                    }
                }
            result.getOrNull()
        }
    }

    suspend fun setSaveRAM(
        game: Game,
        data: ByteArray,
    ) {
        withContext(Dispatchers.IO) {
            val result =
                runCatchingWithRetry(FILE_ACCESS_RETRIES) {
                    if (data.isEmpty()) {
                        return@runCatchingWithRetry
                    }

                    val saveFile = getSaveFile(getSaveRAMFileName(game))
                    saveFile.writeBytes(data)
                }
            result.getOrNull()
        }
    }

    suspend fun getSaveRAMInfo(game: Game): SaveInfo {
        return withContext(Dispatchers.IO) {
            val saveFile = getSaveFile(getSaveRAMFileName(game))
            val fileExists = saveFile.exists() && saveFile.length() > 0
            SaveInfo(fileExists, saveFile.lastModified())
        }
    }

    private suspend fun getSaveFile(fileName: String): File {
        return withContext(Dispatchers.IO) {
            val savesDirectory = directoriesManager.getSavesDirectory()
            File(savesDirectory, fileName)
        }
    }

    /** This name should make it compatible with RetroArch so that users can freely sync saves across the two application. */
    private fun getSaveRAMFileName(game: Game) = "${game.fileName.substringBeforeLast(".")}.srm"

    companion object {
        private const val FILE_ACCESS_RETRIES = 3
    }
}
