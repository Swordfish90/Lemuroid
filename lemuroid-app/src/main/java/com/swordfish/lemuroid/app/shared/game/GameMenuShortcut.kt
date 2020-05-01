package com.swordfish.lemuroid.app.shared.game

import android.view.InputDevice
import android.view.KeyEvent

data class GameMenuShortcut(val keys: Set<Int>, val label: String) {

    companion object {

        fun getBestShortcutForInputDevice(inputDevice: InputDevice): GameMenuShortcut? {
            return DEFAULT_SHORTCUTS
                .firstOrNull { shortcut -> inputDevice.hasKeys(*(shortcut.keys.toIntArray())).all { it } }
        }

        private val DEFAULT_SHORTCUTS = listOf(
            GameMenuShortcut(
                setOf(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR),
                "L3 + R3"
            ),
            GameMenuShortcut(
                setOf(KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_SELECT),
                "Start + Select"
            )
        )
    }
}
