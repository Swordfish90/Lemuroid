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
import android.os.Environment
import androidx.leanback.preference.LeanbackPreferenceFragment
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

// I'm keeping this class here only because it might be needed in the future, but we should rely on SAF now.

@Deprecated("This class is no longer needed use the LocalStorageAccessFrameworkProvider class.")
class LocalStorageProvider(
    private val context: Context,
    override val metadataProvider: GameMetadataProvider,
    private val searchOnlyPrivateDirectories: Boolean = false
) : StorageProvider {

    override val id: String = "local"

    override val name: String = context.getString(R.string.local_storage)

    override val uriSchemes = listOf("file")

    override val prefsFragmentClass: Class<LeanbackPreferenceFragment>? = null

    override val enabledByDefault = true

    override fun listFiles(): Observable<StorageFile> = Observable.empty()
            /*Single.fromCallable {
        searchableDirectories()
                .map { walkDirectory(it) }
                .reduce { acc, iterable -> acc union iterable }
    }*/

    private fun searchableDirectories(): List<File> = if (searchOnlyPrivateDirectories) {
        listOf(*context.getExternalFilesDirs(null))
    } else {
        listOf(Environment.getExternalStorageDirectory())
    }

    private fun walkDirectory(directory: File): Iterable<StorageFile> {
        return directory.walk()
            .filter { file -> file.isFile && file.name.startsWith(".").not() }
                .map { file ->
                    StorageFile(
                            name = file.name,
                            size = file.length(),
                            crc = file.calculateCrc32().toUpperCase(),
                            uri = Uri.parse(file.toURI().toString()))
                }
            .asIterable()
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        File(game.fileUri.path)
    }
}
