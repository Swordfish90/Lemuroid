package com.swordfish.lemuroid.app.tv.settings

import android.content.Context
import android.content.Intent
import android.view.InputDevice
import android.view.KeyEvent
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.input.InputBindingUpdater
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.RetroKey
import com.swordfish.lemuroid.app.shared.input.ShortcutBindingUpdater
import com.swordfish.lemuroid.app.shared.input.lemuroiddevice.getLemuroidInputDevice
import com.swordfish.lemuroid.app.shared.settings.GameShortcutType
import com.swordfish.lemuroid.app.tv.input.TVGamePadBindingActivity
import com.swordfish.lemuroid.app.tv.input.TVGamePadShortcutBindingActivity

class GamePadPreferencesHelper(private val inputDeviceManager: InputDeviceManager) {
    suspend fun addGamePadsPreferencesToScreen(
        context: Context,
        preferenceScreen: PreferenceScreen,
        gamePads: List<InputDevice>,
        enabledGamePads: List<InputDevice>,
    ) {
        val distinctGamePads = getDistinctGamePads(gamePads)
        val distinctEnabledGamePads = getDistinctGamePads(enabledGamePads)

        addEnabledCategory(context, preferenceScreen, distinctGamePads)

        distinctEnabledGamePads
            .forEach {
                addPreferenceCategoryForInputDevice(context, preferenceScreen, it)
            }

        addExtraCategory(context, preferenceScreen)

        refreshGamePadsPreferencesToScreen(preferenceScreen, distinctEnabledGamePads)
    }

    suspend fun refreshGamePadsPreferencesToScreen(
        preferenceScreen: PreferenceScreen,
        enabledGamePads: List<InputDevice>,
    ) {
        getDistinctGamePads(enabledGamePads)
            .forEach { refreshPreferenceCategoryForInputDevice(preferenceScreen, it) }
    }

    private fun getDistinctGamePads(gamePads: List<InputDevice>): List<InputDevice> {
        return gamePads.distinctBy { it.descriptor }
    }

    private fun addEnabledCategory(
        context: Context,
        preferenceScreen: PreferenceScreen,
        gamePads: List<InputDevice>,
    ) {
        if (gamePads.isEmpty()) {
            return
        }

        val categoryTitle = context.resources.getString(R.string.settings_gamepad_category_enabled)
        val category = createCategory(context, preferenceScreen, categoryTitle)
        preferenceScreen.addPreference(category)

        gamePads.forEach { gamePad ->
            category.addPreference(buildGamePadEnabledPreference(context, gamePad))
        }
    }

    private fun addExtraCategory(
        context: Context,
        preferenceScreen: PreferenceScreen,
    ) {
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
        title: String,
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
        inputDevice: InputDevice,
    ) {
        val category = createCategory(context, preferenceScreen, inputDevice.name)
        preferenceScreen.addPreference(category)

        inputDevice.getLemuroidInputDevice().getCustomizableKeys()
            .map { buildKeyBindingPreference(context, inputDevice, it) }
            .forEach { category.addPreference(it) }

        GameShortcutType.entries
            .mapNotNull { buildShortcutPreference(context, inputDevice, it) }
            .forEach { category.addPreference(it) }
    }

    private suspend fun refreshPreferenceCategoryForInputDevice(
        preferenceScreen: PreferenceScreen,
        inputDevice: InputDevice,
    ) {
        val inverseBindings =
            inputDeviceManager.getCurrentBindings(inputDevice)
                .map { it.value to it.key }
                .toMap()

        inputDevice.getLemuroidInputDevice().getCustomizableKeys()
            .forEach { retroKey ->
                val boundKey = inverseBindings[retroKey]?.keyCode ?: KeyEvent.KEYCODE_UNKNOWN
                val preferenceKey = InputDeviceManager.computeKeyBindingRetroKeyPreference(inputDevice, retroKey)
                val preference = preferenceScreen.findPreference<Preference>(preferenceKey)
                preference?.summaryProvider =
                    Preference.SummaryProvider<Preference> {
                        InputKey(boundKey).displayName()
                    }
            }

        val shortcuts =
            inputDeviceManager.getCurrentShortcuts(inputDevice)
                .associateBy { it.type }

        GameShortcutType.entries
            .forEach { type ->
                val preferenceKey = InputDeviceManager.computeGameShortcutPreference(inputDevice, type)
                val preference = preferenceScreen.findPreference<Preference>(preferenceKey)
                preference?.summaryProvider =
                    Preference.SummaryProvider<Preference> {
                        shortcuts[type]?.name ?: ""
                    }
            }
    }

    private fun buildGamePadEnabledPreference(
        context: Context,
        inputDevice: InputDevice,
    ): Preference {
        val preference = SwitchPreference(context)
        preference.key = InputDeviceManager.computeEnabledGamePadPreference(inputDevice)
        preference.title = inputDevice.name
        preference.setDefaultValue(inputDevice.getLemuroidInputDevice().isEnabledByDefault(context))
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun buildKeyBindingPreference(
        context: Context,
        inputDevice: InputDevice,
        retroKey: RetroKey,
    ): Preference {
        val preference = Preference(context)
        preference.key = InputDeviceManager.computeKeyBindingRetroKeyPreference(inputDevice, retroKey)
        preference.title = retroKey.displayName(context)
        preference.setOnPreferenceClickListener {
            displayChangeDialog(context, inputDevice, retroKey.keyCode)
            true
        }
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun displayChangeDialog(
        context: Context,
        inputDevice: InputDevice,
        retroKey: Int,
    ) {
        val intent =
            Intent(context, TVGamePadBindingActivity::class.java).apply {
                putExtra(InputBindingUpdater.REQUEST_DEVICE, inputDevice)
                putExtra(InputBindingUpdater.REQUEST_RETRO_KEY, retroKey)
            }
        context.startActivity(intent)
    }

    private fun buildShortcutPreference(
        context: Context,
        inputDevice: InputDevice,
        type: GameShortcutType,
    ): Preference? {
        val preference = Preference(context)
        preference.key = InputDeviceManager.computeGameShortcutPreference(inputDevice, type)
        preference.title = type.displayName()
        preference.setOnPreferenceClickListener {
            displayShortcutChangeDialog(context, inputDevice, type)
            true
        }
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun displayShortcutChangeDialog(
        context: Context,
        inputDevice: InputDevice,
        type: GameShortcutType,
    ) {
        val intent =
            Intent(context, TVGamePadShortcutBindingActivity::class.java).apply {
                putExtra(ShortcutBindingUpdater.REQUEST_DEVICE, inputDevice)
                putExtra(ShortcutBindingUpdater.REQUEST_SHORTCUT_TYPE, type.name)
            }
        context.startActivity(intent)
    }

    @dagger.Module
    class Module
}
