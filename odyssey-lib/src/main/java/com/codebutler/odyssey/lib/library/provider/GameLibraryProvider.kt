/*
 * GameLibraryProvider.kt
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

package com.codebutler.odyssey.lib.library.provider

import com.codebutler.odyssey.lib.library.GameLibraryFile
import com.codebutler.odyssey.lib.library.db.entity.Game
import io.reactivex.Single
import java.io.File

interface GameLibraryProvider {

    val uriScheme: String

    fun listFiles(): Single<Iterable<GameLibraryFile>>

    fun getGameRom(game: Game): Single<File>

    fun getGameSave(coreId: String, game: Game): Single<ByteArray>

    fun setGameSave(coreId: String, game: Game, data: ByteArray): Single<Unit>
}
