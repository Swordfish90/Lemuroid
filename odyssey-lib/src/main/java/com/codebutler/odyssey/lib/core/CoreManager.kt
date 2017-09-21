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
import com.codebutler.odyssey.lib.core.model.CoreFileInfo
import com.codebutler.odyssey.lib.core.model.CoreInfo
import com.codebutler.odyssey.lib.core.model.CoreMetadata
import okio.Okio
import java.io.File
import java.io.InputStreamReader

class CoreManager(private val http: OdysseyHttp, private val coresDir: File) {

    private val baseUri = Uri.parse("https://buildbot.libretro.com/")
    private val infoZipUri = baseUri.buildUpon().appendEncodedPath("assets/frontend/info.zip").build()
    private val coresUri = baseUri.buildUpon()
            .appendEncodedPath("nightly/android/latest/")
            .appendPath(Build.SUPPORTED_ABIS.first())
            .build()
    private val coresIndexUri = coresUri.buildUpon().appendEncodedPath(".index-extended").build()

    init {
        coresDir.mkdirs()
    }

    fun downloadAllCoreInfo(callback: (response: Response<List<CoreInfo>>) -> Unit) {
        // FIXME: Cache result to disk
        downloadCoreIndex { indexResponse ->
            when (indexResponse) {
                is Response.Success -> downloadCoreMetadata { metadataResponse ->
                    when (metadataResponse) {
                        is Response.Success -> {
                            val index = indexResponse.body
                            val metadata = metadataResponse.body
                            val coreInfoList = index
                                    .filter { coreFileInfo -> metadata.containsKey(coreFileInfo.coreName) }
                                    .map { coreFileInfo -> CoreInfo(coreFileInfo, metadata[coreFileInfo.coreName]!!) }
                            callback(Response.Success(coreInfoList))
                        }
                        is Response.Failure -> callback(Response.Failure(metadataResponse.error))
                    }
                }
                is Response.Failure -> callback(Response.Failure(indexResponse.error))
            }
        }
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

    private fun downloadCoreIndex(callback: (response: Response<List<CoreFileInfo>>) -> Unit) {
        http.download(coresIndexUri, { response ->
            when (response) {
                is Response.Success -> {
                    val stream = response.body
                    val coreFileInfos = InputStreamReader(stream).readText().lines()
                            .filter { line -> line.isNotEmpty() }
                            .map { line -> CoreFileInfo.parseText(line) }
                    callback(Response.Success(coreFileInfos))
                }
                is Response.Failure -> callback(Response.Failure(response.error))
            }
        })
    }

    private fun downloadCoreMetadata(callback: (response: Response<Map<String, CoreMetadata>>) -> Unit) {
        http.downloadZip(infoZipUri, { response ->
            when (response) {
                is Response.Success -> {
                    val zipStream = response.body
                    val metadataMap = mutableMapOf<String, CoreMetadata>()
                    while (true) {
                        val entry = zipStream.nextEntry ?: break
                        val name = entry.name.substringBefore(".")
                        val text = zipStream.bufferedReader().readText()
                        val info = CoreMetadata.parseInfoFile(text)
                        metadataMap[name] = info
                    }
                    callback(Response.Success(metadataMap.toMap()))
                }
                is Response.Failure -> callback(Response.Failure(response.error))
            }
        })
    }
}
