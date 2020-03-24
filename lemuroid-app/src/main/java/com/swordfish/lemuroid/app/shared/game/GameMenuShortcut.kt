package com.swordfish.lemuroid.app.shared.game

import android.view.KeyEvent
import com.swordfish.libretrodroid.gamepad.GamepadInfo

data class GameMenuShortcut(val keys: Set<Int>, val label: String) {

    companion object {

        fun getBestShortcutForGamepad(gamepadInfo: GamepadInfo): GameMenuShortcut? {
            return DEFAULT_SHORTCUTS
                .filter { shortcut -> gamepadInfo.keys.containsAll(shortcut.keys) }
                .firstOrNull()
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
