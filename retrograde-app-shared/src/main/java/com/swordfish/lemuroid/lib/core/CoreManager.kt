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

package com.swordfish.lemuroid.lib.core

import android.net.Uri
import android.os.Build
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Single
import okio.buffer
import okio.sink
import okio.source
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream

class CoreManager(private val directoriesManager: DirectoriesManager, retrofit: Retrofit) {

    private val baseUri = Uri.parse("https://buildbot.libretro.com/")
    private val coresUri = baseUri.buildUpon()
            .appendEncodedPath("nightly/android/latest/")
            .appendPath(Build.SUPPORTED_ABIS.first())
            .build()

    private val api = retrofit.create(CoreManagerApi::class.java)

    private val coresDir = directoriesManager.getCoresDirectory()

    init {
        coresDir.mkdirs()
    }

    fun downloadCore(zipFileName: String, assetsManager: AssetsManager): Single<File> {
        val libFileName = zipFileName.substringBeforeLast(".zip")
        val destFile = File(coresDir, "lib$libFileName")

        if (destFile.exists() && isUpdated(destFile)) {
            return Single.just(destFile)
        }

        Timber.d("Downloading core for system")

        assetsManager.clearAssets(directoriesManager).blockingAwait()

        val uri = coresUri.buildUpon()
                .appendPath(zipFileName)
                .build()

        return api.downloadZip(uri.toString())
                .doOnSuccess { assetsManager.retrieveAssets(api, directoriesManager).blockingAwait() }
                .map { response ->
                    if (!response.isSuccessful) {
                        throw Exception(response.errorBody()!!.string())
                    }
                    val zipStream = response.body()!!
                    while (true) {
                        val entry = zipStream.nextEntry ?: break
                        if (entry.name == libFileName) {
                            zipStream.source().use { zipSource ->
                                destFile.sink().use { fileSink ->
                                    zipSource.buffer().readAll(fileSink)
                                    return@map destFile
                                }
                            }
                        }
                    }
                    throw Exception("Library not found in zip")
                }
    }

    private fun isUpdated(file: File): Boolean {
        // val oldestAllowedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(OLDEST_CORE_DATE)
        // return file.lastModified() >= oldestAllowedDate?.time ?: 0
        return true
    }

    interface CoreManagerApi {

        @GET
        @Streaming
        fun downloadZip(@Url url: String): Single<Response<ZipInputStream>>
    }

    interface AssetsManager {
        fun retrieveAssets(coreManagerApi: CoreManagerApi, directoriesManager: DirectoriesManager): Completable
        fun clearAssets(directoriesManager: DirectoriesManager): Completable
    }

    companion object {
        // Here we can force the core update. (YYYY-MM-DD)
        private const val OLDEST_CORE_DATE = "2020-05-04"
    }
}
