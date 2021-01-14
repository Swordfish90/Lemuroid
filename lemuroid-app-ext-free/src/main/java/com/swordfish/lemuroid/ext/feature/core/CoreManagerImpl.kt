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

package com.swordfish.lemuroid.ext.feature.core

import android.content.Context
import android.net.Uri
import android.os.Build
import com.swordfish.lemuroid.common.files.safeDelete
import com.swordfish.lemuroid.common.kotlin.writeToFile
import com.swordfish.lemuroid.lib.core.CoreManager
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Single
import retrofit2.Retrofit
import timber.log.Timber
import java.io.File

class CoreManagerImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit
) : CoreManager {

    // This is the last tagged versions of cores.
    companion object {
        private val CORES_VERSION = "1.9.0-beta1"
    }

    private val baseUri = Uri.parse("https://github.com/")
    private val coresUri = baseUri.buildUpon()
        .appendEncodedPath("Swordfish90/LemuroidCores/raw/$CORES_VERSION/src/main/jniLibs/")
        .appendPath(Build.SUPPORTED_ABIS.first())
        .build()

    private val api = retrofit.create(CoreManager.CoreManagerApi::class.java)

    override fun downloadCore(
        context: Context,
        coreID: CoreID,
        assetsManager: CoreManager.AssetsManager
    ): Single<String> {
        return assetsManager.retrieveAssetsIfNeeded(api, directoriesManager)
            .andThen(downloadCoreFromGithub(coreID).map { it.absolutePath })
    }

    private fun downloadCoreFromGithub(coreID: CoreID): Single<File> {
        val mainCoresDirectory = directoriesManager.getCoresDirectory()
        val coresDirectory = File(mainCoresDirectory, CORES_VERSION).apply {
            mkdirs()
        }

        val libFileName = coreID.libretroFileName
        val destFile = File(coresDirectory, libFileName)

        if (destFile.exists()) {
            return Single.just(destFile)
        }

        runCatching {
            deleteOutdatedCores(mainCoresDirectory, CORES_VERSION)
        }

        val uri = coresUri.buildUpon()
            .appendPath(libFileName)
            .build()

        return api.downloadFile(uri.toString())
            .map { response ->
                if (!response.isSuccessful) {
                    Timber.e("Download core response was unsuccessful")
                    throw Exception(response.errorBody()!!.string())
                }
                val fileStream = response.body()!!
                fileStream.writeToFile(destFile)
                destFile
            }
            .doOnError { destFile.safeDelete() }
    }

    private fun deleteOutdatedCores(mainCoresDirectory: File, applicationVersion: String) {
        mainCoresDirectory.listFiles()
            ?.filter { it.name != applicationVersion }
            ?.forEach { it.deleteRecursively() }
    }
}
