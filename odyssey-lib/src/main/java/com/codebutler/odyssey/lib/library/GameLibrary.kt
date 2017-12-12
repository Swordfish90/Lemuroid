/*
 * GameLibrary.kt
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

package com.codebutler.odyssey.lib.library

import com.codebutler.odyssey.common.rx.toSingleAsOptional
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.storage.StorageProviderRegistry
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.gojuno.koptional.rxjava2.filterSome
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class GameLibrary(
        private val odysseydb: OdysseyDatabase,
        private val providerProviderRegistry: StorageProviderRegistry) {

    fun indexGames(): Completable = Completable.create { emitter ->
        val startedAtMs = System.currentTimeMillis()
        Observable.fromIterable(providerProviderRegistry.enabledProviders)
                .flatMap { provider ->
                    provider.listFiles()
                            .flattenAsObservable { it }
                            .flatMapSingle { file ->
                                Timber.d("Got file: $file ${file.uri}")
                                odysseydb.gameDao().selectByFileUri(file.uri.toString())
                                        .toSingleAsOptional()
                                        .map { game -> Pair(file, game) }
                            }
                            .doOnNext { (file, game) ->
                                Timber.d("Game already indexed? ${file.name} ${game is Some}")
                                if (game is Some) {
                                    val updatedGame = game.value.copy(lastIndexedAt = startedAtMs)
                                    Timber.d("Update: $updatedGame")
                                    odysseydb.gameDao().update(updatedGame)
                                }
                            }
                            .filter { (_, game) -> game is None }
                            .map { (file, _) -> file }
                            .compose(provider.metadataProvider.transformer(startedAtMs))
                            .filterSome()
                }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { game ->
                            Timber.d("Insert: $game")
                            odysseydb.gameDao().insert(game)
                        },
                        { error ->
                            Timber.e(error, "Error while indexing")
                            emitter.onError(error)
                        },
                        {
                            Timber.d("Done inserting. Looking for games to remove...")
                            removeDeletedGames(startedAtMs)
                            emitter.onComplete()
                        })
    }

    fun getGameRom(game: Game): Single<File> =
            providerProviderRegistry.getProvider(game).getGameRom(game)

    fun getGameSave(game: Game): Single<Optional<ByteArray>> =
            providerProviderRegistry.getProvider(game).getGameSave(game)

    fun setGameSave(game: Game, data: ByteArray): Completable =
            providerProviderRegistry.getProvider(game).setGameSave(game, data)

    private fun removeDeletedGames(startedAtMs: Long) {
        odysseydb.gameDao().selectByLastIndexedAtLessThan(startedAtMs)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { games ->
                            Timber.d("Removing games: $games")
                            odysseydb.gameDao().delete(games)
                        },
                        { error -> Timber.e(error, "Error while removing") })
    }
}
