/*
 * CoreFileInfo.kt
 *
 * Copyright (C) 2017 Odyssey Project
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

package com.codebutler.odyssey.feature.core.model

data class CoreFileInfo(
        val date: String,
        val crc: String,
        val fileName: String) {

    companion object {
        fun parseText(text: String): CoreFileInfo {
            val (date, crc, fileName) = text.split(" ")
            return CoreFileInfo(date, crc, fileName)
        }
    }

    val coreName: String by lazy {
        // "genesis_plus_gx_libretro_android.so.zip" => "genesis_plus_gx_libretro"
        fileName.substringBefore(".").substringBeforeLast("_")
    }
}
