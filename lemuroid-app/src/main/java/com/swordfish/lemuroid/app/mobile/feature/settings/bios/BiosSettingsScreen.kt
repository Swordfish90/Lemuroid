package com.swordfish.lemuroid.app.mobile.feature.settings.bios

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.SettingsSmallGroup
import com.swordfish.lemuroid.lib.bios.Bios

@Composable
fun BiosScreen(viewModel: BiosSettingsViewModel) {
    val uiState = viewModel.uiState
        .collectAsState()
        .value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
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
    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_bios_category_detected)) }
    ) {
        detected.forEach {
            BiosEntry(it, true)
        }
    }
}

@Composable
private fun SupportedEntries(supported: List<Bios>) {
    SettingsSmallGroup(
        title = { Text(text = stringResource(id = R.string.settings_bios_category_not_detected)) }
    ) {
        supported.forEach {
            BiosEntry(it, false)
        }
    }
}

@Composable
fun BiosEntry(bios: Bios, detected: Boolean) {
    SettingsMenuLink(
        title = { Text(text = bios.description) },
        subtitle = { Text(text = bios.displayName()) },
        enabled = detected,
        onClick = { }
    )
}
