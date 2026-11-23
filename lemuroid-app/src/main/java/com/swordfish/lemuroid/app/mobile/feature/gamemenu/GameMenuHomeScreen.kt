package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Intent
import android.text.format.Formatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.memory.rememberMemoryBooleanSettingState
import com.alorma.compose.settings.storage.memory.rememberMemoryIntSettingState
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.tilt.TiltConfigurationMenuEntry
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.retroachievements.RetroAchievementsPreferences
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import com.swordfish.lemuroid.lib.cheats.CheatSupport
import com.swordfish.lemuroid.lib.patch.PatchManager
import com.swordfish.lemuroid.lib.patch.PatchSource
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import kotlin.reflect.KFunction1

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameMenuHomeScreen(
    navController: NavController,
    gameMenuRequest: GameMenuActivity.GameMenuRequest,
    onResult: KFunction1<Intent.() -> Unit, Unit>,
) {
    val context = LocalContext.current
    var showSpeedDialog by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableIntStateOf(gameMenuRequest.fastForwardSpeed) }

    val availablePatches = remember(gameMenuRequest.game.id) {
        PatchManager.discoverPatches(context, gameMenuRequest.game)
    }
    var showPatchesDialog by remember { mutableStateOf(false) }
    val initialPatchIds = remember(gameMenuRequest.game.id) {
        PatchManager.getEnabledPatchIds(context, gameMenuRequest.game.id, availablePatches)
    }
    var enabledPatchIds by remember(gameMenuRequest.game.id) {
        mutableStateOf(initialPatchIds)
    }
    val patchesChanged = enabledPatchIds != initialPatchIds

    val isHardcore = remember {
        val prefs = RetroAchievementsPreferences(context)
        prefs.enabled && prefs.hardcore
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (gameMenuRequest.coreConfig.statesSupported) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.game_menu_save)) },
                subtitle = if (isHardcore) { { Text(text = stringResource(id = R.string.game_menu_disabled_in_hardcore)) } } else null,
                enabled = !isHardcore,
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_save),
                        contentDescription = stringResource(id = R.string.game_menu_save),
                    )
                },
                onClick = { if (!isHardcore) navController.navigateToRoute(GameMenuRoute.SAVE) },
            )

            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.game_menu_load)) },
                subtitle = if (isHardcore) { { Text(text = stringResource(id = R.string.game_menu_disabled_in_hardcore)) } } else null,
                enabled = !isHardcore,
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_load),
                        contentDescription = stringResource(id = R.string.game_menu_load),
                    )
                },
                onClick = { if (!isHardcore) navController.navigateToRoute(GameMenuRoute.LOAD) },
            )
        }

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_restart)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_restart),
                    contentDescription = stringResource(id = R.string.game_menu_restart),
                )
            },
            onClick = {
                onResult {
                    if (patchesChanged) {
                        putExtra(GameMenuContract.RESULT_RELOAD_GAME, true)
                    } else {
                        putExtra(GameMenuContract.RESULT_RESET, true)
                    }
                }
            },
        )

        LemuroidSettingsSwitch(
            title = { Text(text = stringResource(id = R.string.game_menu_mute_audio)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_mute),
                    contentDescription = stringResource(id = R.string.game_menu_mute_audio),
                )
            },
            state = rememberMemoryBooleanSettingState(!gameMenuRequest.audioEnabled),
            onCheckedChange = {
                onResult { putExtra(GameMenuContract.RESULT_ENABLE_AUDIO, !it) }
            },
        )

        if (gameMenuRequest.fastForwardSupported) {
            var isFastForward by remember { mutableStateOf(gameMenuRequest.fastForwardEnabled) }

            ListItem(
                headlineContent = {
                    Text(text = stringResource(id = R.string.game_menu_fast_forward))
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu_fast_forward),
                        contentDescription = stringResource(id = R.string.game_menu_fast_forward),
                    )
                },
                trailingContent = {
                    Switch(
                        checked = isFastForward,
                        onCheckedChange = null,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        role = Role.Switch,
                        onClick = {
                            val next = !isFastForward
                            isFastForward = next
                            onResult { putExtra(GameMenuContract.RESULT_ENABLE_FAST_FORWARD, next) }
                        },
                        onLongClick = {
                            showSpeedDialog = true
                        },
                    ),
            )
        }

        if (gameMenuRequest.numDisks > 1) {
            LemuroidSettingsList(
                title = { Text(text = stringResource(id = R.string.game_menu_change_disk_button)) },
                items = (1..gameMenuRequest.numDisks).map { stringResource(R.string.game_menu_change_disk_disk, it) },
                useSelectedValueAsSubtitle = false,
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_disk),
                        contentDescription = stringResource(id = R.string.game_menu_change_disk_button),
                    )
                },
                state = rememberMemoryIntSettingState(gameMenuRequest.currentDisk),
                onItemSelected = { index, _ ->
                    onResult { putExtra(GameMenuContract.RESULT_CHANGE_DISK, index) }
                },
            )
        }

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_edit_touch_controls)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_controls),
                    contentDescription = stringResource(id = R.string.game_menu_edit_touch_controls),
                )
            },
            onClick = {
                onResult { putExtra(GameMenuContract.RESULT_EDIT_TOUCH_CONTROLS, true) }
            },
        )

        if (CheatSupport.isSystemSupported(gameMenuRequest.game.systemId)) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.game_menu_cheats)) },
                subtitle = if (isHardcore) { { Text(text = stringResource(id = R.string.game_menu_disabled_in_hardcore)) } } else null,
                enabled = !isHardcore,
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_cheat),
                        contentDescription = stringResource(id = R.string.game_menu_cheats),
                    )
                },
                onClick = { if (!isHardcore) navController.navigateToRoute(GameMenuRoute.CHEATS) },
            )
        }

        if (availablePatches.isNotEmpty()) {
            val activeCount = enabledPatchIds.count { id -> availablePatches.any { it.id == id && it.isValid } }
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.game_menu_patches)) },
                subtitle = {
                    Text(
                        text = stringResource(
                            R.string.game_menu_patches_subtitle_format,
                            activeCount,
                            availablePatches.size,
                        ),
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Extension,
                        contentDescription = stringResource(id = R.string.game_menu_patches),
                    )
                },
                onClick = {
                    showPatchesDialog = true
                },
            )
        }

        if (gameMenuRequest.advancedCoreOptions.isNotEmpty() || gameMenuRequest.coreOptions.isNotEmpty()) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.game_menu_settings)) },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_settings),
                        contentDescription = stringResource(id = R.string.game_menu_settings),
                    )
                },
                onClick = { navController.navigateToRoute(GameMenuRoute.OPTIONS) },
            )
        }

        if (gameMenuRequest.allTiltConfigurations.isNotEmpty()) {
            val tiltConfigurationEntries =
                gameMenuRequest.allTiltConfigurations
                    .map { TiltConfigurationMenuEntry.fromTiltConfiguration(it) }

            val selectedIndex =
                gameMenuRequest.allTiltConfigurations
                    .indexOf(gameMenuRequest.currentTiltConfiguration)

            LemuroidSettingsList(
                title = { Text(text = stringResource(id = R.string.game_menu_tilt_sensor)) },
                items = tiltConfigurationEntries.map { stringResource(it.descriptionId) },
                useSelectedValueAsSubtitle = false,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = stringResource(id = R.string.game_menu_tilt_sensor),
                    )
                },
                state = rememberMemoryIntSettingState(selectedIndex),
                onItemSelected = { index, _ ->
                    onResult {
                        putExtra(
                            GameMenuContract.RESULT_CHANGE_TILT_CONFIG,
                            tiltConfigurationEntries[index].configuration,
                        )
                    }
                },
            )
        }

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_quit)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_quit),
                    contentDescription = stringResource(id = R.string.game_menu_quit),
                )
            },
            onClick = {
                onResult { putExtra(GameMenuContract.RESULT_QUIT, true) }
            },
        )
    }

    if (showSpeedDialog) {
        val speedOptions = listOf(
            2 to stringResource(R.string.fast_forward_speed_2x),
            3 to stringResource(R.string.fast_forward_speed_3x),
            4 to stringResource(R.string.fast_forward_speed_4x),
            5 to stringResource(R.string.fast_forward_speed_5x),
        )

        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text(text = stringResource(R.string.settings_title_fast_forward_speed)) },
            text = {
                Column {
                    speedOptions.forEach { (speedValue, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentSpeed == speedValue,
                                    onClick = {
                                        currentSpeed = speedValue
                                        val prefs = SharedPreferencesHelper.getSharedPreferences(context)
                                        prefs.edit()
                                            .putInt(context.getString(R.string.pref_key_fast_forward_speed), speedValue)
                                            .apply()
                                        onResult {
                                            putExtra(GameMenuContract.RESULT_CHANGE_FAST_FORWARD_SPEED, speedValue)
                                        }
                                        showSpeedDialog = false
                                    },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                        ) {
                            RadioButton(
                                selected = currentSpeed == speedValue,
                                onClick = null,
                            )
                            Text(
                                text = label,
                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }

    if (showPatchesDialog) {
        AlertDialog(
            onDismissRequest = { showPatchesDialog = false },
            title = { Text(text = stringResource(id = R.string.game_menu_patches_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (patchesChanged) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.game_menu_patches_restart_prompt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }

                    availablePatches.forEach { patch ->
                        val isEnabled = patch.id in enabledPatchIds && patch.isValid
                        val sourceLabel = if (patch.source == PatchSource.IN_ZIP) {
                            stringResource(R.string.game_menu_patches_source_zip)
                        } else {
                            stringResource(R.string.game_menu_patches_source_folder)
                        }
                        val formattedSize = Formatter.formatFileSize(context, patch.sizeBytes)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = patch.isValid) {
                                    val updated = if (patch.id in enabledPatchIds) {
                                        enabledPatchIds - patch.id
                                    } else {
                                        enabledPatchIds + patch.id
                                    }
                                    enabledPatchIds = updated
                                    PatchManager.setEnabledPatchIds(context, gameMenuRequest.game.id, updated)
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = patch.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "$sourceLabel • $formattedSize",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (!patch.isValid) {
                                    Text(
                                        text = patch.validationError ?: stringResource(R.string.game_menu_patches_invalid_patch),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }

                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    val updated = if (checked) {
                                        enabledPatchIds + patch.id
                                    } else {
                                        enabledPatchIds - patch.id
                                    }
                                    enabledPatchIds = updated
                                    PatchManager.setEnabledPatchIds(context, gameMenuRequest.game.id, updated)
                                },
                                enabled = patch.isValid,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (patchesChanged) {
                    Button(
                        onClick = {
                            showPatchesDialog = false
                            onResult { putExtra(GameMenuContract.RESULT_RELOAD_GAME, true) }
                        },
                    ) {
                        Text(text = stringResource(R.string.game_menu_patches_restart_action))
                    }
                } else {
                    TextButton(onClick = { showPatchesDialog = false }) {
                        Text(text = stringResource(R.string.close))
                    }
                }
            },
            dismissButton = if (patchesChanged) {
                {
                    TextButton(onClick = { showPatchesDialog = false }) {
                        Text(text = stringResource(R.string.close))
                    }
                }
            } else null,
        )
    }
}
