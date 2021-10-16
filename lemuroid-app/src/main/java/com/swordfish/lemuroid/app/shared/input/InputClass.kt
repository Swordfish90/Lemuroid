package com.swordfish.lemuroid.app.shared.input

import android.content.Context
import android.view.InputDevice
import com.swordfish.lemuroid.app.shared.settings.GameMenuShortcut

interface InputClass {
    fun getInputKeys(): List<Int>

    fun getAxesMap(): Map<Int, Int>

    fun getDefaultBindings(): Map<Int, Int>

    fun isSupported(device: InputDevice): Boolean

    fun isEnabledByDefault(appContext: Context): Boolean

    fun getCustomizableKeys(): List<Int>

    fun getSupportedShortcuts(): List<GameMenuShortcut>
}

fun InputDevice.getInputClass(): InputClass {
    return when {
        (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD -> InputClassGamePad
        (sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD -> InputClassKeyboard
        else -> InputClassUnknown
    }
}
