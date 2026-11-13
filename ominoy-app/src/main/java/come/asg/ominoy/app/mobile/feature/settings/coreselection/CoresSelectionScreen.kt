package come.asg.ominoy.app.mobile.feature.settings.coreselection

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.storage.memory.rememberMemoryIntSettingState
import come.asg.ominoy.app.utils.android.settings.OminoyCardSettingsGroup
import come.asg.ominoy.app.utils.android.settings.OminoySettingsList
import come.asg.ominoy.app.utils.android.settings.OminoySettingsPage

@Composable
fun CoresSelectionScreen(
    modifier: Modifier = Modifier,
    viewModel: CoresSelectionViewModel,
) {
    val applicationContext = LocalContext.current.applicationContext

    val cores = viewModel.getSelectedCores().collectAsState(emptyList()).value

    val indexingInProgress = viewModel.indexingInProgress.collectAsState(false).value

    OminoySettingsPage(modifier = modifier.fillMaxWidth()) {
        OminoyCardSettingsGroup {
            cores.forEach { (system, core) ->
                val state = rememberMemoryIntSettingState(system.systemCoreConfigs.indexOf(core))

                OminoySettingsList(
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
