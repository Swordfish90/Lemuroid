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

package come.asg.lemudroid.common.kotlin

import android.util.AtomicFile
import androidx.documentfile.provider.DocumentFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PushbackInputStream
import java.security.MessageDigest
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipInputStream

private const val CRC32_BYTE_ARRAY_SIZE = 16 * 1024
private const val GZIP_INPUT_STREAM_BUFFER_SIZE = 8 * 1024
private const val MAX_ZIP_ENTRIES = 10_000
private const val MAX_ZIP_ENTRY_SIZE = 512L * 1024 * 1024
private const val MAX_ZIP_TOTAL_SIZE = 1L * 1024 * 1024 * 1024
private const val MAX_ZIP_COMPRESSION_RATIO = 1_000L

private class NonClosingOutputStream(outputStream: OutputStream) : FilterOutputStream(outputStream) {
    override fun close() {
        flush()
    }
}

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

fun File.calculateMd5(): String {
    val bytes =
        MessageDigest
            .getInstance("MD5")
            .digest(this.readBytes())
    return bytes.toHexString()
}

fun InputStream.writeToFile(file: File) {
    val atomicFile = AtomicFile(file)
    val outputStream = atomicFile.startWrite()
    try {
        this.use { inputStream ->
            outputStream.use { inputStream.copyTo(it) }
        }
        atomicFile.finishWrite(outputStream)
    } catch (e: Throwable) {
        atomicFile.failWrite(outputStream)
        throw e
    }
}

fun ZipInputStream.extractEntryToFile(
    entryName: String,
    gameFile: File,
) {
    this.use { inputStream ->
        var entries = 0
        var entry = inputStream.nextEntry
        while (entry != null && entry.name != entryName) {
            if (++entries > MAX_ZIP_ENTRIES) throw IOException("ZIP contains too many entries")
            entry = inputStream.nextEntry
        }
        if (entry == null) throw IOException("ZIP entry not found: $entryName")
        validateZipEntry(entry, 0)
        inputStream.copyZipEntryToFile(gameFile, entry.size, 0)
    }
}

/** Extracts an archive while keeping every output path below [directory]. */
fun ZipInputStream.extractEntriesToDirectory(directory: File) {
    val canonicalDirectory = directory.canonicalFile
    if (!canonicalDirectory.exists() && !canonicalDirectory.mkdirs()) {
        throw IOException("Unable to create ZIP destination: $directory")
    }
    var entryCount = 0
    var totalBytes = 0L
    use { inputStream ->
        while (true) {
            val entry = inputStream.nextEntry ?: break
            if (++entryCount > MAX_ZIP_ENTRIES) throw IOException("ZIP contains too many entries")
            val destination = canonicalDirectory.resolveZipEntry(entry.name)
            if (entry.isDirectory) {
                if (!destination.exists() && !destination.mkdirs()) {
                    throw IOException("Unable to create ZIP directory: ${entry.name}")
                }
                continue
            }
            validateZipEntry(entry, totalBytes)
            destination.parentFile?.mkdirs()
            val written = inputStream.copyZipEntryToFile(destination, entry.size, totalBytes)
            totalBytes += written
        }
    }
}

private fun validateZipEntry(
    entry: java.util.zip.ZipEntry,
    totalBytes: Long,
) {
    if (entry.size > MAX_ZIP_ENTRY_SIZE || totalBytes > MAX_ZIP_TOTAL_SIZE) {
        throw IOException("ZIP entry exceeds extraction limits: ${entry.name}")
    }
    if (entry.compressedSize > 0 && entry.size > entry.compressedSize * MAX_ZIP_COMPRESSION_RATIO) {
        throw IOException("ZIP compression ratio is excessive: ${entry.name}")
    }
}

private fun InputStream.copyZipEntryToFile(
    file: File,
    declaredSize: Long,
    totalBytes: Long,
): Long {
    val atomicFile = AtomicFile(file)
    val outputStream = atomicFile.startWrite()
    var copied = 0L
    try {
        outputStream.use { output ->
            val buffer = ByteArray(16 * 1024)
            while (true) {
                val count = read(buffer)
                if (count < 0) break
                copied += count
                if (copied > MAX_ZIP_ENTRY_SIZE || totalBytes + copied > MAX_ZIP_TOTAL_SIZE) {
                    throw IOException("ZIP extraction exceeds size limits")
                }
                output.write(buffer, 0, count)
            }
        }
        if (declaredSize >= 0 && copied != declaredSize) throw IOException("Truncated ZIP entry")
        atomicFile.finishWrite(outputStream)
        return copied
    } catch (e: Throwable) {
        atomicFile.failWrite(outputStream)
        throw e
    }
}

private fun File.resolveZipEntry(entryName: String): File {
    if (entryName.startsWith('/') || entryName.startsWith('\\')) {
        throw IOException("Absolute ZIP entry rejected: $entryName")
    }
    val candidate = File(this, entryName).canonicalFile
    val root = canonicalFile
    if (candidate != root && !candidate.path.startsWith(root.path + File.separator)) {
        throw IOException("ZIP path traversal rejected: $entryName")
    }
    return candidate
}

/** Returns a path-safe basename for data derived from an external provider. */
fun String.safeBasename(fallback: String = "unnamed"): String {
    val name = substringAfterLast('/').substringAfterLast('\\').trim()
    // Unicode letters are valid filenames; retain them to avoid different scripts/names
    // collapsing to the same underscore-only basename.
    return name.takeIf { it.isNotEmpty() && it != "." && it != ".." }?.replace(Regex("[^\\p{L}\\p{N}._-]"), "_")
        ?: fallback
}

fun File.isZipped() = extension == "zip"

fun DocumentFile.isZipped() = type == "application/zip"

/** Returns the uncompressed input stream if gzip compressed. */
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

/** Read bytes from input stream. If the stream is compressed with GZIP the uncompressed data is returned.*/
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

/** Write bytes to file using AtomicFile to prevent corruption. */
fun File.writeBytesAtomic(data: ByteArray) {
    val atomicFile = AtomicFile(this)
    val outputStream = atomicFile.startWrite()
    try {
        outputStream.write(data)
        atomicFile.finishWrite(outputStream)
    } catch (e: Throwable) {
        atomicFile.failWrite(outputStream)
        throw e
    }
}

/** Read text from file using AtomicFile. */
fun File.readTextAtomic(): String {
    val atomicFile = AtomicFile(this)
    return atomicFile.openRead().bufferedReader().use { it.readText() }
}

/** Write text to file using AtomicFile. */
fun File.writeTextAtomic(text: String) {
    val atomicFile = AtomicFile(this)
    val outputStream = atomicFile.startWrite()
    try {
        NonClosingOutputStream(outputStream).writer().use { it.write(text) }
        atomicFile.finishWrite(outputStream)
    } catch (e: Throwable) {
        atomicFile.failWrite(outputStream)
        throw e
    }
}

/** Write bytes to file using GZIP compression via AtomicFile. */
fun File.writeBytesCompressedAtomic(array: ByteArray) {
    val atomicFile = AtomicFile(this)
    val outputStream = atomicFile.startWrite()
    try {
        ByteArrayInputStream(array).use { input ->
            GZIPOutputStream(NonClosingOutputStream(outputStream)).use { usedOutput ->
                input.copyTo(usedOutput)
            }
        }
        atomicFile.finishWrite(outputStream)
    } catch (e: Throwable) {
        atomicFile.failWrite(outputStream)
        throw e
    }
}

/** Read bytes using AtomicFile. If the file is compressed with GZIP the uncompressed data is returned. */
fun File.readBytesUncompressedAtomic(): ByteArray {
    val atomicFile = AtomicFile(this)
    return atomicFile.openRead().use { input ->
        readBytesUncompressedStream(input.uncompressedInputStream())
    }
}
