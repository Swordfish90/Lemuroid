package com.swordfish.lemuroid.lib.saves

import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.io.File

class SavesManager(private val directoriesManager: DirectoriesManager) {

    fun getSaveRAM(game: Game): Maybe<ByteArray> = Maybe.fromCallable {
        val saveFile = getSaveFile(getSaveRAMFileName(game))
        if (saveFile.exists()) {
            saveFile.readBytes()
        } else {
            null
        }
    }

    fun setSaveRAM(game: Game, data: ByteArray): Completable = Completable.fromCallable {
        val saveFile = getSaveFile(getSaveRAMFileName(game))
        saveFile.writeBytes(data)
    }

    fun getSlotSave(game: Game, index: Int): Maybe<ByteArray> {
        assert(index in 0 until MAX_STATES)
        return getSaveState(getSlotSaveFileName(game, index))
    }

    fun setSlotSave(game: Game, data: ByteArray, index: Int): Completable {
        assert(index in 0 until MAX_STATES)
        return setSaveState(getSlotSaveFileName(game, index), data)
    }

    fun getAutoSave(game: Game) = getSaveState(getAutoSaveFileName(game))
    fun setAutoSave(game: Game, data: ByteArray) = setSaveState(getAutoSaveFileName(game), data)

    fun getSavedSlotsInfo(game: Game): Single<List<SaveInfos>> = Single.fromCallable {
        (0 until MAX_STATES).map { getStateFile(getSlotSaveFileName(game, it)) }
                .map { SaveInfos(it.exists(), it.lastModified()) }
                .toList()
    }

    private fun getSaveState(fileName: String): Maybe<ByteArray> = Maybe.fromCallable {
        val saveFile = getStateFile(fileName)
        if (saveFile.exists()) {
            saveFile.readBytes()
        } else {
            null
        }
    }

    private fun setSaveState(fileName: String, data: ByteArray) = Completable.fromCallable {
        val saveFile = getStateFile(fileName)
        saveFile.writeBytes(data)
    }

    private fun getSaveFile(fileName: String): File {
        val savesDirectory = directoriesManager.getSavesDirectory()
        return File(savesDirectory, fileName)
    }

    private fun getStateFile(fileName: String): File {
        val statesDirectories = directoriesManager.getStatesDirectory()
        return File(statesDirectories, fileName)
    }

    /** This name should make it compatible with RetroArch so that users can freely sync saves across the two application. */
    private fun getSaveRAMFileName(game: Game) = "${game.fileName.substringBeforeLast(".")}.srm"
    private fun getAutoSaveFileName(game: Game) = "${game.fileName}.state"
    private fun getSlotSaveFileName(game: Game, index: Int) = "${game.fileName}.slot${index + 1}"

    data class SaveInfos(val exists: Boolean, val date: Long)

    companion object {
        const val MAX_STATES = 4
    }
}
