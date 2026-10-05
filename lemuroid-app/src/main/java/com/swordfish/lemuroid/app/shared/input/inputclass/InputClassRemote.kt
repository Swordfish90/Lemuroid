package com.swordfish.lemuroid.app.shared.input.inputclass

import android.view.KeyEvent
import android.view.MotionEvent
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.inputKeySetOf

object InputClassRemote : InputClass {
    override fun getInputKeys(): Set<InputKey> {
        return INPUT_KEYS
    }

    override fun getAxesMap(): Map<Int, Int> {
        return emptyMap()
    }

    private val INPUT_KEYS =
        inputKeySetOf(
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        )
}
