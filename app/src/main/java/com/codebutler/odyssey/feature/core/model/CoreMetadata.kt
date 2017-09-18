/*
 * CoreMetadata.kt
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

data class CoreMetadata(
        val displayName: String?,
        val authors: List<String>?,
        val supportedExtensions: List<String>?,
        val corename: String?,
        val manufacturer: String?,
        val categories: String?,
        val systemname: String?,
        val database: String?,
        val license: String?,
        val permissions: String?,
        val displayVersion: String?,
        val supportsNoGame: Boolean) {

    companion object {
        fun parseInfoFile(text: String): CoreMetadata {
            val map = mapOf(*text.lines()
                    .filter { line -> line.isNotEmpty() && !line.startsWith("#") }
                    .map { line ->
                        var (key, value) = line.split(delimiters = " = ", limit = 2)
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length - 1)
                        }
                        key to value
                    }
                    .toTypedArray())
            return CoreMetadata(
                    displayName = map["display_name"],
                    authors = map["authors"]?.split("|"),
                    supportedExtensions = map["supported_extensions"]?.split("|"),
                    corename = map["corename"],
                    manufacturer = map["manufacturer"],
                    categories = map["categories"],
                    systemname = map["systemname"],
                    database = map["database"],
                    license = map["license"],
                    permissions = map["permissions"],
                    displayVersion = map["display_version"],
                    supportsNoGame = map["supports_no_game"] == "true"
            )
        }
    }
}
