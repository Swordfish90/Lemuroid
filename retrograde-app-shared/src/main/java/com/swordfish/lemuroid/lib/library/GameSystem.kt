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

package com.swordfish.lemuroid.lib.library

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.core.CoreManager
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.core.assetsmanager.NoAssetsManager
import com.swordfish.lemuroid.lib.core.assetsmanager.PPSSPPAssetsManager
import java.util.Locale

data class GameSystem(
    val id: SystemID,

    val libretroFullName: String,

    val coreName: String,

    @StringRes
    val titleResId: Int,

    @StringRes
    val shortTitleResId: Int,

    @DrawableRes
    val imageResId: Int,

    val sortKey: String,

    val coreFileName: String,

    val uniqueExtensions: List<String>,

    val coreAssetsManager: CoreManager.AssetsManager = NoAssetsManager(),

    val supportsAutosave: Boolean = true,

    val scanOptions: ScanOptions = ScanOptions(),

    val supportedExtensions: List<String> = uniqueExtensions,

    val exposedSettings: List<String> = listOf(),

    val defaultSettings: List<CoreVariable> = listOf()

) {

    companion object {

        private val SYSTEMS = listOf(
                GameSystem(
                        SystemID.ATARI2600,
                        "Atari - 2600",
                        "stella",
                        R.string.game_system_title_atari2600,
                        R.string.game_system_abbr_atari2600,
                        R.drawable.game_system_atari2600,
                        "atari0",
                        "stella_libretro_android.so.zip",
                        uniqueExtensions = listOf("a26"),
                        exposedSettings = listOf("stella_filter")
                ),
                GameSystem(
                        SystemID.NES,
                        "Nintendo - Nintendo Entertainment System",
                        "fceumm",
                        R.string.game_system_title_nes,
                        R.string.game_system_abbr_nes,
                        R.drawable.game_system_nes,
                        "nintendo0",
                        "fceumm_libretro_android.so.zip",
                        uniqueExtensions = listOf("nes")
                ),
                GameSystem(
                        SystemID.SNES,
                        "Nintendo - Super Nintendo Entertainment System",
                        "snes9x",
                        R.string.game_system_title_snes,
                        R.string.game_system_abbr_snes,
                        R.drawable.game_system_snes,
                        "nintendo1",
                        "snes9x_libretro_android.so.zip",
                        uniqueExtensions = listOf("smc", "sfc")
                ),
                GameSystem(
                        SystemID.SMS,
                        "Sega - Master System - Mark III",
                        "genesis_plus_gx",
                        R.string.game_system_title_sms,
                        R.string.game_system_abbr_sms,
                        R.drawable.game_system_sms,
                        "sega0",
                        "genesis_plus_gx_libretro_android.so.zip",
                        uniqueExtensions = listOf("sms"),
                        exposedSettings = listOf(
                            "genesis_plus_gx_blargg_ntsc_filter"
                        )
                ),
                GameSystem(
                        SystemID.GENESIS,
                        "Sega - Mega Drive - Genesis",
                        "genesis_plus_gx",
                        R.string.game_system_title_genesis,
                        R.string.game_system_abbr_genesis,
                        R.drawable.game_system_genesis,
                        "sega1",
                        "genesis_plus_gx_libretro_android.so.zip",
                        uniqueExtensions = listOf("gen", "smd", "md"),
                        exposedSettings = listOf(
                            "genesis_plus_gx_blargg_ntsc_filter"
                        )
                ),
                GameSystem(
                        SystemID.GG,
                        "Sega - Game Gear",
                        "genesis_plus_gx",
                        R.string.game_system_title_gg,
                        R.string.game_system_abbr_gg,
                        R.drawable.game_system_gg,
                        "sega2",
                        "genesis_plus_gx_libretro_android.so.zip",
                        uniqueExtensions = listOf("gg"),
                        exposedSettings = listOf(
                            "genesis_plus_gx_lcd_filter"
                        )
                ),
                GameSystem(
                        SystemID.GB,
                        "Nintendo - Game Boy",
                        "gambatte",
                        R.string.game_system_title_gb,
                        R.string.game_system_abbr_gb,
                        R.drawable.game_system_gb,
                        "nintendo2",
                        "gambatte_libretro_android.so.zip",
                        uniqueExtensions = listOf("gb"),
                        exposedSettings = listOf(
                            "gambatte_gb_colorization",
                            "gambatte_gb_internal_palette",
                            "gambatte_mix_frames"
                        )
                ),
                GameSystem(
                        SystemID.GBC,
                        "Nintendo - Game Boy Color",
                        "gambatte",
                        R.string.game_system_title_gbc,
                        R.string.game_system_abbr_gbc,
                        R.drawable.game_system_gbc,
                        "nintendo3",
                        "gambatte_libretro_android.so.zip",
                        uniqueExtensions = listOf("gbc"),
                        exposedSettings = listOf(
                            "gambatte_gb_colorization",
                            "gambatte_gb_internal_palette",
                            "gambatte_mix_frames"
                        )
                ),
                GameSystem(
                        SystemID.GBA,
                        "Nintendo - Game Boy Advance",
                        "mgba",
                        R.string.game_system_title_gba,
                        R.string.game_system_abbr_gba,
                        R.drawable.game_system_gba,
                        "nintendo4",
                        "mgba_libretro_android.so.zip",
                        uniqueExtensions = listOf("gba"),
                        exposedSettings = listOf(
                            "mgba_solar_sensor_level",
                            "mgba_interframe_blending",
                            "mgba_frameskip",
                            "mgba_color_correction"
                        )
                ),
                GameSystem(
                        SystemID.N64,
                        "Nintendo - Nintendo 64",
                        "mupen64plus_next",
                        R.string.game_system_title_n64,
                        R.string.game_system_abbr_n64,
                        R.drawable.game_system_n64,
                        "nintendo5",
                        "mupen64plus_next_libretro_android.so.zip",
                        uniqueExtensions = listOf("n64", "z64")
                ),
                GameSystem(
                        SystemID.PSX,
                        "Sony - PlayStation",
                        "pcsx_rearmed",
                        R.string.game_system_title_psx,
                        R.string.game_system_abbr_psx,
                        R.drawable.game_system_psx,
                        "sony0",
                        "pcsx_rearmed_libretro_android.so.zip",
                        uniqueExtensions = listOf(),
                        supportedExtensions = listOf("iso", "pbp"),
                        scanOptions = ScanOptions(
                            scanByFilename = false,
                            scanByUniqueExtension = false,
                            scanByPathAndSupportedExtensions = true
                        ),
                        exposedSettings = listOf(
                            "pcsx_rearmed_frameskip",
                            "pcsx_rearmed_pad1type",
                            "pcsx_rearmed_pad2type"
                        ),
                        defaultSettings = listOf(
                            CoreVariable("pcsx_rearmed_drc", "disabled")
                        )
                ),
                GameSystem(
                        SystemID.PSP,
                        "Sony - PlayStation Portable",
                        "ppsspp",
                        R.string.game_system_title_psp,
                        R.string.game_system_abbr_psp,
                        R.drawable.game_system_psp,
                        "sony1",
                        "ppsspp_libretro_android.so.zip",
                        uniqueExtensions = listOf(),
                        supportedExtensions = listOf("iso", "cso", "pbp"),
                        coreAssetsManager = PPSSPPAssetsManager(),
                        supportsAutosave = false,
                        scanOptions = ScanOptions(
                            scanByFilename = false,
                            scanByUniqueExtension = false,
                            scanByPathAndSupportedExtensions = true
                        ),
                        exposedSettings = listOf(
                            "ppsspp_auto_frameskip",
                            "ppsspp_frameskip"
                        )
                ),
                GameSystem(
                        SystemID.FBNEO,
                        "FBNeo - Arcade Games",
                        "fbneo",
                        R.string.game_system_title_arcade_fbneo,
                        R.string.game_system_abbr_arcade_fbneo,
                        R.drawable.game_system_arcade,
                        "arcade",
                        "fbneo_libretro_android.so.zip",
                        uniqueExtensions = listOf(),
                        supportedExtensions = listOf("zip"),
                        scanOptions = ScanOptions(
                            scanByFilename = false,
                            scanByUniqueExtension = false,
                            scanByPathAndFilename = true,
                            scanByPathAndSupportedExtensions = false
                        ),
                        exposedSettings = listOf(
                            "fbneo-frameskip",
                            "fbneo-cpu-speed-adjust"
                        )
                ),
                GameSystem(
                        SystemID.NDS,
                        "Nintendo - Nintendo DS",
                        "desmume",
                        R.string.game_system_title_nds,
                        R.string.game_system_abbr_nds,
                        R.drawable.game_system_ds,
                        "nintendo6",
                        "desmume_libretro_android.so.zip",
                        uniqueExtensions = listOf("nds"),
                        exposedSettings = listOf("desmume_frameskip"),
                        defaultSettings = listOf(CoreVariable("desmume_pointer_type", "touch"))
                )
        )

        private val byIdCache by lazy { mapOf(*SYSTEMS.map { it.id.dbname to it }.toTypedArray()) }
        private val byExtensionCache by lazy {
            val mutableMap = mutableMapOf<String, GameSystem>()
            for (system in SYSTEMS) {
                for (extension in system.uniqueExtensions) {
                    mutableMap[extension.toLowerCase(Locale.US)] = system
                }
            }
            mutableMap.toMap()
        }

        fun findById(id: String): GameSystem = byIdCache.getValue(id)

        fun getSupportedExtensions(): List<String> {
            return SYSTEMS.flatMap { it.supportedExtensions }
        }

        fun findByUniqueFileExtension(fileExtension: String): GameSystem? =
                byExtensionCache[fileExtension.toLowerCase(Locale.US)]

        data class ScanOptions(
            val scanByFilename: Boolean = true,
            val scanByUniqueExtension: Boolean = true,
            val scanByPathAndFilename: Boolean = false,
            val scanByPathAndSupportedExtensions: Boolean = true
        )
    }
}
