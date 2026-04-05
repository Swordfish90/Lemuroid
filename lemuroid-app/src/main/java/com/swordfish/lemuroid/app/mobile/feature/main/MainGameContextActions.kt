package com.swordfish.lemuroid.app.mobile.feature.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameTexts
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidSmallGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameContextActions(
    selectedGameState: MutableState<Game?>,
    shortcutSupported: Boolean,
    onGamePlay: (Game) -> Unit,
    onGameRestart: (Game) -> Unit,
    onFavoriteToggle: (Game, Boolean) -> Unit,
    onCreateShortcut: (Game) -> Unit,
    onSetCustomName: (Game, String) -> Unit,
    onClearCustomName: (Game) -> Unit,
    onSetCustomCoverUri: (Game, String) -> Unit,
    onClearCustomCoverUri: (Game) -> Unit,
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
                onFavoriteToggle = onFavoriteToggle,
                shortcutSupported = shortcutSupported,
                onCreateShortcut = onCreateShortcut,
                onSetCustomName = onSetCustomName,
                onClearCustomName = onClearCustomName,
                onSetCustomCoverUri = onSetCustomCoverUri,
                onClearCustomCoverUri = onClearCustomCoverUri,
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
    onFavoriteToggle: (Game, Boolean) -> Unit,
    shortcutSupported: Boolean,
    onCreateShortcut: (Game) -> Unit,
    onSetCustomName: (Game, String) -> Unit,
    onClearCustomName: (Game) -> Unit,
    onSetCustomCoverUri: (Game, String) -> Unit,
    onClearCustomCoverUri: (Game) -> Unit,
) {
    val context = LocalContext.current
    var showCustomNameDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {

            }
            onSetCustomCoverUri(selectedGame, uri.toString())
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
        Divider()
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

        if (selectedGame.customName != null) {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_remove_custom_name),
                icon = Icons.Default.Clear,
                onClick = {
                    onClearCustomName(selectedGame)
                    selectedGameState.value = null
                },
            )
        } else {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_add_custom_name),
                icon = Icons.Default.Edit,
                onClick = {
                    showCustomNameDialog = true
                },
            )
        }

        if (selectedGame.customCoverUri != null) {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_remove_custom_artwork),
                icon = Icons.Default.Clear,
                onClick = {
                    onClearCustomCoverUri(selectedGame)
                    selectedGameState.value = null
                },
            )
        } else {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_add_custom_artwork),
                icon = Icons.Default.Image,
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
            )
        }
    }

    if (showCustomNameDialog) {
        CustomNameDialog(
            initialName = selectedGame.fileName,
            onDismiss = { showCustomNameDialog = false },
            onConfirm = { newName ->
                showCustomNameDialog = false
                onSetCustomName(selectedGame, newName)
                selectedGameState.value = null
            },
        )
    }
}

@Composable
private fun CustomNameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.game_context_menu_custom_name_dialog_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text.trim())
                    }
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
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
