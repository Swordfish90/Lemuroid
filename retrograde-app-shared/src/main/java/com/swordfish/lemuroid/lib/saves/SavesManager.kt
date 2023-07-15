package com.swordfish.lemuroid.lib.saves

import com.swordfish.lemuroid.common.kotlin.runCatchingWithRetry
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavesManager(private val directoriesManager: DirectoriesManager) {

    suspend fun getSaveRAM(game: Game): ByteArray? = withContext(Dispatchers.IO) {
        val result = runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            val saveFile = getSaveFile(game.systemId, getSaveRAMFileName(game))
            if (saveFile.exists() && saveFile.length() > 0) {
                saveFile.readBytes()
            } else {
                null
            }
        }
        result.getOrNull()
    }

    suspend fun setSaveRAM(game: Game, data: ByteArray): Unit = withContext(Dispatchers.IO) {
        val result = runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            if (data.isEmpty())
                return@runCatchingWithRetry

            val saveFile = getSystemSaveFile(game.systemId, getSaveRAMFileName(game))
            saveFile.writeBytes(data)

            //clean up the legacy file. But only if the byteArrays match!
            if(saveFile.readBytes().contentEquals(data)) {
                val legacySaveFile = getLegacySaveFile(getSaveRAMFileName(game))
                if(legacySaveFile.exists()) {
                    legacySaveFile.delete()
                }
            }
        }
        result.getOrNull()
    }

    suspend fun getSaveRAMInfo(game: Game): SaveInfo = withContext(Dispatchers.IO) {
        val saveFile = getSaveFile(game.systemId, getSaveRAMFileName(game))
        val fileExists = saveFile.exists() && saveFile.length() > 0
        SaveInfo(fileExists, saveFile.lastModified())
    }

    private suspend fun getLegacySaveFile(fileName: String): File = withContext(Dispatchers.IO) {
        val savesDirectory = directoriesManager.getSavesDirectory()
        File(savesDirectory, fileName)
    }

    private suspend fun getSystemSaveFile(system: String, fileName: String): File = withContext(Dispatchers.IO) {
        val savesDirectory = directoriesManager.getSavesDirectory()
        val systemDir = File(savesDirectory, system)

        if(!systemDir.exists()) {
            systemDir.mkdir()
        }
        File(systemDir, fileName)
    }
    private suspend fun getSaveFile(system: String, fileName: String): File = withContext(Dispatchers.IO) {
        val save = getSystemSaveFile(system, fileName)
        if(save.exists()){
            return@withContext save
        }
        getLegacySaveFile(fileName)
    }

    /** This name should make it compatible with RetroArch so that users can freely sync saves across the two application. */
    private fun getSaveRAMFileName(game: Game) = "${game.fileName.substringBeforeLast(".")}.srm"

    companion object {
        private const val FILE_ACCESS_RETRIES = 3
    }
}
