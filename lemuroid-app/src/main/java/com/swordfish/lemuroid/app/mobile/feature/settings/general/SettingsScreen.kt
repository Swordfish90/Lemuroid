package com.swordfish.lemuroid.app.mobile.feature.settings.general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.storage.preferences.BooleanPreferenceSettingValueState
import com.alorma.compose.settings.ui.SettingsList
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.utils.android.SettingsSmallGroup
import com.swordfish.lemuroid.app.utils.android.stringListResource
import com.swordfish.lemuroid.app.utils.settings.IndexPreferenceSettingValueState

@Composable
fun SettingsScreen(
    currentDirectory: String,
    autoSave: BooleanPreferenceSettingValueState,
    hdMode: BooleanPreferenceSettingValueState,
    displayFilterIndex: IndexPreferenceSettingValueState,
    hapticFeedbackModeIndex: IndexPreferenceSettingValueState,
    directoryScanInProgress: State<Boolean>,
    indexingInProgress: State<Boolean>,
    isSaveSyncSupported: Boolean,
    onChangeFolder: () -> Unit = { },
    onRescan: () -> Unit = { },
    onRescanStop: () -> Unit = { },
    onOpenGamePadSettings: () -> Unit = { },
    onOpenSaveSyncSettings: () -> Unit = { },
    onOpenCoresSelectionSettings: () -> Unit = { },
    onOpenBiosSettings: () -> Unit = { },
    onOpenAdvancedSettings: () -> Unit = { }
) {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            RomsSettings(
                currentDirectory,
                onChangeFolder,
                indexingInProgress,
                directoryScanInProgress,
                onRescanStop,
                onRescan
            )
            GeneralSettings(autoSave, hdMode, displayFilterIndex)
            InputSettings(hapticFeedbackModeIndex, onOpenGamePadSettings)
            MiscSettings(
                indexingInProgress,
                isSaveSyncSupported,
                onOpenSaveSyncSettings,
                onOpenCoresSelectionSettings,
                onOpenBiosSettings,
                onOpenAdvancedSettings
            )
        }
    }
}

@Composable
private fun MiscSettings(
    indexingInProgress: State<Boolean>,
    isSaveSyncSupported: Boolean,
    onOpenSaveSyncSettings: () -> Unit,
    onOpenCoresSelectionSettings: () -> Unit,
    onOpenBiosSettings: () -> Unit,
    onOpenAdvancedSettings: () -> Unit
) {
    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_misc)) }
    ) {
        if (isSaveSyncSupported) {
            SettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.settings_title_save_sync)) },
                subtitle = {
                    Text(
                        text = stringResource(id = R.string.settings_description_save_sync)
                    )
                },
                onClick = { onOpenSaveSyncSettings() }
            )
        }
        SettingsMenuLink(
            title = {
                Text(
                    text = stringResource(id = R.string.settings_title_open_cores_selection)
                )
            },
            subtitle = {
                Text(
                    text = stringResource(id = R.string.settings_description_open_cores_selection)
                )
            },
            onClick = { onOpenCoresSelectionSettings() }
        )
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_display_bios_info)) },
            subtitle = {
                Text(
                    text = stringResource(id = R.string.settings_description_display_bios_info)
                )
            },
            enabled = !indexingInProgress.value,
            onClick = { onOpenBiosSettings() }
        )
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_advanced_settings)) },
            subtitle = {
                Text(
                    text = stringResource(id = R.string.settings_description_advanced_settings)
                )
            },
            onClick = { onOpenAdvancedSettings() }
        )
    }
}

@Composable
private fun InputSettings(
    hapticFeedbackModeIndex: IndexPreferenceSettingValueState, onOpenGamePadSettings: () -> Unit
) {
    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_input)) }
    ) {
        SettingsList(
            state = hapticFeedbackModeIndex, title = {
                Text(
                    text = stringResource(id = R.string.settings_title_enable_touch_feedback)
                )
            },
            items = stringListResource(R.array.pref_key_haptic_feedback_mode_display_names)
        )
        SettingsMenuLink(title = { Text(text = stringResource(id = R.string.settings_title_gamepad_settings)) },
            subtitle = {
                Text(
                    text = stringResource(id = R.string.settings_description_gamepad_settings)
                )
            },
            onClick = { onOpenGamePadSettings() })
    }
}

@Composable
private fun GeneralSettings(
    autoSave: BooleanPreferenceSettingValueState,
    hdMode: BooleanPreferenceSettingValueState,
    displayFilterIndex: IndexPreferenceSettingValueState
) {
    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_general)) }
    ) {
        SettingsSwitch(
            state = autoSave,
            title = { Text(text = stringResource(id = R.string.settings_title_enable_autosave)) },
        )
        SettingsSwitch(
            state = hdMode,
            title = { Text(text = stringResource(id = R.string.settings_title_hd_mode)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_hd_mode)) },
        )
        SettingsList(
            enabled = !hdMode.value,
            state = displayFilterIndex,
            title = { Text(text = stringResource(id = R.string.display_filter)) },
            items = stringListResource(R.array.pref_key_shader_filter_display_names)
        )
    }
}

@Composable
private fun RomsSettings(
    currentDirectory: String,
    onChangeFolder: () -> Unit,
    indexingInProgress: State<Boolean>,
    directoryScanInProgress: State<Boolean>,
    onRescanStop: () -> Unit,
    onRescan: () -> Unit
) {
    SettingsSmallGroup(title = { Text(text = stringResource(id = R.string.roms)) }) {
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.directory)) },
            subtitle = { Text(text = currentDirectory) },
            onClick = { onChangeFolder() },
            enabled = !indexingInProgress.value
        )
        if (directoryScanInProgress.value) {
            SettingsMenuLink(title = { Text(text = stringResource(id = R.string.stop)) },
                onClick = { onRescanStop() })
        } else {
            SettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.rescan)) },
                onClick = { onRescan() },
                enabled = !indexingInProgress.value
            )
        }
    }
}
