package com.swordfish.lemuroid.app.mobile.feature.gamemenu.patchcodes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.library.db.entity.PatchCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameMenuPatchCodesScreen(
    viewModel: GameMenuPatchCodesViewModel,
    onCodesChanged: () -> Unit,
) {
    val context = LocalContext.current
    val codes by viewModel.codes.collectAsState()
    val importState by viewModel.importState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.operationComplete.collect {
            onCodesChanged()
        }
    }

    LaunchedEffect(importState) {
        when (val state = importState) {
            is GameMenuPatchCodesViewModel.ImportState.Success -> {
                snackbarHostState.showSnackbar(
                    context.resources.getQuantityString(
                        R.plurals.patch_codes_import_success,
                        state.count,
                        state.count,
                    ),
                )
                viewModel.clearImportState()
            }
            is GameMenuPatchCodesViewModel.ImportState.Error -> {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.patch_codes_import_error, state.message),
                )
                viewModel.clearImportState()
            }
            else -> Unit
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.importFromFile(context, it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                
                SmallFloatingActionButton(
                    onClick = {
                        filePickerLauncher.launch(
                            
                            arrayOf("*/*"),
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = stringResource(R.string.patch_codes_import_from_file),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.patch_codes_add),
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (importState is GameMenuPatchCodesViewModel.ImportState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (codes.isEmpty()) {
                EmptyCodesHint(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    codes.forEach { patch ->
                        PatchCodeItem(
                            patch = patch,
                            
                            onToggle = { viewModel.toggleCode(patch) },
                            onDelete = { viewModel.deleteCode(patch) },
                        )
                        Divider()
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPatchCodeDialog(
            onConfirm = { description, code ->
                viewModel.addCode(description, code)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun EmptyCodesHint(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.patch_codes_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.patch_codes_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PatchCodeItem(
    patch: PatchCode,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = patch.description.ifBlank { stringResource(R.string.patch_codes_no_description) },
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = patch.code,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = patch.enabled,
            onCheckedChange = { onToggle() },
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.patch_codes_delete),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun AddPatchCodeDialog(
    onConfirm: (description: String, code: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var description by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.patch_codes_add_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.patch_codes_description_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        codeError = false
                    },
                    label = { Text(stringResource(R.string.patch_codes_code_hint)) },
                    isError = codeError,
                    supportingText = if (codeError) {
                        { Text(stringResource(R.string.patch_codes_code_error)) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (code.isBlank()) {
                        codeError = true
                    } else {
                        onConfirm(description, code)
                    }
                },
            ) {
                Text(stringResource(R.string.patch_codes_add_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.patch_codes_cancel))
            }
        },
    )
}
