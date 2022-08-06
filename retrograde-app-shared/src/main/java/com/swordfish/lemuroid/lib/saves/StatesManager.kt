package com.swordfish.lemuroid.lib.saves

import com.swordfish.lemuroid.common.kotlin.readBytesUncompressed
import com.swordfish.lemuroid.common.kotlin.runCatchingWithRetry
import com.swordfish.lemuroid.common.kotlin.writeBytesCompressed
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

// TODO Since states are core related we should not put them in the same folder. This break previous versions states
// so I decided to manage a transition phase reading also the old directory. We should safely remove it in a few weeks.

// TODO FILIPPO... Check that every file write is wrapped by Dispatchers.IO
class StatesManager(private val directoriesManager: DirectoriesManager) {

    suspend fun getSlotSave(game: Game, coreID: CoreID, index: Int): SaveState? {
        assert(index in 0 until MAX_STATES)
        return getSaveState(getSlotSaveFileName(game, index), coreID.coreName)
    }

    suspend fun setSlotSave(game: Game, saveState: SaveState, coreID: CoreID, index: Int) {
        assert(index in 0 until MAX_STATES)
        return setSaveState(getSlotSaveFileName(game, index), coreID.coreName, saveState)
    }

    suspend fun getAutoSaveInfo(game: Game, coreID: CoreID): SaveInfo {
        val autoSaveFile = getStateFile(getAutoSaveFileName(game), coreID.coreName)
        val autoSaveHasData = autoSaveFile.length() > 0
        return SaveInfo(autoSaveFile.exists() && autoSaveHasData, autoSaveFile.lastModified())
    }

    suspend fun getAutoSave(game: Game, coreID: CoreID) =
        getSaveState(getAutoSaveFileName(game), coreID.coreName)

    suspend fun setAutoSave(game: Game, coreID: CoreID, saveState: SaveState) =
        setSaveState(getAutoSaveFileName(game), coreID.coreName, saveState)

    suspend fun getSavedSlotsInfo(
        game: Game,
        coreID: CoreID
    ): List<SaveInfo> {
        return (0 until MAX_STATES)
            .map { getStateFileOrDeprecated(getSlotSaveFileName(game, it), coreID.coreName) }
            .map { SaveInfo(it.exists(), it.lastModified()) }
            .toList()
    }

    private suspend fun getSaveState(
        fileName: String,
        coreName: String
    ): SaveState? = withContext(Dispatchers.IO) {
        runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            val saveFile = getStateFileOrDeprecated(fileName, coreName)
            val metadataFile = getMetadataStateFile(fileName, coreName)
            if (saveFile.exists()) {
                val byteArray = saveFile.readBytesUncompressed()
                val stateMetadata = runCatching {
                    Json.Default.decodeFromString(
                        SaveState.Metadata.serializer(),
                        metadataFile.readText()
                    )
                }
                SaveState(byteArray, stateMetadata.getOrNull() ?: SaveState.Metadata())
            } else {
                null
            }
        }.getOrNull()
    }

    private suspend fun setSaveState(
        fileName: String,
        coreName: String,
        saveState: SaveState
    ) {
        runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            writeStateToDisk(fileName, coreName, saveState.state)
            writeMetadataToDisk(fileName, coreName, saveState.metadata)
        }
    }

    private suspend fun writeMetadataToDisk(
        fileName: String,
        coreName: String,
        metadata: SaveState.Metadata
    ) = withContext(Dispatchers.IO) {
        val metadataFile = getMetadataStateFile(fileName, coreName)
        metadataFile.writeText(Json.encodeToString(SaveState.Metadata.serializer(), metadata))
    }

    private suspend fun writeStateToDisk(
        fileName: String,
        coreName: String,
        stateArray: ByteArray
    ) = withContext(Dispatchers.IO) {
        val saveFile = getStateFile(fileName, coreName)
        saveFile.writeBytesCompressed(stateArray)
    }

    @Deprecated("Using this folder collisions might happen across different systems.")
    private suspend fun getStateFileOrDeprecated(fileName: String, coreName: String): File {
        val stateFile = getStateFile(fileName, coreName)
        val deprecatedStateFile = getDeprecatedStateFile(fileName)
        return if (stateFile.exists() || !deprecatedStateFile.exists()) {
            stateFile
        } else {
            deprecatedStateFile
        }
    }

    private suspend fun getStateFile(fileName: String, coreName: String): File = withContext(Dispatchers.IO) {
        val statesDirectories = File(directoriesManager.getStatesDirectory(), coreName)
        statesDirectories.mkdirs()
        File(statesDirectories, fileName)
    }

    private suspend fun getMetadataStateFile(
        stateFileName: String,
        coreName: String
    ) = withContext(Dispatchers.IO) {
        val statesDirectories = File(directoriesManager.getStatesDirectory(), coreName)
        statesDirectories.mkdirs()
        File(statesDirectories, "$stateFileName.metadata")
    }

    @Deprecated("Using this folder collisions might happen across different systems.")
    private suspend fun getDeprecatedStateFile(fileName: String): File = withContext(Dispatchers.IO) {
        val statesDirectories = directoriesManager.getInternalStatesDirectory()
        File(statesDirectories, fileName)
    }

    private fun getAutoSaveFileName(game: Game) = "${game.fileName}.state"
    private fun getSlotSaveFileName(game: Game, index: Int) = "${game.fileName}.slot${index + 1}"

    companion object {
        const val MAX_STATES = 4
        private const val FILE_ACCESS_RETRIES = 3
    }
}
