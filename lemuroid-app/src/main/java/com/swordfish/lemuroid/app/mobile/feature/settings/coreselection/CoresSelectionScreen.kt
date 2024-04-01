package com.swordfish.lemuroid.app.mobile.feature.settings.coreselection

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.storage.memory.rememberMemoryIntSettingState
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsPage

@Composable
fun CoresSelectionScreen(
    padding: MergedPaddingValues,
    viewModel: CoresSelectionViewModel,
) {
    val applicationContext = LocalContext.current.applicationContext

    val cores = viewModel.getSelectedCores().collectAsState(emptyList()).value

    val indexingInProgress = viewModel.indexingInProgress.observeAsState(false).value

    LemuroidSettingsPage(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(padding.asPaddingValues()),
    ) {
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
