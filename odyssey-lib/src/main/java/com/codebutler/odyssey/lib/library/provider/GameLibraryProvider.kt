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
import com.codebutler.odyssey.common.rx.Irrelevant
import io.reactivex.Single

interface GameLibraryProvider {

    val uriScheme: String

    fun listItems(): Single<Iterable<GameLibraryFile>>

    fun getGameRom(file: GameLibraryFile): Single<ByteArray>

    fun getGameSave(coreId: String, file: GameLibraryFile): Single<ByteArray>

    fun setGameSave(coreId: String, file: GameLibraryFile, data: ByteArray): Single<Irrelevant>
}
