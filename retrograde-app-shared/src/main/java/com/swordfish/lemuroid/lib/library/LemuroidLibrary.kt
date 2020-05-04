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

import android.net.Uri
import com.swordfish.lemuroid.common.rx.toSingleAsOptional
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.StorageProviderRegistry
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.swordfish.lemuroid.lib.bios.BiosManager
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.io.File

class LemuroidLibrary(
    private val retrogradedb: RetrogradeDatabase,
    private val providerProviderRegistry: StorageProviderRegistry,
    private val biosManager: BiosManager
) {
    fun indexLibrary(): Completable {
        val startedAtMs = System.currentTimeMillis()

        return Observable.fromIterable(providerProviderRegistry.enabledProviders).concatMap { provider ->
            provider.listUris()
                .flatMapSingle { uri -> retrieveGameForUri(uri) }
                .buffer(BUFFER_SIZE)
                .doOnNext { pairs -> updateExisting(pairs, startedAtMs) }
                .map { pairs -> filterNotExisting(pairs) }
                .map { pairs -> retrieveStorageFiles(provider, pairs) }
                .flatMapSingle { retrieveGames(it, provider, startedAtMs) }
                .doOnNext { (files, games) ->
                    handleNewGames(games)
                    handleUnknownFiles(provider, files, startedAtMs)
                }
        }
        .doOnComplete { removeDeletedBios(startedAtMs) }
        .doOnComplete { removeDeletedGames(startedAtMs) }
        .doOnComplete {
            Timber.i("Library indexing completed in: ${System.currentTimeMillis() - startedAtMs} ms")
        }
        .ignoreElements()
    }

    private fun removeDeletedBios(startedAtMs: Long) {
        biosManager.deleteBiosBefore(startedAtMs)
    }

    private fun retrieveStorageFiles(provider: StorageProvider, pairs: List<Pair<Uri, Optional<Game>>>) =
            pairs.map { (uri, _) -> provider.getStorageFile(uri) }.filterNotNull()

    private fun handleNewGames(games: List<Game>) {
        games.forEach { Timber.d("Insert: $it") }
        retrogradedb.gameDao().insert(games)
    }

    private fun handleUnknownFiles(provider: StorageProvider, files: List<StorageFile>, startedAtMs: Long) {
        files.forEach {
            biosManager.tryAddBiosAfter(it, provider.getInputStream(it.uri), startedAtMs)
        }
    }

    private fun retrieveGames(
        it: List<StorageFile>,
        provider: StorageProvider,
        startedAtMs: Long
    ): Single<Pair<List<StorageFile>, List<Game>>> {
        return Observable.fromIterable(it)
            .flatMap { retrieveGame(it, provider, startedAtMs) }
            .reduce(Pair(listOf(), listOf())) { (files, games), (file, game) ->
                if (game is Some) {
                    files to (games + game.toNullable()!!)
                } else {
                    (files + file) to games
                }
            }
    }

    private fun retrieveGame(storageFile: StorageFile, provider: StorageProvider, startedAtMs: Long) =
        Observable.just(storageFile).compose(provider.metadataProvider.transformer(startedAtMs))
            .map { storageFile to it }

    private fun updateExisting(pairs: MutableList<Pair<Uri, Optional<Game>>>, startedAtMs: Long) {
        pairs.forEach { (uri, game) -> Timber.d("Game already indexed? $uri ${game is Some}") }
        pairs.filter { (_, game) -> game is Some }
                .map { (_, game) -> game.component1()!!.copy(lastIndexedAt = startedAtMs) }
                .let { games ->
                    games.forEach { Timber.d("Update: $it") }
                    retrogradedb.gameDao().update(games)
                }
    }

    private fun filterNotExisting(pairs: List<Pair<Uri, Optional<Game>>>) =
            pairs.filter { (_, game) -> game is None }

    private fun retrieveGameForUri(uri: Uri): Single<Pair<Uri, Optional<Game>>> {
        Timber.d("Retrieving game for uri: $uri")
        return retrogradedb.gameDao().selectByFileUri(uri.toString())
                .toSingleAsOptional()
                .map { game -> Pair(uri, game) }
    }

    private fun removeDeletedGames(startedAtMs: Long) {
        val games = retrogradedb.gameDao().selectByLastIndexedAtLessThan(startedAtMs)
        retrogradedb.gameDao().delete(games)
    }

    fun getGameRom(game: Game): Single<File> =
            providerProviderRegistry.getProvider(game).getGameRom(game)

    companion object {
        // We batch database updates to avoid unnecessary UI updates.
        const val BUFFER_SIZE = 100
    }
}
