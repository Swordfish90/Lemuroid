package com.swordfish.lemuroid.app.shared.settings

import android.view.InputDevice
import android.view.KeyEvent

data class GameMenuShortcut(val name: String, val keys: Set<Int>) {

    companion object {

        fun getDefault(inputDevice: InputDevice): GameMenuShortcut? {
            return ALL_SHORTCUTS
                .firstOrNull { shortcut ->
                    inputDevice.hasKeys(*(shortcut.keys.toIntArray())).all { it }
                }
        }

        val ALL_SHORTCUTS = listOf(
            GameMenuShortcut(
                "L3 + R3",
                setOf(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR)
            ),
            GameMenuShortcut(
                "Select + Start",
                setOf(KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_SELECT)
            )
        )

        fun findByName(name: String): GameMenuShortcut {
            return ALL_SHORTCUTS.first { it.name == name }
        }
    }
}
