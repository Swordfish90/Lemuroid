package com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices

import android.content.Intent
import android.view.InputDevice
import android.view.KeyEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.input.GamePadBindingActivity
import com.swordfish.lemuroid.app.shared.input.InputBindingUpdater
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.lemuroiddevice.getLemuroidInputDevice
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsPage
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import com.swordfish.lemuroid.app.utils.android.settings.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.indexPreferenceState

@Composable
fun InputDevicesSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: InputDevicesSettingsViewModel,
) {
    val state =
        viewModel.uiState
            .collectAsState(InputDevicesSettingsViewModel.State())
            .value

    LemuroidSettingsPage(modifier = modifier.fillMaxSize()) {
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

    LemuroidCardSettingsGroup(title = { Text(text = device.name) }) {
        customizableKeys.forEach { retroKey ->
            val inputKey = bindings.keys[retroKey] ?: InputKey(KeyEvent.KEYCODE_UNKNOWN)

            LemuroidSettingsMenuLink(
                title = { Text(text = retroKey.displayName(LocalContext.current)) },
                subtitle = { Text(text = inputKey.displayName()) },
                onClick = {
                    val intent =
                        Intent(context, GamePadBindingActivity::class.java).apply {
                            putExtra(InputBindingUpdater.REQUEST_DEVICE, device)
                            putExtra(InputBindingUpdater.REQUEST_RETRO_KEY, retroKey.keyCode)
                        }
                    context.startActivity(intent)
                },
            )
        }

        DeviceMenuShortcut(device, bindings.menuShortcuts, bindings.defaultShortcut)
    }
}

@Composable
private fun DeviceMenuShortcut(
    device: InputDevice,
    values: List<String>,
    defaultShortcut: String?,
) {
    if (values.isEmpty() || defaultShortcut == null) {
        return
    }

    val state =
        indexPreferenceState(
            InputDeviceManager.computeGameMenuShortcutPreference(device),
            defaultShortcut,
            values,
        )

    LemuroidSettingsList(
        state = state,
        title = { Text(text = stringResource(R.string.settings_gamepad_title_game_menu)) },
        items = values,
    )
}

@Composable
private fun EnabledDeviceCategory(state: InputDevicesSettingsViewModel.State) {
    LemuroidCardSettingsGroup(title = { Text(text = stringResource(R.string.settings_gamepad_category_enabled)) }) {
        state.devices.forEach { device ->
            LemuroidSettingsSwitch(
                state = booleanPreferenceState(key = device.key, default = device.enabledByDefault),
                title = { Text(text = device.name) },
            )
        }
    }
}

@Composable
private fun GeneralOptionsCategory(viewModel: InputDevicesSettingsViewModel) {
    LemuroidCardSettingsGroup(title = { Text(text = stringResource(R.string.settings_gamepad_category_general)) }) {
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(R.string.settings_gamepad_title_reset_bindings)) },
            onClick = { viewModel.resetAllBindings() },
        )
    }
}
