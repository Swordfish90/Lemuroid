package com.swordfish.lemuroid.app.shared.input

import android.content.Context
import android.view.InputDevice
import com.swordfish.lemuroid.app.shared.settings.GameMenuShortcut

interface InputClass {
    fun getInputKeys(): Set<InputKey>

    fun getCustomizableKeys(): List<RetroKey>

    fun getAxesMap(): Map<Int, Int>

    fun getDefaultBindings(): Map<InputKey, RetroKey>

    fun isSupported(): Boolean

    fun isEnabledByDefault(appContext: Context): Boolean

    fun getSupportedShortcuts(): List<GameMenuShortcut>
}

fun InputDevice?.getInputClass(): InputClass {
    return when {
        this == null -> InputClassUnknown
        (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD -> InputClassGamePad(this)
        (sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD -> InputClassKeyboard(this)
        else -> InputClassUnknown
    }
}
