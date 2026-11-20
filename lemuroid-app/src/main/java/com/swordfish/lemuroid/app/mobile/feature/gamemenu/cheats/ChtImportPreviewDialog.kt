package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.cheats.cht.CandidateStatus
import com.swordfish.lemuroid.lib.cheats.cht.ImportCandidate
import com.swordfish.lemuroid.lib.cheats.cht.ImportMode
import com.swordfish.lemuroid.lib.cheats.cht.ImportPreview

@Composable
fun ChtImportPreviewDialog(
    preview: ImportPreview,
    existingCheatsCount: Int,
    onDismiss: () -> Unit,
    onConfirmImport: (ImportMode) -> Unit,
) {
    var selectedMode by remember { mutableStateOf(ImportMode.MERGE) }
    var showReplaceConfirmation by remember { mutableStateOf(false) }

    val toImportCount =
        if (selectedMode == ImportMode.MERGE) {
            preview.validEntries.size
        } else {
            preview.totalEntries - preview.unsupportedEntries.size - preview.invalidEntries.size
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.92f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(text = stringResource(R.string.cheat_import_preview_title))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.cheat_import_file_label, preview.fileName),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.cheat_import_system_label, preview.systemId.uppercase()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatBadge(
                        label = "${preview.totalEntries} Total",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    StatBadge(
                        label = "${preview.validEntries.size} Valid",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    if (preview.duplicateEntries.isNotEmpty()) {
                        StatBadge(
                            label = "${preview.duplicateEntries.size} Dups",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    val issuesCount = preview.unsupportedEntries.size + preview.invalidEntries.size
                    if (issuesCount > 0) {
                        StatBadge(
                            label = "$issuesCount Issues",
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Text(
                    text = "Import Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Column(
                    modifier =
                        Modifier
                            .selectableGroup()
                            .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ModeRadioButton(
                        selected = selectedMode == ImportMode.MERGE,
                        title = stringResource(R.string.cheat_import_mode_merge),
                        description = stringResource(R.string.cheat_import_mode_merge_desc),
                        onClick = { selectedMode = ImportMode.MERGE },
                    )
                    ModeRadioButton(
                        selected = selectedMode == ImportMode.REPLACE,
                        title = stringResource(R.string.cheat_import_mode_replace),
                        description = stringResource(R.string.cheat_import_mode_replace_desc),
                        onClick = { selectedMode = ImportMode.REPLACE },
                    )
                }

                Divider()

                Text(
                    text = "Entries Preview",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(preview.candidates) { candidate ->
                        CandidateItemView(candidate)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMode == ImportMode.REPLACE && existingCheatsCount > 0) {
                        showReplaceConfirmation = true
                    } else {
                        onConfirmImport(selectedMode)
                    }
                },
                enabled = toImportCount > 0,
            ) {
                Text(text = stringResource(R.string.cheat_import_action) + " ($toImportCount)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )

    if (showReplaceConfirmation) {
        AlertDialog(
            onDismissRequest = { showReplaceConfirmation = false },
            title = { Text(text = stringResource(R.string.cheat_import_confirm_replace_title)) },
            text = { Text(text = stringResource(R.string.cheat_import_confirm_replace_msg, existingCheatsCount)) },
            confirmButton = {
                Button(
                    onClick = {
                        showReplaceConfirmation = false
                        onConfirmImport(ImportMode.REPLACE)
                    },
                ) {
                    Text(text = stringResource(R.string.cheat_import_mode_replace))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplaceConfirmation = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun ModeRadioButton(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                )
                .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(containerColor, RoundedCornerShape(8.dp))
                .padding(vertical = 4.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CandidateItemView(candidate: ImportCandidate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = candidate.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(candidate.status)
            }
            if (candidate.code.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = candidate.code.replace('\n', ' '),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val reason = candidate.reason
            if (reason != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        when (candidate.status) {
                            CandidateStatus.DUPLICATE -> MaterialTheme.colorScheme.tertiary
                            CandidateStatus.UNSUPPORTED -> MaterialTheme.colorScheme.onSurfaceVariant
                            CandidateStatus.INVALID -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: CandidateStatus) {
    val (label, containerColor, contentColor) =
        when (status) {
            CandidateStatus.VALID ->
                Triple(
                    stringResource(R.string.cheat_status_valid),
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                )
            CandidateStatus.DUPLICATE ->
                Triple(
                    stringResource(R.string.cheat_status_duplicate),
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer,
                )
            CandidateStatus.UNSUPPORTED ->
                Triple(
                    stringResource(R.string.cheat_status_unsupported),
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                )
            CandidateStatus.INVALID ->
                Triple(
                    stringResource(R.string.cheat_status_invalid),
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                )
        }

    Box(
        modifier =
            Modifier
                .background(containerColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}
