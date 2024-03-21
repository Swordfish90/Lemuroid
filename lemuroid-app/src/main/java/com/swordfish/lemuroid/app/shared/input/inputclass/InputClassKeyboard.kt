package com.swordfish.lemuroid.app.shared.input.inputclass

import android.view.KeyEvent
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.inputKeySetOf

object InputClassKeyboard : InputClass {
    override fun getInputKeys(): Set<InputKey> {
        return INPUT_KEYS
    }

    override fun getAxesMap(): Map<Int, Int> {
        return emptyMap()
    }

    private val INPUT_KEYS =
        inputKeySetOf(
            KeyEvent.KEYCODE_Q,
            KeyEvent.KEYCODE_W,
            KeyEvent.KEYCODE_E,
            KeyEvent.KEYCODE_R,
            KeyEvent.KEYCODE_T,
            KeyEvent.KEYCODE_Y,
            KeyEvent.KEYCODE_U,
            KeyEvent.KEYCODE_I,
            KeyEvent.KEYCODE_O,
            KeyEvent.KEYCODE_P,
            KeyEvent.KEYCODE_A,
            KeyEvent.KEYCODE_S,
            KeyEvent.KEYCODE_D,
            KeyEvent.KEYCODE_F,
            KeyEvent.KEYCODE_G,
            KeyEvent.KEYCODE_H,
            KeyEvent.KEYCODE_J,
            KeyEvent.KEYCODE_K,
            KeyEvent.KEYCODE_L,
            KeyEvent.KEYCODE_Z,
            KeyEvent.KEYCODE_X,
            KeyEvent.KEYCODE_C,
            KeyEvent.KEYCODE_V,
            KeyEvent.KEYCODE_B,
            KeyEvent.KEYCODE_N,
            KeyEvent.KEYCODE_M,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_SHIFT_LEFT,
            KeyEvent.KEYCODE_ESCAPE,
        )
}
