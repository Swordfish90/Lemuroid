package come.asg.lemudroid.app.mobile.feature.settings.bios

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import come.asg.lemudroid.R
import come.asg.lemudroid.app.utils.android.settings.LemuroidCardSettingsGroup
import come.asg.lemudroid.app.utils.android.settings.LemuroidSettingsMenuLink
import come.asg.lemudroid.app.utils.android.settings.LemuroidSettingsPage
import come.asg.lemudroid.lib.bios.Bios

@Composable
fun BiosScreen(
    modifier: Modifier = Modifier,
    viewModel: BiosSettingsViewModel,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val uiState = viewModel.uiState.collectAsState().value
    val checksumMismatches = uiState.checksumMismatches.toSet()
    LemuroidSettingsPage(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.bios_settings_help),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (uiState.detected.isNotEmpty()) {
            BiosEntries(
                title = stringResource(R.string.settings_bios_category_detected),
                bios = uiState.detected,
                detected = true,
                checksumMismatches = emptySet(),
            )
        }
        if (uiState.notDetected.isNotEmpty()) {
            BiosEntries(
                title = stringResource(R.string.settings_bios_category_not_detected),
                bios = uiState.notDetected,
                detected = false,
                checksumMismatches = checksumMismatches,
            )
        }
    }
}

@Composable
private fun BiosEntries(
    title: String,
    bios: List<Bios>,
    detected: Boolean,
    checksumMismatches: Set<Bios>,
) {
    LemuroidCardSettingsGroup(title = { Text(text = title) }) {
        bios.forEach { biosEntry ->
            BiosEntry(
                bios = biosEntry,
                detected = detected,
                checksumMismatch = biosEntry in checksumMismatches,
            )
        }
    }
}

@Composable
fun BiosEntry(
    bios: Bios,
    detected: Boolean,
    checksumMismatch: Boolean = false,
) {
    val status =
        when {
            checksumMismatch -> stringResource(R.string.bios_status_checksum_mismatch)
            detected -> stringResource(R.string.bios_status_detected)
            else -> stringResource(R.string.bios_status_not_found)
        }
    LemuroidSettingsMenuLink(
        title = { Text(text = bios.description) },
        subtitle = { Text(text = "$status · ${bios.displayName()}") },
        enabled = detected,
        onClick = { },
    )
}
