package com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices

import android.content.Intent
import android.view.InputDevice
import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsList
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.input.GamePadBindingActivity
import com.swordfish.lemuroid.app.shared.input.InputBindingUpdater
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.lemuroiddevice.getLemuroidInputDevice
import com.swordfish.lemuroid.app.utils.android.SettingsSmallGroup
import com.swordfish.lemuroid.app.utils.android.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues
import com.swordfish.lemuroid.app.utils.android.indexPreferenceState

@Composable
fun InputDevicesSettingsScreen(padding: MergedPaddingValues, viewModel: InputDevicesSettingsViewModel) {
    val state = viewModel.uiState
        .collectAsState(InputDevicesSettingsViewModel.State())
        .value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(padding.asPaddingValues())
    ) {
        EnabledDeviceCategory(state)
        state.bindings.forEach { (device, bindings) ->
            DeviceBindingCategory(device, bindings)
        }
        GeneralOptionsCategory(viewModel)
    }
}

@Composable
private fun DeviceBindingCategory(
    device: InputDevice,
    bindings: InputDevicesSettingsViewModel.BindingsView,
) {
    val context = LocalContext.current
    val customizableKeys = device.getLemuroidInputDevice().getCustomizableKeys()

    SettingsSmallGroup(title = { Text(text = device.name) }) {
        customizableKeys.forEach { retroKey ->
            val inputKey = bindings.keys[retroKey] ?: InputKey(KeyEvent.KEYCODE_UNKNOWN)

            SettingsMenuLink(
                title = { Text(text = retroKey.displayName(LocalContext.current)) },
                subtitle = { Text(text = inputKey.displayName()) },
                onClick = {
                    val intent = Intent(context, GamePadBindingActivity::class.java).apply {
                        putExtra(InputBindingUpdater.REQUEST_DEVICE, device)
                        putExtra(InputBindingUpdater.REQUEST_RETRO_KEY, retroKey.keyCode)
                    }
                    context.startActivity(intent)
                }
            )
        }

        DeviceMenuShortcut(device, bindings.menuShortcuts, bindings.defaultShortcut)
    }
}

@Composable
private fun DeviceMenuShortcut(device: InputDevice, values: List<String>, defaultShortcut: String?) {
    if (values.isEmpty() || defaultShortcut == null) {
        return
    }

    val state = indexPreferenceState(
        InputDeviceManager.computeGameMenuShortcutPreference(device),
        defaultShortcut,
        values
    )

    SettingsList(
        state = state,
        title = { Text(text = stringResource(R.string.settings_gamepad_title_game_menu)) },
        items = values
    )
}

@Composable
private fun EnabledDeviceCategory(state: InputDevicesSettingsViewModel.State) {
    SettingsSmallGroup(title = { Text(text = stringResource(R.string.settings_gamepad_category_enabled)) }) {
        state.devices.forEach { device ->
            SettingsSwitch(
                state = booleanPreferenceState(key = device.key, default = device.enabledByDefault),
                title = { Text(text = device.name) },
            )
        }
    }
}

@Composable
private fun GeneralOptionsCategory(viewModel: InputDevicesSettingsViewModel) {
    SettingsSmallGroup(title = { Text(text = stringResource(R.string.settings_gamepad_category_general)) }) {
        SettingsMenuLink(
            title = { Text(text = stringResource(R.string.settings_gamepad_title_reset_bindings)) },
            onClick = { viewModel.resetAllBindings() }
        )
    }
}
