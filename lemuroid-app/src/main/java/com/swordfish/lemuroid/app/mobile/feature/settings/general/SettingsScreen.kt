package com.swordfish.lemuroid.app.mobile.feature.settings.general

import android.net.Uri
import android.provider.DocumentsContract
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import coil.imageLoader
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.mobile.feature.main.navigateToRoute
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsListMultiSelect
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsPage
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSlider
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import com.swordfish.lemuroid.app.utils.android.settings.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.indexPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.intIndexPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.intPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.stringsSetPreferenceState
import com.swordfish.lemuroid.app.utils.android.stringListResource
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.scraper.MediaStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@androidx.annotation.StringRes
private fun MediaStorageManager.MediaType.labelRes(): Int =
    when (this) {
        MediaStorageManager.MediaType.BOXART -> R.string.media_type_boxart
        MediaStorageManager.MediaType.BOXART_BACK -> R.string.media_type_boxart_back
        MediaStorageManager.MediaType.BANNERS -> R.string.media_type_banners
        MediaStorageManager.MediaType.FANART -> R.string.media_type_fanart
        MediaStorageManager.MediaType.LOGOS -> R.string.media_type_logos
        MediaStorageManager.MediaType.SCREENSHOTS -> R.string.media_type_screenshots
        MediaStorageManager.MediaType.TITLES -> R.string.settings_media_source_sstitle
        MediaStorageManager.MediaType.VIDEOS -> R.string.settings_media_source_video
    }

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    navController: NavController,
) {
    val state =
        viewModel.uiState
            .collectAsState(SettingsViewModel.State())
            .value

    val scanInProgress =
        viewModel.directoryScanInProgress
            .collectAsState(false)
            .value

    val indexingInProgress =
        viewModel.indexingInProgress
            .collectAsState(false)
            .value

    LemuroidSettingsPage(modifier = modifier) {
        RomsSettings(
            state = state,
            onAddFolder = { viewModel.changeLocalStorageFolder() },
            onRemoveFolder = { viewModel.removeFolder(it) },
            indexingInProgress = indexingInProgress,
            scanInProgress = scanInProgress,
        )
        GeneralSettings()
        ScraperSettings()
        RetroAchievementsSettings()
        InputSettings(navController = navController)
        MiscSettings(
            indexingInProgress = indexingInProgress,
            isSaveSyncSupported = state.isSaveSyncSupported,
            navController = navController,
        )
    }
}

@Composable
private fun MiscSettings(
    indexingInProgress: Boolean,
    isSaveSyncSupported: Boolean,
    navController: NavController,
) {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_misc)) },
    ) {
        if (isSaveSyncSupported) {
            LemuroidSettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.settings_title_save_sync)) },
                subtitle = {
                    Text(text = stringResource(id = R.string.settings_description_save_sync))
                },
                onClick = { navController.navigateToRoute(MainRoute.SETTINGS_SAVE_SYNC) },
            )
        }
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_open_cores_selection)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_open_cores_selection))
            },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_CORES_SELECTION) },
        )
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_display_bios_info)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_display_bios_info))
            },
            enabled = !indexingInProgress,
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_BIOS) },
        )
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_advanced_settings)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_advanced_settings))
            },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_ADVANCED) },
        )
    }
}

@Composable
private fun InputSettings(navController: NavController) {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_input)) },
    ) {
        LemuroidSettingsList(
            state =
                indexPreferenceState(
                    R.string.pref_key_haptic_feedback_mode,
                    "press",
                    stringListResource(R.array.pref_key_haptic_feedback_mode_values),
                ),
            title = {
                Text(text = stringResource(id = R.string.settings_title_enable_touch_feedback))
            },
            items = stringListResource(R.array.pref_key_haptic_feedback_mode_display_names),
        )
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_gamepad_settings)) },
            subtitle = {
                Text(text = stringResource(id = R.string.settings_description_gamepad_settings))
            },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_INPUT_DEVICES) },
        )
    }
}

@Composable
private fun ScraperSettings() {
    val context = LocalContext.current
    val prefs = remember { SharedPreferencesHelper.getSharedPreferences(context) }
    var username by remember {
        mutableStateOf(
            prefs.getString(context.getString(R.string.pref_key_screenscraper_user), "") ?: ""
        )
    }
    var password by remember {
        mutableStateOf(
            prefs.getString(context.getString(R.string.pref_key_screenscraper_password), "") ?: ""
        )
    }
    var showDialog by remember { mutableStateOf(false) }
    var showMediaStorageDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    val mediaStorageManager = remember { MediaStorageManager(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()
    var storageStats by remember { mutableStateOf(MediaStorageManager.MediaStorageStats()) }

    LaunchedEffect(Unit) {
        storageStats = withContext(Dispatchers.IO) {
            mediaStorageManager.calculateStorageStats()
        }
    }

    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_scraper)) },
    ) {
        LemuroidSettingsList(
            state = indexPreferenceState(
                R.string.pref_key_scraper_priority,
                "MIXED",
                listOf("SCREENSCRAPER", "IGDB", "MIXED"),
            ),
            title = { Text(text = stringResource(id = R.string.settings_title_scraper_priority)) },
            items = listOf(
                stringResource(id = R.string.settings_scraper_priority_screenscraper),
                stringResource(id = R.string.settings_scraper_priority_igdb),
                stringResource(id = R.string.settings_scraper_priority_mixed),
            ),
        )

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_screenscraper_account)) },
            subtitle = {
                Text(
                    text = if (username.isNotBlank()) {
                        "Logged in as: $username"
                    } else {
                        stringResource(id = R.string.settings_description_screenscraper_account)
                    },
                )
            },
            onClick = { showDialog = true },
        )

        LemuroidSettingsList(
            state = indexPreferenceState(
                R.string.pref_key_screenscraper_box_art_format,
                "2D",
                listOf("2D", "3D"),
            ),
            title = { Text(text = stringResource(id = R.string.settings_title_box_art_format)) },
            items = listOf(
                stringResource(id = R.string.settings_box_art_format_2d),
                stringResource(id = R.string.settings_box_art_format_3d),
            ),
        )

        val mediaValues = listOf("banner", "ss", "sstitle", "video", "fanart")
        val mediaTitles = listOf(
            stringResource(id = R.string.settings_media_source_banner),
            stringResource(id = R.string.settings_media_source_ss),
            stringResource(id = R.string.settings_media_source_sstitle),
            stringResource(id = R.string.settings_media_source_video),
            stringResource(id = R.string.settings_media_source_fanart),
        )
        val mediaState = stringsSetPreferenceState(
            R.string.pref_key_screenscraper_media_sources,
            emptySet(),
        )
        LemuroidSettingsListMultiSelect(
            state = mediaState,
            title = { Text(text = stringResource(id = R.string.settings_title_media_sources)) },
            subtitle = {
                val labels = mediaValues.mapIndexedNotNull { index, value ->
                    if (mediaState.value.contains(value)) mediaTitles[index] else null
                }
                Text(
                    text = if (labels.isNotEmpty()) labels.joinToString(", ") else stringResource(R.string.none),
                )
            },
            entryValues = mediaValues,
            entries = mediaTitles,
            confirmButton = stringResource(android.R.string.ok),
        )

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_media_storage)) },
            subtitle = {
                val formatted = if (storageStats.fileCount > 0) {
                    stringResource(
                        id = R.string.settings_media_storage_summary,
                        Formatter.formatShortFileSize(context, storageStats.totalBytes),
                        storageStats.fileCount,
                    )
                } else {
                    stringResource(id = R.string.settings_media_storage_empty)
                }
                Text(text = formatted)
            },
            onClick = {
                coroutineScope.launch {
                    storageStats = withContext(Dispatchers.IO) {
                        mediaStorageManager.calculateStorageStats()
                    }
                    showMediaStorageDialog = true
                }
            },
        )
    }

    if (showMediaStorageDialog) {
        val totalFormatted = Formatter.formatShortFileSize(context, storageStats.totalBytes)
        var expandedSystems by remember { mutableStateOf<Set<String>>(emptySet()) }

        AlertDialog(
            onDismissRequest = { showMediaStorageDialog = false },
            title = { Text(text = stringResource(R.string.settings_title_media_storage)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = if (storageStats.fileCount > 0) {
                            stringResource(
                                R.string.settings_media_storage_summary,
                                totalFormatted,
                                storageStats.fileCount,
                            )
                        } else {
                            stringResource(R.string.settings_media_storage_empty)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (storageStats.perSystemBytes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        storageStats.perSystemBytes.forEach { (systemId, bytes) ->
                            val systemLabel = runCatching {
                                stringResource(GameSystem.findById(systemId).titleResId)
                            }.getOrElse { systemId.uppercase() }
                            val typeBreakdown = storageStats.perSystemTypeBytes[systemId].orEmpty()
                            val isExpanded = expandedSystems.contains(systemId)

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(enabled = typeBreakdown.isNotEmpty()) {
                                            expandedSystems = if (isExpanded) {
                                                expandedSystems - systemId
                                            } else {
                                                expandedSystems + systemId
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                text = systemLabel,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                            if (typeBreakdown.isNotEmpty()) {
                                                Icon(
                                                    imageVector = if (isExpanded) {
                                                        Icons.Filled.ExpandLess
                                                    } else {
                                                        Icons.Filled.ExpandMore
                                                    },
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }
                                        Text(
                                            text = Formatter.formatShortFileSize(context, bytes),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    mediaStorageManager.clearSystemMedia(systemId)
                                                    context.imageLoader.memoryCache?.clear()
                                                    storageStats = mediaStorageManager.calculateStorageStats()
                                                }
                                                expandedSystems = expandedSystems - systemId
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.toast_media_cleared),
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = stringResource(R.string.settings_media_storage_clear_all),
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = isExpanded && typeBreakdown.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 14.dp, bottom = 6.dp),
                                    ) {
                                        typeBreakdown.forEach { (type, typeBytes) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = stringResource(type.labelRes()),
                                                        style = MaterialTheme.typography.bodySmall,
                                                    )
                                                    Text(
                                                        text = Formatter.formatShortFileSize(context, typeBytes),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            withContext(Dispatchers.IO) {
                                                                mediaStorageManager.clearMediaType(systemId, type)
                                                                context.imageLoader.memoryCache?.clear()
                                                                storageStats = mediaStorageManager.calculateStorageStats()
                                                            }
                                                            Toast.makeText(
                                                                context,
                                                                context.getString(R.string.toast_media_cleared),
                                                                Toast.LENGTH_SHORT,
                                                            ).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(36.dp),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Delete,
                                                        contentDescription = stringResource(type.labelRes()),
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMediaStorageDialog = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                if (storageStats.fileCount > 0) {
                    TextButton(
                        onClick = {
                            showMediaStorageDialog = false
                            showClearAllConfirmDialog = true
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.settings_media_storage_clear_all),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
        )
    }

    if (showClearAllConfirmDialog) {
        val totalFormatted = Formatter.formatShortFileSize(context, storageStats.totalBytes)
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = { Text(text = stringResource(R.string.settings_media_storage_clear_confirm_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.settings_media_storage_clear_confirm_message,
                        totalFormatted,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllConfirmDialog = false
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                mediaStorageManager.clearAllMedia()
                                context.imageLoader.memoryCache?.clear()
                                context.imageLoader.diskCache?.clear()
                                storageStats = mediaStorageManager.calculateStorageStats()
                            }
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_media_cleared),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                ) {
                    Text(
                        text = stringResource(R.string.settings_media_storage_clear_all),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }

    if (showDialog) {
        var tempUser by remember { mutableStateOf(username) }
        var tempPass by remember { mutableStateOf(password) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(R.string.settings_title_screenscraper_account)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.settings_screenscraper_dialog_description),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = tempUser,
                        onValueChange = { tempUser = it },
                        label = { Text(stringResource(R.string.settings_screenscraper_username)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = tempPass,
                        onValueChange = { tempPass = it },
                        label = { Text(stringResource(R.string.settings_screenscraper_password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefs.edit()
                            .putString(context.getString(R.string.pref_key_screenscraper_user), tempUser.trim())
                            .putString(context.getString(R.string.pref_key_screenscraper_password), tempPass.trim())
                            .apply()
                        username = tempUser.trim()
                        password = tempPass.trim()
                        showDialog = false
                    },
                ) {
                    Text(stringResource(R.string.game_menu_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun GeneralSettings() {
    val hdMode = booleanPreferenceState(R.string.pref_key_hd_mode, false)
    val immersiveMode = booleanPreferenceState(R.string.pref_key_enable_immersive_mode, false)

    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.settings_category_general)) },
    ) {
        LemuroidSettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_autosave, true),
            title = { Text(text = stringResource(id = R.string.settings_title_enable_autosave)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_enable_autosave)) },
        )
        LemuroidSettingsSwitch(
            state = booleanPreferenceState(R.string.pref_key_direct_play_on_tap, false),
            title = { Text(text = stringResource(id = R.string.settings_title_direct_play_on_tap)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_direct_play_on_tap)) },
        )
        LemuroidSettingsSwitch(
            state = immersiveMode,
            title = { Text(text = stringResource(id = R.string.settings_title_immersive_mode)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_immersive_mode)) },
        )
        LemuroidSettingsSwitch(
            state = hdMode,
            title = { Text(text = stringResource(id = R.string.settings_title_hd_mode)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_hd_mode)) },
        )
        LemuroidSettingsSlider(
            enabled = hdMode.value,
            state =
                intPreferenceState(
                    key = stringResource(id = R.string.pref_key_hd_mode_quality),
                    default = 2,
                ),
            steps = 1,
            valueRange = 0f..2f,
            title = { Text(text = stringResource(R.string.settings_title_hd_quality)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_hd_quality)) },
        )
        LemuroidSettingsList(
            enabled = !hdMode.value,
            state =
                indexPreferenceState(
                    R.string.pref_key_shader_filter,
                    "auto",
                    stringListResource(R.array.pref_key_shader_filter_values).toList(),
                ),
            title = { Text(text = stringResource(id = R.string.display_filter)) },
            items = stringListResource(R.array.pref_key_shader_filter_display_names),
        )
        LemuroidSettingsList(
            state =
                indexPreferenceState(
                    R.string.pref_key_viewport_alignment,
                    "center",
                    stringListResource(R.array.pref_key_viewport_alignment_values).toList(),
                ),
            title = { Text(text = stringResource(id = R.string.settings_title_viewport_alignment)) },
            items = stringListResource(R.array.pref_key_viewport_alignment_display_names),
        )
        LemuroidSettingsList(
            state =
                intIndexPreferenceState(
                    R.string.pref_key_fast_forward_speed,
                    2,
                    listOf(2, 3, 4, 5),
                ),
            title = { Text(text = stringResource(id = R.string.settings_title_fast_forward_speed)) },
            items = listOf(
                stringResource(R.string.fast_forward_speed_2x),
                stringResource(R.string.fast_forward_speed_3x),
                stringResource(R.string.fast_forward_speed_4x),
                stringResource(R.string.fast_forward_speed_5x),
            ),
        )
    }
}

private fun formatFolderUri(folderUri: String): String {
    return runCatching {
        val uri = Uri.parse(folderUri)
        val docId = DocumentsContract.getTreeDocumentId(uri)
        docId.replace(":", "/").trimEnd('/')
    }.getOrElse {
        val decoded = Uri.decode(folderUri)
        val treeIdx = decoded.indexOf("/tree/")
        if (treeIdx != -1) {
            decoded.substring(treeIdx + "/tree/".length).replace(":", "/").trimEnd('/')
        } else {
            folderUri
        }
    }
}

@Composable
private fun RomsSettings(
    state: SettingsViewModel.State,
    onAddFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    indexingInProgress: Boolean,
    scanInProgress: Boolean,
) {
    val context = LocalContext.current
    val folders = state.currentDirectories.toList()
    var folderToRemove by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    LemuroidCardSettingsGroup(title = { Text(text = stringResource(id = R.string.roms)) }) {
        if (folders.isEmpty()) {
            LemuroidSettingsMenuLink(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                    )
                },
                title = { Text(text = stringResource(id = R.string.directory)) },
                subtitle = { Text(text = stringResource(id = R.string.none)) },
                onClick = onAddFolder,
                enabled = !indexingInProgress,
            )
        } else {
            val folderSummary = remember(folders) {
                if (folders.size == 1) {
                    formatFolderUri(folders.first())
                } else {
                    folders.joinToString(", ") { formatFolderUri(it).substringAfterLast('/') }
                }
            }

            LemuroidSettingsMenuLink(
                icon = {
                    Icon(
                        imageVector = if (isExpanded) Icons.Outlined.FolderOpen else Icons.Outlined.Folder,
                        contentDescription = null,
                    )
                },
                title = { Text(text = stringResource(id = R.string.rom_folders_count, folders.size)) },
                subtitle = { Text(text = folderSummary) },
                action = {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = stringResource(if (isExpanded) R.string.action_collapse else R.string.action_expand),
                    )
                },
                onClick = { isExpanded = !isExpanded },
                enabled = !indexingInProgress,
            )

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    folders.forEach { folderUri ->
                        val formattedPath = remember(folderUri) { formatFolderUri(folderUri) }
                        val folderName = remember(folderUri, formattedPath) {
                            runCatching {
                                DocumentFile.fromTreeUri(context, Uri.parse(folderUri))?.name
                            }.getOrNull()?.takeIf { it.isNotBlank() } ?: formattedPath.substringAfterLast('/')
                        }

                        LemuroidSettingsMenuLink(
                            modifier = Modifier.padding(start = 12.dp),
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            title = { Text(text = folderName) },
                            subtitle = { Text(text = formattedPath) },
                            action = {
                                IconButton(
                                    onClick = { folderToRemove = folderUri to folderName },
                                    enabled = !indexingInProgress,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = stringResource(id = R.string.remove_rom_folder_confirm),
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            onClick = {},
                            enabled = !indexingInProgress,
                        )
                    }

                    LemuroidSettingsMenuLink(
                        modifier = Modifier.padding(start = 12.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        title = { Text(text = stringResource(id = R.string.add_rom_folder)) },
                        onClick = onAddFolder,
                        enabled = !indexingInProgress,
                    )
                }
            }
        }

        if (scanInProgress) {
            LemuroidSettingsMenuLink(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                    )
                },
                title = { Text(text = stringResource(id = R.string.stop)) },
                onClick = { LibraryIndexScheduler.cancelLibrarySync(context) },
            )
        } else {
            LemuroidSettingsMenuLink(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                    )
                },
                title = { Text(text = stringResource(id = R.string.rescan)) },
                onClick = { LibraryIndexScheduler.scheduleLibrarySync(context) },
                enabled = !indexingInProgress,
            )
        }
    }

    folderToRemove?.let { (uri, name) ->
        AlertDialog(
            onDismissRequest = { folderToRemove = null },
            title = { Text(text = stringResource(id = R.string.remove_rom_folder_title)) },
            text = {
                Text(text = stringResource(id = R.string.remove_rom_folder_message, name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveFolder(uri)
                        folderToRemove = null
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.remove_rom_folder_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToRemove = null }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        )
    }
}
