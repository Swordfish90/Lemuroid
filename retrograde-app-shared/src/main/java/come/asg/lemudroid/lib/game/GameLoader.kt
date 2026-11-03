/*
 * GameLoader.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package come.asg.lemudroid.lib.game

import android.content.Context
import android.os.Build
import come.asg.lemudroid.lib.bios.BiosManager
import come.asg.lemudroid.lib.core.CoreUpdater
import come.asg.lemudroid.lib.core.CoreVariable
import come.asg.lemudroid.lib.core.CoreVariablesManager
import come.asg.lemudroid.lib.library.CoreID
import come.asg.lemudroid.lib.library.GameSystem
import come.asg.lemudroid.lib.library.LemuroidLibrary
import come.asg.lemudroid.lib.library.SystemCoreConfig
import come.asg.lemudroid.lib.library.db.RetrogradeDatabase
import come.asg.lemudroid.lib.library.db.entity.Game
import come.asg.lemudroid.lib.migration.DesmumeMigrationHandler
import come.asg.lemudroid.lib.saves.SaveState
import come.asg.lemudroid.lib.saves.SavesCoherencyEngine
import come.asg.lemudroid.lib.saves.SavesManager
import come.asg.lemudroid.lib.saves.StatesManager
import come.asg.lemudroid.lib.storage.DirectoriesManager
import come.asg.lemudroid.lib.storage.RomFiles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File

class GameLoader(
    private val lemuroidLibrary: LemuroidLibrary,
    private val statesManager: StatesManager,
    private val savesManager: SavesManager,
    private val coreVariablesManager: CoreVariablesManager,
    private val coreUpdater: CoreUpdater,
    private val retrogradeDatabase: RetrogradeDatabase,
    private val savesCoherencyEngine: SavesCoherencyEngine,
    private val directoriesManager: DirectoriesManager,
    private val biosManager: BiosManager,
    private val desmumeMigrationHandler: DesmumeMigrationHandler,
) {
    sealed class LoadingState {
        object LoadingCore : LoadingState()

        object LoadingGame : LoadingState()

        class Ready(val gameData: GameData) : LoadingState()
    }

    fun load(
        appContext: Context,
        game: Game,
        loadSave: Boolean,
        systemCoreConfig: SystemCoreConfig,
        directLoad: Boolean,
    ): Flow<LoadingState> =
        flow {
            try {
                emit(LoadingState.LoadingCore)

                val system =
                    GameSystem.findByIdOrNull(game.systemId)
                        ?: throw GameLoaderException(GameLoaderError.Generic)

                if (!isArchitectureSupported(systemCoreConfig)) {
                    throw GameLoaderException(GameLoaderError.UnsupportedArchitecture)
                }

                // Dynamic/free builds do not package emulator binaries in the APK.
                // Ensure the selected core is present before GameLoader searches app storage.
                coreUpdater.downloadCores(appContext, listOf(systemCoreConfig.coreID))

                val coreLibrary =
                    findLibrary(appContext, systemCoreConfig.coreID)
                        ?.absolutePath
                        ?: throw GameLoaderException(GameLoaderError.LoadCore)

                emit(LoadingState.LoadingGame)

                val missingBiosFiles = biosManager.getMissingBiosFiles(systemCoreConfig, game)
                if (missingBiosFiles.isNotEmpty()) {
                    throw GameLoaderException(GameLoaderError.MissingBiosFiles(missingBiosFiles))
                }

                val gameFiles =
                    runCatching {
                        val useVFS = systemCoreConfig.supportsLibretroVFS && directLoad
                        val dataFiles = retrogradeDatabase.dataFileDao().selectDataFilesForGame(game.id)
                        lemuroidLibrary.getGameFiles(game, dataFiles, useVFS)
                    }.getOrElse { throw it }

                val saveRAM =
                    runCatching {
                        val data = savesManager.getSaveRAM(game, systemCoreConfig)
                        desmumeMigrationHandler.resolveSaveData(game, systemCoreConfig.coreID, data)
                    }.getOrElse { throw GameLoaderException(GameLoaderError.Saves) }
                val saveRAMData = saveRAM.data

                val quickSaveData =
                    runCatching {
                        val shouldDiscardSave =
                            !savesCoherencyEngine.shouldDiscardAutoSaveState(
                                game,
                                systemCoreConfig.coreID,
                                saveRAM.timestampOverride,
                            )

                        if (systemCoreConfig.statesSupported && loadSave && shouldDiscardSave) {
                            statesManager.getAutoSave(game, systemCoreConfig.coreID)
                        } else {
                            null
                        }
                    }.getOrElse { throw GameLoaderException(GameLoaderError.Saves) }

                val coreVariables =
                    coreVariablesManager.getOptionsForCore(system.id, systemCoreConfig)
                        .toTypedArray()

                val systemDirectory = directoriesManager.getSystemDirectory()
                val savesDirectory = directoriesManager.getSavesDirectory()

                emit(
                    LoadingState.Ready(
                        GameData(
                            game,
                            coreLibrary,
                            gameFiles,
                            quickSaveData,
                            saveRAMData,
                            coreVariables,
                            systemDirectory,
                            savesDirectory,
                        ),
                    ),
                )
            } catch (e: GameLoaderException) {
                Timber.e(e, "Error while preparing game")
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Error while preparing game")
                throw GameLoaderException(GameLoaderError.Generic)
            }
        }

    private fun isArchitectureSupported(systemCoreConfig: SystemCoreConfig): Boolean {
        val supportedOnlyArchitectures = systemCoreConfig.supportedOnlyArchitectures ?: return true
        return Build.SUPPORTED_ABIS.toSet().intersect(supportedOnlyArchitectures).isNotEmpty()
    }

    private fun findLibrary(
        context: Context,
        coreID: CoreID,
    ): File? {
        val files =
            sequenceOf(
                File(context.applicationInfo.nativeLibraryDir),
                context.filesDir,
            )

        val candidates =
            files
                .asSequence()
                .filter { it.exists() }
                .flatMap { it.walkBottomUp() }
                .filter { it.isFile && it.name == coreID.libretroFileName }
                .toList()

        return candidates.firstOrNull { it.canRead() && it.length() > 0L }
            .also { library ->
                if (library == null) {
                    Timber.e(
                        "Libretro core unavailable: name=%s, nativeLibraryDir=%s, filesDir=%s, abis=%s",
                        coreID.libretroFileName,
                        context.applicationInfo.nativeLibraryDir,
                        context.filesDir,
                        Build.SUPPORTED_ABIS.contentToString(),
                    )
                }
            }
    }

    @Suppress("ArrayInDataClass")
    data class GameData(
        val game: Game,
        val coreLibrary: String,
        val gameFiles: RomFiles,
        val quickSaveData: SaveState?,
        val saveRAMData: ByteArray?,
        val coreVariables: Array<CoreVariable>,
        val systemDirectory: File,
        val savesDirectory: File,
    )
}
