/*
 * FileKt.kt
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

package com.swordfish.lemuroid.common.kotlin

import android.util.AtomicFile
import androidx.documentfile.provider.DocumentFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.PushbackInputStream
import java.security.MessageDigest
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipInputStream

private const val CRC32_BYTE_ARRAY_SIZE = 16 * 1024
private const val GZIP_INPUT_STREAM_BUFFER_SIZE = 8 * 1024

fun InputStream.calculateCrc32(): String =
    this.use { fileStream ->
        val buffer = ByteArray(CRC32_BYTE_ARRAY_SIZE)
        return CheckedInputStream(fileStream, CRC32()).use { crcStream ->
            while (crcStream.read(buffer) != -1) {
                // Read file in completely
            }
            crcStream.checksum.value.toStringCRC32()
        }
    }

private val NON_ROM_ZIP_EXTENSIONS =
    setOf(
        "txt", "nfo", "md", "diz", "jpg", "jpeg", "png", "bmp", "gif", "xml", "pdf", "html", "htm",
        "ips", "ups", "bps", "xdelta", "cht", "sav", "srm", "state", "url", "cue"
    )

fun InputStream.calculateRomCrc32(
    fileName: String,
    isArcadeZip: Boolean = false,
    isZipArchive: Boolean = false,
    targetEntryName: String? = null,
): String {
    val isZipped = isZipArchive || fileName.endsWith(".zip", ignoreCase = true)
    if (isArcadeZip || !isZipped) {
        return calculateCrc32().uppercase()
    }

    return this.use { rawStream ->
        val buffer = ByteArray(CRC32_BYTE_ARRAY_SIZE)
        var bestCrc: String? = null
        var bestSize = -1L

        ZipInputStream(rawStream).use { zipStream ->
            var entry = zipStream.nextEntry
            while (entry != null) {
                val entryName = entry.name
                val ext = entryName.substringAfterLast('.', "").lowercase()
                val isIgnored =
                    entry.isDirectory ||
                        entryName.startsWith("__MACOSX/", ignoreCase = true) ||
                        entryName.contains("/__MACOSX/", ignoreCase = true) ||
                        ext in NON_ROM_ZIP_EXTENSIONS

                if (!isIgnored) {
                    val matchesTarget = targetEntryName != null &&
                        (entryName == targetEntryName || entryName.substringAfterLast('/') == targetEntryName)

                    val crc = CRC32()
                    var entryBytes = 0L
                    var read = zipStream.read(buffer)
                    while (read != -1) {
                        crc.update(buffer, 0, read)
                        entryBytes += read
                        read = zipStream.read(buffer)
                    }

                    if (matchesTarget) {
                        return@use crc.value.toStringCRC32().uppercase()
                    }

                    if (entryBytes > bestSize) {
                        bestSize = entryBytes
                        bestCrc = crc.value.toStringCRC32().uppercase()
                    }
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
        }
        bestCrc ?: ""
    }
}

fun File.calculateMd5(): String {
    val bytes =
        MessageDigest
            .getInstance("MD5")
            .digest(this.readBytes())
    return bytes.toHexString()
}

fun InputStream.writeToFile(file: File) {
    this.use { inputStream ->
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
}

fun ZipInputStream.extractEntryToFile(
    entryName: String,
    gameFile: File,
) {
    this.use { inputStream ->
        while (true) {
            val entry = inputStream.nextEntry
            if (entry.name == entryName) break
        }
        inputStream.writeToFile(gameFile)
    }
}

fun File.isZipped() = extension == "zip"

fun DocumentFile.isZipped() = type == "application/zip"

private fun InputStream.uncompressedInputStream(): InputStream {
    val pb = PushbackInputStream(this, 2)
    val signature = ByteArray(2)
    val len = pb.read(signature)
    pb.unread(signature, 0, len)
    return if (signature[0] == 0x1f.toByte() && signature[1] == 0x8b.toByte()) {
        GZIPInputStream(pb, GZIP_INPUT_STREAM_BUFFER_SIZE)
    } else {
        pb
    }
}

private fun readBytesUncompressedStream(inputStream: InputStream): ByteArray =
    inputStream.use { input ->
        val b = ByteArray(GZIP_INPUT_STREAM_BUFFER_SIZE)
        val os = ByteArrayOutputStream()
        os.use { usedOutputStream ->
            var c: Int
            while (input.read(b).also { c = it } != -1) {
                usedOutputStream.write(b, 0, c)
            }
        }
        return os.toByteArray()
    }

fun File.writeBytesAtomic(data: ByteArray) {
    val atomicFile = AtomicFile(this)
    val outputStream = atomicFile.startWrite()
    try {
        outputStream.use { it.write(data) }
        atomicFile.finishWrite(outputStream)
    } catch (e: Throwable) {
        atomicFile.failWrite(outputStream)
        throw e
    }
}

fun File.readTextAtomic(): String {
    val atomicFile = AtomicFile(this)
    return atomicFile.openRead().bufferedReader().use { it.readText() }
}

fun File.writeTextAtomic(text: String) {
    val atomicFile = AtomicFile(this)
    val outputStream = atomicFile.startWrite()
    try {
        outputStream.writer().use { it.write(text) }
        atomicFile.finishWrite(outputStream)
    } catch (e: Throwable) {
        atomicFile.failWrite(outputStream)
        throw e
    }
}

fun File.writeBytesCompressedAtomic(array: ByteArray) {
    val atomicFile = AtomicFile(this)
    val outputStream = atomicFile.startWrite()
    try {
        ByteArrayInputStream(array).use { input ->
            GZIPOutputStream(outputStream).use { usedOutput ->
                input.copyTo(usedOutput)
            }
        }
        atomicFile.finishWrite(outputStream)
    } catch (e: Throwable) {
        atomicFile.failWrite(outputStream)
        throw e
    }
}

fun File.readBytesUncompressedAtomic(): ByteArray {
    val atomicFile = AtomicFile(this)
    return atomicFile.openRead().use { input ->
        readBytesUncompressedStream(input.uncompressedInputStream())
    }
}
