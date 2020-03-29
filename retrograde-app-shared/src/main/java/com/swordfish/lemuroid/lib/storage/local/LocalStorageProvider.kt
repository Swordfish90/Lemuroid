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
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.leanback.preference.LeanbackPreferenceFragment
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.common.kotlin.extractEntryToFile
import com.swordfish.lemuroid.common.kotlin.isZipped
import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.util.zip.ZipInputStream

class LocalStorageProvider(
    private val context: Context,
    private val directoriesManager: DirectoriesManager,
    override val metadataProvider: GameMetadataProvider
) : StorageProvider {

    override val id: String = "local"

    override val name: String = context.getString(R.string.local_storage)

    override val uriSchemes = listOf("file")

    override val prefsFragmentClass: Class<LeanbackPreferenceFragment>? = null

    override val enabledByDefault = true

    override fun listUris(): Observable<Uri> =
        Observable.fromIterable(walkDirectory(getExternalFolder() ?: directoriesManager.getInternalRomsDirectory()))

    override fun getStorageFile(uri: Uri): StorageFile? {
        val documentFile = DocumentFile.fromFile(File(uri.path))
        return DocumentFileParser.parseDocumentFile(context, documentFile)
    }

    private fun getExternalFolder(): File? {
        val prefString = context.getString(R.string.pref_key_legacy_external_folder)
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)
        return preferenceManager.getString(prefString, null)?.let { File(it) }
    }

    private fun walkDirectory(directory: File): Iterable<Uri> {
        return directory.walk()
            .filter { file -> file.isFile && !file.name.startsWith(".") }
            .map { it.toUri() }
            .asIterable()
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        val gamePath = Uri.parse(game.fileUri).path
        val originalFile = File(gamePath)
        if (!originalFile.isZipped() || originalFile.name == game.fileName) {
            return@fromCallable originalFile
        }

        val cacheFile = GameCacheUtils.getCacheFileForGame(LOCAL_STORAGE_CACHE_SUBFOLDER, context, game)
        if (cacheFile.exists()) {
            return@fromCallable cacheFile
        }

        if (originalFile.isZipped()) {
            val stream = ZipInputStream(originalFile.inputStream())
            stream.extractEntryToFile(game.fileName, cacheFile)
        }

        cacheFile
    }

    companion object {
        const val LOCAL_STORAGE_CACHE_SUBFOLDER = "local-storage-games"
    }
}
