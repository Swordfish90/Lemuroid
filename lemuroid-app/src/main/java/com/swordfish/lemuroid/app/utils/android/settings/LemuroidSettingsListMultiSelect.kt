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
    state: SettingValueState<Set<Int>>,
    title: @Composable () -> Unit,
    items: List<String>,
    icon: @Composable (() -> Unit)? = null,
    confirmButton: String,
    useSelectedValuesAsSubtitle: Boolean = true,
    subtitle: @Composable (() -> Unit)? = null,
    onItemsSelected: ((List<String>) -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    if (state.value.any { index -> index >= items.size }) {
        throw IndexOutOfBoundsException("Current indexes for $title list setting cannot be grater than items size")
    }

    var showDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val safeSubtitle =
        if (useSelectedValuesAsSubtitle) {
            {
                Text(
                    text =
                        state.value.map { index -> items[index] }
                            .joinToString(separator = ", ") { it },
                )
            }
        } else {
            subtitle
        }

    LemuroidSettingsMenuLink(
        enabled = enabled,
        icon = icon,
        title = title,
        subtitle = safeSubtitle,
        action = action,
        onClick = { showDialog = true },
    )

    if (!showDialog) return

    val onAdd: (Int) -> Unit = { selectedIndex ->
        val mutable = state.value.toMutableSet()
        mutable.add(selectedIndex)
        state.value = mutable
    }
    val onRemove: (Int) -> Unit = { selectedIndex ->
        val mutable = state.value.toMutableSet()
        mutable.remove(selectedIndex)
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

                items.forEachIndexed { index, item ->
                    val isSelected by rememberUpdatedState(newValue = state.value.contains(index))
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
                            text = item,
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
                    onItemsSelected?.invoke(state.value.map { index -> items[index] })
                },
            ) {
                Text(text = confirmButton)
            }
        },
    )
}
