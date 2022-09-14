package com.swordfish.lemuroid.lib.saves

import android.content.Context
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Maybe
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

class SavesManager(private val directoriesManager: DirectoriesManager) {
    fun getSaveRAM(game: Game, context: Context): Maybe<ByteArray> {
        val ramFileName = getSaveRAMFileName(game)
        copyToInternal(ramFileName, context)
        val sramMaybe: Maybe<ByteArray> = Maybe.fromCallable {
            val saveFile = getSaveFile(ramFileName)
            if (saveFile.exists() && saveFile.length() > 0) {
                saveFile.readBytes()
            } else {
                null
            }
        }
        return sramMaybe.retry(FILE_ACCESS_RETRIES)
    }

    fun setSaveRAM(game: Game, data: ByteArray, context: Context): Completable {
        val ramFileName = getSaveRAMFileName(game)
        val saveCompletable = Completable.fromAction {
            if (data.isEmpty())
                return@fromAction

            val saveFile = getSaveFile(ramFileName)
            saveFile.writeBytes(data)
            copyToExternal(ramFileName, context)
        }
        return saveCompletable.retry(FILE_ACCESS_RETRIES)
    }

    fun getSaveRAMInfo(game: Game): SaveInfo {
        val saveFile = getSaveFile(getSaveRAMFileName(game))
        val fileExists = saveFile.exists() && saveFile.length() > 0
        return SaveInfo(fileExists, saveFile.lastModified())
    }

    private fun getSaveFile(fileName: String): File {
        val savesDirectory = directoriesManager.getInternalSavesDirectroy()
        return File(savesDirectory, fileName)
    }

    /** This name should make it compatible with RetroArch so that users can freely sync saves across the two application. */
    private fun getSaveRAMFileName(game: Game) = "${game.fileName.substringBeforeLast(".")}.srm"

    companion object {
        private const val FILE_ACCESS_RETRIES = 3L
    }

    private fun copyToInternal(filename: String, context: Context) {
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

    private fun copyToExternal(filename: String, context: Context) {
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
