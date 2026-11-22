package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.cheats.CheatType
import com.swordfish.lemuroid.lib.cheats.CheatValidator
import com.swordfish.lemuroid.lib.cheats.ValidationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameMenuEditCheatScreen(
    viewModel: GameMenuCheatsViewModel,
    systemId: String,
    onSaved: () -> Unit,
) {
    val editingCheat by viewModel.editingCheat.collectAsState()
    val masterCodes by viewModel.masterCodes.collectAsState()

    val availableTypes =
        remember(systemId) {
            CheatType.getTypesForSystem(systemId)
        }

    var title by remember(editingCheat) {
        mutableStateOf(editingCheat?.title ?: "")
    }
    var description by remember(editingCheat) {
        mutableStateOf(editingCheat?.description ?: "")
    }
    var selectedType by remember(editingCheat) {
        val initialType =
            editingCheat?.let { CheatType.findById(it.cheatType) }
                ?: availableTypes.firstOrNull()
                ?: CheatType.defaultForSystem(systemId)
        mutableStateOf(initialType)
    }
    var code by remember(editingCheat) {
        mutableStateOf(editingCheat?.code ?: "")
    }
    var isMasterCode by remember(editingCheat) {
        mutableStateOf(editingCheat?.isMasterCode ?: false)
    }
    var selectedMasterCodeId by remember(editingCheat) {
        mutableStateOf(editingCheat?.masterCodeId)
    }
    var enabled by remember(editingCheat) {
        mutableStateOf(editingCheat?.enabled ?: true)
    }

    var titleError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }
    val emptyTitleError = stringResource(R.string.cheat_error_name_empty)
    var masterCodeDropdownExpanded by remember { mutableStateOf(false) }

    val otherMasterCodes =
        remember(masterCodes, editingCheat) {
            masterCodes.filter { it.id != editingCheat?.id }
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleError = null
            },
            label = { Text(stringResource(R.string.cheat_title)) },
            placeholder = { Text(stringResource(R.string.cheat_title_hint)) },
            supportingText =
                titleError?.let { msg ->
                    {
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = titleError != null,
        )

        OutlinedTextField(
            value = code,
            onValueChange = { newCode ->
                code = newCode
                codeError = null
                val detected = CheatValidator.detectType(systemId, newCode)
                if (detected != null && availableTypes.contains(detected)) {
                    selectedType = detected
                }
            },
            label = { Text(stringResource(R.string.cheat_code)) },
            placeholder = { Text(stringResource(R.string.cheat_code_hint)) },
            supportingText =
                codeError?.let { msg ->
                    {
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 8,
            isError = codeError != null,
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.cheat_description)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.cheat_format),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableTypes.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            codeError = null
                        },
                        label = { Text(type.displayName) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.cheat_is_master_code),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.cheat_is_master_code_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isMasterCode,
                onCheckedChange = {
                    isMasterCode = it
                    if (it) {
                        selectedMasterCodeId = null
                    }
                },
            )
        }

        if (!isMasterCode && otherMasterCodes.isNotEmpty()) {
            val selectedMasterCodeName =
                otherMasterCodes.firstOrNull { it.id == selectedMasterCodeId }?.title
                    ?: stringResource(R.string.cheat_master_code_none)

            ExposedDropdownMenuBox(
                expanded = masterCodeDropdownExpanded,
                onExpandedChange = { masterCodeDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = selectedMasterCodeName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.cheat_master_code_dependency)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = masterCodeDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = masterCodeDropdownExpanded,
                    onDismissRequest = { masterCodeDropdownExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.cheat_master_code_none)) },
                        onClick = {
                            selectedMasterCodeId = null
                            masterCodeDropdownExpanded = false
                        },
                    )
                    otherMasterCodes.forEach { mc ->
                        DropdownMenuItem(
                            text = { Text(mc.title) },
                            onClick = {
                                selectedMasterCodeId = mc.id
                                masterCodeDropdownExpanded = false
                            },
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.cheat_enabled),
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                titleError = null
                codeError = null
                if (title.trim().isEmpty()) {
                    titleError = emptyTitleError
                    return@Button
                }
                val result =
                    viewModel.saveCheat(
                        title = title,
                        description = description,
                        cheatType = selectedType,
                        code = code,
                        isMasterCode = isMasterCode,
                        masterCodeId = selectedMasterCodeId,
                        enabled = enabled,
                    )
                when (result) {
                    is ValidationResult.Valid -> onSaved()
                    is ValidationResult.Invalid -> {
                        if (result.message.contains("Cheat name", ignoreCase = true)) {
                            titleError = emptyTitleError
                        } else {
                            codeError = result.message
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.game_menu_save))
        }
    }
}
