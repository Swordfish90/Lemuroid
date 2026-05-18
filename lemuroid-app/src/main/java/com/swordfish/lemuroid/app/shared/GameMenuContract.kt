/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.app.shared

object GameMenuContract {
    const val EXTRA_GAME = "EXTRA_GAME"
    const val EXTRA_SYSTEM_CORE_CONFIG = "EXTRA_SYSTEM_CORE_CONFIG"
    const val EXTRA_CURRENT_DISK = "EXTRA_CURRENT_DISK"
    const val EXTRA_DISKS = "EXTRA_DISKS"
    const val EXTRA_CORE_OPTIONS = "EXTRA_CORE_OPTIONS"
    const val EXTRA_ADVANCED_CORE_OPTIONS = "EXTRA_ADVANCED_CORE_OPTIONS"
    const val EXTRA_AUDIO_ENABLED = "EXTRA_AUDIO_ENABLED"
    const val EXTRA_FAST_FORWARD_SUPPORTED = "EXTRA_FAST_FORWARD_SUPPORTED"
    const val EXTRA_FAST_FORWARD = "EXTRA_FAST_FORWARD"
    const val EXTRA_CURRENT_TILT_CONFIG = "EXTRA_CURRENT_TILT_CONFIG"
    const val EXTRA_TILT_ALL_CONFIGS = "EXTRA_TILT_ALL_CONFIGS"
    const val EXTRA_SCREENSHOT_PATH = "EXTRA_SCREENSHOT_PATH"

    const val RESULT_RESET = "RESULT_RESET"
    const val RESULT_SAVE = "RESULT_SAVE"
    const val RESULT_LOAD = "RESULT_LOAD"
    const val RESULT_QUIT = "RESULT_QUIT"
    const val RESULT_CHANGE_DISK = "RESULT_CHANGE_DISK"
    const val RESULT_EDIT_TOUCH_CONTROLS = "RESULT_EDIT_TOUCH_CONTROLS"
    const val RESULT_ENABLE_AUDIO = "RESULT_ENABLE_AUDIO"
    const val RESULT_ENABLE_FAST_FORWARD = "RESULT_ENABLE_FAST_FORWARD"
    const val RESULT_CHANGE_TILT_CONFIG = "RESULT_CHANGE_TILT_CONFIG"
}
