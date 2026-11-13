package come.asg.ominoy.app.shared.input.ominoydevice

import android.content.Context
import android.view.InputDevice
import come.asg.ominoy.app.shared.input.InputKey
import come.asg.ominoy.app.shared.input.RetroKey
import come.asg.ominoy.app.shared.settings.GameShortcutType

interface OminoyInputDevice {
    fun getCustomizableKeys(): List<RetroKey>

    fun getDefaultBindings(): Map<InputKey, RetroKey>

    fun isSupported(): Boolean

    fun isEnabledByDefault(appContext: Context): Boolean

    fun getSupportedShortcuts(): List<GameShortcutType>
}

fun InputDevice?.getOminoyInputDevice(): OminoyInputDevice =
    when {
        this == null -> OminoyInputDeviceUnknown
        (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD -> OminoyInputDeviceGamePad(this)
        (sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD -> OminoyInputDeviceKeyboard(this)
        else -> OminoyInputDeviceUnknown
    }
