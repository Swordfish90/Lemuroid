/*
 * GameLibrary.kt
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

package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.common.rx.toSingleAsOptional
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.StorageProviderRegistry
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.gojuno.koptional.rxjava2.filterSome
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class GameLibrary(
    private val retrogradedb: RetrogradeDatabase,
    private val providerProviderRegistry: StorageProviderRegistry
) {

    fun indexGames(): Completable {
        val startedAtMs = System.currentTimeMillis()

        return Observable.fromIterable(providerProviderRegistry.enabledProviders).concatMap { provider ->
            provider.listFiles()
                .flatMapSingle { file -> retrieveGameFromFile(file) }
                .buffer(BUFFER_SIZE)
                .doOnNext { pairs -> updateExisting(pairs, startedAtMs) }
                .map { pairs -> filterNotExisting(pairs) }
                .map { pairs -> pairs.map { (file, _) -> file } }
                .flatMapSingle { retrieveMetadata(it, provider, startedAtMs) }
                .doOnNext { games: List<Game> ->
                    games.forEach { Timber.d("Insert: $it") }
                    retrogradedb.gameDao().insert(games)
                }
                .doOnComplete { removeDeletedGames(startedAtMs) }
        }
        .subscribeOn(Schedulers.io())
        .ignoreElements()
    }

    private fun retrieveMetadata(
        it: List<StorageFile>,
        provider: StorageProvider,
        startedAtMs: Long
    ): Single<List<Game>> {
        return Observable.fromIterable(it)
                .compose(provider.metadataProvider.transformer(startedAtMs))
                .filterSome()
                .toList()
    }

    private fun updateExisting(pairs: MutableList<Pair<StorageFile, Optional<Game>>>, startedAtMs: Long) {
        pairs.forEach { (file, game) -> Timber.d("Game already indexed? ${file.name} ${game is Some}") }
        pairs.filter { (_, game) -> game is Some }
                .map { (_, game) -> game.component1()!!.copy(lastIndexedAt = startedAtMs) }
                .let { games ->
                    games.forEach { Timber.d("Update: $it") }
                    retrogradedb.gameDao().update(games)
                }
    }

    private fun filterNotExisting(pairs: List<Pair<StorageFile, Optional<Game>>>) =
            pairs.filter { (_, game) -> game is None }

    private fun retrieveGameFromFile(file: StorageFile): Single<Pair<StorageFile, Optional<Game>>> {
        Timber.d("Retrieving game for file: $file ${file.uri}")
        return retrogradedb.gameDao().selectByFileUri(file.uri.toString())
                .toSingleAsOptional()
                .map { game -> Pair(file, game) }
    }

    private fun removeDeletedGames(startedAtMs: Long) {
        val games = retrogradedb.gameDao().selectByLastIndexedAtLessThan(startedAtMs)
        retrogradedb.gameDao().delete(games)
    }

    fun getGameRom(game: Game): Single<File> =
            providerProviderRegistry.getProvider(game).getGameRom(game)

    fun getGameSave(game: Game): Single<Optional<ByteArray>> =
            providerProviderRegistry.getProvider(game).getGameSave(game)

    fun setGameSave(game: Game, data: ByteArray): Completable =
            providerProviderRegistry.getProvider(game).setGameSave(game, data)

    companion object {
        // We batch database updates to avoid unnecessary UI updates.
        const val BUFFER_SIZE = 100
    }
}
