package com.swordfish.lemuroid.app.mobile.feature.settings.advanced

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.isProVersion
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsPage
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSlider
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import com.swordfish.lemuroid.app.utils.android.settings.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.indexPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.intPreferenceState

@Composable
fun AdvancedSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdvancedSettingsViewModel,
    navController: NavHostController,
) {
    val uiState =
        viewModel.uiState
            .collectAsState()
            .value

    LemuroidSettingsPage(
        modifier = modifier.fillMaxSize(),
    ) {
        if (uiState?.cache == null) {
            return@LemuroidSettingsPage
        }

        InputSettings()
        GeneralSettings(uiState.cache, viewModel, navController)
        if (isProVersion()) {
            ExperimentalSettings(viewModel)
        }
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
                intPreferenceState(
                    key = stringResource(id = R.string.pref_key_tilt_sensitivity_index),
                    default = 6,
                ),
            steps = 10,
            valueRange = 0f..10f,
            enabled = true,
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
    val factoryResetDialogState = remember { mutableStateOf(false) }

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
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_reset_settings)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_reset_settings)) },
            onClick = { factoryResetDialogState.value = true },
        )
    }

    if (factoryResetDialogState.value) {
        FactoryResetDialog(factoryResetDialogState, viewModel, navController)
    }
}

@Composable
private fun ExperimentalSettings(viewModel: AdvancedSettingsViewModel) {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_experimental)) },
    ) {
        LemuroidSettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_citra_experimental_save_states, false),
            title = { Text(text = stringResource(id = R.string.settings_title_citra_experimental_save_states)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_citra_experimental_save_states))
            },
        )
    }
    Citra3DSKeysSettings(viewModel)
}

@Composable
private fun Citra3DSKeysSettings(viewModel: AdvancedSettingsViewModel) {
    val keysState by viewModel.keysState.collectAsState()
    val context = LocalContext.current
    val filePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.installKeysFromUri(context, it) }
        }
    var showUrlDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_citra_keys)) },
    ) {
        LemuroidSettingsMenuLink(
            enabled = false,
            title = { Text(text = stringResource(id = R.string.settings_title_citra_keys_status)) },
            subtitle = {
                if (keysState.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.settings_citra_keys_downloading))
                    }
                } else {
                    Text(
                        text =
                            if (keysState.keysPresent) {
                                stringResource(id = R.string.settings_value_citra_keys_present)
                            } else {
                                stringResource(id = R.string.settings_value_citra_keys_absent)
                            },
                    )
                }
            },
            onClick = {},
        )
        LemuroidSettingsMenuLink(
            enabled = !keysState.isLoading,
            title = { Text(text = stringResource(id = R.string.settings_title_citra_keys_load_file)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_citra_keys_load_file)) },
            onClick = { filePicker.launch(arrayOf("*/*")) },
        )
        LemuroidSettingsMenuLink(
            enabled = !keysState.isLoading,
            title = { Text(text = stringResource(id = R.string.settings_title_citra_keys_load_url)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_citra_keys_load_url)) },
            onClick = { showUrlDialog = true },
        )
        if (keysState.keysPresent && !keysState.isLoading) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.settings_title_citra_keys_delete)) },
                subtitle = { Text(text = stringResource(id = R.string.settings_description_citra_keys_delete)) },
                onClick = { showDeleteConfirmDialog = true },
            )
        }
    }

    if (showUrlDialog) {
        var urlText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text(text = stringResource(id = R.string.settings_title_citra_keys_load_url)) },
            text = {
                Column {
                    Text(text = stringResource(id = R.string.settings_citra_keys_disclaimer))
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text(text = stringResource(id = R.string.settings_citra_keys_url_hint)) },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUrlDialog = false
                        viewModel.installKeysFromUrl(urlText)
                    },
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = stringResource(id = R.string.settings_citra_keys_delete_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.settings_citra_keys_delete_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteKeys()
                    },
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
        )
    }

    keysState.error?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text(text = stringResource(id = R.string.settings_citra_keys_error_title)) },
            text = { Text(text = errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
        )
    }
}

@Composable
private fun FactoryResetDialog(
    factoryResetDialogState: MutableState<Boolean>,
    viewModel: AdvancedSettingsViewModel,
    navController: NavController,
) {
    val onDismiss = {
        factoryResetDialogState.value = false
    }
    AlertDialog(
        title = { Text(stringResource(id = R.string.reset_settings_warning_message_title)) },
        text = { Text(stringResource(id = R.string.reset_settings_warning_message_description)) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.resetAllSettings()
                    navController.popBackStack(MainRoute.SETTINGS.route, false)
                },
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
    )
}
