package com.swordfish.lemuroid.app.shared.input

import android.content.Context
import android.content.Intent
import android.view.InputDevice
import android.view.KeyEvent
import com.swordfish.lemuroid.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking

/**
 * Captures a single physical button and binds it as the turbo trigger for a target RetroPad button.
 *
 * Mirrors [ShortcutBindingUpdater]/[InputBindingUpdater] but for turbo fire: on key release it
 * persists `target -> trigger` through [InputDeviceManager.updateTurboBinding].
 */
@OptIn(DelicateCoroutinesApi::class)
class TurboBindingUpdater(private val inputDeviceManager: InputDeviceManager, intent: Intent) {
    val extras = parseExtras(intent)

    fun getTitle(context: Context): String {
        val targetName = InputKey(extras.targetKeyCode).displayName()
        return context.getString(R.string.turbo_binding_update_title, targetName)
    }

    fun getMessage(context: Context): String {
        return context.getString(R.string.turbo_binding_update_description, extras.device.name)
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

        runBlocking {
            inputDeviceManager.updateTurboBinding(event.device, extras.targetKeyCode, event.keyCode)
        }

        return true
    }

    private fun isTargetedDevice(device: InputDevice?): Boolean {
        return device != null && extras.device.name == device.name
    }

    private fun parseExtras(intent: Intent): IntentExtras {
        val device =
            intent.extras?.getParcelable<InputDevice>(REQUEST_DEVICE)
                ?: throw IllegalArgumentException("REQUEST_DEVICE has not been passed")

        val targetKeyCode =
            intent.extras?.getInt(REQUEST_TARGET_KEY)
                ?: throw IllegalArgumentException("REQUEST_TARGET_KEY has not been passed")

        return IntentExtras(device, targetKeyCode)
    }

    data class IntentExtras(val device: InputDevice, val targetKeyCode: Int)

    companion object {
        const val REQUEST_DEVICE = "REQUEST_DEVICE"
        const val REQUEST_TARGET_KEY = "REQUEST_TARGET_KEY"
    }
}
