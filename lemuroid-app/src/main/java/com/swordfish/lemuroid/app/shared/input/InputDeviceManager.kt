package com.swordfish.lemuroid.app.shared.input

import android.content.Context
import android.content.SharedPreferences
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import androidx.core.content.edit
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.app.shared.input.lemuroiddevice.getLemuroidInputDevice
import com.swordfish.lemuroid.app.shared.settings.GameShortcut
import com.swordfish.lemuroid.app.shared.settings.GameShortcutType
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.json.Json

@OptIn(ExperimentalCoroutinesApi::class)
class InputDeviceManager(
    private val context: Context,
    sharedPreferencesFactory: Lazy<SharedPreferences>,
) {
    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager

    private val sharedPreferences by lazy { sharedPreferencesFactory.get() }

    private val flowSharedPreferences by lazy { FlowSharedPreferences(sharedPreferences) }

    fun getInputBindingsObservable(): Flow<(InputDevice?) -> Map<InputKey, RetroKey>> {
        return getEnabledInputsObservable()
            .flatMapLatest { devices ->
                val allDeviceBindingsFlows = devices.map { device -> getBindingsFlow(device).map { device to it } }
                combine(allDeviceBindingsFlows) { it.toMap() }
            }
            .map { bindings -> { bindings[it] ?: mapOf() } }
    }

    fun getGameShortcutsObservable(): Flow<Map<InputDevice, List<GameShortcut>>> {
        return getEnabledInputsObservable()
            .flatMapLatest { devices ->
                val allShortcutFlows = devices.map { device -> getShortcutBindingsFlow(device).map { device to it } }
                combine(allShortcutFlows) { it.toMap() }
            }
    }

    fun getGamePadsPortMapperObservable(): Flow<(InputDevice?) -> Int?> {
        return getEnabledInputsObservable().map { gamePads ->
            val portMappings =
                gamePads
                    .mapIndexed { index, inputDevice -> inputDevice.id to index }
                    .toMap()
            return@map { inputDevice -> portMappings[inputDevice?.id] }
        }
    }

    private fun getBindingsFlow(inputDevice: InputDevice): Flow<Map<InputKey, RetroKey>> {
        return flowSharedPreferences.getString(computeKeyBindingGamePadPreference(inputDevice))
            .asFlow()
            .map { parseBindingsPreference(it, inputDevice) }
            .flowOn(Dispatchers.IO)
    }

    private fun getShortcutBindingsFlow(device: InputDevice): Flow<List<GameShortcut>> {
        val flows =
            GameShortcutType.entries.map { type ->
                flowSharedPreferences.getString(computeGameShortcutPreference(device, type))
                    .asFlow()
                    .map { parseShortcutPreference(it, device, type) }
            }
        return if (flows.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(flows) { shortcuts -> shortcuts.mapNotNull { it } }
        }.flowOn(Dispatchers.IO)
    }

    private fun parseShortcutPreference(
        preference: String?,
        device: InputDevice,
        type: GameShortcutType,
    ): GameShortcut? {
        return if (preference.isNullOrEmpty()) {
            GameShortcut.getDefault(device, type)
        } else {
            val decoded = runCatching { Json.decodeFromString(bindingsComboSerializer, preference) }
            val combo = decoded.getOrNull() ?: return GameShortcut.getDefault(device, type)
            GameShortcut(type = type, keys = setOf(combo.first.keyCode, combo.second.keyCode))
        }
    }

    suspend fun getCurrentBindings(inputDevice: InputDevice): Map<InputKey, RetroKey> {
        return withContext(Dispatchers.IO) {
            val preference =
                sharedPreferences.getString(
                    computeKeyBindingGamePadPreference(inputDevice),
                    "",
                )
            parseBindingsPreference(preference, inputDevice)
        }
    }

    suspend fun getCurrentShortcuts(inputDevice: InputDevice): List<GameShortcut> {
        return withContext(Dispatchers.IO) {
            GameShortcutType.entries.mapNotNull { type ->
                val preference = sharedPreferences.getString(computeGameShortcutPreference(inputDevice, type), "")
                parseShortcutPreference(preference, inputDevice, type)
            }
        }
    }

    private fun parseBindingsPreference(
        preference: String?,
        inputDevice: InputDevice,
    ): Map<InputKey, RetroKey> {
        val defaultBindings = getDefaultBinding(inputDevice)
        if (preference.isNullOrEmpty()) {
            return defaultBindings
        }

        val decoded = runCatching { Json.decodeFromString(bindingsMapSerializer, preference) }
        return decoded.getOrDefault(defaultBindings)
    }

    suspend fun updateBinding(
        inputDevice: InputDevice,
        retroKey: RetroKey,
        inputKey: InputKey,
    ) = withContext(Dispatchers.IO) {
        val prevBindings =
            getCurrentBindings(inputDevice).entries
                .map { it.key to it.value }
                .filter { (_, value) -> value != retroKey }

        val bindings = prevBindings + listOf(inputKey to retroKey)

        val sharedPreferencesContent = Json.encodeToString(bindingsMapSerializer, bindings.toMap())

        sharedPreferences.edit(commit = true) {
            putString(computeKeyBindingGamePadPreference(inputDevice), sharedPreferencesContent)
        }
    }

    suspend fun updateShortcutBinding(
        inputDevice: InputDevice,
        shortcutType: GameShortcutType,
        inputKeys: Pair<InputKey, InputKey>,
    ) = withContext(Dispatchers.IO) {
        sharedPreferences.edit(commit = true) {
            val key = computeGameShortcutPreference(inputDevice, shortcutType)
            val value = Json.encodeToString(bindingsComboSerializer, inputKeys)
            putString(key, value)
        }
    }

    suspend fun resetAllBindings() =
        withContext(Dispatchers.IO) {
            sharedPreferences.edit(commit = true) {
                sharedPreferences.all.keys
                    .filter { it.startsWith(GAME_PAD_BINDING_PREFERENCE_BASE_KEY) }
                    .forEach { remove(it) }
            }
        }

    fun getGamePadsObservable(): Flow<List<InputDevice>> {
        val result = MutableStateFlow(getAllGamePads())

        val listener =
            object : InputManager.InputDeviceListener {
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

    fun getDistinctGamePadsObservable(): Flow<List<InputDevice>> {
        return getGamePadsObservable()
            .map { device -> device.distinctBy { it.descriptor } }
    }

    fun getEnabledInputsObservable(): Flow<List<InputDevice>> {
        return getGamePadsObservable()
            .flatMapLatest { devices ->
                if (devices.isEmpty()) {
                    return@flatMapLatest flowOf(emptyList())
                }

                val deviceStatuesFlows = devices.map { getDeviceStatus(it) }
                combine(deviceStatuesFlows) { deviceStatues ->
                    deviceStatues
                        .filter { it.enabled }
                        .map { it.device }
                }
            }
    }

    private fun getDeviceStatus(inputDevice: InputDevice): Flow<DeviceStatus> {
        val defaultValue = inputDevice.getLemuroidInputDevice().isEnabledByDefault(context)
        return flowSharedPreferences.getBoolean(computeEnabledGamePadPreference(inputDevice), defaultValue)
            .asFlow()
            .map { isEnabled -> DeviceStatus(inputDevice, isEnabled) }
    }

    private fun getDefaultBinding(inputDevice: InputDevice): Map<InputKey, RetroKey> {
        return inputDevice
            .getLemuroidInputDevice()
            .getDefaultBindings()
    }

    private fun getAllGamePads(): List<InputDevice> {
        return runCatching {
            InputDevice.getDeviceIds()
                .map { InputDevice.getDevice(it) }
                .filterNotNull()
                .filter { it.getLemuroidInputDevice().isSupported() }
                .filter { it.name !in BLACKLISTED_DEVICES }
                .sortedBy { it.controllerNumber }
        }.getOrNull() ?: listOf()
    }

    private data class DeviceStatus(val device: InputDevice, val enabled: Boolean)

    companion object {
        private const val GAME_PAD_BINDING_PREFERENCE_BASE_KEY = "pref_key_gamepad_binding_key"
        private const val GAME_PAD_ENABLED_PREFERENCE_BASE_KEY = "pref_key_gamepad_enabled"

        private val bindingsMapSerializer = MapSerializer(InputKey.serializer(), RetroKey.serializer())
        private val bindingsComboSerializer = PairSerializer(InputKey.serializer(), InputKey.serializer())

        private fun getSharedPreferencesId(inputDevice: InputDevice) = inputDevice.descriptor

        // This is a last resort, but sadly there are some devices which present keys and the
        // SOURCE_GAMEPAD, so we basically black list them.
        private val BLACKLISTED_DEVICES =
            setOf(
                "virtual-search",
            )

        fun computeEnabledGamePadPreference(inputDevice: InputDevice) =
            "${GAME_PAD_ENABLED_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}"

        fun computeGameShortcutPreference(
            inputDevice: InputDevice,
            type: GameShortcutType,
        ) = "${GAME_PAD_BINDING_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}_shortcut_$type."

        fun computeKeyBindingGamePadPreference(inputDevice: InputDevice) =
            "${GAME_PAD_BINDING_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}"

        fun computeKeyBindingRetroKeyPreference(
            inputDevice: InputDevice,
            retroKey: RetroKey,
        ): String {
            val keyCode = retroKey.keyCode
            return "${GAME_PAD_BINDING_PREFERENCE_BASE_KEY}_${getSharedPreferencesId(inputDevice)}_$keyCode"
        }

        val OUTPUT_KEYS: List<RetroKey> =
            retroKeysOf(
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
            )
    }
}
