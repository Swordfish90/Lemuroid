/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.appextension.roomcord

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import com.google.gson.Gson
import com.swordfish.lemuroid.app.appextension.remoteconfig.IRemoteConfigFetcher
import com.swordfish.lemuroid.app.appextension.remoteconfig.getRoomcordRoomId
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

class RoomcordImageUploader @Inject constructor(
    private val roomcordApiImpl: RoomcordApiImpl,
    private val remoteConfig: IRemoteConfigFetcher
) {

    private val downloadClient = OkHttpClient()
    private val gson = Gson()

    data class ShareImages(val screenshotUrl: String?, val coverUrl: String?)

    suspend fun uploadForShare(game: Game, screenshotPath: String?): ShareImages = withContext(Dispatchers.IO) {
        val roomId = remoteConfig.getRoomcordRoomId()
        if (roomId.isEmpty()) return@withContext ShareImages(null, null)

        val screenshotUrl = if (screenshotPath != null) {
            try {
                val file = File(screenshotPath)
                val bytes = file.readBytes()
                file.delete()
                uploadBytes(roomId, bytes)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to upload screenshot: ${e.message}")
                null
            }
        } else null

        val coverFrontUrl = game.coverFrontUrl
        val coverUrl = if (!coverFrontUrl.isNullOrEmpty()) {
            try {
                val bytes = readImageBytes(coverFrontUrl)
                if (bytes != null && bytes.isNotEmpty()) uploadBytes(roomId, bytes) else null
            } catch (e: Exception) {
                Log.w(TAG, "Failed to upload cover: ${e.message}")
                null
            }
        } else null

        ShareImages(screenshotUrl, coverUrl)
    }

    suspend fun uploadGameImage(game: Game): String? = withContext(Dispatchers.IO) {
        val roomId = remoteConfig.getRoomcordRoomId()
        if (roomId.isEmpty()) return@withContext null

        val bytes = downloadOrGeneratePlaceholder(game)
        uploadBytes(roomId, bytes)
    }

    private fun downloadOrGeneratePlaceholder(game: Game): ByteArray {
        val url = game.coverFrontUrl

        if (!url.isNullOrEmpty()) {
            try {
                val bytes = readImageBytes(url)
                if (bytes != null && bytes.isNotEmpty()) {
                    Log.d(TAG, "Loaded cover image: ${bytes.size} bytes from $url")
                    return bytes
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load cover image: ${e.message}")
            }
        }

        Log.d(TAG, "Generating placeholder for: ${game.title}")
        return generatePlaceholder(game)
    }

    private fun readImageBytes(url: String): ByteArray? {
        val uri = android.net.Uri.parse(url)
        return when (uri.scheme) {
            "file" -> {
                val file = java.io.File(uri.path ?: return null)
                if (file.exists()) file.readBytes() else null
            }
            "http", "https" -> {
                val safeUrl = url.replace("http://", "https://").replace(" ", "%20")
                val request = Request.Builder().url(safeUrl).build()
                val response = downloadClient.newCall(request).execute()
                if (response.isSuccessful) response.body?.bytes() else null
            }
            else -> null
        }
    }

    private fun generatePlaceholder(game: Game): ByteArray {
        val width = 400
        val height = 300
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = systemColor(game.systemId)
        canvas.drawColor(bgColor)

        // Dark overlay gradient at bottom
        val overlayPaint = Paint().apply {
            color = Color.argb(140, 0, 0, 0)
        }
        canvas.drawRect(RectF(0f, height * 0.5f, width.toFloat(), height.toFloat()), overlayPaint)

        // System badge (top-right)
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 0, 0, 0)
        }
        val badgeText = game.systemId.uppercase()
        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val badgeBounds = Rect()
        badgeTextPaint.getTextBounds(badgeText, 0, badgeText.length, badgeBounds)
        val badgePad = 10f
        val badgeLeft = width - badgeBounds.width() - badgePad * 2 - 8f
        val badgeTop = 8f
        val badgeRight = width - 8f
        val badgeBottom = badgeBounds.height() + badgePad * 2 + 8f
        canvas.drawRoundRect(RectF(badgeLeft, badgeTop, badgeRight, badgeBottom), 6f, 6f, badgePaint)
        canvas.drawText(
            badgeText,
            badgeLeft + badgePad,
            badgeBottom - badgePad,
            badgeTextPaint
        )

        // Game title (bottom area)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val maxWidth = width - 32f
        val title = ellipsize(game.title, titlePaint, maxWidth)
        canvas.drawText(title, 16f, height - 48f, titlePaint)

        // "FullRoid" watermark
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(160, 255, 255, 255)
            textSize = 20f
        }
        canvas.drawText("FullRoid", 16f, height - 16f, watermarkPaint)

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        bitmap.recycle()
        return out.toByteArray()
    }

    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) {
            end--
        }
        return text.substring(0, end) + ellipsis
    }

    private suspend fun uploadBytes(roomId: String, bytes: ByteArray): String? {
        return try {
            val mimeType = detectMimeType(bytes)
            val extension = if (mimeType == "image/png") "png" else "jpg"
            val requestBody = bytes.toRequestBody(mimeType.toMediaType())
            val part = MultipartBody.Part.createFormData("image", "cover.$extension", requestBody)
            val response = roomcordApiImpl.api?.uploadImage(roomId, part)
            val json = response?.string() ?: return null
            Log.d(TAG, "Upload response: $json")
            gson.fromJson(json, UploadResponse::class.java)?.url
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload image: ${e.message}")
            null
        }
    }

    private fun detectMimeType(bytes: ByteArray): String {
        if (bytes.size < 4) return "image/jpeg"
        return when {
            bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte() -> "image/jpeg"
            bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte() -> "image/png"
            bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x38.toByte() -> "image/gif"
            else -> "image/jpeg"
        }
    }

    private fun systemColor(systemId: String): Int {
        return when (systemId.lowercase()) {
            "nes" -> Color.rgb(140, 40, 40)
            "snes" -> Color.rgb(60, 60, 140)
            "gba", "gb", "gbc" -> Color.rgb(90, 50, 130)
            "gba" -> Color.rgb(110, 60, 150)
            "n64" -> Color.rgb(40, 100, 60)
            "nds" -> Color.rgb(180, 80, 30)
            "psx" -> Color.rgb(30, 60, 130)
            "psp" -> Color.rgb(0, 70, 140)
            "genesis", "megadrive" -> Color.rgb(30, 30, 80)
            "mame2003plus", "fbneo" -> Color.rgb(60, 30, 10)
            else -> Color.rgb(40, 40, 60)
        }
    }

    companion object {
        private const val TAG = "RoomcordImageUploader"
    }
}
