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
import com.swordfish.lemuroid.lib.core.CoreVariable
import java.util.Locale

data class GameSystem(
    val id: SystemID,

    val libretroFullName: String,

    @StringRes
    val titleResId: Int,

    @StringRes
    val shortTitleResId: Int,

    val systemCoreConfigs: List<SystemCoreConfig>,

    val uniqueExtensions: List<String>,

    val mergeDPADAndLeftStickEvents: Boolean = false,

    val scanOptions: ScanOptions = ScanOptions(),

    val supportedExtensions: List<String> = uniqueExtensions,

    val virtualGamePadOptions: VirtualGamePadOptions = VirtualGamePadOptions(),

    val hasMultiDiskSupport: Boolean = false,

    val fastForwardSupport: Boolean = true,
) {

    companion object {

        private val SYSTEMS = listOf(
            GameSystem(
                SystemID.ATARI2600,
                "Atari - 2600",
                R.string.game_system_title_atari2600,
                R.string.game_system_abbr_atari2600,
                listOf(
                    SystemCoreConfig(
                        coreID = CoreID.STELLA,
                        exposedSettings = listOf("stella_filter")
                    )
                ),
                uniqueExtensions = listOf("a26"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.NES,
                "Nintendo - Nintendo Entertainment System",
                R.string.game_system_title_nes,
                R.string.game_system_abbr_nes,
                listOf(SystemCoreConfig(CoreID.FCEUMM)),
                uniqueExtensions = listOf("nes"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.SNES,
                "Nintendo - Super Nintendo Entertainment System",
                R.string.game_system_title_snes,
                R.string.game_system_abbr_snes,
                listOf(SystemCoreConfig(CoreID.SNES9X)),
                uniqueExtensions = listOf("smc", "sfc"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.SMS,
                "Sega - Master System - Mark III",
                R.string.game_system_title_sms,
                R.string.game_system_abbr_sms,
                listOf(
                    SystemCoreConfig(
                        CoreID.GENESIS_PLUS_GX,
                        exposedSettings = listOf("genesis_plus_gx_blargg_ntsc_filter"),
                    )
                ),
                uniqueExtensions = listOf("sms"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GENESIS,
                "Sega - Mega Drive - Genesis",
                R.string.game_system_title_genesis,
                R.string.game_system_abbr_genesis,
                listOf(
                    SystemCoreConfig(
                        CoreID.GENESIS_PLUS_GX,
                        exposedSettings = listOf("genesis_plus_gx_blargg_ntsc_filter"),
                    )
                ),
                uniqueExtensions = listOf("gen", "smd", "md"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GG,
                "Sega - Game Gear",
                R.string.game_system_title_gg,
                R.string.game_system_abbr_gg,
                listOf(
                    SystemCoreConfig(
                        CoreID.GENESIS_PLUS_GX,
                        exposedSettings = listOf("genesis_plus_gx_lcd_filter"),
                    )
                ),
                uniqueExtensions = listOf("gg"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GB,
                "Nintendo - Game Boy",
                R.string.game_system_title_gb,
                R.string.game_system_abbr_gb,
                listOf(
                    SystemCoreConfig(
                        CoreID.GAMBATTE,
                        exposedSettings = listOf(
                            "gambatte_gb_colorization",
                            "gambatte_gb_internal_palette",
                            "gambatte_mix_frames"
                        )
                    ),
                ),
                uniqueExtensions = listOf("gb"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GBC,
                "Nintendo - Game Boy Color",
                R.string.game_system_title_gbc,
                R.string.game_system_abbr_gbc,
                listOf(
                    SystemCoreConfig(
                        CoreID.GAMBATTE,
                        exposedSettings = listOf(
                            "gambatte_gb_colorization",
                            "gambatte_gb_internal_palette",
                            "gambatte_mix_frames"
                        )
                    ),
                ),
                uniqueExtensions = listOf("gbc"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.GBA,
                "Nintendo - Game Boy Advance",
                R.string.game_system_title_gba,
                R.string.game_system_abbr_gba,
                listOf(
                    SystemCoreConfig(
                        CoreID.MGBA,
                        exposedSettings = listOf(
                            "mgba_solar_sensor_level",
                            "mgba_interframe_blending",
                            "mgba_frameskip",
                            "mgba_color_correction"
                        )
                    ),
                ),
                uniqueExtensions = listOf("gba"),
                mergeDPADAndLeftStickEvents = true
            ),
            GameSystem(
                SystemID.N64,
                "Nintendo - Nintendo 64",
                R.string.game_system_title_n64,
                R.string.game_system_abbr_n64,
                listOf(
                    SystemCoreConfig(
                        CoreID.MUPEN64_PLUS_NEXT,
                        defaultSettings = listOf(
                            CoreVariable("mupen64plus-43screensize", "320x240")
                        )
                    )
                ),
                uniqueExtensions = listOf("n64", "z64"),
                virtualGamePadOptions = VirtualGamePadOptions(true),
            ),
            GameSystem(
                SystemID.PSX,
                "Sony - PlayStation",
                R.string.game_system_title_psx,
                R.string.game_system_abbr_psx,
                listOf(
                    SystemCoreConfig(
                        CoreID.PCSX_REARMED,
                        exposedSettings = listOf(
                            "pcsx_rearmed_drc",
                            "pcsx_rearmed_frameskip",
                            "pcsx_rearmed_pad1type",
                            "pcsx_rearmed_pad2type"
                        ),
                        defaultSettings = listOf(
                            CoreVariable("pcsx_rearmed_drc", "disabled")
                        )
                    )
                ),
                uniqueExtensions = listOf(),
                supportedExtensions = listOf("iso", "pbp", "chd", "cue", "m3u"),
                scanOptions = ScanOptions(
                    scanByFilename = false,
                    scanByUniqueExtension = false,
                    scanByPathAndSupportedExtensions = true
                ),
                virtualGamePadOptions = VirtualGamePadOptions(true),
                hasMultiDiskSupport = true
            ),
            GameSystem(
                SystemID.PSP,
                "Sony - PlayStation Portable",
                R.string.game_system_title_psp,
                R.string.game_system_abbr_psp,
                listOf(
                    SystemCoreConfig(
                        CoreID.PPSSPP,
                        exposedSettings = listOf(
                            "ppsspp_auto_frameskip",
                            "ppsspp_frameskip"
                        ),
                    )
                ),
                uniqueExtensions = listOf(),
                supportedExtensions = listOf("iso", "cso", "pbp"),
                scanOptions = ScanOptions(
                    scanByFilename = false,
                    scanByUniqueExtension = false,
                    scanByPathAndSupportedExtensions = true
                ),
                virtualGamePadOptions = VirtualGamePadOptions(true),
                fastForwardSupport = false
            ),
            GameSystem(
                SystemID.FBNEO,
                "FBNeo - Arcade Games",
                R.string.game_system_title_arcade_fbneo,
                R.string.game_system_abbr_arcade_fbneo,
                listOf(
                    SystemCoreConfig(
                        CoreID.FBNEO,
                        exposedSettings = listOf(
                            "fbneo-frameskip",
                            "fbneo-cpu-speed-adjust"
                        )
                    )
                ),
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
            ),
            GameSystem(
                SystemID.MAME2003PLUS,
                "MAME 2003-Plus",
                R.string.game_system_title_arcade_mame2003_plus,
                R.string.game_system_abbr_arcade_mame2003_plus,
                listOf(
                    SystemCoreConfig(
                        CoreID.MAME2003PLUS,
                        statesSupported = false
                    )
                ),
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
            ),
            GameSystem(
                SystemID.NDS,
                "Nintendo - Nintendo DS",
                R.string.game_system_title_nds,
                R.string.game_system_abbr_nds,
                listOf(
                    SystemCoreConfig(
                        CoreID.DESMUME,
                        exposedSettings = listOf("desmume_frameskip"),
                        defaultSettings = listOf(
                            CoreVariable("desmume_pointer_type", "touch"),
                            CoreVariable("desmume_frameskip", "1")
                        )
                    ),
                    SystemCoreConfig(
                        CoreID.MELONDS,
                        exposedSettings = listOf(
                            "melonds_threaded_renderer",
                            "melonds_jit_enable"
                        ),
                        defaultSettings = listOf(
                            CoreVariable("melonds_touch_mode", "Touch"),
                            CoreVariable("melonds_threaded_renderer", "enabled")
                        )
                    )
                ),
                uniqueExtensions = listOf("nds"),
                mergeDPADAndLeftStickEvents = true,
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

        fun all() = SYSTEMS

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
