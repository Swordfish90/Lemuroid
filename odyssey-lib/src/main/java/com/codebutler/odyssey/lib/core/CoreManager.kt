/*
 * CoreManager.kt
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

package com.codebutler.odyssey.lib.core

import android.net.Uri
import android.os.Build
import com.codebutler.odyssey.common.http.OdysseyHttp
import com.codebutler.odyssey.common.http.OdysseyHttp.Response
import okio.Okio
import java.io.File

class CoreManager(private val http: OdysseyHttp, private val coresDir: File) {

    private val baseUri = Uri.parse("https://buildbot.libretro.com/")
    private val coresUri = baseUri.buildUpon()
            .appendEncodedPath("nightly/android/latest/")
            .appendPath(Build.SUPPORTED_ABIS.first())
            .build()

    init {
        coresDir.mkdirs()
    }

    fun downloadCore(zipFileName: String, callback: (response: Response<File>) -> Unit) {
        val libFileName = zipFileName.substringBeforeLast(".zip")
        val destFile = File(coresDir, "lib$libFileName")

         if (destFile.exists()) {
            callback(Response.Success(destFile))
            return
        }

        val uri = coresUri.buildUpon()
                .appendPath(zipFileName)
                .build()

        http.downloadZip(uri, { response ->
            when (response) {
                is Response.Success -> {
                    val zipStream = response.body
                    while (true) {
                        val entry = zipStream.nextEntry ?: break
                        if (entry.name == libFileName) {
                            Okio.source(zipStream).use { zipSource ->
                                Okio.sink(destFile).use { fileSink ->
                                    Okio.buffer(zipSource).readAll(fileSink)
                                    callback(Response.Success(destFile))
                                    return@downloadZip
                                }
                            }
                        }
                    }
                    callback(Response.Failure(Exception("Library not found in zip")))
                }
                is Response.Failure -> callback(Response.Failure(response.error))
            }
        })
    }
}
