/*
 * ByteArrayKt.kt
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

// https://bitbucket.org/snippets/gelin/zLebo/extension-functions-to-format-bytes-as-hex

private val CHARS = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

fun Byte.toHexString(): String {
    val i = this.toInt()
    val char2 = CHARS[i and 0x0f]
    val char1 = CHARS[i shr 4 and 0x0f]
    return "$char1$char2"
}

fun ByteArray.toHexString(): String {
    val builder = StringBuilder()
    for (b in this) {
        builder.append(b.toHexString())
    }
    return builder.toString()
}

fun ByteArray.isAllZeros(): Boolean = this.firstOrNull { it != 0x0.toByte() } == null

/** Return the index at which the array was found or -1. */
fun ByteArray.indexOf(byteArray: ByteArray): Int {
    if (byteArray.isEmpty()) {
        return 0
    }

    outer@ for (i in 0 until this.size - byteArray.size + 1) {
        for (j in byteArray.indices) {
            if (this[i + j] != byteArray[j]) {
                continue@outer
            }
        }
        return i
    }
    return -1
}
