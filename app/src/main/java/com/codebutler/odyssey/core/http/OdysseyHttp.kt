/*
 * OdysseyHttp.kt
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

package com.codebutler.odyssey.core.http

import android.net.Uri
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream

// FIXME: Replace all this with Retrofit/RxJava/etc
class OdysseyHttp(private val client: OkHttpClient) {

    companion object {
        private const val TAG = "OdysseyHttp"
    }

    sealed class Response<T> {
        class Success<T>(val body: T): Response<T>()
        class Failure<T>(val error: Exception): Response<T>()
    }

    fun download(uri: Uri, callback: (response: Response<InputStream>) -> Unit) {
        val request = Request.Builder()
                .url(uri.toString())
                .build()

        Log.d(TAG, "Download: $uri")

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                try {
                    if (!response.isSuccessful) {
                        callback(Response.Failure(Exception("Code: ${response.code()}")))
                        return
                    }
                    val source = response.body()!!.source()
                    Okio.buffer(source).use { buffer ->
                        callback(Response.Success(buffer.inputStream()))
                    }
                } catch (e: Exception) {
                    callback(Response.Failure(e))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(Response.Failure(e))
            }
        })
    }

    fun downloadZip(uri: Uri, callback: (response: Response<ZipInputStream>) -> Unit) {
        download(uri, { response ->
            when (response) {
                is Response.Success<InputStream> -> {
                    ZipInputStream(response.body).use { zipStream ->
                        callback(Response.Success(zipStream))
                    }
                }
                is Response.Failure -> callback(Response.Failure(response.error))
            }
        })
    }
}
