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

    val scanOptions: ScanOptions = ScanOptions(),

    val supportedExtensions: List<String> = uniqueExtensions,

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
                        exposedSettings = listOf(
                            "stella_filter",
                            "stella_crop_hoverscan"
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.ATARI_2600)
                        )
                    )
                ),
                uniqueExtensions = listOf("a26"),
            ),
            GameSystem(
                SystemID.NES,
                "Nintendo - Nintendo Entertainment System",
                R.string.game_system_title_nes,
                R.string.game_system_abbr_nes,
                listOf(
                    SystemCoreConfig(
                        CoreID.FCEUMM,
                        exposedSettings = listOf(
                            "fceumm_overscan_h",
                            "fceumm_overscan_v",
                        ),
                        exposedAdvancedSettings = listOf(
                            "fceumm_nospritelimit",
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.NES)
                        ),
                    )
                ),
                uniqueExtensions = listOf("nes"),
            ),
            GameSystem(
                SystemID.SNES,
                "Nintendo - Super Nintendo Entertainment System",
                R.string.game_system_title_snes,
                R.string.game_system_abbr_snes,
                listOf(
                    SystemCoreConfig(
                        CoreID.SNES9X,
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.SNES)
                        )
                    )
                ),
                uniqueExtensions = listOf("smc", "sfc"),
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
                        exposedAdvancedSettings = listOf(
                            "genesis_plus_gx_no_sprite_limit",
                            "genesis_plus_gx_overscan"
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.SMS)
                        )
                    )
                ),
                uniqueExtensions = listOf("sms"),
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
                        exposedAdvancedSettings = listOf(
                            "genesis_plus_gx_no_sprite_limit",
                            "genesis_plus_gx_overscan"
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.GENESIS_3, ControllerConfigs.GENESIS_6),
                            1 to arrayListOf(ControllerConfigs.GENESIS_3, ControllerConfigs.GENESIS_6),
                            2 to arrayListOf(ControllerConfigs.GENESIS_3, ControllerConfigs.GENESIS_6),
                            3 to arrayListOf(ControllerConfigs.GENESIS_3, ControllerConfigs.GENESIS_6)
                        )
                    )
                ),
                uniqueExtensions = listOf("gen", "smd", "md"),
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
                        exposedAdvancedSettings = listOf(
                            "genesis_plus_gx_no_sprite_limit",
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.GG)
                        )
                    )
                ),
                uniqueExtensions = listOf("gg"),
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
                            "gambatte_mix_frames",
                            "gambatte_dark_filter_level"
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.GB)
                        )
                    ),
                ),
                uniqueExtensions = listOf("gb"),
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
                            "gambatte_mix_frames",
                            "gambatte_dark_filter_level"
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.GB)
                        )
                    ),
                ),
                uniqueExtensions = listOf("gbc"),
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
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.GBA)
                        )
                    ),
                ),
                uniqueExtensions = listOf("gba"),
            ),
            GameSystem(
                SystemID.N64,
                "Nintendo - Nintendo 64",
                R.string.game_system_title_n64,
                R.string.game_system_abbr_n64,
                listOf(
                    SystemCoreConfig(
                        CoreID.MUPEN64_PLUS_NEXT,
                        exposedSettings = listOf(
                            "mupen64plus-43screensize",
                            "mupen64plus-cpucore",
                            "mupen64plus-BilinearMode",
                        ),
                        defaultSettings = listOf(
                            CoreVariable("mupen64plus-43screensize", "320x240")
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.N64)
                        )
                    )
                ),
                uniqueExtensions = listOf("n64", "z64"),
            ),
            GameSystem(
                SystemID.PSX,
                "Sony - PlayStation",
                R.string.game_system_title_psx,
                R.string.game_system_abbr_psx,
                listOf(
                    SystemCoreConfig(
                        CoreID.PCSX_REARMED,
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.PSX_STANDARD, ControllerConfigs.PSX_DUALSHOCK),
                            1 to arrayListOf(ControllerConfigs.PSX_STANDARD, ControllerConfigs.PSX_DUALSHOCK),
                            2 to arrayListOf(ControllerConfigs.PSX_STANDARD, ControllerConfigs.PSX_DUALSHOCK),
                            3 to arrayListOf(ControllerConfigs.PSX_STANDARD, ControllerConfigs.PSX_DUALSHOCK),
                        ),
                        exposedSettings = listOf(
                            "pcsx_rearmed_frameskip"
                        ),
                        exposedAdvancedSettings = listOf(
                            "pcsx_rearmed_drc"
                        ),
                        defaultSettings = listOf(
                            CoreVariable("pcsx_rearmed_drc", "disabled"),
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
                        exposedAdvancedSettings = listOf(
                            "ppsspp_cpu_core",
                            "ppsspp_internal_resolution",
                            "ppsspp_texture_scaling_level",
                            "ppsspp_texture_scaling_type",
                            "ppsspp_texture_filtering"
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.PSP)
                        )
                    )
                ),
                uniqueExtensions = listOf(),
                supportedExtensions = listOf("iso", "cso", "pbp"),
                scanOptions = ScanOptions(
                    scanByFilename = false,
                    scanByUniqueExtension = false,
                    scanByPathAndSupportedExtensions = true
                ),
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
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.FB_NEO_4, ControllerConfigs.FB_NEO_6)
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
            ),
            GameSystem(
                SystemID.MAME2003PLUS,
                "MAME 2003-Plus",
                R.string.game_system_title_arcade_mame2003_plus,
                R.string.game_system_abbr_arcade_mame2003_plus,
                listOf(
                    SystemCoreConfig(
                        CoreID.MAME2003PLUS,
                        statesSupported = false,
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.MAME_2003_4, ControllerConfigs.MAME_2003_6)
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
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.DESMUME)
                        )
                    ),
                    SystemCoreConfig(
                        CoreID.MELONDS,
                        exposedSettings = listOf(
                            "melonds_screen_layout"
                        ),
                        exposedAdvancedSettings = listOf(
                            "melonds_threaded_renderer",
                            "melonds_jit_enable",
                        ),
                        defaultSettings = listOf(
                            CoreVariable("melonds_touch_mode", "Touch"),
                            CoreVariable("melonds_threaded_renderer", "enabled")
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.MELONDS)
                        )
                    )
                ),
                uniqueExtensions = listOf("nds"),
            ),
            GameSystem(
                SystemID.ATARI7800,
                "Atari - 7800",
                R.string.game_system_title_atari7800,
                R.string.game_system_abbr_atari7800,
                listOf(
                    SystemCoreConfig(
                        CoreID.PROSYSTEM,
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.ATARI7800)
                        )
                    ),
                ),
                uniqueExtensions = listOf("a78"),
                supportedExtensions = listOf("bin")
            ),
            GameSystem(
                SystemID.LYNX,
                "Atari - Lynx",
                R.string.game_system_title_lynx,
                R.string.game_system_abbr_lynx,
                listOf(
                    SystemCoreConfig(
                        CoreID.HANDY,
                        requiredBIOSFiles = listOf(
                            "lynxboot2.img"
                        ),
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.LYNX)
                        )
                    ),
                ),
                uniqueExtensions = listOf("lnx"),
            ),
            GameSystem(
                SystemID.PC_ENGINE,
                "NEC - PC Engine - TurboGrafx 16",
                R.string.game_system_title_pce,
                R.string.game_system_abbr_pce,
                listOf(
                    SystemCoreConfig(
                        CoreID.MEDNAFEN_PCE_FAST,
                        controllerConfigs = hashMapOf(
                            0 to arrayListOf(ControllerConfigs.PCE)
                        )
                    ),
                ),
                uniqueExtensions = listOf("pce"),
                supportedExtensions = listOf("bin"),
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

        fun findSystemForCore(coreID: CoreID): List<GameSystem> {
            return all().filter { system -> system.systemCoreConfigs.any { it.coreID == coreID } }
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
