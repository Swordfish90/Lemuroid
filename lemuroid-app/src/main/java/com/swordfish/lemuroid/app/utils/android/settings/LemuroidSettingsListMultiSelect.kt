package com.swordfish.lemuroid.app.utils.android.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.storage.base.SettingValueState

@Composable
fun LemuroidSettingsListMultiSelect(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    state: SettingValueState<Set<String>>,
    title: @Composable () -> Unit,
    entryValues: List<String>,
    entries: List<String>,
    icon: @Composable (() -> Unit)? = null,
    confirmButton: String,
    subtitle: @Composable (() -> Unit)? = null,
    onItemsSelected: ((List<String>) -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    if (entryValues.size != entries.size) {
        throw IllegalArgumentException("entries and entryValues need to have the same size")
    }

    var showDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LemuroidSettingsMenuLink(
        enabled = enabled,
        icon = icon,
        title = title,
        subtitle = subtitle,
        action = action,
        onClick = { showDialog = true },
    )

    if (!showDialog) return

    val onAdd: (Int) -> Unit = { selectedIndex ->
        val mutable = state.value.toMutableSet()
        mutable.add(entryValues[selectedIndex])
        state.value = mutable
    }
    val onRemove: (Int) -> Unit = { selectedIndex ->
        val mutable = state.value.toMutableSet()
        mutable.remove(entryValues[selectedIndex])
        state.value = mutable
    }

    AlertDialog(
        title = title,
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
            ) {
                if (subtitle != null) {
                    subtitle()
                    Spacer(modifier = Modifier.size(8.dp))
                }

                entryValues.forEachIndexed { index, item ->
                    val isSelected by rememberUpdatedState(newValue = state.value.contains(item))
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .toggleable(
                                    role = Role.Checkbox,
                                    value = isSelected,
                                    onValueChange = {
                                        if (isSelected) {
                                            onRemove(index)
                                        } else {
                                            onAdd(index)
                                        }
                                    },
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = entries[index],
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                        )
                    }
                }
            }
        },
        onDismissRequest = { showDialog = false },
        confirmButton = {
            TextButton(
                onClick = {
                    showDialog = false
                    onItemsSelected?.invoke(entryValues.filter { state.value.contains(it) })
                },
            ) {
                Text(text = confirmButton)
            }
        },
    )
}
