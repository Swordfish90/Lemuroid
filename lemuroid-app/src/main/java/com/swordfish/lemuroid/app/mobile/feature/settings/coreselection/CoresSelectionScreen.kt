package com.swordfish.lemuroid.app.mobile.feature.settings.coreselection

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.storage.memory.rememberMemoryIntSettingState
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsPage

@Composable
fun CoresSelectionScreen(
    modifier: Modifier = Modifier,
    viewModel: CoresSelectionViewModel,
) {
    val applicationContext = LocalContext.current.applicationContext

    val cores = viewModel.getSelectedCores().collectAsState(emptyList()).value

    val indexingInProgress = viewModel.indexingInProgress.collectAsState(false).value

    LemuroidSettingsPage(modifier = modifier.fillMaxWidth()) {
        ElevatedCard(
            colors =
                CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.settings_core_selection_ds_info),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LemuroidCardSettingsGroup {
            cores.forEach { (system, core) ->
                val state = rememberMemoryIntSettingState(system.systemCoreConfigs.indexOf(core))

                LemuroidSettingsList(
                    state = state,
                    title = { Text(text = stringResource(system.titleResId)) },
                    items = system.systemCoreConfigs.map { it.coreID.coreDisplayName },
                    enabled = !indexingInProgress,
                    onItemSelected = { index, _ ->
                        viewModel.changeCore(system, system.systemCoreConfigs[index], applicationContext)
                    },
                )
            }
        }
    }
}
