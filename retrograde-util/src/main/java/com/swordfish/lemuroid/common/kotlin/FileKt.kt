/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.common.kotlin

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
import java.io.RandomAccessFile
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream

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

/** Returns the uncompressed input stream if gzip compressed. */
private fun File.uncompressedInputStream(): InputStream {
    val pb = PushbackInputStream(inputStream(), 2)
    val signature = ByteArray(2)
    val len = pb.read(signature)
    pb.unread(signature, 0, len)
    return if (signature[0] == 0x1f.toByte() && signature[1] == 0x8b.toByte()) {
        GZIPInputStream(pb, GZIP_INPUT_STREAM_BUFFER_SIZE)
    } else {
        pb
    }
}

/** Write bytes to file using GZIP compression. */
fun File.writeBytesCompressed(array: ByteArray) {
    val inputStream = ByteArrayInputStream(array)
    val outputStream = GZIPOutputStream(this.outputStream())
    inputStream.use { usedInputStream ->
        outputStream.use { usedOutputStream ->
            usedInputStream.copyTo(usedOutputStream)
        }
    }
}

/** Read bytes from file. If the file is compressed with GZIP the uncompressed data is returned.*/
fun File.readBytesUncompressed(): ByteArray =
    uncompressedInputStream().use { input ->
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

fun File.is7Zipped() = extension.equals("7z", ignoreCase = true)

fun DocumentFile.is7Zipped() = name?.endsWith(".7z", ignoreCase = true) == true

fun File.extract7zEntryToFile(
    entryName: String,
    outputFile: File,
) {
    val randomAccessFile = RandomAccessFile(this, "r")
    val inStream = RandomAccessFileInStream(randomAccessFile)

    try {
        val archive: IInArchive = SevenZip.openInArchive(null, inStream)

        try {
            val itemCount = archive.numberOfItems
            var foundIndex = -1

            // Find the entry by name
            for (i in 0 until itemCount) {
                val path = archive.getStringProperty(i, PropID.PATH)
                if (path == entryName) {
                    foundIndex = i
                    break
                }
            }

            if (foundIndex == -1) {
                throw IllegalArgumentException("Entry $entryName not found in 7z archive")
            }

            // Extract the file
            outputFile.outputStream().use { fos ->
                val outStream = object : ISequentialOutStream {
                    override fun write(data: ByteArray): Int {
                        fos.write(data)
                        return data.size
                    }
                }
                archive.extractSlow(foundIndex, outStream)
            }
        } finally {
            archive.close()
        }
    } finally {
        inStream.close()
        randomAccessFile.close()
    }
}

/** Data class for 7z archive entry info */
data class SevenZEntry(
    val name: String,
    val size: Long,
    val crc: Long?,
    val isDirectory: Boolean,
)

/** Get list of entries from 7z archive without extracting */
fun File.get7zEntries(): List<SevenZEntry> {
    val randomAccessFile = RandomAccessFile(this, "r")
    val inStream = RandomAccessFileInStream(randomAccessFile)
    val entries = mutableListOf<SevenZEntry>()

    try {
        val archive: IInArchive = SevenZip.openInArchive(null, inStream)

        try {
            val itemCount = archive.numberOfItems
            for (i in 0 until itemCount) {
                val path = archive.getStringProperty(i, PropID.PATH) ?: continue
                val size = archive.getProperty(i, PropID.SIZE) as? Long ?: 0L
                val crc = archive.getProperty(i, PropID.CRC) as? Long
                val isDir = archive.getProperty(i, PropID.IS_FOLDER) as? Boolean ?: false

                entries.add(SevenZEntry(path, size, crc, isDir))
            }
        } finally {
            archive.close()
        }
    } finally {
        inStream.close()
        randomAccessFile.close()
    }

    return entries
}
