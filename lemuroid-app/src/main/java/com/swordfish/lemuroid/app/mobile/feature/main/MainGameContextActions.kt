package com.swordfish.lemuroid.app.mobile.feature.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameTexts
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidSmallGameImage
import com.swordfish.lemuroid.lib.cheats.CheatSupport
import com.swordfish.lemuroid.lib.library.db.entity.Game
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameContextActions(
    selectedGameState: MutableState<Game?>,
    shortcutSupported: Boolean,
    onGamePlay: (Game) -> Unit,
    onGameRestart: (Game) -> Unit,
    onGameInfo: (Game) -> Unit,
    onManageCheats: (Game) -> Unit,
    onFavoriteToggle: (Game, Boolean) -> Unit,
    onAddToCollection: (Game) -> Unit,
    onCreateShortcut: (Game) -> Unit,
    onSetCustomName: (Game, String) -> Unit,
    onClearCustomName: (Game) -> Unit,
    onSetCustomCoverUri: (Game, String) -> Unit,
    onClearCustomCoverUri: (Game) -> Unit,
    onDeleteGame: (Game) -> Unit,
) {
    val modalSheetState = rememberModalBottomSheetState(true)
    val selectedGame = selectedGameState.value

    LaunchedEffect(selectedGame) {
        if (selectedGame != null) {
            modalSheetState.show()
        } else {
            modalSheetState.hide()
        }
    }

    if (selectedGame != null) {
        ModalBottomSheet(
            sheetState = modalSheetState,
            onDismissRequest = { selectedGameState.value = null },
        ) {
            ContextActionContent(
                selectedGame = selectedGame,
                onGamePlay = onGamePlay,
                selectedGameState = selectedGameState,
                onGameRestart = onGameRestart,
                onGameInfo = onGameInfo,
                onManageCheats = onManageCheats,
                onFavoriteToggle = onFavoriteToggle,
                onAddToCollection = onAddToCollection,
                shortcutSupported = shortcutSupported,
                onCreateShortcut = onCreateShortcut,
                onSetCustomName = onSetCustomName,
                onClearCustomName = onClearCustomName,
                onSetCustomCoverUri = onSetCustomCoverUri,
                onClearCustomCoverUri = onClearCustomCoverUri,
                onDeleteGame = onDeleteGame,
            )
        }
    }
}

@Composable
private fun ContextActionContent(
    selectedGame: Game,
    onGamePlay: (Game) -> Unit,
    selectedGameState: MutableState<Game?>,
    onGameRestart: (Game) -> Unit,
    onGameInfo: (Game) -> Unit,
    onManageCheats: (Game) -> Unit,
    onFavoriteToggle: (Game, Boolean) -> Unit,
    onAddToCollection: (Game) -> Unit,
    shortcutSupported: Boolean,
    onCreateShortcut: (Game) -> Unit,
    onSetCustomName: (Game, String) -> Unit,
    onClearCustomName: (Game) -> Unit,
    onSetCustomCoverUri: (Game, String) -> Unit,
    onClearCustomCoverUri: (Game) -> Unit,
    onDeleteGame: (Game) -> Unit,
) {
    val context = LocalContext.current
    var showCustomizeDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                } catch (e: SecurityException) {
                    Timber.e(e, "Failed to persist read URI permission for custom cover")
                }
                onSetCustomCoverUri(selectedGame, uri.toString())
                showCustomizeDialog = false
                selectedGameState.value = null
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom)),
    ) {
        ContextActionHeader(game = selectedGame)
        HorizontalDivider()
        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_resume),
            icon = Icons.Default.PlayArrow,
            onClick = {
                onGamePlay(selectedGame)
                selectedGameState.value = null
            },
        )
        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_restart),
            icon = Icons.Default.RestartAlt,
            onClick = {
                onGameRestart(selectedGame)
                selectedGameState.value = null
            },
        )
        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_game_info),
            icon = Icons.Default.Info,
            onClick = {
                onGameInfo(selectedGame)
                selectedGameState.value = null
            },
        )

        if (CheatSupport.isSystemSupported(selectedGame.systemId)) {
            ContextActionEntry(
                label = stringResource(id = R.string.game_menu_cheats),
                painter = painterResource(id = R.drawable.ic_menu_cheat),
                onClick = {
                    onManageCheats(selectedGame)
                    selectedGameState.value = null
                },
            )
        }

        if (selectedGame.isFavorite) {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_remove_from_favorites),
                icon = Icons.Default.FavoriteBorder,
                onClick = {
                    onFavoriteToggle(selectedGame, false)
                    selectedGameState.value = null
                },
            )
        } else {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_add_to_favorites),
                icon = Icons.Default.Favorite,
                onClick = {
                    onFavoriteToggle(selectedGame, true)
                    selectedGameState.value = null
                },
            )
        }

        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_add_to_collection),
            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
            onClick = {
                onAddToCollection(selectedGame)
                selectedGameState.value = null
            },
        )

        if (shortcutSupported) {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_create_shortcut),
                icon = Icons.Default.AppShortcut,
                onClick = {
                    onCreateShortcut(selectedGame)
                    selectedGameState.value = null
                },
            )
        }

        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_customize),
            icon = Icons.Default.Edit,
            onClick = {
                showCustomizeDialog = true
            },
        )

        HorizontalDivider()

        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_delete_rom),
            icon = Icons.Default.Delete,
            contentColor = MaterialTheme.colorScheme.error,
            onClick = {
                showDeleteConfirmDialog = true
            },
        )
    }

    if (showCustomizeDialog) {
        var nameInput by remember(selectedGame.id, selectedGame.customName) {
            mutableStateOf(selectedGame.customName ?: selectedGame.title)
        }
        AlertDialog(
            onDismissRequest = { showCustomizeDialog = false },
            title = { Text(text = stringResource(id = R.string.dialog_customize_game_title)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(id = R.string.dialog_customize_name_label),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            if (selectedGame.customName != null) {
                                TextButton(
                                    onClick = {
                                        onClearCustomName(selectedGame)
                                        nameInput = selectedGame.title
                                    },
                                ) {
                                    Text(text = stringResource(id = R.string.dialog_customize_reset))
                                }
                            }
                        }
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    HorizontalDivider()

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(id = R.string.dialog_customize_artwork_label),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            if (selectedGame.customCoverUri != null) {
                                TextButton(
                                    onClick = {
                                        onClearCustomCoverUri(selectedGame)
                                        showCustomizeDialog = false
                                        selectedGameState.value = null
                                    },
                                ) {
                                    Text(text = stringResource(id = R.string.dialog_customize_reset))
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LemuroidSmallGameImage(
                                modifier = Modifier.size(48.dp),
                                game = selectedGame,
                            )
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(id = R.string.dialog_customize_choose_artwork))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = nameInput.trim().isNotEmpty(),
                    onClick = {
                        val trimmed = nameInput.trim()
                        if (trimmed == selectedGame.title) {
                            if (selectedGame.customName != null) {
                                onClearCustomName(selectedGame)
                            }
                        } else if (trimmed.isNotEmpty() && trimmed != selectedGame.customName) {
                            onSetCustomName(selectedGame, trimmed)
                        }
                        showCustomizeDialog = false
                        selectedGameState.value = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.game_menu_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomizeDialog = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
        )
    }

    if (showDeleteConfirmDialog) {
        val actualFileName = remember(selectedGame.fileUri, selectedGame.fileName) {
            resolveActualFileName(context, selectedGame.fileUri, selectedGame.fileName)
        }
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = stringResource(id = R.string.dialog_delete_rom_title)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.dialog_delete_rom_message,
                        selectedGame.displayName(),
                        actualFileName,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteGame(selectedGame)
                        selectedGameState.value = null
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.dialog_delete_rom_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun ContextActionHeader(game: Game) {
    Row(
        modifier =
            Modifier.padding(
                start = 16.dp,
                top = 8.dp,
                bottom = 8.dp,
                end = 16.dp,
            ),
    ) {
        LemuroidSmallGameImage(
            modifier =
                Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .align(Alignment.CenterVertically),
            game = game,
        )
        LemuroidGameTexts(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            game = game,
        )
    }
}

@Composable
private fun ContextActionEntry(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    contentColor: Color = LocalContentColor.current,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(start = 16.dp),
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = label,
            color = contentColor,
        )
    }
}

@Composable
private fun ContextActionEntry(
    modifier: Modifier = Modifier,
    label: String,
    painter: Painter,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(start = 16.dp),
            painter = painter,
            contentDescription = label,
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = label,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FakeScrim(modalSheetState: SheetState) {
    AnimatedVisibility(
        visible = modalSheetState.targetValue != SheetValue.Hidden,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(BottomSheetDefaults.ScrimColor),
        )
    }
}

private fun resolveActualFileName(
    context: android.content.Context,
    fileUri: String,
    fallbackFileName: String,
): String {
    val uri = runCatching { Uri.parse(fileUri) }.getOrNull() ?: return fallbackFileName
    val resolved = when (uri.scheme) {
        "content" -> runCatching {
            DocumentFile.fromSingleUri(context, uri)?.name
        }.getOrNull()
        "file", null -> uri.path?.let { File(it).name }
        else -> null
    }
    return resolved?.takeIf { it.isNotBlank() } ?: fallbackFileName
}
