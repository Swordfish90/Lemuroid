/*
 * CoreManager.kt
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

package come.asg.lemudroid.ext.feature.core

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import come.asg.lemudroid.common.files.safeDelete
import come.asg.lemudroid.common.kotlin.writeToFile
import come.asg.lemudroid.lib.core.CoreUpdater
import come.asg.lemudroid.lib.library.CoreID
import come.asg.lemudroid.lib.preferences.SharedPreferencesHelper
import come.asg.lemudroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import timber.log.Timber
import java.io.File
import java.io.IOException

class CoreUpdaterImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit,
) : CoreUpdater {
    // This is the last tagged versions of cores.
    companion object {
        private const val CORES_VERSION = "1.17.0"
    }

    private val baseUri = Uri.parse("https://github.com/Swordfish90/OMINOYCores/")

    private val api = retrofit.create(CoreUpdater.CoreManagerApi::class.java)

    override suspend fun downloadCores(
        context: Context,
        coreIDs: List<CoreID>,
    ) {
        val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(context.applicationContext)
        coreIDs.asFlow()
            .onEach { retrieveAssets(it, sharedPreferences) }
            .onEach { retrieveFile(context, it) }
            .collect()
    }

    private suspend fun retrieveFile(
        context: Context,
        coreID: CoreID,
    ) {
        findBundledLibrary(context, coreID) ?: downloadCoreFromGithub(coreID)
    }

    private suspend fun retrieveAssets(
        coreID: CoreID,
        sharedPreferences: SharedPreferences,
    ) {
        CoreID.getAssetManager(coreID)
            .retrieveAssetsIfNeeded(api, directoriesManager, sharedPreferences)
    }

    private suspend fun downloadCoreFromGithub(coreID: CoreID): File {
        Timber.i("Downloading core $coreID from github")

        val mainCoresDirectory = directoriesManager.getCoresDirectory()
        val coresDirectory =
            File(mainCoresDirectory, CORES_VERSION).apply {
                mkdirs()
            }

        val libFileName = coreID.libretroFileName
        val destFile = File(coresDirectory, libFileName)

        if (destFile.isFile && destFile.length() > 0L) {
            return destFile
        }
        destFile.safeDelete()

        runCatching {
            deleteOutdatedCores(mainCoresDirectory, CORES_VERSION)
        }

        try {
            var lastError: Throwable? = null
            for (abi in Build.SUPPORTED_ABIS) {
                val uri =
                    baseUri.buildUpon()
                        .appendEncodedPath("raw/$CORES_VERSION/lemuroid_core_${coreID.coreName}/src/main/jniLibs/")
                        .appendPath(abi)
                        .appendPath(libFileName)
                        .build()
                try {
                    downloadFile(uri, destFile)
                    if (destFile.isFile && destFile.length() > 0L) break
                } catch (e: Throwable) {
                    lastError = e
                    destFile.safeDelete()
                }
            }
            if (!destFile.isFile || destFile.length() == 0L) {
                throw lastError ?: IOException("Downloaded core is empty: $libFileName")
            }
            return destFile
        } catch (e: Throwable) {
            destFile.safeDelete()
            throw e
        }
    }

    private suspend fun downloadFile(
        uri: Uri,
        destFile: File,
    ) {
        val response = api.downloadFile(uri.toString())

        if (!response.isSuccessful) {
            Timber.e("Download core response was unsuccessful")
            throw Exception(response.errorBody()?.string() ?: "Download error")
        }

        val body = response.body() ?: throw IOException("Core response has no body: $uri")
        body.writeToFile(destFile)
        if (!destFile.isFile || destFile.length() == 0L) {
            throw IOException("Downloaded core is empty: $uri")
        }
    }

    private suspend fun findBundledLibrary(
        context: Context,
        coreID: CoreID,
    ): File? =
        withContext(Dispatchers.IO) {
            File(context.applicationInfo.nativeLibraryDir)
                .takeIf { it.exists() }
                ?.walkBottomUp()
                ?.firstOrNull {
                    it.isFile && it.name == coreID.libretroFileName && it.canRead() && it.length() > 0L
                }
        }

    private fun deleteOutdatedCores(
        mainCoresDirectory: File,
        applicationVersion: String,
    ) {
        mainCoresDirectory.listFiles()
            ?.filter { it.name != applicationVersion }
            ?.forEach { it.deleteRecursively() }
    }
}
