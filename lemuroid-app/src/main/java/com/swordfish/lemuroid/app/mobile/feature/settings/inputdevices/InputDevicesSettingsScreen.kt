package com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices

import android.view.InputDevice
import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsList
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.RetroKey
import com.swordfish.lemuroid.app.shared.input.lemuroiddevice.getLemuroidInputDevice
import com.swordfish.lemuroid.app.utils.android.SettingsSmallGroup
import com.swordfish.lemuroid.app.utils.android.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.indexPreferenceState

@Composable
fun InputDevicesSettingsScreen(
    state: InputDevicesSettingsViewModel.State,
    onBindingClicked: (InputDevice, RetroKey) -> Unit
) {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            EnabledDevice(state)
            state.bindings.forEach { (device, bindings) ->
                SettingsSmallGroup(title = { Text(text = device.name) }) {
                    DeviceBinding(device, bindings.keys, onBindingClicked)
                    DeviceMenuShortcut(device, bindings.menuShortcuts, bindings.defaultShortcut)
                }
            }
        }
    }
}

@Composable
private fun DeviceBinding(
    device: InputDevice,
    bindings: Map<RetroKey, InputKey>,
    onBindingClicked: (InputDevice, RetroKey) -> Unit
) {
    val customizableKeys = device.getLemuroidInputDevice().getCustomizableKeys()

    customizableKeys.forEach { retroKey ->
        val inputKey = bindings[retroKey] ?: InputKey(KeyEvent.KEYCODE_UNKNOWN)

        SettingsMenuLink(
            title = { Text(text = retroKey.displayName(LocalContext.current)) },
            subtitle = { Text(text = inputKey.displayName()) },
            onClick = { onBindingClicked(device, retroKey) }
        )
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
private fun EnabledDevice(state: InputDevicesSettingsViewModel.State) {
    SettingsGroup {
        state.devices.forEach { device ->
            SettingsSwitch(
                state = booleanPreferenceState(key = device.key, default = device.enabledByDefault),
                title = { Text(text = device.name) },
            )
        }
    }
}
