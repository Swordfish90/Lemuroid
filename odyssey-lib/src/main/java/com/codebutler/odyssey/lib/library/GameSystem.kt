/*
 * GameSystem.kt
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

package com.codebutler.odyssey.lib.library

import android.support.annotation.StringRes
import com.codebutler.odyssey.lib.R

data class GameSystem(
        val id: String,

        @StringRes
        val titleResId: Int,

        @StringRes
        val shortTitleResId: Int,

        val coreFileName: String,

        val supportedExtensions: List<String>) {

    companion object {
        private val SYSTEMS = listOf(
                GameSystem(
                        "nes",
                        R.string.game_system_title_nes,
                        R.string.game_system_abbr_nes,
                        "quicknes_libretro_android.so.zip",
                        listOf("nes")
                ),
                GameSystem(
                        "snes",
                        R.string.game_system_title_snes,
                        R.string.game_system_abbr_snes,
                        "snes9x_libretro_android.so.zip",
                        listOf("smc", "sfc", "swc", "fig")
                ),
                GameSystem(
                        "genesis",
                        R.string.game_system_title_genesis,
                        R.string.game_system_abbr_genesis,
                        "picodrive_libretro_android.so.zip",
                        listOf("gen", "smd", "md")
                ),
                GameSystem(
                        "gb",
                        R.string.game_system_title_gb,
                        R.string.game_system_abbr_gb,
                        "mgba_libretro_android.so.zip",
                        listOf("gb")

                ),
                GameSystem(
                        "gba",
                        R.string.game_system_title_gba,
                        R.string.game_system_abbr_gba,
                        "mgba_libretro_android.so.zip",
                        listOf("gba")
                ),
                GameSystem(
                        "gbc",
                        R.string.game_system_title_gbc,
                        R.string.game_system_abbr_gbc,
                        "mgba_libretro_android.so.zip",
                        listOf("gbc")
                )
        )

        fun findById(id: String): GameSystem? = SYSTEMS.find { it.id == id }

        fun findByOeid(oeid: String): GameSystem?
                = findById(oeid.replaceFirst("openemu.system.", ""))

        fun findByFileExtension(fileExtension: String): GameSystem? =
                SYSTEMS.find { it.supportedExtensions.contains(fileExtension) }
    }
}
