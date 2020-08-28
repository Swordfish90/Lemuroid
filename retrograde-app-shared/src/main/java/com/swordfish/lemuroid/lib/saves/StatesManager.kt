package com.swordfish.lemuroid.lib.saves

import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import kotlinx.serialization.json.Json
import java.io.File

// TODO Since states are core related we should not put them in the same folder. This break previous versions states
// so I decided to manage a transition phase reading also the old directory. We should safely remove it in a few weeks.

class StatesManager(private val directoriesManager: DirectoriesManager) {

    fun getSlotSave(game: Game, system: GameSystem, index: Int): Maybe<SaveState> {
        assert(index in 0 until MAX_STATES)
        return getSaveState(getSlotSaveFileName(game, index), system.coreName)
    }

    fun setSlotSave(game: Game, saveState: SaveState, system: GameSystem, index: Int): Completable {
        assert(index in 0 until MAX_STATES)
        return setSaveState(getSlotSaveFileName(game, index), system.coreName, saveState)
    }

    fun getAutoSave(game: Game, system: GameSystem) =
            getSaveState(getAutoSaveFileName(game), system.coreName)

    fun setAutoSave(game: Game, system: GameSystem, saveState: SaveState) =
            setSaveState(getAutoSaveFileName(game), system.coreName, saveState)

    fun getSavedSlotsInfo(game: Game, coreName: String): Single<List<SaveStateInfo>> = Single.fromCallable {
        (0 until MAX_STATES)
                .map { getStateFileOrDeprecated(getSlotSaveFileName(game, it), coreName) }
                .map {
                    SaveStateInfo(
                        it.exists(), it.lastModified()
                    )
                }
                .toList()
    }

    private fun getSaveState(fileName: String, coreName: String): Maybe<SaveState> = Maybe.fromCallable {
        val saveFile = getStateFileOrDeprecated(fileName, coreName)
        val metadataFile = getMetadataStateFile(fileName, coreName)
        if (saveFile.exists()) {
            val byteArray = saveFile.readBytes()
            val stateMetadata = runCatching {
                Json.Default.decodeFromString<SaveState.Metadata>(
                    SaveState.Metadata.serializer(), metadataFile.readText()
                )
            }
            SaveState(byteArray, stateMetadata.getOrNull() ?: SaveState.Metadata())
        } else {
            null
        }
    }

    private fun setSaveState(
        fileName: String,
        coreName: String,
        saveState: SaveState
    ) = Completable.fromCallable {
        val saveFile = getStateFile(fileName, coreName)
        saveFile.writeBytes(saveState.state)

        val metadataFile = getMetadataStateFile(fileName, coreName)
        metadataFile.writeText(Json.encodeToString(
            SaveState.Metadata.serializer(), saveState.metadata)
        )
    }

    @Deprecated("Using this folder collisions might happen across different systems.")
    private fun getStateFileOrDeprecated(fileName: String, coreName: String): File {
        val stateFile = getStateFile(fileName, coreName)
        val deprecatedStateFile = getDeprecatedStateFile(fileName)
        return if (stateFile.exists() || !deprecatedStateFile.exists()) {
            stateFile
        } else {
            deprecatedStateFile
        }
    }

    private fun getStateFile(fileName: String, coreName: String): File {
        val statesDirectories = File(directoriesManager.getStatesDirectory(), coreName)
        statesDirectories.mkdirs()
        return File(statesDirectories, fileName)
    }

    private fun getMetadataStateFile(stateFileName: String, coreName: String): File {
        val statesDirectories = File(directoriesManager.getStatesDirectory(), coreName)
        statesDirectories.mkdirs()
        return File(statesDirectories, stateFileName + ".metadata")
    }

    @Deprecated("Using this folder collisions might happen across different systems.")
    private fun getDeprecatedStateFile(fileName: String): File {
        val statesDirectories = directoriesManager.getInternalStatesDirectory()
        return File(statesDirectories, fileName)
    }

    private fun getAutoSaveFileName(game: Game) = "${game.fileName}.state"
    private fun getSlotSaveFileName(game: Game, index: Int) = "${game.fileName}.slot${index + 1}"

    companion object {
        const val MAX_STATES = 4
    }
}
