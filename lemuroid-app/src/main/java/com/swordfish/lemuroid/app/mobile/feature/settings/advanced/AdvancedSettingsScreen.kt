package com.swordfish.lemuroid.app.mobile.feature.settings.advanced

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alorma.compose.settings.storage.disk.rememberPreferenceIntSettingState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsPage
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSlider
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import com.swordfish.lemuroid.app.utils.android.settings.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.indexPreferenceState

@Composable
fun AdvancedSettingsScreen(
    padding: MergedPaddingValues,
    viewModel: AdvancedSettingsViewModel,
    navController: NavHostController,
) {
    val uiState =
        viewModel.uiState
            .collectAsState()
            .value

    LemuroidSettingsPage(
        modifier =
            Modifier
                .padding(padding.asPaddingValues()),
    ) {
        if (uiState?.cache == null) {
            return@LemuroidSettingsPage
        }

        InputSettings()
        GeneralSettings(uiState.cache, viewModel, navController)
    }
}

@Composable
private fun InputSettings() {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_input)) },
    ) {
        val rumbleEnabled = booleanPreferenceState(R.string.pref_key_enable_rumble, false)
        LemuroidSettingsSwitch(
            state = rumbleEnabled,
            title = { Text(text = stringResource(id = R.string.settings_title_enable_rumble)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_enable_rumble)) },
        )
        LemuroidSettingsSwitch(
            enabled = rumbleEnabled.value,
            state = booleanPreferenceState(R.string.pref_key_enable_device_rumble, false),
            title = { Text(text = stringResource(id = R.string.settings_title_enable_device_rumble)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_enable_device_rumble)) },
        )
        LemuroidSettingsSlider(
            state =
                rememberPreferenceIntSettingState(
                    key = stringResource(id = R.string.pref_key_tilt_sensitivity_index),
                    defaultValue = 6,
                ),
            steps = 10,
            valueRange = 0f..10f,
            title = { Text(text = stringResource(R.string.settings_title_tilt_sensitivity)) },
        )
    }
}

@Composable
private fun GeneralSettings(
    cacheState: AdvancedSettingsViewModel.CacheState,
    viewModel: AdvancedSettingsViewModel,
    navController: NavController,
) {
    val context = LocalContext.current.applicationContext

    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_general)) },
    ) {
        LemuroidSettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_low_latency_audio, false),
            title = { Text(text = stringResource(id = R.string.settings_title_low_latency_audio)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_low_latency_audio)) },
        )
        LemuroidSettingsList(
            title = { Text(text = stringResource(R.string.settings_title_maximum_cache_usage)) },
            items = cacheState.displayNames,
            state =
                indexPreferenceState(
                    R.string.pref_key_max_cache_size,
                    cacheState.default,
                    cacheState.values,
                ),
        )
        LemuroidSettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_allow_direct_game_load, true),
            title = { Text(text = stringResource(id = R.string.settings_title_direct_game_load)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_direct_game_load)) },
        )
        LemuroidSettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_legacy_hd_mode, false),
            title = { Text(text = stringResource(id = R.string.settings_title_legacy_hd_mode)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_legacy_hd_mode)) },
        )
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_reset_settings)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_reset_settings)) },
            onClick = {
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.reset_settings_warning_message_title)
                    .setMessage(R.string.reset_settings_warning_message_description)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        viewModel.resetAllSettings()
                        navController.popBackStack(MainRoute.SETTINGS.route, false)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .show()
            },
        )
    }
}
