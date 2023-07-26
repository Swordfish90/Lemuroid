package com.swordfish.lemuroid.app.mobile.feature.settings.advanced

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsList
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSlider
import com.alorma.compose.settings.ui.SettingsSwitch
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.utils.android.SettingsSmallGroup
import com.swordfish.lemuroid.app.utils.android.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.fractionPreferenceState
import com.swordfish.lemuroid.app.utils.android.indexPreferenceState

@Composable
fun AdvancedSettingsScreen(
    cacheState: AdvancedSettingsViewModel.CacheState?,
    onResetSettings: () -> Unit
) {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            if (cacheState == null) {
                return@Column
            }

            InputSettings()
            GeneralSettings(cacheState, onResetSettings)
        }
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
    onResetSettings: () -> Unit
) {
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
            onClick = { onResetSettings() }
        )
    }
}
