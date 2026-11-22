package com.swordfish.lemuroid.lib.scraper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class MediaStorageManager(
    private val context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "AlphaLemuroid")
                .build()
            chain.proceed(request)
        }
        .dispatcher(
            Dispatcher().apply {
                maxRequests = 64
                maxRequestsPerHost = 25
            },
        )
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build(),
) {
    enum class MediaType(val folderName: String) {
        BOXART("boxart"),
        BOXART_BACK("boxart_back"),
        BANNERS("banners"),
        FANART("fanart"),
        LOGOS("logos"),
        SCREENSHOTS("screenshots"),
        TITLES("titles"),
        VIDEOS("videos"),
    }

    val rootMediaDir: File by lazy {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        File(base, "media").apply { mkdirs() }
    }

    fun getSystemDir(systemId: String): File {
        val cleanSystem = sanitizeFilename(systemId.lowercase())
        return File(rootMediaDir, cleanSystem).apply { mkdirs() }
    }

    fun getCategoryDir(systemId: String, type: MediaType): File {
        return File(getSystemDir(systemId), type.folderName).apply { mkdirs() }
    }

    fun getRomMediaDir(systemId: String, type: MediaType, romBasename: String): File {
        val cleanRom = sanitizeFilename(romBasename)
        return File(getCategoryDir(systemId, type), cleanRom).apply { mkdirs() }
    }

    fun deleteMediaForGame(systemId: String, romBasename: String) {
        val cleanSystem = sanitizeFilename(systemId.lowercase())
        val systemDir = File(rootMediaDir, cleanSystem)
        if (!systemDir.exists()) return
        val cleanRom = sanitizeFilename(romBasename)

        MediaType.values().forEach { type ->
            val categoryDir = File(systemDir, type.folderName)
            if (!categoryDir.exists()) return@forEach

            when (type) {
                MediaType.SCREENSHOTS, MediaType.TITLES -> {
                    val romDir = File(categoryDir, cleanRom)
                    if (romDir.exists()) {
                        romDir.deleteRecursively()
                    }
                }
                else -> {
                    categoryDir.listFiles()?.forEach { file ->
                        if (file.isFile && (file.name == cleanRom || file.name.startsWith("$cleanRom."))) {
                            file.delete()
                        }
                    }
                }
            }
        }
    }

    fun getTargetFile(
        systemId: String,
        type: MediaType,
        romBasename: String,
        index: Int? = null,
        extension: String = "webp",
    ): File {
        val cleanRom = sanitizeFilename(romBasename)
        return when (type) {
            MediaType.SCREENSHOTS -> {
                val dir = getRomMediaDir(systemId, type, cleanRom)
                val idxStr = index?.let { String.format(java.util.Locale.ROOT, "%02d", it) } ?: "01"
                File(dir, "ss_$idxStr.$extension")
            }
            MediaType.TITLES -> {
                val dir = getRomMediaDir(systemId, type, cleanRom)
                val idxStr = index?.let { String.format(java.util.Locale.ROOT, "%02d", it) } ?: "01"
                File(dir, "title_$idxStr.$extension")
            }
            MediaType.VIDEOS -> {
                File(getCategoryDir(systemId, type), "$cleanRom.mp4")
            }
            else -> {
                File(getCategoryDir(systemId, type), "$cleanRom.$extension")
            }
        }
    }

    suspend fun saveOrDownloadMedia(
        url: String,
        systemId: String,
        type: MediaType,
        romBasename: String,
        downloadedUrlMap: MutableMap<String, File>,
        index: Int? = null,
    ): File? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null

        downloadedUrlMap[url]?.let { existingFile ->
            if (existingFile.exists() && existingFile.length() > 0) {
                return@withContext existingFile
            }
        }

        val isVideo = type == MediaType.VIDEOS
        val targetFile = getTargetFile(
            systemId = systemId,
            type = type,
            romBasename = romBasename,
            index = index,
            extension = if (isVideo) "mp4" else "webp",
        )

        if (targetFile.exists() && targetFile.length() > 0) {
            downloadedUrlMap[url] = targetFile
            return@withContext targetFile
        }

        if (!isVideo) {
            val fallbackPng = File(targetFile.parentFile, targetFile.nameWithoutExtension + ".png")
            val fallbackJpg = File(targetFile.parentFile, targetFile.nameWithoutExtension + ".jpg")
            if (fallbackPng.exists() && fallbackPng.length() > 0) {
                downloadedUrlMap[url] = fallbackPng
                return@withContext fallbackPng
            }
            if (fallbackJpg.exists() && fallbackJpg.length() > 0) {
                downloadedUrlMap[url] = fallbackJpg
                return@withContext fallbackJpg
            }
        }

        val downloadedBytes = runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AlphaLemuroid")
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                response.body?.bytes()
            }
        }.getOrNull() ?: return@withContext null

        if (downloadedBytes.isEmpty()) return@withContext null

        val finalFile = if (isVideo) {
            saveBytesAtomically(downloadedBytes, targetFile)
        } else {
            saveImageWithAdaptiveCompression(downloadedBytes, targetFile)
        }

        if (finalFile != null && finalFile.exists() && finalFile.length() > 0) {
            downloadedUrlMap[url] = finalFile
        }
        finalFile
    }

    private fun isWebp(bytes: ByteArray): Boolean {
        return bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()
    }

    private fun saveImageWithAdaptiveCompression(rawBytes: ByteArray, targetWebpFile: File): File? {
    
        if (isWebp(rawBytes)) {
            return saveBytesAtomically(rawBytes, targetWebpFile)
        }

        val bitmap = runCatching {
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
        }.getOrNull()

        if (bitmap == null) {
    
            return saveBytesAtomically(rawBytes, targetWebpFile)
        }

        val webpBytes = try {
            ByteArrayOutputStream().use { stream ->
                val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                val success = bitmap.compress(format, 80, stream)
                if (success) stream.toByteArray() else null
            }
        } finally {
            bitmap.recycle()
        }

        return if (webpBytes != null && webpBytes.isNotEmpty()) {
            saveBytesAtomically(webpBytes, targetWebpFile)
        } else {
            val rawFile = File(targetWebpFile.parentFile, targetWebpFile.nameWithoutExtension + ".png")
            saveBytesAtomically(rawBytes, rawFile)
        }
    }

    private fun saveBytesAtomically(bytes: ByteArray, destination: File): File? {
        destination.parentFile?.mkdirs()
        val tempFile = File(destination.parentFile, "${destination.name}.tmp")
        return try {
            FileOutputStream(tempFile).use { it.write(bytes) }
            if (tempFile.renameTo(destination)) {
                destination
            } else {
                tempFile.copyTo(destination, overwrite = true)
                tempFile.delete()
                destination
            }
        } catch (e: Throwable) {
            tempFile.delete()
            null
        }
    }

    fun toFileUri(file: File?): String? {
        if (file == null || !file.exists() || file.length() == 0L) return null
        return Uri.fromFile(file).toString()
    }

    fun isMediaUriValid(uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        return if (uriString.startsWith("file://")) {
            runCatching {
                val path = Uri.parse(uriString).path
                !path.isNullOrBlank() && File(path).let { it.exists() && it.length() > 0L }
            }.getOrDefault(false)
        } else {
            true
        }
    }

    fun hasLocalMedia(systemId: String, type: MediaType, romBasename: String): Boolean {
        val cleanSystem = sanitizeFilename(systemId.lowercase())
        val categoryDir = File(File(rootMediaDir, cleanSystem), type.folderName)
        if (!categoryDir.exists()) return false
        val cleanRom = sanitizeFilename(romBasename)
        return when (type) {
            MediaType.SCREENSHOTS, MediaType.TITLES -> {
                val romDir = File(categoryDir, cleanRom)
                romDir.exists() && romDir.listFiles()?.any { it.isFile && it.length() > 0L } == true
            }
            else -> {
                categoryDir.listFiles()?.any { file ->
                    file.isFile && file.length() > 0L &&
                        (file.name == cleanRom || file.name.startsWith("$cleanRom."))
                } == true
            }
        }
    }

    fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._\\-()]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
    }

    data class MediaStorageStats(
        val totalBytes: Long = 0L,
        val fileCount: Int = 0,
        val perSystemBytes: Map<String, Long> = emptyMap(),
        val perSystemTypeBytes: Map<String, Map<MediaType, Long>> = emptyMap(),
        val perTypeBytes: Map<MediaType, Long> = emptyMap(),
    )

    fun calculateStorageStats(): MediaStorageStats {
        if (!rootMediaDir.exists()) return MediaStorageStats()
        var totalBytes = 0L
        var fileCount = 0
        val perSystem = linkedMapOf<String, Long>()
        val perSystemType = linkedMapOf<String, Map<MediaType, Long>>()
        val perType = linkedMapOf<MediaType, Long>()

        rootMediaDir.listFiles()?.forEach { systemDir ->
            if (systemDir.isDirectory) {
                var sysBytes = 0L
                systemDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        val len = file.length()
                        sysBytes += len
                        totalBytes += len
                        fileCount++
                    }
                }
                if (sysBytes > 0L) {
                    perSystem[systemDir.name] = sysBytes
                    val typeMap = linkedMapOf<MediaType, Long>()
                    MediaType.values().forEach { type ->
                        val typeDir = File(systemDir, type.folderName)
                        if (typeDir.isDirectory) {
                            val bytes = typeDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                            if (bytes > 0L) {
                                typeMap[type] = bytes
                                perType[type] = (perType[type] ?: 0L) + bytes
                            }
                        }
                    }
                    perSystemType[systemDir.name] =
                        typeMap.entries
                            .sortedByDescending { it.value }
                            .associate { it.key to it.value }
                }
            } else if (systemDir.isFile) {
                totalBytes += systemDir.length()
                fileCount++
            }
        }

        val sortedPerSystem = perSystem.entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }

        return MediaStorageStats(
            totalBytes = totalBytes,
            fileCount = fileCount,
            perSystemBytes = sortedPerSystem,
            perSystemTypeBytes = perSystemType,
            perTypeBytes = perType.entries.sortedByDescending { it.value }.associate { it.key to it.value },
        )
    }


    fun clearMediaType(systemId: String?, type: MediaType): Boolean {
        if (!rootMediaDir.exists()) return true
        val systemDirs = if (systemId != null) {
            listOf(File(rootMediaDir, sanitizeFilename(systemId.lowercase())))
        } else {
            rootMediaDir.listFiles()?.filter { it.isDirectory }.orEmpty()
        }
        var allDeleted = true
        systemDirs.forEach { systemDir ->
            val typeDir = File(systemDir, type.folderName)
            if (typeDir.exists() && !typeDir.deleteRecursively()) {
                allDeleted = false
            }
        }
        return allDeleted
    }

    fun clearAllMedia(): Boolean {
        if (!rootMediaDir.exists()) return true
        var allDeleted = true
        rootMediaDir.listFiles()?.forEach { child ->
            if (!child.deleteRecursively()) {
                allDeleted = false
            }
        }
        return allDeleted
    }

    fun clearSystemMedia(systemId: String): Boolean {
        val cleanSystem = sanitizeFilename(systemId.lowercase())
        val systemDir = File(rootMediaDir, cleanSystem)
        return !systemDir.exists() || systemDir.deleteRecursively()
    }
}
