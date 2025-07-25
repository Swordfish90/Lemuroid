package com.swordfish.lemuroid.app.shared.input.lemuroiddevice

import android.content.Context
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.RetroKey
import com.swordfish.lemuroid.app.shared.settings.GameShortcutType

object LemuroidInputDeviceUnknown : LemuroidInputDevice {
    override fun getDefaultBindings(): Map<InputKey, RetroKey> = emptyMap()

    override fun isSupported(): Boolean = false

    override fun isEnabledByDefault(appContext: Context): Boolean = false

    override fun getSupportedShortcuts(): List<GameShortcutType> = emptyList()

    override fun getCustomizableKeys(): List<RetroKey> = emptyList()
}
