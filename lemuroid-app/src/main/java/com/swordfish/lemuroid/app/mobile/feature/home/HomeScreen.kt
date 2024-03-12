@file:OptIn(ExperimentalMaterial3Api::class)

package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
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
import com.swordfish.lemuroid.app.utils.android.ComposableLifecycle
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun HomeScreen(
    padding: MergedPaddingValues,
    viewModel: HomeViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit
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
        padding,
        state.value,
        onGameClick,
        onGameLongClick,
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
    paddings: MergedPaddingValues,
    state: HomeViewModel.UIState,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onEnableNotificationsClicked: () -> Unit,
    onSetDirectoryClicked: () -> Unit,
) {
    val finalPadding = paddings + PaddingValues(vertical = 16.dp)

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(finalPadding.asPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(state.showNoPermissionNotification) {
            HomeNotification(
                titleId = R.string.home_notification_title,
                messageId = R.string.home_notification_message,
                actionId = R.string.home_notification_action,
                onAction = onEnableNotificationsClicked
            )
        }
        AnimatedVisibility(state.showNoGamesNotification) {
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
            onGameClicked,
            onGameLongClick
        )
        HomeRow(
            stringResource(id = R.string.favorites),
            state.favoritesGames,
            onGameClicked,
            onGameLongClick
        )
        HomeRow(
            stringResource(id = R.string.discover),
            state.discoveryGames,
            onGameClicked,
            onGameLongClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeRow(
    title: String,
    games: List<Game>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit
) {
    if (games.isEmpty()) {
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(games.size, key = { games[it].id }) { index ->
                val game = games[index]
                LemuroidGameCard(
                    modifier = Modifier
                        .widthIn(0.dp, 144.dp)
                        .animateItemPlacement(),
                    game = game,
                    onClick = { onGameClicked(game) },
                    onLongClick = { onGameLongClick(game) }
                )
            }
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
