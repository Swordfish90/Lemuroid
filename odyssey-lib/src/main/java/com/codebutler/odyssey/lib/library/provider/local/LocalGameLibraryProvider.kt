/*
 * LocalGameLibraryProvider.kt
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

package com.codebutler.odyssey.lib.library.provider.local

import android.net.Uri
import android.os.Environment
import com.codebutler.odyssey.common.kotlin.calculateCrc32
import com.codebutler.odyssey.lib.library.GameLibraryFile
import com.codebutler.odyssey.lib.library.provider.GameLibraryProvider
import io.reactivex.Single
import java.io.File

class LocalGameLibraryProvider : GameLibraryProvider {
    override val uriScheme: String
        get() = "file"

    override fun listFiles(): Single<Iterable<GameLibraryFile>> {
        return Single.create<Iterable<GameLibraryFile>> { emitter ->
            try {
                val items = Environment.getExternalStorageDirectory()
                        .walk()
                        .maxDepth(1)
                        .filter { it.isFile }
                        .map { file -> GameLibraryFile(
                                name = file.name,
                                size = file.length(),
                                crc = file.calculateCrc32().toUpperCase(),
                                uri = Uri.parse(file.toURI().toString()))
                        }
                        .asIterable()
                emitter.onSuccess(items)
            } catch (e: Throwable) {
                emitter.onError(e)
            }
        }
    }

    override fun fileExists(uri: Uri): Single<Boolean> = Single.just(File(uri.path).exists())

    override fun getGameRom(file: GameLibraryFile): Single<ByteArray> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(File(file.uri.path).readBytes())
            } catch (e: Throwable) {
                emitter.onError(e)
            }
        }
    }

    override fun getGameSave(coreId: String, file: GameLibraryFile): Single<ByteArray> {
        TODO("not implemented")
    }

    override fun setGameSave(coreId: String, file: GameLibraryFile, data: ByteArray): Single<Unit> {
        TODO("not implemented")
    }
}
