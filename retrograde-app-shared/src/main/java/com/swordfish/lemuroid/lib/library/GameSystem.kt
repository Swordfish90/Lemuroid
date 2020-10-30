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
    val shortTitleResId: Int,

    val coreFileName: String,

    val uniqueExtensions: List<String>,

    val coreAssetsManager: CoreManager.AssetsManager = NoAssetsManager(),

    val mergeDPADAndLeftStickEvents: Boolean = false,

    val scanOptions: ScanOptions = ScanOptions(),

    val supportedExtensions: List<String> = uniqueExtensions,

    val exposedSettings: List<String> = listOf(),

    val defaultSettings: List<CoreVariable> = listOf(),

    val virtualGamePadOptions: VirtualGamePadOptions = VirtualGamePadOptions(),

    val hasMultiDiskSupport: Boolean = false,

    val fastForwardSupport: Boolean = true,

    val statesSupported: Boolean = true

) {

    companion object {

        private val SYSTEMS = listOf(
            GameSystem(
                SystemID.ATARI2600,
                "Atari - 2600",
                "stella",
                R.string.game_system_abbr_atari2600,
                "libstella_libretro_android.so",
                uniqueExtensions = listOf("a26"),
                exposedSettings = listOf("stella_filter"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.NES,
                "Nintendo - Nintendo Entertainment System",
                "fceumm",
                R.string.game_system_abbr_nes,
                "libfceumm_libretro_android.so",
                uniqueExtensions = listOf("nes"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.SNES,
                "Nintendo - Super Nintendo Entertainment System",
                "snes9x",
                R.string.game_system_abbr_snes,
                "libsnes9x_libretro_android.so",
                uniqueExtensions = listOf("smc", "sfc"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.SMS,
                "Sega - Master System - Mark III",
                "genesis_plus_gx",
                R.string.game_system_abbr_sms,
                "libgenesis_plus_gx_libretro_android.so",
                uniqueExtensions = listOf("sms"),
                exposedSettings = listOf(
                    "genesis_plus_gx_blargg_ntsc_filter"
                ),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GENESIS,
                "Sega - Mega Drive - Genesis",
                "genesis_plus_gx",
                R.string.game_system_abbr_genesis,
                "libgenesis_plus_gx_libretro_android.so",
                uniqueExtensions = listOf("gen", "smd", "md"),
                exposedSettings = listOf(
                    "genesis_plus_gx_blargg_ntsc_filter"
                ),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GG,
                "Sega - Game Gear",
                "genesis_plus_gx",
                R.string.game_system_abbr_gg,
                "libgenesis_plus_gx_libretro_android.so",
                uniqueExtensions = listOf("gg"),
                exposedSettings = listOf(
                    "genesis_plus_gx_lcd_filter"
                ),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GB,
                "Nintendo - Game Boy",
                "gambatte",
                R.string.game_system_abbr_gb,
                "libgambatte_libretro_android.so",
                uniqueExtensions = listOf("gb"),
                exposedSettings = listOf(
                    "gambatte_gb_colorization",
                    "gambatte_gb_internal_palette",
                    "gambatte_mix_frames"
                ),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GBC,
                "Nintendo - Game Boy Color",
                "gambatte",
                R.string.game_system_abbr_gbc,
                "libgambatte_libretro_android.so",
                uniqueExtensions = listOf("gbc"),
                exposedSettings = listOf(
                    "gambatte_gb_colorization",
                    "gambatte_gb_internal_palette",
                    "gambatte_mix_frames"
                ),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GBA,
                "Nintendo - Game Boy Advance",
                "mgba",
                R.string.game_system_abbr_gba,
                "libmgba_libretro_android.so",
                uniqueExtensions = listOf("gba"),
                exposedSettings = listOf(
                    "mgba_solar_sensor_level",
                    "mgba_interframe_blending",
                    "mgba_frameskip",
                    "mgba_color_correction"
                ),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.N64,
                "Nintendo - Nintendo 64",
                "mupen64plus_next",
                R.string.game_system_abbr_n64,
                "libmupen64plus_next_gles3_libretro_android.so",
                uniqueExtensions = listOf("n64", "z64"),
                virtualGamePadOptions = VirtualGamePadOptions(true),
                defaultSettings = listOf(
                    CoreVariable("mupen64plus-43screensize", "320x240")
                ),
            ),
            GameSystem(
                SystemID.PSX,
                "Sony - PlayStation",
                "pcsx_rearmed",
                R.string.game_system_abbr_psx,
                "libpcsx_rearmed_libretro_android.so",
                uniqueExtensions = listOf(),
                supportedExtensions = listOf("iso", "pbp", "chd", "cue", "m3u"),
                scanOptions = ScanOptions(
                    scanByFilename = false,
                    scanByUniqueExtension = false,
                    scanByPathAndSupportedExtensions = true
                ),
                exposedSettings = listOf(
                    "pcsx_rearmed_drc",
                    "pcsx_rearmed_frameskip",
                    "pcsx_rearmed_pad1type",
                    "pcsx_rearmed_pad2type"
                ),
                defaultSettings = listOf(
                    CoreVariable("pcsx_rearmed_drc", "disabled")
                ),
                virtualGamePadOptions = VirtualGamePadOptions(true),
                hasMultiDiskSupport = true
            ),
            GameSystem(
                SystemID.PSP,
                "Sony - PlayStation Portable",
                "ppsspp",
                R.string.game_system_abbr_psp,
                "libppsspp_libretro_android.so",
                uniqueExtensions = listOf(),
                supportedExtensions = listOf("iso", "cso", "pbp"),
                coreAssetsManager = PPSSPPAssetsManager(),
                scanOptions = ScanOptions(
                    scanByFilename = false,
                    scanByUniqueExtension = false,
                    scanByPathAndSupportedExtensions = true
                ),
                exposedSettings = listOf(
                    "ppsspp_auto_frameskip",
                    "ppsspp_frameskip"
                ),
                virtualGamePadOptions = VirtualGamePadOptions(true),
                fastForwardSupport = false
            ),
            GameSystem(
                SystemID.FBNEO,
                "FBNeo - Arcade Games",
                "fbneo",
                R.string.game_system_abbr_arcade_fbneo,
                "libfbneo_libretro_android.so",
                uniqueExtensions = listOf(),
                supportedExtensions = listOf("zip"),
                scanOptions = ScanOptions(
                    scanByFilename = false,
                    scanByUniqueExtension = false,
                    scanByPathAndFilename = true,
                    scanByPathAndSupportedExtensions = false
                ),
                mergeDPADAndLeftStickEvents = true,
                virtualGamePadOptions = VirtualGamePadOptions(true),
                exposedSettings = listOf(
                    "fbneo-frameskip",
                    "fbneo-cpu-speed-adjust"
                )
            ),
            GameSystem(
                SystemID.MAME2003PLUS,
                "MAME 2003-Plus",
                "mame2003_plus",
                R.string.game_system_abbr_arcade_mame2003_plus,
                "libmame2003_plus_libretro_android.so",
                uniqueExtensions = listOf(),
                supportedExtensions = listOf("zip"),
                scanOptions = ScanOptions(
                    scanByFilename = false,
                    scanByUniqueExtension = false,
                    scanByPathAndFilename = true,
                    scanByPathAndSupportedExtensions = false
                ),
                mergeDPADAndLeftStickEvents = true,
                virtualGamePadOptions = VirtualGamePadOptions(true),
                statesSupported = false
            ),
            GameSystem(
                SystemID.NDS,
                "Nintendo - Nintendo DS",
                "desmume",
                R.string.game_system_abbr_nds,
                "libdesmume_libretro_android.so",
                uniqueExtensions = listOf("nds"),
                mergeDPADAndLeftStickEvents = true,
                exposedSettings = listOf("desmume_frameskip"),
                defaultSettings = listOf(
                    CoreVariable("desmume_pointer_type", "touch"),
                    CoreVariable("desmume_frameskip", "1")
                )
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

        data class VirtualGamePadOptions(
            val hasRotation: Boolean = false
        )
    }
}
