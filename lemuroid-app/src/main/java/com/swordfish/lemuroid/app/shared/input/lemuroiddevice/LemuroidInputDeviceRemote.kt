package com.swordfish.lemuroid.app.shared.input.lemuroiddevice

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.RetroKey
import com.swordfish.lemuroid.app.shared.input.bindingsOf
import com.swordfish.lemuroid.app.shared.input.inputKeysOf
import com.swordfish.lemuroid.app.shared.input.supportsAllKeys
import com.swordfish.lemuroid.app.shared.settings.GameShortcutType

class LemuroidInputDeviceRemote(private val device: InputDevice) : LemuroidInputDevice {
    override fun getCustomizableKeys(): List<RetroKey> = InputDeviceManager.OUTPUT_KEYS

    override fun getDefaultBindings() = DEFAULT_BINDINGS

    override fun isEnabledByDefault(appContext: Context): Boolean {
        return device.supportsAllKeys(MINIMAL_SUPPORTED_KEYS)
    }

    override fun getSupportedShortcuts(): List<GameShortcutType> = emptyList()

    override fun isSupported(): Boolean {
        return sequenceOf(
            (device.sources and InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD,
            device.supportsAllKeys(MINIMAL_SUPPORTED_KEYS),
            device.isVirtual.not(),
        ).all { it }
    }

    companion object {
        private val MINIMAL_SUPPORTED_KEYS =
            inputKeysOf(
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_MENU,
                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_MEDIA_REWIND,
            )

        private val DEFAULT_BINDINGS =
            bindingsOf(
                KeyEvent.KEYCODE_DPAD_UP to KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_DOWN to KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_LEFT to KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_RIGHT to KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_CENTER to KeyEvent.KEYCODE_BUTTON_START,
                KeyEvent.KEYCODE_MENU to KeyEvent.KEYCODE_BUTTON_X,
                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD to KeyEvent.KEYCODE_BUTTON_Y,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE to KeyEvent.KEYCODE_BUTTON_A,
                KeyEvent.KEYCODE_MEDIA_REWIND  to KeyEvent.KEYCODE_BUTTON_B,
            )
    }
}
