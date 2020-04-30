package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import androidx.preference.*
import com.swordfish.lemuroid.R

class GamePadBindingsPreferences(private val gamePadManager: GamePadManager) {

    fun resetAllBindings() = gamePadManager.resetAllBindings()

    fun addGamePadsPreferencesToScreen(context: Context, preferenceScreen: PreferenceScreen) {
        gamePadManager.getDistinctGamePads()
            .forEach { addPreferenceCategoryForInputDevice(context, preferenceScreen, it) }

        addExtraCategory(context, preferenceScreen)
    }

    private fun addExtraCategory(context: Context, preferenceScreen: PreferenceScreen) {
        val categoryTitle = context.resources.getString(R.string.settings_gamepad_category_general)
        val category = createCategory(context, preferenceScreen, categoryTitle)

        val preference = Preference(context)
        preference.key = context.resources.getString(R.string.pref_key_reset_gamepad_bindings)
        preference.title = context.resources.getString(R.string.settings_gamepad_title_reset_bindings)
        preference.isIconSpaceReserved = false
        category.addPreference(preference)
    }

    private fun createCategory(context: Context, preferenceScreen: PreferenceScreen, title: String): PreferenceCategory {
        val category = PreferenceCategory(context)
        preferenceScreen.addPreference(category)
        category.title = title
        category.isIconSpaceReserved = false
        return category
    }

    private fun addPreferenceCategoryForInputDevice(context: Context, preferenceScreen: PreferenceScreen, inputDevice: InputDevice) {
        val category = createCategory(context, preferenceScreen, inputDevice.name)
        preferenceScreen.addPreference(category)

        GamePadManager.RETRO_PAD_KEYS
            .filter { inputDevice.hasKeys(it)[0] }
            .map { getPreferenceForKey(context, inputDevice, it) }
            .forEach { category.addPreference(it) }
    }

    private fun getPreferenceForKey(context: Context, inputDevice: InputDevice, key: Int): Preference {
        val retroKeys = GamePadManager.RETRO_PAD_KEYS
        val retroPadButtonNames = retroKeys.map { getRetroPadKeyName(context, it) }

        val preference = ListPreference(context)
        preference.key = GamePadManager.computeKeyBindingPreference(inputDevice, key)
        preference.title = getButtonKeyName(context, key)
        preference.entries = retroPadButtonNames.toTypedArray()
        preference.entryValues = retroKeys.map { it.toString() } .toTypedArray()
        preference.setValueIndex(retroKeys.indexOf(key))
        preference.setDefaultValue(gamePadManager.getDefaultBinding(key).toString())
        preference.summaryProvider = Preference.SummaryProvider<ListPreference> {
            getRetroPadKeyName(context, it.value.toInt())
        }
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun getButtonKeyName(context: Context, key: Int) =
        context.resources.getString(R.string.settings_gamepad_button_name, keyCodeToTextMap[key])

    private fun getRetroPadKeyName(context: Context, key: Int) =
        context.resources.getString(R.string.settings_retropad_button_name, keyCodeToTextMap[key])

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
                KeyEvent.KEYCODE_BUTTON_10 to "10"
            )
        }
    }

    @dagger.Module
    class Module
}
