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

import com.swordfish.lemuroid.lib.core.CoreManager
import com.swordfish.lemuroid.lib.library.LemuroidLibrary
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.common.rx.toSingleAsOptional
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.saves.SavesManager
import io.reactivex.Observable
import timber.log.Timber
import java.io.File

class GameLoader(
    private val coreManager: CoreManager,
    private val lemuroidLibrary: LemuroidLibrary,
    private val savesManager: SavesManager,
    private val coreVariablesManager: CoreVariablesManager,
    private val retrogradeDatabase: RetrogradeDatabase
) {

    fun load(game: Game, loadSave: Boolean): Observable<LoadingState> {
        return prepareGame(game, loadSave)
    }

    sealed class LoadingState {
        object LoadingCore : LoadingState()
        object LoadingGame : LoadingState()
        class Ready(val gameData: GameData) : LoadingState()
    }

    private fun prepareGame(game: Game, loadQuickSave: Boolean) = Observable.create<LoadingState> { emitter ->
        try {
            emitter.onNext(LoadingState.LoadingCore)

            val gameSystem = GameSystem.findById(game.systemId)

            val coreLibrary = coreManager.downloadCore(gameSystem, gameSystem.coreAssetsManager).blockingGet()

            emitter.onNext(LoadingState.LoadingGame)

            val gameFile = lemuroidLibrary.getGameRom(game).blockingGet()

            retrogradeDatabase.dataFileDao().selectDataFilesForGame(game.id).forEach {
                lemuroidLibrary.prepareDataFile(game, it).blockingAwait()
            }

            val saveRAMData = savesManager.getSaveRAM(game).toSingleAsOptional().blockingGet().toNullable()

            val quickSaveData = if (loadQuickSave) {
                savesManager.getAutoSave(game, gameSystem).toSingleAsOptional().blockingGet().toNullable()
            } else {
                null
            }

            val coreVariables = coreVariablesManager.getCoreOptionsForSystem(gameSystem).blockingGet().toTypedArray()

            emitter.onNext(
                LoadingState.Ready(GameData(game, coreLibrary, gameFile, quickSaveData, saveRAMData, coreVariables))
            )
        } catch (e: Exception) {
            Timber.e(e, "Error while preparing game")
            emitter.onError(e)
        } finally {
            emitter.onComplete()
        }
    }

    @Suppress("ArrayInDataClass")
    data class GameData(
        val game: Game,
        val coreLibrary: String,
        val gameFile: File,
        val quickSaveData: ByteArray?,
        val saveRAMData: ByteArray?,
        val coreVariables: Array<CoreVariable>
    )
}
