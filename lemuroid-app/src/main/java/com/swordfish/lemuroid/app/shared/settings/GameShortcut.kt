package com.swordfish.lemuroid.app.shared.settings

import android.view.InputDevice
import android.view.KeyEvent
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.input.InputKey

data class GameShortcut(
    val type: GameShortcutType,
    val keys: Set<Int>
) {
    val name : String = keys.joinToString(" + ") { InputKey(it).displayName() }

    companion object {
        fun getDefault( inputDevice: InputDevice, type: GameShortcutType ): GameShortcut? {
            val combo = when (type) {
                GameShortcutType.MENU -> KeyEvent.KEYCODE_BUTTON_START to KeyEvent.KEYCODE_BUTTON_SELECT
                GameShortcutType.QUICK_SAVE -> KeyEvent.KEYCODE_BUTTON_R1 to KeyEvent.KEYCODE_BUTTON_R2
                GameShortcutType.QUICK_LOAD -> KeyEvent.KEYCODE_BUTTON_L1 to KeyEvent.KEYCODE_BUTTON_L2
            }
            if (inputDevice.hasKeys(combo.first, combo.second).all { it }) {
                return GameShortcut(keys = setOf(combo.first, combo.second), type = type )
            }
            return null
        }
    }
}

enum class GameShortcutType {
    MENU,
    QUICK_SAVE,
    QUICK_LOAD;

    fun displayName() = name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

