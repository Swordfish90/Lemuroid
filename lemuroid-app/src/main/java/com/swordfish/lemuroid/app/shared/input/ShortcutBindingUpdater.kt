package com.swordfish.lemuroid.app.shared.input

import android.content.Context
import android.content.Intent
import android.view.InputDevice
import android.view.KeyEvent
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.GameShortcutType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking

@OptIn(DelicateCoroutinesApi::class)
class ShortcutBindingUpdater(private val inputDeviceManager: InputDeviceManager, intent: Intent) {
    val extras = parseExtras(intent)

    private var firstKeyCodeInCombo: Int? = null

    fun getTitle(context: Context): String {
        return context.getString(R.string.shortcut_binding_update_title, extras.shortcutType.displayName())
    }

    fun getMessage(context: Context): String {
        return context.getString(R.string.shortcut_binding_update_description, extras.device.name)
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        return when (event.action) {
            KeyEvent.ACTION_DOWN -> onKeyDown(event)
            KeyEvent.ACTION_UP -> onKeyUp(event)
            else -> false
        }
    }

    private fun onKeyDown(event: KeyEvent): Boolean {
        return isTargetedDevice(event.device)
    }

    private fun onKeyUp(event: KeyEvent): Boolean {
        if (!isTargetedDevice(event.device)) return false

        if (firstKeyCodeInCombo == null) {
            firstKeyCodeInCombo = event.keyCode
            return false // wait for second key
        } else {
            if (firstKeyCodeInCombo == event.keyCode) return false // ignore same key press
            val combo = Pair(InputKey(firstKeyCodeInCombo!!), InputKey(event.keyCode))
            // TODO runBlocking here should go away.
            runBlocking {
                inputDeviceManager.updateShortcutBinding(event.device, extras.shortcutType, combo)
            }
            return true
        }
    }

    private fun isTargetedDevice(device: InputDevice?): Boolean {
        return device != null && extras.device.name == device.name
    }

    private fun parseExtras(intent: Intent): IntentExtras {
        val device =
            intent.extras?.getParcelable<InputDevice>(REQUEST_DEVICE)
                ?: throw IllegalArgumentException("REQUEST_DEVICE has not been passed")

        val shortcutType =
            intent.extras?.getString(REQUEST_SHORTCUT_TYPE)
                ?: throw IllegalArgumentException("REQUEST_SHORTCUT_TYPE has not been passed")

        return IntentExtras(device, GameShortcutType.valueOf(shortcutType))
    }

    data class IntentExtras(val device: InputDevice, val shortcutType: GameShortcutType)

    companion object {
        const val REQUEST_DEVICE = "REQUEST_DEVICE"
        const val REQUEST_SHORTCUT_TYPE = "REQUEST_SHORTCUT_TYPE"
    }
}
