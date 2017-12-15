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

package com.codebutler.retrograde.common.kotlin

import java.io.File
import java.io.FileInputStream
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream

fun File.calculateCrc32(): String = FileInputStream(this).use { fileStream ->
    val buffer = ByteArray(1024)
    CheckedInputStream(fileStream, CRC32()).use { crcStream ->
        while (crcStream.read(buffer) != -1) {
            // Read file in completely
        }
        return "%x".format(crcStream.checksum.value)
    }
}
