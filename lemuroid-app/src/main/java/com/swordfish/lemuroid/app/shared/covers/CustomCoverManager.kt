package com.swordfish.lemuroid.app.shared.covers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.IOException
import kotlin.math.min

object CustomCoverManager {
    private const val DIRECTORY_NAME = "custom_covers"
    private const val MAX_DIMENSION = 512
    private const val JPEG_QUALITY = 90

    fun save(context: Context, gameId: Int, uri: Uri): String? {
        val directory = File(context.filesDir, DIRECTORY_NAME)
        if (!directory.exists() && !directory.mkdirs()) return null

        val target = File(directory, "$gameId.jpg")
        val temporary = File(directory, "$gameId.tmp")
        return try {
            val bitmap = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: return null
            val square = cropToSquare(bitmap)
            val scaled = scaleDown(square)
            val compressed = temporary.outputStream().use { output ->
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            }
            if (scaled !== square) scaled.recycle()
            if (square !== bitmap) square.recycle()
            bitmap.recycle()
            if (!compressed) {
                temporary.delete()
                null
            } else if (!temporary.renameTo(target)) {
                temporary.delete()
                null
            } else {
                target.absolutePath
            }
        } catch (_: IOException) {
            temporary.delete()
            null
        } catch (_: SecurityException) {
            temporary.delete()
            null
        }
    }

    fun delete(path: String?) {
        path?.let { File(it).delete() }
    }

    fun isUsable(path: String?): Boolean = path?.let { File(it).isFile } == true

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val left = (bitmap.width - size) / 2
        val top = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, left, top, size, size)
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        if (bitmap.width <= MAX_DIMENSION) return bitmap
        return Bitmap.createScaledBitmap(bitmap, MAX_DIMENSION, MAX_DIMENSION, true)
    }
}

fun Context.customCoverDirectory(): File = File(filesDir, "custom_covers")
