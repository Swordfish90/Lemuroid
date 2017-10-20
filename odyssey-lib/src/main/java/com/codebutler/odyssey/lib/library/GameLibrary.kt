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

import android.util.Log
import com.codebutler.odyssey.common.rx.toSingleAsOptional
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.library.provider.GameLibraryProvider
import com.codebutler.odyssey.lib.ovgdb.OvgdbManager
import com.codebutler.odyssey.lib.ovgdb.db.OvgdbDatabase
import com.codebutler.odyssey.lib.ovgdb.db.entity.Release
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.gojuno.koptional.rxjava2.filterSome
import com.gojuno.koptional.toOptional
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GameLibrary(
        private val odysseydb: OdysseyDatabase,
        private val ovgdbManager: OvgdbManager,
        private val libraryProviders: List<GameLibraryProvider>) {

    companion object {
        const val TAG = "OdysseyLibrary"
    }

    fun indexGames() {
        ovgdbManager.dbReady
                .observeOn(Schedulers.io())
                .subscribe { ovgdb -> indexGamesWithDb(ovgdb) }
    }

    // FIXME: Move this somewhere else.
    fun getProvider(game: Game) = libraryProviders.find { it.uriScheme == game.fileUri.scheme }!!

    private fun indexGamesWithDb(ovgdb: OvgdbDatabase) {
        val startedAtMs = System.currentTimeMillis()
        Observable.fromIterable(libraryProviders)
                .flatMapSingle { provider -> provider.listFiles() }
                .flatMapIterable { it }
                .flatMapSingle { file ->
                    Log.d(TAG, "Got file: $file ${file.uri}")
                    odysseydb.gameDao().selectByFileUri(file.uri.toString())
                            .toSingleAsOptional()
                            .map { game -> Pair(file, game) }
                }
                .doOnNext { (file, game) -> Log.d(TAG, "Game already indexed? ${file.name} ${game is Some}") }
                .doOnNext { (_, game) ->
                    if (game is Some) {
                        val updatedGame = game.value.copy(lastIndexedAt = startedAtMs)
                        Log.d(TAG, "Update: $updatedGame")
                        odysseydb.gameDao().update(updatedGame)
                    }
                }
                .filter { (_, game) -> game is None }
                .map { (file, _) -> file }
                .flatMapSingle { file ->
                    when (file.crc) {
                        null -> Maybe.empty()
                        else -> ovgdb.romDao().findByCRC(file.crc)
                    }.switchIfEmpty(ovgdb.romDao().findByFileName(sanitizeRomFileName(file.name)))
                            .toSingleAsOptional()
                            .map { rom -> Pair(file, rom) }
                }
                .doOnNext { (file, rom) -> Log.d(TAG, "Rom Found: ${file.name} ${rom is Some}") }
                .flatMapSingle { (file, rom) ->
                    when (rom) {
                        is Some -> ovgdb.releaseDao().findByRomId(rom.value.id)
                                .toSingleAsOptional()
                        else -> Single.just<Optional<Release>>(None)
                    }.map { release -> Triple(file, rom, release) }
                }
                .doOnNext { (file, _, release) -> Log.d(TAG, "Release found: ${file.name}, ${release is Some}") }
                .flatMapSingle { (file, rom, release) ->
                    when (rom) {
                        is Some -> ovgdb.systemDao().findById(rom.value.systemId)
                                .toSingleAsOptional()
                        else -> Single.just(None)
                    }.map { ovgdbSystem -> Triple(file, release, ovgdbSystem) }
                }
                .doOnNext { (file, _, ovgdbSystem) -> Log.d(TAG, "OVGDB System Found: ${file.name}, ${ovgdbSystem is Some}") }
                .map { (file, release, ovgdbSystem) ->
                    var system = when (ovgdbSystem) {
                        is Some -> {
                            val gs = GameSystem.findByShortName(ovgdbSystem.value.shortName)
                            if (gs == null) {
                                Log.e(TAG, "System '${ovgdbSystem.value.shortName}' not found")
                            }
                            gs
                        }
                        else -> null
                    }
                    if (system == null) {
                        Log.d(TAG, "System not found, trying file extension: ${file.name}")
                        system = GameSystem.findByFileExtension(file.extension)
                    }
                    if (system == null) {
                        Log.d(TAG, "Giving up on ${file.name}")
                    } else {
                        Log.d(TAG, "Found system!! $system")
                    }
                    Triple(file, release, system.toOptional())
                }
                .map { (file, release, system) ->
                    when (system) {
                        is Some -> Game(
                                fileName = file.name,
                                fileUri = file.uri,
                                title = release.toNullable()?.titleName ?: file.name,
                                systemId = system.value.id,
                                developer = release.toNullable()?.developer,
                                coverFrontUrl = release.toNullable()?.coverFront,
                                lastIndexedAt = startedAtMs
                        ).toOptional()
                        else -> None
                    }
                }
                .filterSome()
                .subscribe(
                        { game ->
                            Log.d(TAG, "Insert: $game")
                            odysseydb.gameDao().insert(game)
                        },
                        { error -> Log.e(TAG, "Error while indexing", error) },
                        {
                            Log.d(TAG, "Done inserting. Looking for games to remove...")
                            removeDeletedGames(startedAtMs)
                        })
    }

    private fun removeDeletedGames(startedAtMs: Long) {
        odysseydb.gameDao().selectByLastIndexedAtLessThan(startedAtMs)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { games ->
                            Log.d(TAG, "Removing games: $games")
                            odysseydb.gameDao().delete(games)
                        },
                        { error -> Log.e(TAG, "Error while removing", error) })
    }

    private fun sanitizeRomFileName(fileName: String): String {
        return fileName
                .replace("(U)", "(USA)")
                .replace("(J)", "(Japan)")
                .replace(" [!]", "")
                .replace(Regex("\\.v64$"), ".n64")
                .replace(Regex("\\.z64$"), ".n64")
    }
}
