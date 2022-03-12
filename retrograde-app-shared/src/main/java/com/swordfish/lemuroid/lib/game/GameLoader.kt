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

package com.swordfish.lemuroid.lib.game

import android.content.Context
import com.swordfish.lemuroid.common.rx.toSingleAsOptional
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.LemuroidLibrary
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SaveState
import com.swordfish.lemuroid.lib.saves.SavesCoherencyEngine
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.RomFiles
import io.reactivex.Observable
import timber.log.Timber
import java.io.File

class GameLoader(
    private val lemuroidLibrary: LemuroidLibrary,
    private val statesManager: StatesManager,
    private val savesManager: SavesManager,
    private val coreVariablesManager: CoreVariablesManager,
    private val retrogradeDatabase: RetrogradeDatabase,
    private val savesCoherencyEngine: SavesCoherencyEngine,
    private val directoriesManager: DirectoriesManager
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
        directLoad: Boolean
    ): Observable<LoadingState> = Observable.create { emitter ->
        try {
            emitter.onNext(LoadingState.LoadingCore)

            val system = GameSystem.findById(game.systemId)

            val coreLibrary = runCatching {
                findLibrary(appContext, systemCoreConfig.coreID)!!.absolutePath
            }.getOrElse { throw GameLoaderException(GameLoaderError.LOAD_CORE) }

            emitter.onNext(LoadingState.LoadingGame)

            if (!areRequiredBiosFilesPresent(systemCoreConfig)) {
                throw GameLoaderException(GameLoaderError.MISSING_BIOS)
            }

            val gameFiles = runCatching {
                val useVFS = systemCoreConfig.supportsLibretroVFS && directLoad
                val dataFiles = retrogradeDatabase.dataFileDao().selectDataFilesForGame(game.id)
                lemuroidLibrary.getGameFiles(game, dataFiles, useVFS).blockingGet()
            }.getOrElse { throw GameLoaderException(GameLoaderError.LOAD_GAME) }

            val saveRAMData = runCatching {
                savesManager.getSaveRAM(game).toSingleAsOptional().blockingGet().toNullable()
            }.getOrElse { throw GameLoaderException(GameLoaderError.SAVES) }

            val quickSaveData = runCatching {
                val shouldDiscardSave =
                    !savesCoherencyEngine.shouldDiscardAutoSaveState(game, systemCoreConfig.coreID)

                if (systemCoreConfig.statesSupported && loadSave && shouldDiscardSave) {
                    statesManager.getAutoSave(game, systemCoreConfig.coreID)
                        .toSingleAsOptional()
                        .blockingGet()
                        .toNullable()
                } else {
                    null
                }
            }.getOrElse { throw GameLoaderException(GameLoaderError.SAVES) }

            val coreVariables = coreVariablesManager.getOptionsForCore(system.id, systemCoreConfig)
                .blockingGet()
                .toTypedArray()

            val systemDirectory = directoriesManager.getSystemDirectory()
            val savesDirectory = directoriesManager.getSavesDirectory()

            emitter.onNext(
                LoadingState.Ready(
                    GameData(
                        game,
                        coreLibrary,
                        gameFiles,
                        quickSaveData,
                        saveRAMData,
                        coreVariables,
                        systemDirectory,
                        savesDirectory
                    )
                )
            )
        } catch (e: GameLoaderException) {
            Timber.e(e, "Error while preparing game")
            emitter.onError(e)
        } catch (e: Exception) {
            Timber.e(e, "Error while preparing game")
            emitter.onError(GameLoaderException(GameLoaderError.GENERIC))
        } finally {
            emitter.onComplete()
        }
    }

    private fun findLibrary(context: Context, coreID: CoreID): File? {
        val files = sequenceOf(
            File(context.applicationInfo.nativeLibraryDir),
            context.filesDir
        )

        return files
            .flatMap { it.walkBottomUp() }
            .firstOrNull { it.name == coreID.libretroFileName }
    }

    private fun areRequiredBiosFilesPresent(systemCoreConfig: SystemCoreConfig): Boolean {
        return systemCoreConfig.requiredBIOSFiles
            .map { File(directoriesManager.getSystemDirectory(), it) }
            .all { it.exists() }
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
        val savesDirectory: File
    )
}
