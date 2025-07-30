package com.swordfish.lemuroid.app.shared.input.lemuroiddevice

import android.content.Context
import android.view.InputDevice
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.RetroKey
import com.swordfish.lemuroid.app.shared.settings.GameShortcutType

interface LemuroidInputDevice {
    fun getCustomizableKeys(): List<RetroKey>

    fun getDefaultBindings(): Map<InputKey, RetroKey>

    fun isSupported(): Boolean

    fun isEnabledByDefault(appContext: Context): Boolean

    fun getSupportedShortcuts(): List<GameShortcutType>
}

fun InputDevice?.getLemuroidInputDevice(): LemuroidInputDevice {
    return when {
        this == null -> LemuroidInputDeviceUnknown
        (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD -> LemuroidInputDeviceGamePad(this)
        (sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD -> LemuroidInputDeviceKeyboard(this)
        else -> LemuroidInputDeviceUnknown
    }
}
