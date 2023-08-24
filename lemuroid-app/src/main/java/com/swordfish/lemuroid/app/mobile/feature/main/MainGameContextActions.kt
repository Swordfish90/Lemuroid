package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameTexts
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidSmallGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun MainGameContextActions(
    selectedGameState: MutableState<Game?>,
    shortcutSupported: Boolean,
    onGamePlay: (Game) -> Unit,
    onGameRestart: (Game) -> Unit,
    onFavoriteToggle: (Game, Boolean) -> Unit,
    onCreateShortcut: (Game) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val selectedGame = selectedGameState.value

    LaunchedEffect(selectedGame) {
        if (selectedGame != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    if (selectedGame != null) {
        Dialog(onDismissRequest = { selectedGameState.value = null }) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = AlertDialogDefaults.shape,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                ContextActionContent(
                    selectedGame,
                    onGamePlay,
                    selectedGameState,
                    onGameRestart,
                    onFavoriteToggle,
                    shortcutSupported,
                    onCreateShortcut
                )
            }
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
    onCreateShortcut: (Game) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ContextActionHeader(game = selectedGame)
        Divider()
        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_resume),
            icon = Icons.Default.PlayArrow,
            onClick = {
                onGamePlay(selectedGame)
                selectedGameState.value = null
            }
        )
        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_restart),
            icon = Icons.Default.RestartAlt,
            onClick = {
                onGameRestart(selectedGame)
                selectedGameState.value = null
            }
        )

        if (selectedGame.isFavorite) {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_remove_from_favorites),
                icon = Icons.Default.FavoriteBorder,
                onClick = {
                    onFavoriteToggle(selectedGame, false)
                    selectedGameState.value = null
                }
            )
        } else {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_add_to_favorites),
                icon = Icons.Default.Favorite,
                onClick = {
                    onFavoriteToggle(selectedGame, true)
                    selectedGameState.value = null
                }
            )
        }

        if (shortcutSupported) {
            ContextActionEntry(
                label = stringResource(id = R.string.game_context_menu_create_shortcut),
                icon = Icons.Default.AppShortcut,
                onClick = {
                    onCreateShortcut(selectedGame)
                    selectedGameState.value = null
                }
            )
        }
    }
}

@Composable
private fun ContextActionHeader(game: Game) {
    Row(
        modifier = Modifier.padding(
            start = 16.dp,
            top = 8.dp,
            bottom = 8.dp,
            end = 16.dp
        )
    ) {
        LemuroidSmallGameImage(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .align(Alignment.CenterVertically),
            game = game
        )
        LemuroidGameTexts(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            game = game
        )
    }
}

@Composable
private fun ContextActionEntry(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(start = 16.dp),
            imageVector = icon,
            contentDescription = label
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = label
        )
    }
}
