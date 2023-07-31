@file:OptIn(ExperimentalMaterial3Api::class)

package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.GameCard
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun HomeScreen(
    state: HomeViewModel.UIState,
    onGameClicked: (Game) -> Unit,
    onEnableNotificationsClicked: () -> Unit,
    onSetDirectoryClicked: () -> Unit,
) {
    AppTheme {
        Surface {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.showNoPermissionNotification) {
                    HomeNotification(
                        titleId = R.string.home_notification_title,
                        messageId = R.string.home_notification_message,
                        actionId = R.string.home_notification_action,
                        onAction = onEnableNotificationsClicked
                    )
                }
                if (state.showNoGamesNotification) {
                    HomeNotification(
                        titleId = R.string.home_empty_title,
                        messageId = R.string.home_empty_message,
                        actionId = R.string.home_empty_action,
                        onAction = onSetDirectoryClicked
                    )
                }
                HomeRow(
                    stringResource(id = R.string.recent),
                    state.recentGames,
                    onGameClicked
                )
                HomeRow(
                    stringResource(id = R.string.favorites),
                    state.favoritesGames,
                    onGameClicked
                )
                HomeRow(
                    stringResource(id = R.string.discover),
                    state.discoveryGames,
                    onGameClicked
                )
            }
        }
    }
}

@Composable
private fun HomeRow(title: String, games: List<Game>, onGameClicked: (Game) -> Unit) {
    if (games.isEmpty()) {
        return
    }
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        games.forEach {
            GameCard(
                modifier = Modifier.widthIn(0.dp, 144.dp),
                game = it,
                onClick = { onGameClicked(it) }
            )
        }
    }
}

@Composable
private fun HomeNotification(titleId: Int, messageId: Int, actionId: Int, onAction: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(titleId),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(messageId),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onAction
            ) {
                Text(stringResource(id = actionId))
            }
        }
    }
}
