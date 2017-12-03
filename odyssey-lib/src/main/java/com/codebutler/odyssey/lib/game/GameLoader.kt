/*
 * GameLoader.kt
 *
 * Copyright (C) 2017 Odyssey Project
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

package com.codebutler.odyssey.lib.game

import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.GameLibrary
import com.codebutler.odyssey.lib.library.GameSystem
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.dao.updateAsync
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.gojuno.koptional.Optional
import io.reactivex.Single
import io.reactivex.functions.Function3
import java.io.File

class GameLoader(
        private val coreManager: CoreManager,
        private val odysseyDatabase: OdysseyDatabase,
        private val gameLibrary: GameLibrary) {

    fun load(gameId: Int): Single<GameData> {
        return odysseyDatabase.gameDao().selectById(gameId)
                .flatMapSingle { game -> prepareGame(game) }
                .doOnSuccess { data -> updateTimestamp(data.game) }
    }

    private fun prepareGame(game: Game): Single<GameData> {
        val gameSystem = GameSystem.findById(game.systemId)!!

        val coreObservable = coreManager.downloadCore(gameSystem.coreFileName)
        val gameObservable = gameLibrary.getGameRom(game)
        val saveObservable = gameLibrary.getGameSave(game)

        return Single.zip(
                coreObservable,
                gameObservable,
                saveObservable,
                Function3<File, File, Optional<ByteArray>, GameData> { coreFile, gameFile, saveData ->
                    GameData(game, coreFile, gameFile, saveData.toNullable())
                })
    }

    private fun updateTimestamp(game: Game) {
        odysseyDatabase.gameDao()
                .updateAsync(game.copy(lastPlayedAt = System.currentTimeMillis()))
                .subscribe()
    }

    @Suppress("ArrayInDataClass")
    data class GameData(
            val game: Game,
            val coreFile: File,
            val gameFile: File,
            val saveData: ByteArray?)
}
