package com.swordfish.lemuroid.lib.citra

import android.content.Context
import android.net.Uri
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Citra3DSKeysManager(private val directoriesManager: DirectoriesManager) {

    fun keysFile(): File =
        File(directoriesManager.getSavesDirectory(), "Fullroid/sysdata/keys.txt")

    fun keysPresent(): Boolean = keysFile().exists() && keysFile().length() > 0

    suspend fun installFromUri(
        context: Context,
        uri: Uri,
    ) {
        withContext(Dispatchers.IO) {
            val dest = keysFile()
            dest.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IOException("Cannot open selected file")
            validateKeysFile(dest)
        }
    }

    suspend fun installFromUrl(url: String) {
        withContext(Dispatchers.IO) {
            val dest = keysFile()
            dest.parentFile?.mkdirs()
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 60_000
            try {
                connection.connect()
                val code = connection.responseCode
                if (code !in 200..299) {
                    throw IOException("Server returned HTTP $code")
                }
                connection.inputStream.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
                validateKeysFile(dest)
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun deleteKeys() =
        withContext(Dispatchers.IO) {
            keysFile().delete()
        }

    private fun validateKeysFile(file: File) {
        val header = ByteArray(10_240)
        val len = file.inputStream().use { it.read(header) }
        val text = String(header, 0, maxOf(len, 0), Charsets.UTF_8)
        if (!text.contains(":AES")) {
            file.delete()
            throw IOException("This does not appear to be a valid keys.txt file")
        }
    }
}
