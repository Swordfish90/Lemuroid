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
            migrateLegacyFiles(game, coreID)
            readSaveState(stateFile(game, coreID, index), metadataFile(game, coreID, index))
        }

    suspend fun setSlotSave(
        game: Game,
        saveState: SaveState,
        coreID: CoreID,
        index: Int,
    ) = withContext(Dispatchers.IO) {
        require(index in 0 until MAX_STATES) { "State slot index must be in 0 until $MAX_STATES" }
        migrateLegacyFiles(game, coreID)
        writeSaveState(stateFile(game, coreID, index), metadataFile(game, coreID, index), saveState)
    }

    suspend fun getAutoSaveInfo(
        game: Game,
        coreID: CoreID,
    ): SaveInfo =
        withContext(Dispatchers.IO) {
            migrateLegacyFiles(game, coreID)
            val file = stateFile(game, coreID, AUTO_SLOT)
            SaveInfo(file.isFile && file.length() > 0, file.lastModified())
        }

    suspend fun getAutoSave(
        game: Game,
        coreID: CoreID,
    ): SaveState? =
        withContext(Dispatchers.IO) {
            migrateLegacyFiles(game, coreID)
            readSaveState(stateFile(game, coreID, AUTO_SLOT), metadataFile(game, coreID, AUTO_SLOT))
        }

    suspend fun setAutoSave(
        game: Game,
        coreID: CoreID,
        saveState: SaveState,
    ) = withContext(Dispatchers.IO) {
        migrateLegacyFiles(game, coreID)
        writeSaveState(stateFile(game, coreID, AUTO_SLOT), metadataFile(game, coreID, AUTO_SLOT), saveState)
    }

    suspend fun getSavedSlotsInfo(
        game: Game,
        coreID: CoreID,
    ): List<SaveInfo> =
        withContext(Dispatchers.IO) {
            migrateLegacyFiles(game, coreID)
            (0 until MAX_STATES).map { index ->
                val file = stateFile(game, coreID, index)
                SaveInfo(file.isFile && file.length() > 0, file.lastModified())
            }
        }

    private suspend fun readSaveState(stateFile: File, metadataFile: File): SaveState? {
        return runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            if (!stateFile.isFile || stateFile.length() <= 0) return@runCatchingWithRetry null
            val metadata = runCatching {
                Json.Default.decodeFromString(
                    SaveState.Metadata.serializer(),
                    metadataFile.readTextAtomic(),
                )
            }.getOrNull() ?: SaveState.Metadata()
            SaveState(stateFile.readBytesUncompressedAtomic(), metadata)
        }.getOrThrow()
    }

    private suspend fun writeSaveState(
        stateFile: File,
        metadataFile: File,
        saveState: SaveState,
    ) {
        runCatchingWithRetry(FILE_ACCESS_RETRIES) {
            stateFile.parentFile?.mkdirs()
            stateFile.writeBytesCompressedAtomic(saveState.state)
            metadataFile.writeTextAtomic(Json.encodeToString(SaveState.Metadata.serializer(), saveState.metadata))
        }.getOrThrow()
    }

    private fun migrateLegacyFiles(game: Game, coreID: CoreID) {
        val legacyDirectory = legacyCoreDirectory(coreID.coreName)
        val directory = gameStateDirectory(game, coreID)
        val baseName = game.fileName.safeBasename()
        val hashedBaseName = "${baseName}-${stableGameIdentity(game.fileUri, game.systemId, coreID.coreName)}"

        migratePair(
            listOf("$baseName.state", "$hashedBaseName.state"),
            File(directory, "autosave.state"),
            legacyDirectory,
        )
        migratePair(
            listOf("$baseName.state.metadata", "$hashedBaseName.state.metadata"),
            File(directory, "autosave.state.metadata"),
            legacyDirectory,
        )
        (0 until MAX_STATES).forEach { index ->
            val slot = index + 1
            migratePair(
                listOf("$baseName.slot$slot", "$hashedBaseName.slot$slot"),
                File(directory, "slot$slot.state"),
                legacyDirectory,
            )
            migratePair(
                listOf("$baseName.slot$slot.metadata", "$hashedBaseName.slot$slot.metadata"),
                File(directory, "slot$slot.state.metadata"),
                legacyDirectory,
            )
        }
    }

    private fun migratePair(
        legacyNames: List<String>,
        destination: File,
        legacyDirectory: File,
    ) {
        SaveStoragePaths.migrateNewestIfMissing(legacyNames.map { File(legacyDirectory, it) }, destination)
    }

    private fun legacyCoreDirectory(coreName: String): File =
        File(directoriesManager.getStatesDirectory(), coreName.safeBasename()).apply { mkdirs() }

    private fun gameStateDirectory(game: Game, coreID: CoreID): File =
        SaveStoragePaths.gameDirectory(
            legacyCoreDirectory(coreID.coreName),
            game,
            "state:${coreID.coreName}",
        )

    private fun stateFile(game: Game, coreID: CoreID, index: Int): File =
        File(gameStateDirectory(game, coreID), if (index == AUTO_SLOT) "autosave.state" else "slot${index + 1}.state")

    private fun metadataFile(game: Game, coreID: CoreID, index: Int): File =
        File(gameStateDirectory(game, coreID), if (index == AUTO_SLOT) "autosave.state.metadata" else "slot${index + 1}.state.metadata")

    companion object {
        const val MAX_STATES = 4
        private const val AUTO_SLOT = -1
        private const val FILE_ACCESS_RETRIES = 3
    }
}
