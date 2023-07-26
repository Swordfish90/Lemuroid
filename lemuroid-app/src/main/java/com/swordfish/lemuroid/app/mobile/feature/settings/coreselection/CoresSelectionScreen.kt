package com.swordfish.lemuroid.app.mobile.feature.settings.coreselection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.storage.base.rememberIntSettingState
import com.alorma.compose.settings.ui.SettingsList
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig

@Composable
fun CoresSelectionScreen(
    cores: List<CoresSelection.SelectedCore>,
    indexingInProgress: Boolean,
    onCoreChanged: (GameSystem, SystemCoreConfig) -> Unit
) {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            cores.forEach { (system, core) ->
                val state = rememberIntSettingState()
                state.value = system.systemCoreConfigs.indexOf(core)

                SettingsList(
                    state = state,
                    title = { Text(text = stringResource(system.titleResId)) },
                    items = system.systemCoreConfigs.map { it.coreID.coreDisplayName },
                    enabled = !indexingInProgress,
                    onItemSelected = { index, _ ->
                        onCoreChanged(system, system.systemCoreConfigs[index])
                    }
                )
            }
        }
    }
}
