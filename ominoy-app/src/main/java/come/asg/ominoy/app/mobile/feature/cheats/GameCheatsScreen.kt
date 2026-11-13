package come.asg.ominoy.app.mobile.feature.cheats

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import come.asg.ominoy.R
import come.asg.ominoy.lib.library.SystemID
import come.asg.ominoy.lib.library.db.dao.GameCheatDao
import come.asg.ominoy.lib.library.db.entity.GameCheat
import kotlinx.coroutines.launch
import timber.log.Timber

private const val CHEAT_TYPE_PREFERENCES = "game_cheat_code_type_preferences"

@Composable
fun GameCheatsScreen(
    gameId: Int,
    gameName: String = "",
    systemId: String,
    gameCheatDao: GameCheatDao,
    modifier: Modifier = Modifier,
    onCheatsChanged: () -> Unit = {},
) {
    val cheats by gameCheatDao.observeForGame(gameId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val codeTypes = remember(systemId) { CheatCodeType.forSystem(systemId) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCheat by remember { mutableStateOf<GameCheat?>(null) }
    var addError by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (gameName.isNotBlank()) {
            Text(text = gameName, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = stringResource(R.string.game_cheats_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (codeTypes.isEmpty()) {
            Text(
                text = stringResource(R.string.game_cheats_type_unsupported_system),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Text(
                text = stringResource(R.string.game_cheats_add),
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        if (cheats.isEmpty()) {
            Text(
                text = stringResource(R.string.game_cheats_empty),
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            cheats.forEach { cheat ->
                GameCheatItem(
                    cheat = cheat,
                    onEnabledChanged = { enabled ->
                        scope.launch {
                            runCatching { gameCheatDao.update(cheat.copy(enabled = enabled)) }
                                .onSuccess { onCheatsChanged() }
                                .onFailure { Timber.e(it, "Unable to update cheat ${cheat.id}") }
                        }
                    },
                    onDelete = {
                        scope.launch {
                            runCatching { gameCheatDao.delete(cheat) }
                                .onSuccess { onCheatsChanged() }
                                .onFailure { Timber.e(it, "Unable to delete cheat ${cheat.id}") }
                        }
                    },
                    onEdit = { editingCheat = cheat },
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showAddDialog) {
        val initialType = remember(context, systemId) { readLastType(context, systemId, codeTypes) }
        GameCheatDialog(
            titleRes = R.string.game_cheats_add_title,
            systemId = systemId,
            codeTypes = codeTypes,
            initialType = initialType,
            onTypeSelected = { saveLastType(context, systemId, it) },
            onDismiss = { showAddDialog = false },
            persistenceError = addError,
            onSave = { name, code, type ->
                addError = false
                scope.launch {
                    runCatching {
                        gameCheatDao.insert(
                            GameCheat(
                                gameId = gameId,
                                name = name,
                                code = code,
                                enabled = true,
                                codeType = type.persistedValue,
                            ),
                        )
                    }.onSuccess {
                        onCheatsChanged()
                        showAddDialog = false
                    }.onFailure {
                        addError = true
                        Timber.e(it, "Unable to add a cheat for game $gameId")
                    }
                }
            },
        )
    }

    editingCheat?.let { cheat ->
        GameCheatDialog(
            titleRes = R.string.game_cheats_edit_title,
            initialName = cheat.name,
            initialCode =
                editableCheatCode(
                    cheat.code,
                    CheatCodeType.fromPersistedValue(cheat.codeType),
                    systemId,
                ),
            initialType = CheatCodeType.fromPersistedValue(cheat.codeType),
            systemId = systemId,
            codeTypes = codeTypes,
            onTypeSelected = { saveLastType(context, systemId, it) },
            onDismiss = { editingCheat = null },
            persistenceError = editError,
            onSave = { name, code, type ->
                editError = false
                scope.launch {
                    runCatching {
                        // Copying the row retains its current enabled state.
                        gameCheatDao.update(cheat.copy(name = name, code = code, codeType = type.persistedValue))
                    }.onSuccess {
                        onCheatsChanged()
                        editingCheat = null
                    }.onFailure {
                        editError = true
                        Timber.e(it, "Unable to edit cheat ${cheat.id}")
                    }
                }
            },
        )
    }
}

@Composable
private fun GameCheatItem(
    cheat: GameCheat,
    onEnabledChanged: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = cheat.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(typeLabel(CheatCodeType.fromPersistedValue(cheat.codeType))),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = cheat.code,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Switch(checked = cheat.enabled, onCheckedChange = onEnabledChanged)
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.game_cheats_edit, cheat.name),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.game_cheats_delete, cheat.name),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun GameCheatDialog(
    titleRes: Int,
    systemId: String,
    codeTypes: List<CheatCodeType>,
    initialName: String = "",
    initialCode: String = "",
    initialType: CheatCodeType? = null,
    onTypeSelected: (CheatCodeType) -> Unit,
    onDismiss: () -> Unit,
    persistenceError: Boolean = false,
    onSave: (name: String, code: String, type: CheatCodeType) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var code by remember(initialCode) { mutableStateOf(initialCode) }
    var selectedType by remember(systemId, initialName, initialType) { mutableStateOf(initialType) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    val normalizedCode = selectedType?.let { CheatCodeValidator.normalize(code, it, systemId) }
    val canSave = name.isNotBlank() && selectedType != null && normalizedCode != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.game_cheats_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { typeMenuExpanded = true },
                        enabled = codeTypes.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "${stringResource(R.string.game_cheats_type_label)}: " +
                                stringResource(typeLabel(selectedType)),
                        )
                    }
                    DropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false },
                    ) {
                        codeTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(stringResource(typeLabel(type))) },
                                onClick = {
                                    selectedType = type
                                    typeMenuExpanded = false
                                    onTypeSelected(type)
                                },
                            )
                        }
                    }
                }
                if (codeTypes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.game_cheats_type_unsupported_system),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(stringResource(R.string.game_cheats_code)) },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (systemId == SystemID.GBA.dbname) {
                    Text(
                        text = stringResource(R.string.game_cheats_gba_enable_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (persistenceError) {
                    Text(
                        text = stringResource(R.string.game_cheats_save_failed),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                when {
                    selectedType == null ->
                        Text(
                            text = stringResource(R.string.game_cheats_type_select_required),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    normalizedCode == null ->
                        Text(
                            text = stringResource(errorMessage(selectedType!!, systemId)),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = {
                    val type = selectedType
                    if (type != null) normalizedCode?.let { onSave(name.trim(), it, type) }
                },
            ) {
                Text(stringResource(R.string.game_cheats_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private fun typeLabel(type: CheatCodeType?): Int =
    when (type) {
        CheatCodeType.GAME_SHARK -> R.string.game_cheats_type_game_shark
        CheatCodeType.GAME_GENIE -> R.string.game_cheats_type_game_genie
        CheatCodeType.ACTION_REPLAY -> R.string.game_cheats_type_action_replay
        CheatCodeType.CODE_BREAKER -> R.string.game_cheats_type_code_breaker
        null -> R.string.game_cheats_type_not_set
    }

private fun errorMessage(
    type: CheatCodeType,
    systemId: String,
): Int =
    when (type) {
        CheatCodeType.GAME_SHARK ->
            if (systemId ==
                SystemID.GBA.dbname
            ) {
                R.string.game_cheats_error_gba_gameshark
            } else {
                R.string.game_cheats_error_gb_gameshark
            }
        CheatCodeType.GAME_GENIE -> R.string.game_cheats_error_game_genie
        CheatCodeType.ACTION_REPLAY -> R.string.game_cheats_error_action_replay
        CheatCodeType.CODE_BREAKER -> R.string.game_cheats_error_code_breaker
    }

private fun readLastType(
    context: Context,
    systemId: String,
    options: List<CheatCodeType>,
): CheatCodeType? {
    val stored =
        context
            .getSharedPreferences(CHEAT_TYPE_PREFERENCES, Context.MODE_PRIVATE)
            .getString(systemId, null)
    return CheatCodeType.fromPersistedValue(stored)?.takeIf(options::contains) ?: options.firstOrNull()
}

private fun saveLastType(
    context: Context,
    systemId: String,
    type: CheatCodeType,
) {
    context
        .getSharedPreferences(CHEAT_TYPE_PREFERENCES, Context.MODE_PRIVATE)
        .edit()
        .putString(systemId, type.persistedValue)
        .apply()
}

/** Stored '+' separates code groups; paired GBA formats require two groups on each editable line. */
private fun editableCheatCode(
    code: String,
    type: CheatCodeType?,
    systemId: String,
): String {
    val effectiveType = type ?: CheatCodeType.forSystem(systemId).firstOrNull()
    return effectiveType?.let { CheatCodeValidator.toEditableText(code, it, systemId) }
        ?: code.replace("+", "\n")
}
