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

import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.InputStream
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream
import java.util.zip.ZipInputStream

private const val CRC32_BYTE_ARRAY_SIZE = 16 * 1024

fun InputStream.calculateCrc32(): String = this.use { fileStream ->
    val buffer = ByteArray(CRC32_BYTE_ARRAY_SIZE)
    CheckedInputStream(fileStream, CRC32()).use { crcStream ->
        while (crcStream.read(buffer) != -1) {
            // Read file in completely
        }
        return crcStream.checksum.value.toStringCRC32()
    }
}

fun InputStream.writeToFile(file: File) {
    this.use { inputStream ->
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
}

fun ZipInputStream.extractEntryToFile(entryName: String, gameFile: File) {
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
