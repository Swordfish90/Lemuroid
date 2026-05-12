/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.lib.core.assetsmanager

import android.content.Context
import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream

class PPSSPPAssetsManager : CoreID.AssetsManager {
    override suspend fun clearAssets(directoriesManager: DirectoriesManager) {
        getAssetsDirectory(directoriesManager).deleteRecursively()
    }

    override suspend fun retrieveAssetsIfNeeded(
        context: Context,
        coreUpdaterApi: CoreUpdater.CoreManagerApi,
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences,
    ) {
        if (!updateRequired(directoriesManager, sharedPreferences)) {
            return
        }

        try {
            extractBundledAssets(context, directoriesManager, sharedPreferences)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to extract PPSSPP bundled assets")
            getAssetsDirectory(directoriesManager).deleteRecursively()
        }
    }

    private suspend fun extractBundledAssets(
        context: Context,
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences,
    ) = withContext(Dispatchers.IO) {
        val coreAssetsDirectory = getAssetsDirectory(directoriesManager)
        coreAssetsDirectory.deleteRecursively()
        coreAssetsDirectory.mkdirs()

        context.assets.open(PPSSPP_ASSETS_FILENAME).use { input ->
            ZipInputStream(input).use { zipInputStream ->
                while (true) {
                    val entry = zipInputStream.nextEntry ?: break
                    Timber.d("Writing PPSSPP asset: ${entry.name}")
                    val destFile = File(coreAssetsDirectory, entry.name)
                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        destFile.parentFile?.mkdirs()
                        zipInputStream.copyTo(destFile.outputStream())
                    }
                }
            }
        }

        sharedPreferences.edit()
            .putString(PPSSPP_ASSETS_VERSION_KEY, PPSSPP_ASSETS_VERSION)
            .commit()
    }

    private suspend fun updateRequired(
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences,
    ): Boolean = withContext(Dispatchers.IO) {
        val directoryExists = getAssetsDirectory(directoriesManager).exists()
        val currentVersion = sharedPreferences.getString(PPSSPP_ASSETS_VERSION_KEY, "none")
        !directoryExists || currentVersion != PPSSPP_ASSETS_VERSION
    }

    private suspend fun getAssetsDirectory(directoriesManager: DirectoriesManager): File =
        withContext(Dispatchers.IO) {
            File(directoriesManager.getSystemDirectory(), PPSSPP_ASSETS_FOLDER_NAME)
        }

    companion object {
        const val PPSSPP_ASSETS_VERSION = "1.15"
        const val PPSSPP_ASSETS_FILENAME = "ppsspp.zip"
        const val PPSSPP_ASSETS_VERSION_KEY = "ppsspp_assets_version_key"
        const val PPSSPP_ASSETS_FOLDER_NAME = "PPSSPP"
    }
}
