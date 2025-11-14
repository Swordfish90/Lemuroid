package com.swordfish.lemuroid.app.shared.settings

import android.view.InputDevice
import android.view.KeyEvent
import com.swordfish.lemuroid.app.shared.input.InputKey

data class GameShortcut(
    val type: GameShortcutType,
    val keys: Set<Int>,
) {
    val name: String = keys.joinToString(" + ") { InputKey(it).displayName() }

    companion object {
        fun getDefault(
            inputDevice: InputDevice,
            type: GameShortcutType,
        ): GameShortcut? {
            if (type != GameShortcutType.MENU) return null
            return if (inputDevice.hasKeys(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR).all { it }) {
                GameShortcut(keys = setOf(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR), type = type)
            } else if (inputDevice.hasKeys(KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_START).all { it }) {
                GameShortcut(keys = setOf(KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_START), type = type)
            } else {
                null
            }
        }
    }
}

enum class GameShortcutType {
    MENU,
    QUICK_LOAD,
    QUICK_SAVE,
    TOGGLE_FAST_FORWARD,
    ;

    fun displayName() = name.split('_').joinToString(" ") { it.lowercase().replaceFirstChar { it.uppercase() } }
}
