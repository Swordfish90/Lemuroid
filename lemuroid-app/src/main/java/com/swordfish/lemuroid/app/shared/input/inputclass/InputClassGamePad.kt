package com.swordfish.lemuroid.app.shared.input.inputclass

import android.view.KeyEvent
import android.view.MotionEvent
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.inputKeySetOf

object InputClassGamePad : InputClass {
    override fun getInputKeys(): Set<InputKey> {
        return INPUT_KEYS
    }

    override fun getAxesMap(): Map<Int, Int> {
        return AXES_MAP
    }

    private val AXES_MAP =
        mapOf(
            MotionEvent.AXIS_BRAKE to KeyEvent.KEYCODE_BUTTON_L2,
            MotionEvent.AXIS_THROTTLE to KeyEvent.KEYCODE_BUTTON_R2,
            MotionEvent.AXIS_LTRIGGER to KeyEvent.KEYCODE_BUTTON_L2,
            MotionEvent.AXIS_RTRIGGER to KeyEvent.KEYCODE_BUTTON_R2,
        )

    private val INPUT_KEYS =
        inputKeySetOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_C,
            KeyEvent.KEYCODE_BUTTON_Z,
            KeyEvent.KEYCODE_BUTTON_1,
            KeyEvent.KEYCODE_BUTTON_2,
            KeyEvent.KEYCODE_BUTTON_3,
            KeyEvent.KEYCODE_BUTTON_4,
            KeyEvent.KEYCODE_BUTTON_5,
            KeyEvent.KEYCODE_BUTTON_6,
            KeyEvent.KEYCODE_BUTTON_7,
            KeyEvent.KEYCODE_BUTTON_8,
            KeyEvent.KEYCODE_BUTTON_9,
            KeyEvent.KEYCODE_BUTTON_10,
            KeyEvent.KEYCODE_BUTTON_11,
            KeyEvent.KEYCODE_BUTTON_12,
            KeyEvent.KEYCODE_BUTTON_13,
            KeyEvent.KEYCODE_BUTTON_14,
            KeyEvent.KEYCODE_BUTTON_15,
            KeyEvent.KEYCODE_BUTTON_16,
            KeyEvent.KEYCODE_BUTTON_MODE,
            KeyEvent.KEYCODE_BACK,
        )
}
