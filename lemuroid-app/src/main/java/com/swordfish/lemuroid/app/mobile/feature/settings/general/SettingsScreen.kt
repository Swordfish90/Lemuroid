package com.swordfish.lemuroid.app.mobile.feature.settings.general

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import com.alorma.compose.settings.ui.SettingsList
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.mobile.feature.main.navigateToRoute
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.app.utils.android.SettingsSmallGroup
import com.swordfish.lemuroid.app.utils.android.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues
import com.swordfish.lemuroid.app.utils.android.indexPreferenceState
import com.swordfish.lemuroid.app.utils.android.stringListResource

@Composable
fun SettingsScreen(
    padding: MergedPaddingValues,
    viewModel: SettingsViewModel,
    navController: NavController
) {
    val state = viewModel.uiState
        .collectAsState(SettingsViewModel.State())
        .value

    val scanInProgress = viewModel.directoryScanInProgress
        .observeAsState(false)
        .value

    val indexingInProgress = viewModel.indexingInProgress
        .observeAsState(false)
        .value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(padding.asPaddingValues()),
    ) {
        RomsSettings(
            state = state,
            onChangeFolder = { viewModel.changeLocalStorageFolder() },
            indexingInProgress = indexingInProgress,
            scanInProgress = scanInProgress,
        )
        GeneralSettings()
        InputSettings(navController = navController)
        MiscSettings(
            indexingInProgress = indexingInProgress,
            isSaveSyncSupported = state.isSaveSyncSupported,
            navController = navController,
        )
    }
}

@Composable
private fun MiscSettings(
    indexingInProgress: Boolean,
    isSaveSyncSupported: Boolean,
    navController: NavController,
) {
    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_misc)) }
    ) {
        if (isSaveSyncSupported) {
            SettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.settings_title_save_sync)) },
                subtitle = {
                    Text(text = stringResource(id = R.string.settings_description_save_sync))
                },
                onClick = { navController.navigateToRoute(MainRoute.SETTINGS_SAVE_SYNC) }
            )
        }
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_open_cores_selection)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_open_cores_selection))
            },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_CORES_SELECTION) }
        )
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_display_bios_info)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_display_bios_info))
            },
            enabled = !indexingInProgress,
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_BIOS) }
        )
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_advanced_settings)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_advanced_settings))
            },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_ADVANCED) }
        )
    }
}

@Composable
private fun InputSettings(navController: NavController) {
    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_input)) }
    ) {
        SettingsList(
            state = indexPreferenceState(
                R.string.pref_key_haptic_feedback_mode,
                "press",
                stringListResource(R.array.pref_key_haptic_feedback_mode_values)
            ),
            title = {
                Text(text = stringResource(id = R.string.settings_title_enable_touch_feedback))
            },
            items = stringListResource(R.array.pref_key_haptic_feedback_mode_display_names)
        )
        SettingsMenuLink(title = { Text(text = stringResource(id = R.string.settings_title_gamepad_settings)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_gamepad_settings))
            },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_INPUT_DEVICES) })
    }
}

@Composable
private fun GeneralSettings() {
    val hdMode = booleanPreferenceState(R.string.pref_key_hd_mode, false)

    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_general)) }
    ) {
        SettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_autosave, true),
            title = { Text(text = stringResource(id = R.string.settings_title_enable_autosave)) },
        )
        SettingsSwitch(
            state = hdMode,
            title = { Text(text = stringResource(id = R.string.settings_title_hd_mode)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_hd_mode)) },
        )
        SettingsList(
            enabled = !hdMode.value,
            state = indexPreferenceState(
                R.string.pref_key_shader_filter,
                "auto",
                stringListResource(R.array.pref_key_shader_filter_values).toList()
            ),
            title = { Text(text = stringResource(id = R.string.display_filter)) },
            items = stringListResource(R.array.pref_key_shader_filter_display_names)
        )
    }
}

@Composable
private fun RomsSettings(
    state: SettingsViewModel.State,
    onChangeFolder: () -> Unit,
    indexingInProgress: Boolean,
    scanInProgress: Boolean,
) {
    val context = LocalContext.current.applicationContext

    val currentDirectory = state.currentDirectory
    val emptyDirectory = stringResource(R.string.none)

    val currentDirectoryName = remember(state.currentDirectory) {
        runCatching {
            DocumentFile.fromTreeUri(context, Uri.parse(currentDirectory))?.name
        }.getOrNull() ?: emptyDirectory
    }

    SettingsSmallGroup(title = { Text(text = stringResource(id = R.string.roms)) }) {
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.directory)) },
            subtitle = { Text(text = currentDirectoryName) },
            onClick = { onChangeFolder() },
            enabled = !indexingInProgress
        )
        if (scanInProgress) {
            SettingsMenuLink(title = { Text(text = stringResource(id = R.string.stop)) },
                onClick = { LibraryIndexScheduler.cancelLibrarySync(context) })
        } else {
            SettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.rescan)) },
                onClick = { LibraryIndexScheduler.scheduleLibrarySync(context) },
                enabled = !indexingInProgress
            )
        }
    }
}
