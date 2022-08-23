package com.swordfish.lemuroid.app.shared.input

import android.content.Context
import android.content.SharedPreferences
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import com.swordfish.lemuroid.app.shared.settings.GameMenuShortcut
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.withContext

class InputDeviceManager(
    private val context: Context,
    sharedPreferencesFactory: Lazy<SharedPreferences>
) {

    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager

    private val sharedPreferences by lazy { sharedPreferencesFactory.get() }

    fun getInputBindingsObservable(): Flow<(InputDevice?) -> Map<Int, Int>> {
        return getEnabledInputsObservable()
            .map { inputDevices ->
                inputDevices.associateWith { getBindings(it) }
            }
            .map { bindings -> { bindings[it] ?: mapOf() } }
    }

    fun getInputMenuShortCutObservable(): Flow<GameMenuShortcut?> {
        return getEnabledInputsObservable()
            .map { devices ->
                val device = devices.firstOrNull()
                device
                    ?.let {
                        sharedPreferences.getString(
                            computeGameMenuShortcutPreference(it),
                            GameMenuShortcut.getDefault(it)?.name
                        )
                    }
                    ?.let { GameMenuShortcut.findByName(device, it) }
            }
    }

    fun getGamePadsPortMapperObservable(): Flow<(InputDevice?) -> Int?> {
        return getEnabledInputsObservable().map { gamePads ->
            val portMappings = gamePads
                .mapIndexed { index, inputDevice -> inputDevice.id to index }
                .toMap()
            return@map { inputDevice -> portMappings[inputDevice?.id] }
        }
    }

    private suspend fun getBindings(inputDevice: InputDevice): Map<Int, Int> {
        return inputDevice.getInputClass().getInputKeys()
            .associateWith { keyCode ->
                val mappedKeyCode = retrieveMappingFromPreferences(
                    inputDevice,
                    keyCode
                )
                mappedKeyCode
            }
    }

    suspend fun resetAllBindings() = withContext(Dispatchers.IO) {
        val editor = sharedPreferences.edit()
        sharedPreferences.all.keys
            .filter { it.startsWith(GAME_PAD_BINDING_PREFERENCE_BASE_KEY) }
            .forEach { editor.remove(it) }
        editor.commit()
    }

    fun getGamePadsObservable(): Flow<List<InputDevice>> {
        val result = MutableStateFlow(getAllGamePads())

        val listener = object : InputManager.InputDeviceListener {
            override fun onInputDeviceAdded(deviceId: Int) {
                result.value = getAllGamePads()
            }

            override fun onInputDeviceChanged(deviceId: Int) {
                result.value = getAllGamePads()
            }

            override fun onInputDeviceRemoved(deviceId: Int) {
                result.value = getAllGamePads()
            }
        }

        return result
            .onSubscription { inputManager.registerInputDeviceListener(listener, null) }
            .onCompletion { inputManager.unregisterInputDeviceListener(listener) }
    }

    fun getEnabledInputsObservable(): Flow<List<InputDevice>> {
        return getGamePadsObservable()
            .map { devices ->
                devices.filter { isDeviceEnabled(it) }
            }
    }

    private suspend fun isDeviceEnabled(device: InputDevice): Boolean = withContext(Dispatchers.IO) {
        val defaultValue = device.getInputClass().isEnabledByDefault(context, device)
        sharedPreferences.getBoolean(computeEnabledGamePadPreference(device), defaultValue)
    }

    private suspend fun retrieveMappingFromPreferences(
        inputDevice: InputDevice,
        keyCode: Int
    ): Int = withContext(Dispatchers.IO) {
        val sharedPreferencesKey = computeKeyBindingPreference(inputDevice, keyCode)
        val sharedPreferencesDefault = getDefaultBinding(inputDevice, keyCode).toString()
        val result = sharedPreferences.getString(sharedPreferencesKey, sharedPreferencesDefault)!!

        result.toInt()
    }

    fun getDefaultBinding(inputDevice: InputDevice, keyCode: Int): Int {
        return inputDevice
            .getInputClass()
            .getDefaultBindings()
            .getValue(keyCode)
    }

    private fun getAllGamePads(): List<InputDevice> {
        return runCatching {
            InputDevice.getDeviceIds()
                .map { InputDevice.getDevice(it) }
                .filter { it.getInputClass().isSupported(it) }
                .filter { it.name !in BLACKLISTED_DEVICES }
                .sortedBy { it.controllerNumber }
        }.getOrNull() ?: listOf()
    }

    companion object {
        private const val GAME_PAD_BINDING_PREFERENCE_BASE_KEY = "pref_key_gamepad_binding"
        private const val GAME_PAD_ENABLED_PREFERENCE_BASE_KEY = "pref_key_gamepad_enabled"

        private fun getSharedPreferencesId(inputDevice: InputDevice) = inputDevice.descriptor

        // This is a last resort, but sadly there are some devices which present keys and the
        // SOURCE_GAMEPAD, so we basically black list them.
        private val BLACKLISTED_DEVICES = setOf(
            "virtual-search"
        )

        fun computeEnabledGamePadPreference(inputDevice: InputDevice) =
            "${GAME_PAD_ENABLED_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}"

        fun computeGameMenuShortcutPreference(inputDevice: InputDevice) =
            "${GAME_PAD_BINDING_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}_gamemenu"

        fun computeKeyBindingPreference(inputDevice: InputDevice, keyCode: Int) =
            "${GAME_PAD_BINDING_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}_$keyCode"

        val OUTPUT_KEYS = listOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_MODE,
            KeyEvent.KEYCODE_UNKNOWN,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_BACK
        )
    }
}
