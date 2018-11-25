/*
 * GameSystem.kt
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

package com.codebutler.retrograde.lib.library

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.codebutler.retrograde.lib.R
import java.util.Locale

data class GameSystem(
    val id: String,

    @StringRes
    val titleResId: Int,

    @StringRes
    val shortTitleResId: Int,

    @DrawableRes
    val imageResId: Int,

    val sortKey: String,

    val coreFileName: String,

    val supportedExtensions: List<String>
) {

    companion object {
        private val SYSTEMS = listOf(
                GameSystem(
                        "nes",
                        R.string.game_system_title_nes,
                        R.string.game_system_abbr_nes,
                        R.drawable.game_system_nes,
                        "nintendo0",
                        "quicknes_libretro_android.so.zip",
                        listOf("nes")
                ),
                GameSystem(
                        "snes",
                        R.string.game_system_title_snes,
                        R.string.game_system_abbr_snes,
                        R.drawable.game_system_snes,
                        "nintendo1",
                        "snes9x_libretro_android.so.zip",
                        listOf("smc", "sfc", "swc", "fig")
                ),
                GameSystem(
                        "md",
                        R.string.game_system_title_genesis,
                        R.string.game_system_abbr_genesis,
                        R.drawable.game_system_genesis,
                        "sega0",
                        "picodrive_libretro_android.so.zip",
                        listOf("gen", "smd", "md")
                ),
                GameSystem(
                        "gb",
                        R.string.game_system_title_gb,
                        R.string.game_system_abbr_gb,
                        R.drawable.game_system_gb,
                        "nintendo2",
                        "mgba_libretro_android.so.zip",
                        listOf("gb")

                ),
                GameSystem(
                        "gbc",
                        R.string.game_system_title_gbc,
                        R.string.game_system_abbr_gbc,
                        R.drawable.game_system_gbc,
                        "nintendo3",
                        "mgba_libretro_android.so.zip",
                        listOf("gbc")
                ),
                GameSystem(
                        "gba",
                        R.string.game_system_title_gba,
                        R.string.game_system_abbr_gba,
                        R.drawable.game_system_gba,
                        "nintendo4",
                        "mgba_libretro_android.so.zip",
                        listOf("gba")
                ),
                GameSystem(
                        "arcade",
                        R.string.game_system_title_arcade,
                        R.string.game_system_abbr_arcade,
                        R.drawable.game_system_arcade,
                        "arcade",
                        "fbalpha_libretro_android.so.zip",
                        listOf("zip")
                )
        )

        private val byIdCache by lazy { mapOf(*SYSTEMS.map { it.id to it }.toTypedArray()) }
        private val byExtensionCache by lazy {
            val mutableMap = mutableMapOf<String, GameSystem>()
            for (system in SYSTEMS) {
                for (extension in system.supportedExtensions) {
                    mutableMap[extension.toLowerCase(Locale.US)] = system
                }
            }
            mutableMap.toMap()
        }

        fun findById(id: String): GameSystem? = byIdCache[id]

        fun findByShortName(shortName: String): GameSystem? =
                findById(shortName.toLowerCase())

        fun findByFileExtension(fileExtension: String): GameSystem? =
                byExtensionCache[fileExtension.toLowerCase(Locale.US)]
    }
}
