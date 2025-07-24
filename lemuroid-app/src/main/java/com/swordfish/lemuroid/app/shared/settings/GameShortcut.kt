package com.swordfish.lemuroid.app.shared.settings

import android.view.InputDevice
import com.swordfish.lemuroid.app.shared.input.lemuroiddevice.getLemuroidInputDevice
import com.swordfish.lemuroid.R

data class GameShortcut(
    val name: String,
    val keys: Set<Int>,
    val type: GameShortcutType
) {
    companion object {
        fun getDefault(
            inputDevice: InputDevice,
            type: GameShortcutType
        ): GameShortcut? {
            return inputDevice.getLemuroidInputDevice()
                .getSupportedShortcuts()
                .filter { it.type == type }
                .firstOrNull { shortcut -> inputDevice.hasKeys(*(shortcut.keys.toIntArray())).all { it } }
        }

        fun findByName(
            device: InputDevice,
            name: String,
        ): GameShortcut? {
            return device.getLemuroidInputDevice()
                .getSupportedShortcuts()
                .firstOrNull { it.name == name }
        }
    }
}

enum class GameShortcutType {
    MENU,
    QUICK_SAVE,
    QUICK_LOAD;

    fun getStringResource() = when (this) {
        MENU -> R.string.settings_gamepad_title_game_menu
        QUICK_SAVE -> R.string.settings_gamepad_title_quick_save
        QUICK_LOAD -> R.string.settings_gamepad_title_quick_load
    }
}

