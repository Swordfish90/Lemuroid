package com.swordfish.lemuroid.app.mobile.feature.settings.savesync

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsListMultiSelect
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import com.swordfish.lemuroid.app.utils.android.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues
import com.swordfish.lemuroid.app.utils.android.indexSetPreferenceState
import com.swordfish.lemuroid.lib.library.CoreID

@Composable
fun SaveSyncSettingsScreen(padding: MergedPaddingValues, viewModel: SaveSyncSettingsViewModel) {
    val context = LocalContext.current.applicationContext

    val saveSyncState = viewModel.uiState
        .collectAsState()
        .value

    val isSyncInProgress = viewModel.indexingInProgress
        .observeAsState(true)
        .value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(padding.asPaddingValues()),
    ) {
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_save_sync_configure, saveSyncState.provider)) },
            subtitle = { Text(text = saveSyncState.configInfo) },
            enabled = !isSyncInProgress,
            onClick = { context.startActivity(Intent(context, saveSyncState.settingsActivity)) }
        )
        SettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_save_sync_enable, default = false),
            title = { Text(text = stringResource(id = R.string.settings_save_sync_include_saves)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_save_sync_include_saves_description, saveSyncState.savesSpace)) },
            enabled = saveSyncState.isConfigured && !isSyncInProgress
        )
        SettingsListMultiSelect(
            state = indexSetPreferenceState(
                stringResource(R.string.pref_key_save_sync_cores),
                emptySet(),
                CoreID.values().map { it.coreName }
            ),
            title = { Text(text = stringResource(id = R.string.settings_save_sync_include_states)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_save_sync_include_states_description)) },
            useSelectedValuesAsSubtitle = false,
            items = saveSyncState.coreNames,
            enabled = saveSyncState.isConfigured && !isSyncInProgress,
            confirmButton = stringResource(id = R.string.ok)
        )
        SettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_save_sync_auto, default = false),
            title = { Text(text = stringResource(id = R.string.settings_save_sync_enable_auto)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_save_sync_enable_auto_description)) },
            enabled = saveSyncState.isConfigured && !isSyncInProgress
        )
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_save_sync_refresh)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_save_sync_refresh_description, saveSyncState.lastSyncInfo)) },
            enabled = saveSyncState.isConfigured && !isSyncInProgress,
            onClick = { SaveSyncWork.enqueueManualWork(context) }
        )
    }
}
