/*
 * LocalGameLibraryProvider.kt
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

package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import android.net.Uri
import androidx.leanback.preference.LeanbackPreferenceFragment
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

class LocalStorageProvider(
    context: Context,
    private val directoriesManager: DirectoriesManager,
    override val metadataProvider: GameMetadataProvider
) : StorageProvider {

    override val id: String = "local"

    override val name: String = context.getString(R.string.local_storage)

    override val uriSchemes = listOf("file")

    override val prefsFragmentClass: Class<LeanbackPreferenceFragment>? = null

    override val enabledByDefault = true

    override fun listFiles(): Observable<StorageFile> =
        Observable.fromIterable(walkDirectory(directoriesManager.getInternalRomsDirectory()))

    private fun walkDirectory(directory: File): Iterable<StorageFile> {
        return directory.walk()
            .filter { file -> file.isFile && !file.name.startsWith(".") }
            .map { file ->
                StorageFile(
                    name = file.name,
                    size = file.length(),
                    crc = file.calculateCrc32().toUpperCase(),
                    uri = Uri.parse(file.toURI().toString()),
                    path = file.parent
                )
            }
            .asIterable()
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        File(game.fileUri.path)
    }
}
