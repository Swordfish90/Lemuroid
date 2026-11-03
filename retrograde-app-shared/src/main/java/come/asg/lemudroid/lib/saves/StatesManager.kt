package come.asg.lemudroid.lib.saves

import come.asg.lemudroid.common.kotlin.readBytesUncompressedAtomic
import come.asg.lemudroid.common.kotlin.readTextAtomic
import come.asg.lemudroid.common.kotlin.runCatchingWithRetry
import come.asg.lemudroid.common.kotlin.safeBasename
import come.asg.lemudroid.common.kotlin.stableGameIdentity
import come.asg.lemudroid.common.kotlin.writeBytesCompressedAtomic
import come.asg.lemudroid.common.kotlin.writeTextAtomic
import come.asg.lemudroid.lib.library.CoreID
import come.asg.lemudroid.lib.library.db.entity.Game
import come.asg.lemudroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class StatesManager(private val directoriesManager: DirectoriesManager) {
    suspend fun getSlotSave(
        game: Game,
        coreID: CoreID,
        index: Int,
    ): SaveState? =
        withContext(Dispatchers.IO) {
            require(index in 0 until MAX_STATES) { "State slot index must be in 0 until $MAX_STATES" }
            getSaveState(
                getSlotSaveFileName(game, index),
                coreID.coreName,
                getHashedSlotSaveFileName(game, coreID, index),
            )
        }

    suspend fun setSlotSave(
        game: Game,
        saveState: SaveState,
        coreID: CoreID,
        index: Int,
    ) = withContext(Dispatchers.IO) {
        require(index in 0 until MAX_STATES) { "State slot index must be in 0 until $MAX_STATES" }
        setSaveState(getSlotSaveFileName(game, index), coreID.coreName, saveState)
    }

    suspend fun getAutoSaveInfo(
        game: Game,
        coreID: CoreID,
    ): SaveInfo =
        withContext(Dispatchers.IO) {
            val legacyFile = getStateFile(getAutoSaveFileName(game), coreID.coreName)
            val modernFile = getStateFile(getHashedAutoSaveFileName(game, coreID), coreID.coreName)
            val autoSaveFile =
                listOf(legacyFile, modernFile)
                    .filter { it.isFile && it.length() > 0 }
                    .maxByOrNull(File::lastModified)
                    ?: if (legacyFile.exists()) legacyFile else modernFile
            val autoSaveHasData = autoSaveFile.length() > 0
            SaveInfo(autoSaveFile.exists() && autoSaveHasData, autoSaveFile.lastModified())
        }

    suspend fun getAutoSave(
        game: Game,
        coreID: CoreID,
    ) = withContext(Dispatchers.IO) {
        getSaveState(getAutoSaveFileName(game), coreID.coreName, getHashedAutoSaveFileName(game, coreID))
    }

    suspend fun setAutoSave(
        game: Game,
        coreID: CoreID,
        saveState: SaveState,
    ) = withContext(Dispatchers.IO) {
        setSaveState(getAutoSaveFileName(game), coreID.coreName, saveState)
    }

    suspend fun getSavedSlotsInfo(
        game: Game,
        coreID: CoreID,
    ): List<SaveInfo> =
        withContext(Dispatchers.IO) {
            (0 until MAX_STATES)
                .map { index ->
                    val legacy = getStateFile(getSlotSaveFileName(game, index), coreID.coreName)
                    val modern = getStateFile(getHashedSlotSaveFileName(game, coreID, index), coreID.coreName)
                    val file =
                        listOf(legacy, modern)
                            .filter { it.isFile && it.length() > 0 }
                            .maxByOrNull(File::lastModified)
                            ?: if (legacy.exists()) legacy else modern
                    SaveInfo(file.exists(), file.lastModified())
                }
                .toList()
        }

    private suspend fun getSaveState(
        fileName: String,
        coreName: String,
        legacyFileName: String? = null,
    ): SaveState? {
        return runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            val currentSaveFile = getStateFile(fileName, coreName)
            val legacySaveFile = legacyFileName?.let { getStateFile(it, coreName) }
            val selectedFile =
                listOfNotNull(currentSaveFile, legacySaveFile)
                    .filter { it.isFile && it.length() > 0 }
                    .maxByOrNull(File::lastModified)
                    ?: if (currentSaveFile.exists()) {
                        currentSaveFile
                    } else {
                        legacySaveFile
                            ?: currentSaveFile
                    }
            val effectiveFileName = if (selectedFile == currentSaveFile) fileName else legacyFileName ?: fileName
            val saveFile = selectedFile
            val metadataFile = getMetadataStateFile(effectiveFileName, coreName)
            if (saveFile.exists()) {
                val byteArray = saveFile.readBytesUncompressedAtomic()
                val stateMetadata =
                    runCatching {
                        Json.Default.decodeFromString(
                            SaveState.Metadata.serializer(),
                            metadataFile.readTextAtomic(),
                        )
                    }
                SaveState(byteArray, stateMetadata.getOrNull() ?: SaveState.Metadata())
            } else {
                null
            }
        }.getOrThrow()
    }

    private suspend fun setSaveState(
        fileName: String,
        coreName: String,
        saveState: SaveState,
    ) {
        runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            writeStateToDisk(fileName, coreName, saveState.state)
            writeMetadataToDisk(fileName, coreName, saveState.metadata)
        }.getOrThrow()
    }

    private fun writeMetadataToDisk(
        fileName: String,
        coreName: String,
        metadata: SaveState.Metadata,
    ) {
        val metadataFile = getMetadataStateFile(fileName, coreName)
        metadataFile.writeTextAtomic(Json.encodeToString(SaveState.Metadata.serializer(), metadata))
    }

    private fun writeStateToDisk(
        fileName: String,
        coreName: String,
        stateArray: ByteArray,
    ) {
        val saveFile = getStateFile(fileName, coreName)
        saveFile.writeBytesCompressedAtomic(stateArray)
    }

    private fun getStateFile(
        fileName: String,
        coreName: String,
    ): File {
        val statesDirectories = File(directoriesManager.getStatesDirectory(), coreName.safeBasename())
        statesDirectories.mkdirs()
        return File(statesDirectories, fileName.safeBasename("state"))
    }

    private fun getMetadataStateFile(
        stateFileName: String,
        coreName: String,
    ): File {
        val statesDirectories = File(directoriesManager.getStatesDirectory(), coreName.safeBasename())
        statesDirectories.mkdirs()
        return File(statesDirectories, "${stateFileName.safeBasename("state")}.metadata")
    }

    private fun getAutoSaveFileName(game: Game) = "${game.fileName.safeBasename()}.state"

    private fun getHashedAutoSaveFileName(
        game: Game,
        coreID: CoreID,
    ) = "${game.fileName.safeBasename()}-${stableGameIdentity(
        game.fileUri,
        game.systemId,
        coreID.coreName,
    )}.state"

    private fun getSlotSaveFileName(
        game: Game,
        index: Int,
    ) = "${game.fileName.safeBasename()}.slot${index + 1}"

    private fun getHashedSlotSaveFileName(
        game: Game,
        coreID: CoreID,
        index: Int,
    ) = "${game.fileName.safeBasename()}-${stableGameIdentity(
        game.fileUri,
        game.systemId,
        coreID.coreName,
    )}.slot${index + 1}"

    companion object {
        const val MAX_STATES = 4
        private const val FILE_ACCESS_RETRIES = 3
    }
}
