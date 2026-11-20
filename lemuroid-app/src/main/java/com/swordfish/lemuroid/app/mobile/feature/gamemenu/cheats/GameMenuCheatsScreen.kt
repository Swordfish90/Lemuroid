package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.cheats.CheatType
import com.swordfish.lemuroid.lib.cheats.cht.ChtExportManager
import com.swordfish.lemuroid.lib.library.db.entity.Cheat

@Composable
fun GameMenuCheatsScreen(
    viewModel: GameMenuCheatsViewModel,
    onAddCheatClicked: () -> Unit,
    onEditCheatClicked: (Cheat) -> Unit,
) {
    val context = LocalContext.current
    val cheats by viewModel.cheats.collectAsState()
    val masterCodes by viewModel.masterCodes.collectAsState()
    val importPreview by viewModel.importPreview.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val exportSuccess by viewModel.exportSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var cheatToDelete by remember { mutableStateOf<Cheat?>(null) }
    var showClearAllConfirmation by remember { mutableStateOf(false) }

    val openDocumentLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri: Uri? ->
            if (uri != null) {
                val fileName =
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst() && nameIndex != -1) cursor.getString(nameIndex) else null
                    } ?: "cheats.cht"
                try {
                    val content =
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader(Charsets.UTF_8).readText()
                        }
                    if (content != null) {
                        viewModel.loadChtContentForImport(content, fileName)
                    } else {
                        Toast.makeText(context, "Failed to read file", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    val createDocumentLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("*/*"),
        ) { uri: Uri? ->
            if (uri != null) {
                viewModel.exportToUri(uri, context.contentResolver)
            }
        }

    LaunchedEffect(Unit) {
        viewModel.importTrigger.collect {
            openDocumentLauncher.launch(arrayOf("*/*"))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exportTrigger.collect {
            val fileName = ChtExportManager.buildSafeExportFileName(viewModel.game)
            createDocumentLauncher.launch(fileName)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.clearAllTrigger.collect {
            showClearAllConfirmation = true
        }
    }

    LaunchedEffect(importResult) {
        importResult?.let { res ->
            val msg = context.getString(R.string.cheat_import_success, res.importedCount)
            val detail = if (res.skippedDuplicatesCount > 0) " (${res.skippedDuplicatesCount} skipped)" else ""
            Toast.makeText(context, "$msg$detail", Toast.LENGTH_SHORT).show()
            viewModel.dismissImportResult()
        }
    }

    LaunchedEffect(exportSuccess) {
        exportSuccess?.let { count ->
            Toast.makeText(context, context.getString(R.string.cheat_export_success, count), Toast.LENGTH_SHORT).show()
            viewModel.dismissExportSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.dismissErrorMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cheats.isEmpty()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.game_menu_no_cheats),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(cheats, key = { it.id }) { cheat ->
                    val masterCode =
                        if (cheat.masterCodeId != null) {
                            masterCodes.firstOrNull { it.id == cheat.masterCodeId }
                        } else {
                            null
                        }
                    val isPrerequisiteUnmet =
                        cheat.masterCodeId != null && (masterCode == null || !masterCode.enabled)

                    CheatItemCard(
                        cheat = cheat,
                        masterCode = masterCode,
                        isPrerequisiteUnmet = isPrerequisiteUnmet,
                        onToggle = { viewModel.toggleCheat(cheat) },
                        onEdit = {
                            viewModel.prepareEditCheat(cheat)
                            onEditCheatClicked(cheat)
                        },
                        onDelete = { cheatToDelete = cheat },
                    )
                }
            }
        }

        FloatingActionButton(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            onClick = {
                viewModel.prepareAddCheat()
                onAddCheatClicked()
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.game_menu_add_cheat),
            )
        }
    }

        cheatToDelete?.let { cheat ->
            val hasDependents = cheat.isMasterCode && cheats.any { it.masterCodeId == cheat.id }
            AlertDialog(
                onDismissRequest = { cheatToDelete = null },
                title = { Text(stringResource(R.string.cheat_delete_confirmation_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.cheat_delete_confirmation_message, cheat.title))
                        if (hasDependents) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.cheat_delete_master_warning),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCheat(cheat)
                            cheatToDelete = null
                        },
                    ) {
                        Text(stringResource(R.string.cheat_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { cheatToDelete = null }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
            )
        }

        if (showClearAllConfirmation) {
            AlertDialog(
                onDismissRequest = { showClearAllConfirmation = false },
                title = { Text(stringResource(R.string.cheat_clear_all_title)) },
                text = { Text(stringResource(R.string.cheat_clear_all_message, cheats.size)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllCheats()
                            showClearAllConfirmation = false
                        },
                    ) {
                        Text(stringResource(R.string.cheat_clear_all_action), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearAllConfirmation = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
            )
        }

        importPreview?.let { preview ->
            ChtImportPreviewDialog(
                preview = preview,
                existingCheatsCount = cheats.size,
                onDismiss = { viewModel.dismissImportPreview() },
                onConfirmImport = { mode -> viewModel.confirmImport(mode) },
            )
        }
    }

@Composable
fun CheatsOverflowMenu(
    viewModel: GameMenuCheatsViewModel,
) {
    var expanded by remember { mutableStateOf(false) }
    val cheats by viewModel.cheats.collectAsState()

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.cheat_menu_more),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.cheat_import_cht_file)) },
                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                onClick = {
                    expanded = false
                    viewModel.triggerImport()
                },
            )
            if (cheats.isNotEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.cheat_export_cht_file)) },
                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                    onClick = {
                        expanded = false
                        viewModel.triggerExport()
                    },
                )
                Divider()
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.cheat_clear_all),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        expanded = false
                        viewModel.triggerClearAll()
                    },
                )
            }
        }
    }
}

@Composable
private fun CheatItemCard(
    cheat: Cheat,
    masterCode: Cheat?,
    isPrerequisiteUnmet: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (cheat.enabled) {
                        Box(
                            modifier =
                                Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                        )
                    }
                    Text(
                        text = cheat.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (cheat.enabled) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (cheat.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val cheatType = CheatType.findById(cheat.cheatType)
                    val typeName = cheatType?.displayName ?: cheat.cheatType
                    val firstLineCode = cheat.code.lines().firstOrNull { it.isNotBlank() } ?: ""
                    val hasMoreLines = cheat.code.lines().filter { it.isNotBlank() }.size > 1
                    val codeDisplay = if (hasMoreLines) "$firstLineCode (+)" else firstLineCode

                    Text(
                        text = "$typeName  •  $codeDisplay",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val desc = cheat.description
                    if (!desc.isNullOrBlank()) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }

                    if (cheat.isMasterCode || isPrerequisiteUnmet || masterCode != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (cheat.isMasterCode) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Text(
                                        text = stringResource(R.string.cheat_badge_master),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }

                            if (isPrerequisiteUnmet) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                        Text(
                                            text = stringResource(R.string.cheat_badge_prerequisite_disabled),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            } else if (masterCode != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                ) {
                                    Text(
                                        text = stringResource(R.string.cheat_badge_requires, masterCode.title),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Switch(
                                checked = cheat.enabled,
                                onCheckedChange = { onToggle() },
                            )
                            Text(
                                text =
                                    if (cheat.enabled) {
                                        stringResource(R.string.cheat_enabled)
                                    } else {
                                        stringResource(R.string.cheat_disabled)
                                    },
                                style = MaterialTheme.typography.labelMedium,
                                color =
                                    if (cheat.enabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = onEdit) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.game_menu_edit_cheat),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = onDelete) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.cheat_delete),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
