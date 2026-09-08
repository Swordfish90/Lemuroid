package com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices

import android.content.Context
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
import com.swordfish.lemuroid.app.mobile.feature.input.GamePadShortcutBindingActivity
import com.swordfish.lemuroid.app.mobile.feature.input.GamePadTurboBindingActivity
import com.swordfish.lemuroid.app.shared.input.InputBindingUpdater
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.ShortcutBindingUpdater
import com.swordfish.lemuroid.app.shared.input.TurboBindingUpdater
import com.swordfish.lemuroid.app.shared.input.TurboConfig
import com.swordfish.lemuroid.app.shared.input.lemuroiddevice.getLemuroidInputDevice
import com.swordfish.lemuroid.app.shared.settings.GameShortcut
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsPage
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSlider
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import com.swordfish.lemuroid.app.utils.android.settings.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.intPreferenceState

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
            DeviceTurboCategory(device, bindings.turbo)
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

        bindings.shortcuts.forEach {
            DeviceShortcutBinding(context, device, it)
        }
    }
}

@Composable
private fun DeviceShortcutBinding(
    context: Context,
    device: InputDevice,
    shortcut: GameShortcut,
) {
    LemuroidSettingsMenuLink(
        title = { Text(text = shortcut.type.displayName()) },
        subtitle = { Text(text = shortcut.name) },
        onClick = {
            val intent =
                Intent(context, GamePadShortcutBindingActivity::class.java).apply {
                    putExtra(ShortcutBindingUpdater.REQUEST_DEVICE, device)
                    putExtra(ShortcutBindingUpdater.REQUEST_SHORTCUT_TYPE, shortcut.type.name)
                }
            context.startActivity(intent)
        },
    )
}

@Composable
private fun DeviceTurboCategory(
    device: InputDevice,
    turbo: TurboConfig,
) {
    val context = LocalContext.current
    val frequencyState =
        intPreferenceState(
            key = InputDeviceManager.computeTurboFrequencyPreference(device),
            default = TurboConfig.DEFAULT_FREQUENCY_HZ,
        )

    LemuroidCardSettingsGroup(title = { Text(text = stringResource(R.string.settings_gamepad_category_turbo)) }) {
        LemuroidSettingsSwitch(
            state =
                booleanPreferenceState(
                    key = InputDeviceManager.computeTurboEnabledPreference(device),
                    default = false,
                ),
            title = { Text(text = stringResource(R.string.settings_gamepad_turbo_enable)) },
            subtitle = { Text(text = stringResource(R.string.settings_gamepad_turbo_enable_summary)) },
        )

        TurboConfig.CONFIGURABLE_TARGETS.forEach { targetKeyCode ->
            val triggerKeyCode = turbo.triggerForTarget(targetKeyCode) ?: KeyEvent.KEYCODE_UNKNOWN
            LemuroidSettingsMenuLink(
                enabled = turbo.enabled,
                title = {
                    Text(
                        text =
                            stringResource(
                                R.string.settings_gamepad_turbo_button,
                                InputKey(targetKeyCode).displayName(),
                            ),
                    )
                },
                subtitle = { Text(text = InputKey(triggerKeyCode).displayName()) },
                onClick = {
                    val intent =
                        Intent(context, GamePadTurboBindingActivity::class.java).apply {
                            putExtra(TurboBindingUpdater.REQUEST_DEVICE, device)
                            putExtra(TurboBindingUpdater.REQUEST_TARGET_KEY, targetKeyCode)
                        }
                    context.startActivity(intent)
                },
            )
        }

        LemuroidSettingsSlider(
            enabled = turbo.enabled,
            state = frequencyState,
            steps = TURBO_FREQUENCY_STEPS,
            valueRange = TurboConfig.MIN_FREQUENCY_HZ.toFloat()..TurboConfig.MAX_FREQUENCY_HZ.toFloat(),
            title = { Text(text = stringResource(R.string.settings_gamepad_turbo_frequency)) },
            subtitle = {
                Text(text = stringResource(R.string.settings_gamepad_turbo_frequency_value, frequencyState.value))
            },
        )
    }
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

// Snaps the 5..30 Hz range to 5 Hz increments: 5, 10, 15, 20, 25, 30.
private const val TURBO_FREQUENCY_STEPS = 4
