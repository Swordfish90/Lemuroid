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
import com.codebutler.odyssey.lib.library.provider.GameLibraryProviderRegistry
import com.codebutler.odyssey.lib.ovgdb.OvgdbManager
import com.codebutler.odyssey.lib.ovgdb.db.OvgdbDatabase
import com.codebutler.odyssey.lib.ovgdb.db.entity.Release
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.gojuno.koptional.rxjava2.filterSome
import com.gojuno.koptional.toOptional
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class GameLibrary(
        private val odysseydb: OdysseyDatabase,
        private val ovgdbManager: OvgdbManager,
        private val providerRegistry: GameLibraryProviderRegistry) {

    fun indexGames(): Completable = Completable.create { emitter ->
        ovgdbManager.dbReady
                .observeOn(Schedulers.io())
                .subscribe { ovgdb -> indexGamesWithDb(ovgdb, emitter) }
    }

    fun getGameRom(game: Game): Single<File>
            = providerRegistry.getProvider(game).getGameRom(game)

    fun getGameSave(game: Game): Single<Optional<ByteArray>>
            = providerRegistry.getProvider(game).getGameSave(game)

    fun setGameSave(game: Game, data: ByteArray): Completable
            = providerRegistry.getProvider(game).setGameSave(game, data)

    private fun indexGamesWithDb(ovgdb: OvgdbDatabase, emitter: CompletableEmitter) {
        val startedAtMs = System.currentTimeMillis()
        Observable.fromIterable(providerRegistry.providers)
                .flatMapSingle { provider -> provider.listFiles() }
                .flatMapIterable { it }
                .flatMapSingle { file ->
                    Timber.d("Got file: $file ${file.uri}")
                    odysseydb.gameDao().selectByFileUri(file.uri.toString())
                            .toSingleAsOptional()
                            .map { game -> Pair(file, game) }
                }
                .doOnNext { (file, game) -> Timber.d("Game already indexed? ${file.name} ${game is Some}") }
                .doOnNext { (_, game) ->
                    if (game is Some) {
                        val updatedGame = game.value.copy(lastIndexedAt = startedAtMs)
                        Timber.d("Update: $updatedGame")
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
                .doOnNext { (file, rom) -> Timber.d("Rom Found: ${file.name} ${rom is Some}") }
                .flatMapSingle { (file, rom) ->
                    when (rom) {
                        is Some -> ovgdb.releaseDao().findByRomId(rom.value.id)
                                .toSingleAsOptional()
                        else -> Single.just<Optional<Release>>(None)
                    }.map { release -> Triple(file, rom, release) }
                }
                .doOnNext { (file, _, release) -> Timber.d("Release found: ${file.name}, ${release is Some}") }
                .flatMapSingle { (file, rom, release) ->
                    when (rom) {
                        is Some -> ovgdb.systemDao().findById(rom.value.systemId)
                                .toSingleAsOptional()
                        else -> Single.just(None)
                    }.map { ovgdbSystem -> Triple(file, release, ovgdbSystem) }
                }
                .doOnNext { (file, _, ovgdbSystem) -> Timber.d("OVGDB System Found: ${file.name}, ${ovgdbSystem is Some}") }
                .map { (file, release, ovgdbSystem) ->
                    var system = when (ovgdbSystem) {
                        is Some -> {
                            val gs = GameSystem.findByShortName(ovgdbSystem.value.shortName)
                            if (gs == null) {
                                Timber.e("System '${ovgdbSystem.value.shortName}' not found")
                            }
                            gs
                        }
                        else -> null
                    }
                    if (system == null) {
                        Timber.d("System not found, trying file extension: ${file.name}")
                        system = GameSystem.findByFileExtension(file.extension)
                    }
                    if (system == null) {
                        Timber.d("Giving up on ${file.name}")
                    } else {
                        Timber.d("Found system!! $system")
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
                            Timber.d("Insert: $game")
                            odysseydb.gameDao().insert(game)
                        },
                        { error ->
                            Timber.e("Error while indexing", error)
                            emitter.onError(error)
                        },
                        {
                            Timber.d("Done inserting. Looking for games to remove...")
                            removeDeletedGames(startedAtMs)
                            emitter.onComplete()
                        })
    }

    private fun removeDeletedGames(startedAtMs: Long) {
        odysseydb.gameDao().selectByLastIndexedAtLessThan(startedAtMs)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { games ->
                            Timber.d("Removing games: $games")
                            odysseydb.gameDao().delete(games)
                        },
                        { error -> Timber.e("Error while removing", error) })
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
