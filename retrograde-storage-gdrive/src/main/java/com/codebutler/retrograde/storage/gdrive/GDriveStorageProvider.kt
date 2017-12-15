/*
 * GDriveGameLibraryProvider.kt
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.retrograde.storage.gdrive

import android.content.Context
import android.net.Uri
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.codebutler.retrograde.lib.library.metadata.GameMetadataProvider
import com.codebutler.retrograde.lib.ovgdb.db.OvgdbMetadataProvider
import com.codebutler.retrograde.lib.storage.StorageFile
import com.codebutler.retrograde.lib.storage.StorageProvider
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class GDriveStorageProvider(componentBuilder: GDriveComponent.Builder) : StorageProvider {

    companion object {
        private const val SPACE_APP_DATA = "appDataFolder"

        val SCOPES = listOf(Scope(DriveScopes.DRIVE_READONLY), Scope(DriveScopes.DRIVE_APPDATA))
    }

    val component = componentBuilder.build()

    @Inject lateinit var context: Context
    @Inject lateinit var driveBrowser: GDriveBrowser
    @Inject lateinit var ovgdbMetadataProvider: OvgdbMetadataProvider

    init {
        component.inject(this)
    }

    override val id = "gdrive"

    override val name: String = context.getString(R.string.gdrive_google_drive)

    override val uriSchemes = listOf("gdrive")

    override val prefsFragmentClass = GDrivePreferenceFragment::class.java

    override val metadataProvider: GameMetadataProvider = ovgdbMetadataProvider

    override fun listFiles(): Single<Iterable<StorageFile>> = Single.fromCallable {
        val folderId = getGameLibraryFolderId() ?: return@fromCallable listOf<StorageFile>()
        driveBrowser.listRecursive(folderId)
                .filter { file -> file.getSize() != null }
                .map { file ->
                    StorageFile(
                            name = file.name,
                            size = file.getSize(),
                            uri = Uri.Builder()
                                    .scheme(uriSchemes.first())
                                    .authority(file.id)
                                    .build())
                }
                .asIterable()
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        val gamesCacheDir = File(context.cacheDir, "gdrive-games")
        gamesCacheDir.mkdirs()
        val gameFile = File(gamesCacheDir, game.fileName)
        if (gameFile.exists()) {
            return@fromCallable gameFile
        }
        FileOutputStream(gameFile).use { stream ->
            driveBrowser.downloadById(game.fileUri.authority, stream)
        }
        gameFile
    }

    override fun getGameSave(game: Game): Single<Optional<ByteArray>> = Single.fromCallable {
        val fileName = getSaveFileName(game)
        driveBrowser.downloadByName(SPACE_APP_DATA, null, fileName).toOptional()
    }

    override fun setGameSave(game: Game, data: ByteArray): Completable = Completable.fromCallable {
        val fileName = getSaveFileName(game)
        driveBrowser.uploadByName(SPACE_APP_DATA, null, fileName, data)
    }

    private fun getGameLibraryFolderId(): String? {
        val prefs = context.getSharedPreferences(GDrivePreferenceFragment.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(GDrivePreferenceFragment.PREF_KEY_FOLDER_ID, null)
    }

    private fun getSaveFileName(game: Game) = "${game.fileName}.sram"
}
