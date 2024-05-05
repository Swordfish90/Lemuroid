package com.swordfish.lemuroid.app.shared.settings

import android.view.InputDevice
import com.swordfish.lemuroid.app.shared.input.lemuroiddevice.getLemuroidInputDevice

data class GameMenuShortcut(val name: String, val keys: Set<Int>) {
    companion object {
        fun getDefault(inputDevice: InputDevice): GameMenuShortcut? {
            return inputDevice.getLemuroidInputDevice()
                .getSupportedShortcuts()
                .firstOrNull { shortcut ->
                    inputDevice.hasKeys(*(shortcut.keys.toIntArray())).all { it }
                }
        }

        fun findByName(
            device: InputDevice,
            name: String,
        ): GameMenuShortcut? {
            return device.getLemuroidInputDevice()
                .getSupportedShortcuts()
                .firstOrNull { it.name == name }
        }
    }
}
