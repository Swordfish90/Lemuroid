package com.swordfish.lemuroid.app.mobile.feature.settings.bios

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsPage
import com.swordfish.lemuroid.lib.bios.Bios

@Composable
fun BiosScreen(
    modifier: Modifier = Modifier,
    viewModel: BiosSettingsViewModel,
) {
    val uiState =
        viewModel.uiState
            .collectAsState()
            .value

    LemuroidSettingsPage(modifier = modifier.fillMaxSize()) {
        if (uiState.detected.isNotEmpty()) {
            DetectedEntries(uiState.detected)
        }
        if (uiState.notDetected.isNotEmpty()) {
            SupportedEntries(uiState.notDetected)
        }
    }
}

@Composable
private fun DetectedEntries(detected: List<Bios>) {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_bios_category_detected)) },
    ) {
        detected.forEach {
            BiosEntry(it, true)
        }
    }
}

@Composable
private fun SupportedEntries(supported: List<Bios>) {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_bios_category_not_detected)) },
    ) {
        supported.forEach {
            BiosEntry(it, false)
        }
    }
}

@Composable
fun BiosEntry(
    bios: Bios,
    detected: Boolean,
) {
    LemuroidSettingsMenuLink(
        title = { Text(text = bios.description) },
        subtitle = { Text(text = bios.displayName()) },
        enabled = detected,
        onClick = { },
    )
}
