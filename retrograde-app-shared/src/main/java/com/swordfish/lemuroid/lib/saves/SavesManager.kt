package com.swordfish.lemuroid.lib.saves

import android.content.Context
import com.swordfish.lemuroid.common.kotlin.runCatchingWithRetry
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream

class SavesManager(private val directoriesManager: DirectoriesManager) {

    suspend fun getSaveRAM(game: Game, context: Context): ByteArray? = withContext(Dispatchers.IO) {
        val result = runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            val ramFileName = getSaveRAMFileName(game)
            copyToInternal(ramFileName, context)
            val saveFile = getSaveFile(ramFileName)
            if (saveFile.exists() && saveFile.length() > 0) {
                saveFile.readBytes()
            } else {
                null
            }
        }
        result.getOrNull()
    }

    suspend fun setSaveRAM(game: Game, data: ByteArray, context: Context): Unit = withContext(Dispatchers.IO) {
        val ramFileName = getSaveRAMFileName(game)
        val result = runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            if (data.isEmpty())
                return@runCatchingWithRetry

            val saveFile = getSaveFile(ramFileName)
            saveFile.writeBytes(data)
            copyToExternal(ramFileName, context)
        }
        result.getOrNull()
    }

    suspend fun getSaveRAMInfo(game: Game): SaveInfo = withContext(Dispatchers.IO) {
        val saveFile = getSaveFile(getSaveRAMFileName(game))
        val fileExists = saveFile.exists() && saveFile.length() > 0
        SaveInfo(fileExists, saveFile.lastModified())
    }

    private suspend fun getSaveFile(fileName: String): File = withContext(Dispatchers.IO) {
        val savesDirectory = directoriesManager.getInternalSavesDirectroy()
        File(savesDirectory, fileName)
    }

    /** This name should make it compatible with RetroArch so that users can freely sync saves across the two application. */
    private fun getSaveRAMFileName(game: Game) = "${game.fileName.substringBeforeLast(".")}.srm"

    companion object {
        private const val FILE_ACCESS_RETRIES = 3
    }

    private suspend fun copyToInternal(filename: String, context: Context) {
        Timber.i("Sync Savegamedata from external storage if required.")
        val internalSaveFile = getSaveFile(filename)
        directoriesManager.getSavesDirectory().let {
            val test = it?.findFile(filename)
            if(test != null) {
                // File in external storage is newer, so copy it to local
                if(internalSaveFile.lastModified() < test.lastModified()) {
                    val inputStream = context.contentResolver.openInputStream(test.uri)

                    val byteBuffer = ByteArrayOutputStream()
                    val bufferSize = test.length()

                    // toInt is valid, since saveFile.readBytes() has an internal limitation of 2Gb anyway
                    val buffer = ByteArray(bufferSize.toInt())
                    var len = 0
                    while (inputStream!!.read(buffer).also { len = it } != -1) {
                        byteBuffer.write(buffer, 0, len)
                    }

                    if(!internalSaveFile.exists()){
                        internalSaveFile.createNewFile()
                    }
                    internalSaveFile.writeBytes(buffer)
                }
            }
        }
    }

    private suspend fun copyToExternal(filename: String, context: Context) {
        Timber.i("Sync Savegamedata from internal storage if required.")
        val internalSaveFile = getSaveFile(filename)

        directoriesManager.getSavesDirectory().let {
            val test = it?.findFile(filename)
            if(test != null) {
                // File in external storage is newer, so copy it to local
                if(internalSaveFile.lastModified() > test.lastModified()) {
                    val outputStream = context.contentResolver.openOutputStream(test.uri)
                    outputStream?.write(internalSaveFile.readBytes())
                }
            }
        }
    }
}
