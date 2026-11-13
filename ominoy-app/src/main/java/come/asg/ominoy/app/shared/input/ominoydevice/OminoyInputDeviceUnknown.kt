package come.asg.ominoy.app.shared.input.ominoydevice

import android.content.Context
import come.asg.ominoy.app.shared.input.InputKey
import come.asg.ominoy.app.shared.input.RetroKey
import come.asg.ominoy.app.shared.settings.GameShortcutType

object OminoyInputDeviceUnknown : OminoyInputDevice {
    override fun getDefaultBindings(): Map<InputKey, RetroKey> = emptyMap()

    override fun isSupported(): Boolean = false

    override fun isEnabledByDefault(appContext: Context): Boolean = false

    override fun getSupportedShortcuts(): List<GameShortcutType> = emptyList()

    override fun getCustomizableKeys(): List<RetroKey> = emptyList()
}
