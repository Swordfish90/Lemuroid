package com.swordfish.lemuroid.app.shared.input

import android.view.KeyEvent
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
@JvmInline
value class InputKey(val keyCode: Int) {
    fun displayName(): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "L3"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "R3"
            KeyEvent.KEYCODE_BUTTON_MODE -> "Options"
            KeyEvent.KEYCODE_UNKNOWN -> " - "
            else ->
                KeyEvent.keyCodeToString(keyCode)
                    .split("_")
                    .last()
                    .lowercase()
                    .replaceFirstChar { it.titlecase(Locale.ENGLISH) }
        }
    }
}
