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

package come.asg.lemudroid.lib.library

import androidx.annotation.StringRes
import come.asg.lemudroid.lib.R
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
    val hasTouchScreen: Boolean = false,
) {
    companion object {
        private val SYSTEMS =
            listOf(
                GameSystem(
                    SystemID.GB,
                    "Nintendo - Game Boy",
                    R.string.game_system_title_gb,
                    R.string.game_system_abbr_gb,
                    listOf(
                        SystemCoreConfig(
                            CoreID.MGBA,
                            exposedSettings =
                                listOf(
                                    ExposedSetting(
                                        "mgba_interframe_blending",
                                        R.string.setting_mgba_interframe_blending,
                                        arrayListOf(
                                            ExposedSetting.Value("OFF", R.string.value_mgba_interframe_blending_off),
                                            ExposedSetting.Value("MIX", R.string.value_mgba_interframe_blending_mix),
                                            ExposedSetting.Value(
                                                "MIX_SMART",
                                                R.string.value_mgba_interframe_blending_mix,
                                            ),
                                            ExposedSetting.Value(
                                                "LCD_GHOSTING",
                                                R.string.value_mgba_interframe_blending_lcd_ghosting,
                                            ),
                                            ExposedSetting.Value(
                                                "LCD_GHOSTING_FAST",
                                                R.string.value_mgba_interframe_blending_lcd_ghosting_fast,
                                            ),
                                        ),
                                    ),
                                ),
                            controllerConfigs =
                                hashMapOf(
                                    0 to arrayListOf(ControllerConfigs.GB),
                                ),
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
                            CoreID.MGBA,
                            exposedSettings =
                                listOf(
                                    ExposedSetting(
                                        "mgba_interframe_blending",
                                        R.string.setting_mgba_interframe_blending,
                                        arrayListOf(
                                            ExposedSetting.Value("OFF", R.string.value_mgba_interframe_blending_off),
                                            ExposedSetting.Value("MIX", R.string.value_mgba_interframe_blending_mix),
                                            ExposedSetting.Value(
                                                "MIX_SMART",
                                                R.string.value_mgba_interframe_blending_mix,
                                            ),
                                            ExposedSetting.Value(
                                                "LCD_GHOSTING",
                                                R.string.value_mgba_interframe_blending_lcd_ghosting,
                                            ),
                                            ExposedSetting.Value(
                                                "LCD_GHOSTING_FAST",
                                                R.string.value_mgba_interframe_blending_lcd_ghosting_fast,
                                            ),
                                        ),
                                    ),
                                ),
                            rumbleSupported = true,
                            controllerConfigs =
                                hashMapOf(
                                    0 to arrayListOf(ControllerConfigs.GBC),
                                ),
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
                            exposedSettings =
                                listOf(
                                    ExposedSetting(
                                        "mgba_solar_sensor_level",
                                        R.string.setting_mgba_solar_sensor_level,
                                    ),
                                    ExposedSetting(
                                        "mgba_interframe_blending",
                                        R.string.setting_mgba_interframe_blending,
                                        arrayListOf(
                                            ExposedSetting.Value(
                                                "OFF",
                                                R.string.value_mgba_interframe_blending_off,
                                            ),
                                            ExposedSetting.Value(
                                                "MIX",
                                                R.string.value_mgba_interframe_blending_mix,
                                            ),
                                            ExposedSetting.Value(
                                                "MIX_SMART",
                                                R.string.value_mgba_interframe_blending_mix,
                                            ),
                                            ExposedSetting.Value(
                                                "LCD_GHOSTING",
                                                R.string.value_mgba_interframe_blending_lcd_ghosting,
                                            ),
                                            ExposedSetting.Value(
                                                "LCD_GHOSTING_FAST",
                                                R.string.value_mgba_interframe_blending_lcd_ghosting_fast,
                                            ),
                                        ),
                                    ),
                                    ExposedSetting(
                                        "mgba_frameskip",
                                        R.string.setting_mgba_frameskip,
                                        arrayListOf(
                                            ExposedSetting.Value(
                                                "disabled",
                                                R.string.value_mgba_frameskip_disabled,
                                            ),
                                            ExposedSetting.Value("auto", R.string.value_mgba_frameskip_auto),
                                        ),
                                    ),
                                    ExposedSetting(
                                        "mgba_color_correction",
                                        R.string.setting_mgba_color_correction,
                                        arrayListOf(
                                            ExposedSetting.Value(
                                                "OFF",
                                                R.string.value_mgba_color_correction_off,
                                            ),
                                            ExposedSetting.Value(
                                                "GBA",
                                                R.string.value_mgba_color_correction_gba,
                                            ),
                                        ),
                                    ),
                                ),
                            rumbleSupported = true,
                            controllerConfigs =
                                hashMapOf(
                                    0 to arrayListOf(ControllerConfigs.GBA),
                                ),
                        ),
                    ),
                    uniqueExtensions = listOf("gba"),
                ),
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

        fun findByIdOrNull(id: String): GameSystem? = byIdCache[id]

        fun findById(id: String): GameSystem =
            findByIdOrNull(id) ?: throw NoSuchElementException("Unknown game system: $id")

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
            val scanByPathAndSupportedExtensions: Boolean = true,
            val scanBySimilarSerial: Boolean = false,
        )
    }
}
