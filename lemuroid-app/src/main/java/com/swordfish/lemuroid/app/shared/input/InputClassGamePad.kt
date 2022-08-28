package com.swordfish.lemuroid.app.shared.input

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.swordfish.lemuroid.app.shared.settings.GameMenuShortcut

class InputClassGamePad(private val device: InputDevice) : InputClass {

    override fun getInputKeys() = INPUT_KEYS.toSet()

    override fun getAxesMap() = AXES_MAP

    override fun getDefaultBindings(): Map<InputKey, RetroKey> {
        val allAvailableInputs = InputDeviceManager.OUTPUT_KEYS
            .associate {
                InputKey(it.keyCode) to getDefaultBindingForKey(device, it)
            }

        val defaultOverride = bindingsOf(
            KeyEvent.KEYCODE_BUTTON_A to KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_B to KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_X to KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_Y to KeyEvent.KEYCODE_BUTTON_X
        )

        return allAvailableInputs + defaultOverride
    }

    private fun getDefaultBindingForKey(
        device: InputDevice,
        it: RetroKey
    ): RetroKey {
        val defaultBinding = if (device.hasKeys(it.keyCode).first()) {
            RetroKey(it.keyCode)
        } else {
            RetroKey(KeyEvent.KEYCODE_UNKNOWN)
        }
        return defaultBinding
    }

    override fun isEnabledByDefault(appContext: Context): Boolean {
        return device.supportsAllKeys(MINIMAL_KEYS_DEFAULT_ENABLED)
    }

    override fun getSupportedShortcuts(): List<GameMenuShortcut> = listOf(
        GameMenuShortcut(
            "L3 + R3",
            setOf(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR)
        ),
        GameMenuShortcut(
            "Select + Start",
            setOf(KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_SELECT)
        )
    )

    override fun isSupported(): Boolean {
        return sequenceOf(
            device.sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD,
            device.supportsAllKeys(MINIMAL_SUPPORTED_KEYS),
            device.isVirtual.not(),
            device.controllerNumber > 0
        ).all { it }
    }

    override fun getCustomizableKeys(): List<RetroKey> {
        val deviceAxis = device.motionRanges
            .map { it.axis }
            .toSet()

        val keysMappedToAxis = getAxesMap()
            .filter { it.key in deviceAxis }
            .map { it.value }
            .toSet()

        return CUSTOMIZABLE_KEYS
            .filter { it.keyCode !in keysMappedToAxis }
    }

    companion object {
        private val MINIMAL_SUPPORTED_KEYS = inputKeysOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
        )

        private val MINIMAL_KEYS_DEFAULT_ENABLED = MINIMAL_SUPPORTED_KEYS + inputKeysOf(
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT,
        )

        private val INPUT_KEYS = inputKeysOf(
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
            KeyEvent.KEYCODE_BACK
        )

        private val AXES_MAP = mapOf(
            MotionEvent.AXIS_BRAKE to KeyEvent.KEYCODE_BUTTON_L2,
            MotionEvent.AXIS_THROTTLE to KeyEvent.KEYCODE_BUTTON_R2,
            MotionEvent.AXIS_LTRIGGER to KeyEvent.KEYCODE_BUTTON_L2,
            MotionEvent.AXIS_RTRIGGER to KeyEvent.KEYCODE_BUTTON_R2
        )

        private val CUSTOMIZABLE_KEYS: List<RetroKey> = retroKeysOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_MODE,
        )
    }
}
