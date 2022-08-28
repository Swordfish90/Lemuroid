package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.getInputClass

class GamePadPreferencesHelper(private val inputDeviceManager: InputDeviceManager) {

    fun addGamePadsPreferencesToScreen(
        context: Context,
        preferenceScreen: PreferenceScreen,
        gamePads: List<InputDevice>
    ) {
        val distinctGamePads = gamePads.distinctBy { it.descriptor }

        addEnabledCategory(context, preferenceScreen, distinctGamePads)

        distinctGamePads
            .forEach { addPreferenceCategoryForInputDevice(context, preferenceScreen, it) }

        addExtraCategory(context, preferenceScreen)
    }

    private fun addEnabledCategory(
        context: Context,
        preferenceScreen: PreferenceScreen,
        gamePads: List<InputDevice>
    ) {
        if (gamePads.isEmpty())
            return

        val categoryTitle = context.resources.getString(R.string.settings_gamepad_category_enabled)
        val category = createCategory(context, preferenceScreen, categoryTitle)
        preferenceScreen.addPreference(category)

        gamePads.forEach { gamePad ->
            category.addPreference(buildGamePadEnabledPreference(context, gamePad))
        }
    }

    private fun addExtraCategory(context: Context, preferenceScreen: PreferenceScreen) {
        val categoryTitle = context.resources.getString(R.string.settings_gamepad_category_general)
        val category = createCategory(context, preferenceScreen, categoryTitle)

        Preference(context).apply {
            key = context.resources.getString(R.string.pref_key_reset_gamepad_bindings)
            title = context.resources.getString(R.string.settings_gamepad_title_reset_bindings)
            isIconSpaceReserved = false
            category.addPreference(this)
        }
    }

    private fun createCategory(
        context: Context,
        preferenceScreen: PreferenceScreen,
        title: String
    ): PreferenceCategory {
        val category = PreferenceCategory(context)
        preferenceScreen.addPreference(category)
        category.title = title
        category.isIconSpaceReserved = false
        return category
    }

    private fun addPreferenceCategoryForInputDevice(
        context: Context,
        preferenceScreen: PreferenceScreen,
        inputDevice: InputDevice
    ) {
        val category = createCategory(context, preferenceScreen, inputDevice.name)
        preferenceScreen.addPreference(category)

        inputDevice.getInputClass().getCustomizableKeys()
            .filter { inputDevice.hasKeys(it)[0] }
            .map { buildKeyBindingPreference(context, inputDevice, it) }
            .forEach {
                category.addPreference(it)
            }

        buildGameMenuShortcutPreference(context, inputDevice)?.let {
            category.addPreference(it)
        }
    }

    private fun buildGamePadEnabledPreference(
        context: Context,
        inputDevice: InputDevice
    ): Preference {
        val preference = SwitchPreference(context)
        preference.key = InputDeviceManager.computeEnabledGamePadPreference(inputDevice)
        preference.title = inputDevice.name
        preference.setDefaultValue(inputDevice.getInputClass().isEnabledByDefault(context, inputDevice))
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun buildKeyBindingPreference(
        context: Context,
        inputDevice: InputDevice,
        key: Int
    ): Preference {
        val outputKeys = InputDeviceManager.OUTPUT_KEYS
        val outputKeysName = outputKeys.map { getRetroPadKeyName(context, it) }
        val defaultBinding = inputDeviceManager.getDefaultBinding(inputDevice, key)

        val preference = ListPreference(context)
        preference.key = InputDeviceManager.computeKeyBindingPreference(inputDevice, key)
        preference.title = getButtonKeyName(context, key)
        preference.entries = outputKeysName.toTypedArray()
        preference.entryValues = outputKeys.map { it.toString() }.toTypedArray()
        preference.setValueIndex(outputKeys.indexOf(defaultBinding))
        preference.setDefaultValue(defaultBinding.toString())
        preference.summaryProvider = Preference.SummaryProvider<ListPreference> {
            getRetroPadKeyName(context, it.value.toInt())
        }
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun buildGameMenuShortcutPreference(
        context: Context,
        inputDevice: InputDevice
    ): Preference? {
        val default = GameMenuShortcut.getDefault(inputDevice) ?: return null
        val supportedShortcuts = inputDevice.getInputClass().getSupportedShortcuts()

        val preference = ListPreference(context)
        preference.key = InputDeviceManager.computeGameMenuShortcutPreference(inputDevice)
        preference.title = context.getString(R.string.settings_gamepad_title_game_menu)
        preference.entries = supportedShortcuts.map { it.name }.toTypedArray()
        preference.entryValues = supportedShortcuts.map { it.name }.toTypedArray()
        preference.setValueIndex(supportedShortcuts.indexOf(default))
        preference.setDefaultValue(default.name)
        preference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun getButtonKeyName(context: Context, key: Int) =
        context.resources.getString(R.string.settings_gamepad_button_name, keyCodeToTextMap[key])

    private fun getRetroPadKeyName(context: Context, key: Int): String {
        return if (key == KeyEvent.KEYCODE_UNKNOWN) {
            context.resources.getString(R.string.settings_retropad_button_unassigned)
        } else {
            context.resources.getString(R.string.settings_retropad_button_name, keyCodeToTextMap[key])
        }
    }

    companion object {
        private val keyCodeToTextMap by lazy {
            mapOf(
                KeyEvent.KEYCODE_BUTTON_A to "A",
                KeyEvent.KEYCODE_BUTTON_B to "B",
                KeyEvent.KEYCODE_BUTTON_X to "X",
                KeyEvent.KEYCODE_BUTTON_Y to "Y",
                KeyEvent.KEYCODE_BUTTON_START to "Start",
                KeyEvent.KEYCODE_BUTTON_SELECT to "Select",
                KeyEvent.KEYCODE_BUTTON_L1 to "L1",
                KeyEvent.KEYCODE_BUTTON_L2 to "L2",
                KeyEvent.KEYCODE_BUTTON_R1 to "R1",
                KeyEvent.KEYCODE_BUTTON_R2 to "R2",
                KeyEvent.KEYCODE_BUTTON_THUMBL to "L3",
                KeyEvent.KEYCODE_BUTTON_THUMBR to "R3",
                KeyEvent.KEYCODE_BUTTON_1 to "1",
                KeyEvent.KEYCODE_BUTTON_2 to "2",
                KeyEvent.KEYCODE_BUTTON_3 to "3",
                KeyEvent.KEYCODE_BUTTON_4 to "4",
                KeyEvent.KEYCODE_BUTTON_5 to "5",
                KeyEvent.KEYCODE_BUTTON_6 to "6",
                KeyEvent.KEYCODE_BUTTON_7 to "7",
                KeyEvent.KEYCODE_BUTTON_8 to "8",
                KeyEvent.KEYCODE_BUTTON_9 to "9",
                KeyEvent.KEYCODE_BUTTON_10 to "10",
                KeyEvent.KEYCODE_BUTTON_11 to "11",
                KeyEvent.KEYCODE_BUTTON_12 to "12",
                KeyEvent.KEYCODE_BUTTON_13 to "13",
                KeyEvent.KEYCODE_BUTTON_14 to "14",
                KeyEvent.KEYCODE_BUTTON_15 to "15",
                KeyEvent.KEYCODE_BUTTON_16 to "16",
                KeyEvent.KEYCODE_BUTTON_MODE to "Option",
                KeyEvent.KEYCODE_BUTTON_Z to "Z",
                KeyEvent.KEYCODE_BUTTON_C to "C",
                KeyEvent.KEYCODE_Q to "Q",
                KeyEvent.KEYCODE_W to "W",
                KeyEvent.KEYCODE_E to "E",
                KeyEvent.KEYCODE_R to "R",
                KeyEvent.KEYCODE_T to "T",
                KeyEvent.KEYCODE_Y to "Y",
                KeyEvent.KEYCODE_U to "U",
                KeyEvent.KEYCODE_I to "I",
                KeyEvent.KEYCODE_O to "O",
                KeyEvent.KEYCODE_P to "P",
                KeyEvent.KEYCODE_A to "A",
                KeyEvent.KEYCODE_S to "S",
                KeyEvent.KEYCODE_D to "D",
                KeyEvent.KEYCODE_F to "F",
                KeyEvent.KEYCODE_G to "G",
                KeyEvent.KEYCODE_H to "H",
                KeyEvent.KEYCODE_J to "J",
                KeyEvent.KEYCODE_K to "K",
                KeyEvent.KEYCODE_L to "L",
                KeyEvent.KEYCODE_Z to "Z",
                KeyEvent.KEYCODE_X to "X",
                KeyEvent.KEYCODE_C to "C",
                KeyEvent.KEYCODE_V to "V",
                KeyEvent.KEYCODE_B to "B",
                KeyEvent.KEYCODE_N to "N",
                KeyEvent.KEYCODE_M to "M",
                KeyEvent.KEYCODE_DPAD_UP to "Up",
                KeyEvent.KEYCODE_DPAD_LEFT to "Left",
                KeyEvent.KEYCODE_DPAD_RIGHT to "Right",
                KeyEvent.KEYCODE_DPAD_DOWN to "Down",
                KeyEvent.KEYCODE_ENTER to "Enter",
                KeyEvent.KEYCODE_SHIFT_LEFT to "Shift",
                KeyEvent.KEYCODE_UNKNOWN to ""
            )
        }
    }

    @dagger.Module
    class Module
}
