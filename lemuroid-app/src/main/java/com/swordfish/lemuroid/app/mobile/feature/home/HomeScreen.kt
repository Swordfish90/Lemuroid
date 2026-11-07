package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.ElevatedCard
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.layout.LazyWeightedVerticalGrid
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameCard
import com.swordfish.lemuroid.app.utils.android.ComposableLifecycle
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.CoverArtPolicy
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onOpenCoreSelection: () -> Unit,
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.updatePermissions(applicationContext)
            }
            else -> { }
        }
    }

    val permissionsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (!isGranted) {
                context.displayDetailsSettingsScreen()
            }
        }

    val state = viewModel.getViewStates().collectAsState(HomeViewModel.UIState())
    HomeScreen(
        modifier,
        state.value,
        viewModel.games.collectAsLazyPagingItems(),
        onGameClick,
        onGameLongClick,
        onOpenCoreSelection,
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return@HomeScreen
            }

            permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        { permissionsLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        { viewModel.changeLocalStorageFolder(context) },
    ) // TODO COMPOSE We need to understand what's going to happen here.
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeViewModel.UIState,
    games: LazyPagingItems<Game>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onOpenCoreSelection: () -> Unit,
    onEnableNotificationsClicked: () -> Unit,
    onEnableMicrophoneClicked: () -> Unit,
    onSetDirectoryClicked: () -> Unit,
) {
    LazyWeightedVerticalGrid(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        spacing = 16.dp,
    ) {
        if (state.showNoNotificationPermissionCard) {
            item(
                key = "notice-notification-permission",
                contentType = "notice",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                HomeNotification(
                    titleId = R.string.home_notification_title,
                    messageId = R.string.home_notification_message,
                    actionId = R.string.home_notification_action,
                    onAction = onEnableNotificationsClicked,
                )
            }
        }
        if (state.showNoGamesCard) {
            item(
                key = "notice-empty-library",
                contentType = "notice",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                HomeNotification(
                    titleId = R.string.home_empty_title,
                    messageId = R.string.home_empty_message,
                    actionId = R.string.home_empty_action,
                    enabled = !state.indexInProgress,
                    onAction = onSetDirectoryClicked,
                )
            }
        }
        if (state.showNoMicrophonePermissionCard) {
            item(
                key = "notice-microphone-permission",
                contentType = "notice",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                HomeNotification(
                    titleId = R.string.home_microphone_title,
                    messageId = R.string.home_microphone_message,
                    actionId = R.string.home_microphone_action,
                    onAction = onEnableMicrophoneClicked,
                )
            }
        }
        if (state.showDesmumeDeprecatedCard) {
            item(
                key = "notice-desmume-deprecated",
                contentType = "notice",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                HomeNotification(
                    titleId = R.string.home_notification_desmume_deprecated_title,
                    messageId = R.string.home_notification_desmume_deprecated_message,
                    actionId = R.string.home_notification_desmume_deprecated_action,
                    onAction = onOpenCoreSelection,
                )
            }
        }
        items(
            count = games.itemCount,
            key = games.itemKey { it.id },
            contentType = games.itemContentType { "game" },
            itemRowWeight = { index -> games.peek(index)?.let(CoverArtPolicy::aspectRatio) ?: 1f },
        ) { index ->
            val game = games[index]
            if (game == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
            } else {
                LemuroidGameCard(
                    modifier = Modifier.animateItem(),
                    game = game,
                    onClick = { onGameClicked(game) },
                    onLongClick = { onGameLongClick(game) },
                )
            }
        }
    }
}

@Composable
private fun HomeNotification(
    titleId: Int,
    messageId: Int,
    actionId: Int,
    enabled: Boolean = true,
    onAction: () -> Unit = { },
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(titleId),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(messageId),
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onAction,
                enabled = enabled,
            ) {
                Text(stringResource(id = actionId))
            }
        }
    }
}
