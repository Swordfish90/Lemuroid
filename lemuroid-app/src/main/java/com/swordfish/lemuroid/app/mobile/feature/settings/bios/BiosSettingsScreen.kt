package com.swordfish.lemuroid.app.mobile.feature.settings.bios

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
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
    val context = LocalContext.current
    val importResult = viewModel.importResult.collectAsState().value
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            DocumentFile.fromSingleUri(context, uri)?.let { file ->
                viewModel.importPs2File(file, context.contentResolver)
            }
        }
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            DocumentFile.fromTreeUri(context, uri)?.let { folder ->
                viewModel.importPs2Folders(folder, context.contentResolver)
            }
        }
    }
    LaunchedEffect(importResult) {
        importResult?.let {
            Toast.makeText(
                context,
                context.getString(if (it) R.string.settings_bios_ps2_import_success else R.string.settings_bios_ps2_import_error),
                Toast.LENGTH_LONG,
            ).show()
            viewModel.importResult.value = null
        }
    }

    val uiState =
        viewModel.uiState
            .collectAsState()
            .value

    LemuroidSettingsPage(modifier = modifier.fillMaxSize()) {
        LemuroidCardSettingsGroup(
            title = { Text(text = stringResource(id = R.string.settings_bios_ps2_title)) },
        ) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.settings_bios_ps2_import)) },
                subtitle = { Text(text = stringResource(id = R.string.settings_bios_ps2_description)) },
                onClick = { filePicker.launch(arrayOf("*/*")) },
            )
        }
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
