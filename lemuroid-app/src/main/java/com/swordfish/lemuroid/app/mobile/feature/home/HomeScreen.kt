@file:OptIn(ExperimentalMaterial3Api::class)

package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameCard
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.utils.android.ComposableLifecycle
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    gameInteractor: GameInteractor
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.updateNotificationPermission(applicationContext)
            }
            else -> { }
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            context.displayDetailsSettingsScreen()
        }
    }

    val state = viewModel.getViewStates().collectAsState(HomeViewModel.UIState())
    HomeScreen(
        state.value,
        { gameInteractor.onGamePlay(it) },
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return@HomeScreen
            }

            permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        { viewModel.changeLocalStorageFolder(context) }
    ) // TODO COMPOSE We need to understand what's going to happen here.
}

@Composable
private fun HomeScreen(
    state: HomeViewModel.UIState,
    onGameClicked: (Game) -> Unit,
    onEnableNotificationsClicked: () -> Unit,
    onSetDirectoryClicked: () -> Unit,
) {
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
            LemuroidGameCard(
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
