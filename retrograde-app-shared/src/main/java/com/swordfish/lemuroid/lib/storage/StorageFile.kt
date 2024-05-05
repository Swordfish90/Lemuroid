/*
 * StorageFile.kt
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

package com.swordfish.lemuroid.lib.storage

import android.net.Uri
import com.swordfish.lemuroid.common.files.FileUtils
import com.swordfish.lemuroid.lib.library.SystemID

data class StorageFile(
    val name: String,
    val size: Long,
    val crc: String? = null,
    val serial: String? = null,
    val uri: Uri,
    val path: String? = null,
    val systemID: SystemID? = null,
) {
    val extension: String
        get() = FileUtils.extractExtension(name)

    val extensionlessName: String
        get() = FileUtils.discardExtension(name)
}
