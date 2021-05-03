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
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Retrofit
import timber.log.Timber
import java.io.File

class CoreUpdaterImpl(
    private val directoriesManager: DirectoriesManager,
    retrofit: Retrofit
) : CoreUpdater {

    // This is the last tagged versions of cores.
    companion object {
        private val CORES_VERSION = "1.11"
    }

    private val baseUri = Uri.parse("https://github.com/Swordfish90/LemuroidCores/")

    private val api = retrofit.create(CoreUpdater.CoreManagerApi::class.java)

    override fun downloadCores(context: Context, coreIDs: List<CoreID>): Completable {
        val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(context.applicationContext)
        return Observable.fromIterable(coreIDs)
            .flatMapCompletable { coreId ->
                CoreID.getAssetManager(coreId)
                    .retrieveAssetsIfNeeded(api, directoriesManager, sharedPreferences)
                    .andThen(findBundledLibrary(context, coreId))
                    .switchIfEmpty(downloadCoreFromGithub(coreId))
                    .ignoreElement()
            }
    }

    private fun downloadCoreFromGithub(coreID: CoreID): Single<File> {
        Timber.i("Downloading core $coreID from github")

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

        val uri = baseUri.buildUpon()
            .appendEncodedPath("raw/$CORES_VERSION/lemuroid_core_${coreID.coreName}/src/main/jniLibs/")
            .appendPath(Build.SUPPORTED_ABIS.first())
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

    private fun findBundledLibrary(context: Context, coreID: CoreID) = Maybe.fromCallable {
        File(context.applicationInfo.nativeLibraryDir)
            .walkBottomUp()
            .firstOrNull { it.name == coreID.libretroFileName }
    }

    private fun deleteOutdatedCores(mainCoresDirectory: File, applicationVersion: String) {
        mainCoresDirectory.listFiles()
            ?.filter { it.name != applicationVersion }
            ?.forEach { it.deleteRecursively() }
    }
}
