package com.codebutler.odyssey.feature.core

import android.net.Uri
import android.os.Build
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Okio
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream

class CoreManager(private val coresDir: File) {

    companion object {
        private const val TAG = "CoreManager"
    }

    private val baseUri = Uri.parse("https://buildbot.libretro.com/nightly/android/latest/")

    private val client = OkHttpClient()

    init {
        coresDir.mkdirs()
    }

    fun downloadCore(name: String, callback: ((coreFile: File?) -> Unit)) {
        val destFile = File(coresDir, "lib$name.so") // FIXME

        Log.d(TAG, "downloadCore: $name, exists: ${destFile.exists()}")

        if (destFile.exists()) {
            callback(destFile)
            return
        }

        val request = Request.Builder()
                .url(baseUri.buildUpon()
                        .appendPath(Build.SUPPORTED_ABIS.first())
                        .appendPath("$name.so.zip") // FIXME
                        .build().toString())
                .build()

        Log.d(TAG, "Downloading: ${request.url()}")

        // FIXME: Clean up this mess
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "Got Response: ${response.code()}")
                val source = response.body()!!.source()
                Okio.buffer(source).use { buffer ->
                    ZipInputStream(buffer.inputStream()).use { zipStream ->
                        while (true) {
                            val entry = zipStream.nextEntry ?: break
                            Log.d(TAG, "Found zip entry: ${entry.name}")
                            if (entry.name.startsWith(name)) {
                                Okio.source(zipStream).use { zipSource ->
                                    Okio.sink(destFile).use { fileSink ->
                                        Okio.buffer(zipSource).readAll(fileSink)
                                        callback(destFile)
                                        return
                                    }
                                }
                            }
                        }
                    }
                }
                callback(null)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "Failed to download: $name", e)
                callback(null)
            }
        })
    }
}
