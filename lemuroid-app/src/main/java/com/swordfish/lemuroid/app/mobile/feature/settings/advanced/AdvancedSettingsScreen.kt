package com.swordfish.lemuroid.app.mobile.feature.settings.advanced

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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alorma.compose.settings.ui.SettingsList
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSlider
import com.alorma.compose.settings.ui.SettingsSwitch
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.utils.android.SettingsSmallGroup
import com.swordfish.lemuroid.app.utils.android.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues
import com.swordfish.lemuroid.app.utils.android.fractionPreferenceState
import com.swordfish.lemuroid.app.utils.android.indexPreferenceState

@Composable
fun AdvancedSettingsScreen(
    padding: MergedPaddingValues,
    viewModel: AdvancedSettingsViewModel,
    navController: NavHostController
) {
    val uiState = viewModel.uiState
        .collectAsState()
        .value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(padding.asPaddingValues()),
    ) {
        if (uiState?.cache == null) {
            return@Column
        }

        InputSettings()
        GeneralSettings(uiState.cache, viewModel, navController)
    }
}

@Composable
private fun InputSettings() {
    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_input)) }
    ) {
        SettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_enable_rumble, false),
            title = { Text(text = stringResource(id = R.string.settings_title_enable_rumble)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_enable_rumble)) },
        )
        SettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_enable_device_rumble, false),
            title = { Text(text = stringResource(id = R.string.settings_title_enable_device_rumble)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_enable_device_rumble)) },
        )
        SettingsSlider(
            state = fractionPreferenceState(
                key = stringResource(id = R.string.pref_key_tilt_sensitivity_index),
                denominator = 10,
                defaultNumerator = 6
            ),
            title = { Text(text = stringResource(R.string.settings_title_tilt_sensitivity)) }
        )
    }
}

@Composable
private fun GeneralSettings(
    cacheState: AdvancedSettingsViewModel.CacheState,
    viewModel: AdvancedSettingsViewModel,
    navController: NavController
) {
    val context = LocalContext.current.applicationContext

    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_general)) }
    ) {
        SettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_low_latency_audio, false),
            title = { Text(text = stringResource(id = R.string.settings_title_low_latency_audio)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_low_latency_audio)) },
        )
        SettingsList(
            title = { Text(text = stringResource(R.string.settings_title_maximum_cache_usage)) },
            items = cacheState.displayNames,
            state = indexPreferenceState(
                R.string.pref_key_max_cache_size,
                cacheState.default,
                cacheState.values
            )
        )
        SettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_allow_direct_game_load, true),
            title = { Text(text = stringResource(id = R.string.settings_title_direct_game_load)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_direct_game_load)) },
        )
        SettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_legacy_hd_mode, false),
            title = { Text(text = stringResource(id = R.string.settings_title_legacy_hd_mode)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_legacy_hd_mode)) },
        )
        SettingsSlider(
            state = fractionPreferenceState(
                key = stringResource(id = R.string.pref_key_tilt_sensitivity_index),
                denominator = 10,
                defaultNumerator = 6
            ),
            title = { Text(text = stringResource(R.string.settings_title_tilt_sensitivity)) }
        )
        SettingsMenuLink(
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
            }
        )
    }
}
