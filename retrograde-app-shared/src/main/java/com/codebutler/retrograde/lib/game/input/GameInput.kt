package com.codebutler.retrograde.lib.game.input

import android.content.Context
import android.hardware.input.InputManager
import android.util.SparseArray
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.codebutler.retrograde.common.kotlin.containsAny
import com.codebutler.retrograde.lib.retro.Retro

class GameInput(context: Context) : InputManager.InputDeviceListener {
    private val inputManager = context.getSystemService(InputManager::class.java)
    private val state = State()

    init {
        inputManager.registerInputDeviceListener(this, null)
        inputManager.inputDeviceIds.forEach { deviceId -> onInputDeviceAdded(deviceId) }
    }

    fun deinit() {
        inputManager.unregisterInputDeviceListener(this)
    }

    fun onKeyEvent(event: KeyEvent) {
        val controllerNumber = state.getControllerNumberForDeviceId(event.deviceId)
        val pressedKeys = state.getPressedKeysForControllerNumber(controllerNumber)
        when (event.action) {
            KeyEvent.ACTION_DOWN -> pressedKeys.add(event.keyCode)
            KeyEvent.ACTION_UP -> pressedKeys.remove(event.keyCode)
        }
    }

    fun onMotionEvent(event: MotionEvent) {
        var left = false
        var right = false
        var up = false
        var down = false

        val coords = MotionEvent.PointerCoords()
        event.getPointerCoords(0, coords)

        when (coords.getAxisValue(MotionEvent.AXIS_HAT_X)) {
            1.0F -> right = true
            -1.0F -> left = true
        }

        when (coords.getAxisValue(MotionEvent.AXIS_HAT_Y)) {
            1.0F -> down = true
            -1.0F -> up = true
        }

        when (event.rawX) {
            1.0F -> right = true
            -1.0F -> left = true
        }

        when (event.rawY) {
            1.0F -> down = true
            -1.0F -> up = true
        }

        val controllerNumber = state.getControllerNumberForDeviceId(event.deviceId)
        val pressedKeys = state.getPressedKeysForControllerNumber(controllerNumber)

        fun updateKey(keyCode: Int, isPressed: Boolean) {
            if (isPressed) {
                pressedKeys.add(keyCode)
            } else {
                pressedKeys.remove(keyCode)
            }
        }

        updateKey(KeyEvent.KEYCODE_DPAD_LEFT, left)
        updateKey(KeyEvent.KEYCODE_DPAD_RIGHT, right)
        updateKey(KeyEvent.KEYCODE_DPAD_UP, up)
        updateKey(KeyEvent.KEYCODE_DPAD_DOWN, down)
    }

    override fun onInputDeviceAdded(deviceId: Int) {
        val device = inputManager.getInputDevice(deviceId)
        val isGameController = (device.sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
                device.sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)
        if (isGameController) {
            state.addDevice(device)
        }
    }

    override fun onInputDeviceChanged(deviceId: Int) {
        onInputDeviceAdded(deviceId)
    }

    override fun onInputDeviceRemoved(deviceId: Int) {
        state.removeDevice(deviceId)
    }

    fun isButtonPressed(port: Int, device: Int, id: Int): Boolean {
        val pressedKeys = state.getPressedKeysForControllerNumber(port + 1)
        when (Retro.Device.fromValue(device)) {
            Retro.Device.NONE -> { }
            Retro.Device.JOYPAD -> {
                return when (Retro.DeviceId.fromValue(id)) {
                    Retro.DeviceId.JOYPAD_A -> pressedKeys.containsAny(
                            KeyEvent.KEYCODE_A,
                            KeyEvent.KEYCODE_BUTTON_A)
                    Retro.DeviceId.JOYPAD_B -> pressedKeys.containsAny(
                            KeyEvent.KEYCODE_B,
                            KeyEvent.KEYCODE_BUTTON_B)
                    Retro.DeviceId.JOYPAD_DOWN -> pressedKeys.contains(KeyEvent.KEYCODE_DPAD_DOWN)
                    Retro.DeviceId.JOYPAD_L -> pressedKeys.contains(KeyEvent.KEYCODE_BUTTON_L1)
                    Retro.DeviceId.JOYPAD_L2 -> pressedKeys.contains(KeyEvent.KEYCODE_BUTTON_L2)
                    Retro.DeviceId.JOYPAD_L3 -> false
                    Retro.DeviceId.JOYPAD_LEFT -> pressedKeys.contains(KeyEvent.KEYCODE_DPAD_LEFT)
                    Retro.DeviceId.JOYPAD_R -> pressedKeys.contains(KeyEvent.KEYCODE_BUTTON_R1)
                    Retro.DeviceId.JOYPAD_R2 -> pressedKeys.contains(KeyEvent.KEYCODE_BUTTON_R2)
                    Retro.DeviceId.JOYPAD_R3 -> false
                    Retro.DeviceId.JOYPAD_RIGHT -> pressedKeys.contains(KeyEvent.KEYCODE_DPAD_RIGHT)
                    Retro.DeviceId.JOYPAD_SELECT -> pressedKeys.contains(KeyEvent.KEYCODE_BUTTON_SELECT)
                    Retro.DeviceId.JOYPAD_START -> pressedKeys.containsAny(
                            KeyEvent.KEYCODE_BUTTON_START,
                            KeyEvent.KEYCODE_ENTER)
                    Retro.DeviceId.JOYPAD_UP -> pressedKeys.contains(KeyEvent.KEYCODE_DPAD_UP)
                    Retro.DeviceId.JOYPAD_X -> pressedKeys.containsAny(
                            KeyEvent.KEYCODE_BUTTON_X,
                            KeyEvent.KEYCODE_X)
                    Retro.DeviceId.JOYPAD_Y -> pressedKeys.containsAny(
                            KeyEvent.KEYCODE_BUTTON_Y,
                            KeyEvent.KEYCODE_Y)
                    else -> return false
                }
            }
            Retro.Device.MOUSE -> TODO()
            Retro.Device.KEYBOARD -> return false
            Retro.Device.LIGHTGUN -> TODO()
            Retro.Device.ANALOG -> return false
            Retro.Device.POINTER -> TODO()
        }
        return false
    }

    private class State {
        /**
         * Maps Android InputDevice IDs to Android controller numbers.
         *
         * @see InputDevice.getId
         * @see InputDevice.getControllerNumber
         */
        private val deviceIdToControllerNumber: SparseArray<Int> = SparseArray()

        /**
         * Gets pressed keys for a controller by number.
         * NOTE: The first valid number is 1.
         *
         * @see InputDevice.getControllerNumber
         */
        private val pressedKeysForControllerNumber: SparseArray<MutableSet<Int>> = SparseArray()

        fun getPressedKeysForControllerNumber(controllerNumber: Int): MutableSet<Int> {
            if (pressedKeysForControllerNumber.get(controllerNumber) == null) {
                pressedKeysForControllerNumber.put(controllerNumber, mutableSetOf())
            }
            return pressedKeysForControllerNumber.get(controllerNumber)
        }

        // Assume Player 1 for any unknown devices.
        fun getControllerNumberForDeviceId(deviceId: Int): Int =
                deviceIdToControllerNumber.get(deviceId, 1)

        fun addDevice(device: InputDevice) {
            deviceIdToControllerNumber.put(device.id, device.controllerNumber)
        }

        fun removeDevice(deviceId: Int) {
            deviceIdToControllerNumber.remove(deviceId)
        }
    }
}
