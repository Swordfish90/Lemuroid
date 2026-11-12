package come.asg.ominoy.app.mobile.feature.home

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import come.asg.ominoy.R
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.RetroAchievementsAccountBridge
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readGameplayCredentials
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readWebApiCredentials
import come.asg.ominoy.app.mobile.shared.compose.ui.OminoyGameCard
import come.asg.ominoy.app.mobile.shared.compose.ui.OminoyGameImage
import come.asg.ominoy.app.mobile.shared.compose.ui.OminoyHomeColors as HomeColors
import come.asg.ominoy.app.shared.main.GamePlaytimeStats
import come.asg.ominoy.app.shared.game.RetroAchievementsEarnedStore
import come.asg.ominoy.app.shared.game.RetroAchievementsSyncProgress
import come.asg.ominoy.app.shared.game.RetroAchievementsSyncWork
import come.asg.ominoy.app.shared.settings.DataFolderPickerLauncher
import come.asg.ominoy.app.utils.android.ComposableLifecycle
import come.asg.ominoy.common.displayDetailsSettingsScreen
import come.asg.ominoy.lib.library.db.entity.Game
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import java.util.Date
import java.io.File

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
    onOpenCoreSelection: () -> Unit,
    onOpenSearch: () -> Unit,
    onHelpPressed: () -> Unit,
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    LaunchedEffect(Unit) {
        if (RetroAchievementsAccountBridge.credentials.value == null) {
            readGameplayCredentials(applicationContext)?.let(RetroAchievementsAccountBridge::set)
        }
    }

    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            viewModel.updatePermissions(applicationContext)
        }
    }

    val permissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) context.displayDetailsSettingsScreen()
        }

    val state = viewModel.getViewStates().collectAsState(HomeViewModel.UIState()).value
    val syncProgress by RetroAchievementsSyncProgress.state.collectAsState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var wasSyncing by remember { mutableStateOf(false) }
    LaunchedEffect(syncProgress.running, syncProgress.finishedMessage) {
        if (wasSyncing && !syncProgress.running && syncProgress.finishedMessage != null) {
            Toast.makeText(applicationContext, syncProgress.finishedMessage, Toast.LENGTH_SHORT).show()
        }
        wasSyncing = syncProgress.running
    }
    HomeScreen(
        modifier = modifier,
        state = state,
        onGameClicked = onGameClick,
        onGameLongClick = onGameLongClick,
        onGameFavoriteToggle = onGameFavoriteToggle,
        onOpenCoreSelection = onOpenCoreSelection,
        onOpenSearch = onOpenSearch,
        onHelpPressed = onHelpPressed,
        onSyncAchievements = {
            scope.launch {
                val credentials = withContext(Dispatchers.IO) { readWebApiCredentials(applicationContext) }
                if (credentials == null) {
                    Toast.makeText(applicationContext, "API is not configured", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val games = viewModel.loadAllGames()
                if (games.isEmpty()) {
                    Toast.makeText(applicationContext, "No games in library", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                // Do not short-circuit when artwork/metadata are already cached. Refresh
                // must also prepare the native RA response cache for offline game launch.
                RetroAchievementsSyncProgress.begin(games)
                RetroAchievementsSyncWork.enqueue(applicationContext)
            }
        },
        onRetryFailedGames = { failedIds ->
            scope.launch {
                if (withContext(Dispatchers.IO) { readWebApiCredentials(applicationContext) } == null) {
                    Toast.makeText(applicationContext, "API is not configured", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val failedGames = viewModel.loadAllGames().filter { it.id in failedIds }
                if (failedGames.isEmpty()) return@launch
                RetroAchievementsSyncProgress.begin(failedGames)
                RetroAchievementsSyncWork.enqueue(applicationContext, failedIds)
            }
        },
        onEnableNotificationsClicked = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onEnableMicrophoneClicked = { permissionsLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        onSetDirectoryClicked = { viewModel.changeLocalStorageFolder(context) },
        onSetDataFolderClicked = { DataFolderPickerLauncher.pickFolder(context) },
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HomeScreen(
    modifier: Modifier,
    state: HomeViewModel.UIState,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
    onOpenCoreSelection: () -> Unit,
    onOpenSearch: () -> Unit,
    onHelpPressed: () -> Unit,
    onSyncAchievements: () -> Unit,
    onRetryFailedGames: (Set<Int>) -> Unit,
    onEnableNotificationsClicked: () -> Unit,
    onEnableMicrophoneClicked: () -> Unit,
    onSetDirectoryClicked: () -> Unit,
    onSetDataFolderClicked: () -> Unit,
) {
    val retroAchievementsCredentials by RetroAchievementsAccountBridge.credentials.collectAsState()
    val retroAchievementsLoggedIn = retroAchievementsCredentials != null
    val games = remember(state.recentGames, state.favoritesGames, state.discoveryGames) {
        state.recentGames.take(10).ifEmpty { state.favoritesGames.take(10).ifEmpty { state.discoveryGames.take(10) } }
    }
    var selectedGameId by remember(games) { mutableStateOf(games.firstOrNull()?.id) }
    val carouselState = rememberLazyListState()
    val selectedGame = games.firstOrNull { it.id == selectedGameId } ?: games.firstOrNull()
    val context = LocalContext.current
    val syncProgress by RetroAchievementsSyncProgress.state.collectAsState()
    var showSyncProgress by remember { mutableStateOf(false) }
    val syncTransition = rememberInfiniteTransition(label = "homeAchievementSync")
    val syncRotation by syncTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "homeAchievementSyncRotation",
    )
    var statsGame by remember { mutableStateOf<Game?>(null) }
    var statsAchievement by remember { mutableStateOf<RetroAchievementsEarnedStore.Achievement?>(null) }
    var statsRefreshToken by remember { mutableStateOf(0) }
    val openStats = { game: Game ->
        statsGame = game
        statsRefreshToken++
    }
    var artworkColor by remember(selectedGame?.id) { mutableStateOf(fallbackArtworkColor(selectedGame)) }
    val animatedArtworkColor by animateColorAsState(
        targetValue = artworkColor,
        animationSpec = tween(durationMillis = 720),
        label = "artworkBackgroundColor",
    )
    val vibrantArtworkColor = energizeArtworkColor(animatedArtworkColor)
    val artworkAccentColor = shiftArtworkHue(vibrantArtworkColor, 42f)

    LaunchedEffect(selectedGame?.id, selectedGame?.displayCoverUrl()) {
        val game = selectedGame ?: return@LaunchedEffect
        val cover = game.displayCoverUrl()
        artworkColor = if (cover == null) fallbackArtworkColor(game) else {
            withContext(Dispatchers.IO) { loadArtworkColor(context, cover) } ?: fallbackArtworkColor(game)
        }
    }

    LaunchedEffect(carouselState, games) {
        snapshotFlow {
            val layoutInfo = carouselState.layoutInfo
            val viewportCenter = layoutInfo.viewportSize.width / 2
            layoutInfo.visibleItemsInfo
                .minByOrNull { item -> abs(item.offset + item.size / 2 - viewportCenter) }
                ?.index
        }
            .map { index -> index?.let { games.getOrNull(it)?.id } }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { selectedGameId = it }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedGame != null) {
            OminoyGameImage(
                game = selectedGame,
                modifier = Modifier.fillMaxSize().blur(42.dp).graphicsLayer { alpha = 0.40f },
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        vibrantArtworkColor.copy(alpha = 0.48f),
                        artworkAccentColor.copy(alpha = 0.24f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.88f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
        )
        Box(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().height(700.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            vibrantArtworkColor.copy(alpha = 0.42f),
                            artworkAccentColor.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                        radius = 1050f,
                    ),
                ),
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 144.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 24.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "OMINOY",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                )
                Text("RETRO, READY.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
            }
            IconButton(onClick = onOpenSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Search games", tint = MaterialTheme.colorScheme.onSurface)
            }
            Surface(shape = CircleShape, color = Color(0xFF8AEBDD), modifier = Modifier.padding(start = 4.dp)) {
                IconButton(
                    onClick = { if (syncProgress.running) showSyncProgress = true else onSyncAchievements() },
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = if (syncProgress.running) "Show achievement sync progress" else "Download achievements",
                        tint = Color(0xFF10131A),
                        modifier = Modifier.graphicsLayer { rotationZ = if (syncProgress.running) syncRotation else 0f },
                    )
                }
            }
        }

        if (showSyncProgress) {
            RetroAchievementsSyncSheet(
                progress = syncProgress,
                onClose = { showSyncProgress = false },
                onRetryFailed = { onRetryFailedGames(syncProgress.failedGames.map { it.game.id }.toSet()) },
            )
        }

        AnimatedVisibility(state.showNoNotificationPermissionCard) {
            HomeNotification(
                R.string.home_notification_title,
                R.string.home_notification_message,
                R.string.home_notification_action,
                onEnableNotificationsClicked,
            )
        }
        AnimatedVisibility(state.showNoGamesCard) {
            HomeNotification(
                R.string.home_empty_title,
                R.string.home_empty_message,
                R.string.home_empty_action,
                onSetDirectoryClicked,
                enabled = !state.indexInProgress,
            )
        }
        AnimatedVisibility(state.showNoDataFolderCard) {
            HomeNotification(R.string.data_folder_setup_title, R.string.data_folder_setup_message, R.string.data_folder_setup_action, onSetDataFolderClicked)
        }
        AnimatedVisibility(state.showNoMicrophonePermissionCard) {
            HomeNotification(R.string.home_microphone_title, R.string.home_microphone_message, R.string.home_microphone_action, onEnableMicrophoneClicked)
        }
        AnimatedVisibility(state.showDesmumeDeprecatedCard) {
            HomeNotification(
                R.string.home_notification_desmume_deprecated_title,
                R.string.home_notification_desmume_deprecated_message,
                R.string.home_notification_desmume_deprecated_action,
                onOpenCoreSelection,
            )
        }

        Text(
            text = if (selectedGame?.lastPlayedAt != null) "Pick up where you left off" else "Your next adventure",
            modifier = Modifier.padding(start = 24.dp, top = 4.dp, end = 24.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 25.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.7).sp,
        )

        if (selectedGame != null) {
            val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val sidePadding = ((maxWidth - 250.dp) / 2).coerceAtLeast(0.dp)
                LazyRow(
                    state = carouselState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = carouselState),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = sidePadding),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    itemsIndexed(games, key = { _, game -> game.id }) { index, game ->
                        val selected = selectedGame.id == game.id
                        val scale by animateFloatAsState(if (selected) 1f else 0.92f, tween(240), label = "coverScale")
                        val opacity by animateFloatAsState(if (selected) 1f else 0.76f, tween(240), label = "coverOpacity")
                        Box(
                            modifier = Modifier.width(250.dp).height(250.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale; alpha = opacity }
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF342A4A), Color(0xFF17131F), Color(0xFF271A29)),
                                    ),
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
                                .combinedClickable(
                                    onClick = {
                                        selectedGameId = game.id
                                        coroutineScope.launch { carouselState.animateScrollToItem(index) }
                                        onGameClicked(game)
                                    },
                                    onLongClick = { onGameLongClick(game) },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            OminoyGameImage(
                                game = game,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)),
                            )
                            Surface(
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.52f),
                            ) {
                                IconButton(
                                    onClick = { openStats(game) },
                                    modifier = Modifier.size(38.dp),
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                                ) {
                                    Icon(Icons.Filled.Info, contentDescription = "${game.displayName()} play statistics")
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = selectedGame.displayName(),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    maxLines = 2,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${selectedGame.systemId.uppercase()}  ·  ${if (selectedGame.lastPlayedAt == null) "Not played yet" else "Recently played"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        IconButton(onClick = { onGameFavoriteToggle(selectedGame, !selectedGame.isFavorite) }) {
                            Icon(
                                imageVector = if (selectedGame.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (selectedGame.isFavorite) "Remove favorite" else "Add favorite",
                                tint = if (selectedGame.isFavorite) Color(0xFFFF83A8) else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Button(
                        onClick = { onGameClicked(selectedGame) },
                        shape = RoundedCornerShape(100.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4F1F8), contentColor = Color(0xFF0C0B10)),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (selectedGame.lastPlayedAt == null) "Play" else "Resume", fontWeight = FontWeight.Bold)
                    }
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        IconButton(onClick = { onGameLongClick(selectedGame) }) {
                            Icon(Icons.Filled.MoreHoriz, contentDescription = "More game actions", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    repeat(minOf(games.size, 7)) { index ->
                        Box(
                            Modifier.size(if (games[index].id == selectedGame.id) 18.dp else 5.dp, 5.dp)
                                .clip(CircleShape)
                                .background(if (games[index].id == selectedGame.id) Color(0xFF8AEBDD) else Color.White.copy(alpha = 0.24f)),
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        when {
                            !state.romFolderConfigured -> "Start your library"
                            state.indexInProgress -> "Scanning your ROM folder"
                            else -> "No games found in this folder"
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        when {
                            !state.romFolderConfigured -> "Choose a ROM folder using Select Directory above to bring your games here."
                            state.indexInProgress -> "OMINOY is scanning the selected folder for games."
                            else -> "The folder is saved, but no supported games were found. Check that it contains .gb, .gbc, or .gba files, then rescan in Settings."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        HomeRow("Recently played", state.recentGames, onGameClicked, onGameLongClick) { openStats(it) }
        HomeRow("Favorites", state.favoritesGames, onGameClicked, onGameLongClick) { openStats(it) }
        HomeRow("Discover", state.discoveryGames, onGameClicked, onGameLongClick) { openStats(it) }
        Spacer(Modifier.height(8.dp))
        }
        statsGame?.let { game ->
            val playStats = remember(game.id, statsRefreshToken) {
                val totalMs = GamePlaytimeStats.totalMs(context, game.id)
                val recordedLastPlayed = GamePlaytimeStats.lastPlayed(context, game.id)
                val lastPlayed = recordedLastPlayed.takeIf { it > 0L }?.let(::Date) ?: game.lastPlayedAt?.let(::Date)
                Triple(totalMs, GamePlaytimeStats.sessions(context, game.id), lastPlayed)
            }
            val totalMinutes = playStats.first / 60_000L
            val played = if (totalMinutes >= 60) "${totalMinutes / 60} hr ${totalMinutes % 60} min" else "$totalMinutes min"
            var cachedAchievements by remember(game.id, game.title, game.displayName(), game.fileName, statsRefreshToken) {
                mutableStateOf(run {
                    val directlyLinked = RetroAchievementsEarnedStore.read(context, game.id)
                    val syncedByTitle = RetroAchievementsEarnedStore.readAll(context).filter { cached ->
                        cached.localLibraryGameId == game.id || game.matchesCachedAchievementTitle(cached.gameTitle)
                    }
                    (directlyLinked + syncedByTitle).distinctBy { it.raGameId to it.id }
                })
            }
            val persistedAccount = remember(game.id, statsRefreshToken) { readGameplayCredentials(context) }
            val webApiCredentials = remember(game.id, statsRefreshToken) { readWebApiCredentials(context) }
            var achievementSyncing by remember(game.id, statsRefreshToken) { mutableStateOf(false) }
            LaunchedEffect(game.id, statsRefreshToken, webApiCredentials?.apiKey) {
                if (cachedAchievements.isEmpty() && webApiCredentials != null && !achievementSyncing) {
                    achievementSyncing = true
                    withContext(Dispatchers.IO) {
                        runCatching {
                            RetroAchievementsEarnedStore.syncLocalGame(
                                context = context,
                                libraryGameId = game.id,
                                fileUri = game.fileUri,
                                systemId = game.systemId,
                                gameTitle = game.displayName(),
                                username = webApiCredentials.username,
                                apiKey = webApiCredentials.apiKey,
                                onArtworkProgress = null,
                            )
                        }
                    }
                    cachedAchievements = withContext(Dispatchers.IO) {
                        val direct = RetroAchievementsEarnedStore.read(context, game.id)
                        val byTitle = RetroAchievementsEarnedStore.readAll(context).filter { cached ->
                            cached.localLibraryGameId == game.id || game.matchesCachedAchievementTitle(cached.gameTitle)
                        }
                        (direct + byTitle).distinctBy { it.raGameId to it.id }
                    }
                    achievementSyncing = false
                }
            }
            HomeGameStatsDialog(
                game = game,
                played = played,
                sessions = playStats.second,
                lastPlayed = playStats.third,
                achievements = cachedAchievements,
                achievementSyncing = achievementSyncing,
                accountAvailable = retroAchievementsLoggedIn || persistedAccount != null,
                onClose = { statsGame = null },
                onAchievement = { statsAchievement = it },
            )
        }
        statsAchievement?.let { achievement ->
            val game = statsGame
            if (game != null) HomeAchievementDetailDialog(game, achievement) { statsAchievement = null }
        }

    }
}

fun Game.matchesCachedAchievementTitle(cachedTitle: String): Boolean {
    val localNames = listOf(title, displayName(), fileName.substringBeforeLast('.', fileName))
        .map(::normalizeHomeGameTitle)
        .filter { it.length >= 4 }
    val cached = normalizeHomeGameTitle(cachedTitle)
    if (cached.length < 4) return false
    return localNames.any { local ->
        local == cached ||
            (minOf(local.length, cached.length) >= 7 && (local.contains(cached) || cached.contains(local))) ||
            run {
                val localTokens = local.split(' ').filter { it.length > 2 }.toSet()
                val cacheTokens = cached.split(' ').filter { it.length > 2 }.toSet()
                val smaller = minOf(localTokens.size, cacheTokens.size)
                smaller > 0 && localTokens.intersect(cacheTokens).size.toFloat() / smaller >= .75f
            }
    }
}

private fun normalizeHomeGameTitle(value: String): String {
    val withoutTags = value
        .replace(Regex("\\([^)]*\\)|\\[[^]]*]"), " ")
        .replace(Regex("\\.(zip|7z|rar|nes|gb|gbc|gba|sfc|smc|bin|rom)$", RegexOption.IGNORE_CASE), " ")
    val ascii = java.text.Normalizer.normalize(withoutTags, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
    val ignored = setOf("usa", "europe", "japan", "world", "rev", "revision", "version", "proto", "beta", "the", "and")
    return ascii.split(' ').filterNot { it in ignored }.joinToString(" ")
}

@Composable
fun HomeGameStatsDialog(
    game: Game,
    played: String,
    sessions: Int,
    lastPlayed: Date?,
    achievements: List<RetroAchievementsEarnedStore.Achievement>,
    achievementSyncing: Boolean,
    accountAvailable: Boolean,
    onClose: () -> Unit,
    onAchievement: (RetroAchievementsEarnedStore.Achievement) -> Unit,
) {
    val earned = achievements.filter { it.earned }
    val pending = achievements.filterNot { it.earned }
    val progress = if (achievements.isNotEmpty()) earned.size.toFloat() / achievements.size else 0f
    var achievementFilter by remember(game.id) { mutableStateOf(if (earned.isNotEmpty()) "Unlocked" else "Show all") }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        BoxWithConstraints(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = .72f)).pointerInput(onClose) { detectTapGestures(onTap = { onClose() }) },
            contentAlignment = Alignment.BottomCenter,
        ) {
            val interaction = remember { MutableInteractionSource() }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth(.96f).widthIn(max = 520.dp)
                    .heightIn(max = maxHeight * .9f)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)).background(HomeColors.CardVariant)
                    .clickable(interactionSource = interaction, indication = null) { }
                    .verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).width(42.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .22f)))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(60.dp).clip(RoundedCornerShape(18.dp)).background(HomeColors.Surface)) {
                        OminoyGameImage(game = game, modifier = Modifier.fillMaxSize())
                    }
                    Column(Modifier.weight(1f)) {
                        Text(game.displayName(), color = HomeColors.Text, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 2)
                        Text("${game.systemId.uppercase()} · Last played ${lastPlayed?.let { android.text.format.DateFormat.getMediumDateFormat(androidx.compose.ui.platform.LocalContext.current).format(it) } ?: "Never"}", color = HomeColors.Muted, fontSize = 12.sp)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) { Text("×", fontSize = 26.sp, color = HomeColors.Muted) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeStatTile(played, "Time played", Modifier.weight(1f))
                    HomeStatTile(sessions.toString(), "Sessions", Modifier.weight(1f))
                }
                Surface(shape = RoundedCornerShape(22.dp), color = HomeColors.Card, border = androidx.compose.foundation.BorderStroke(1.dp, HomeColors.Line)) {
                    Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Achievements", color = HomeColors.Text, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Text(
                                if (achievementSyncing) "Loading…" else "${earned.size} / ${achievements.size}",
                                color = HomeColors.Mint,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                            )
                        }
                        if (achievementSyncing) {
                            LinearProgressIndicator(Modifier.fillMaxWidth(), color = HomeColors.Mint)
                            Text("Loading available achievements for this game…", color = HomeColors.Muted, fontSize = 13.sp)
                        } else if (achievements.isEmpty()) {
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = HomeColors.Mint, trackColor = Color.White.copy(alpha = .12f))
                            Text(
                                if (accountAvailable) "Achievement details are not cached for this game yet. Launch it with RetroAchievements sync enabled to load them."
                                else "Log in to RetroAchievements to see earned and pending achievements.",
                                color = HomeColors.Muted, fontSize = 13.sp, lineHeight = 18.sp,
                            )
                        } else {
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = HomeColors.Mint, trackColor = Color.White.copy(alpha = .12f))
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)).background(HomeColors.SurfaceVariant).padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                listOf("Unlocked" to earned.size, "Locked" to pending.size, "Show all" to achievements.size).forEach { (label, count) ->
                                    TextButton(
                                        onClick = { achievementFilter = label },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (achievementFilter == label) HomeColors.Mint else Color.Transparent,
                                            contentColor = if (achievementFilter == label) Color(0xFF10131A) else HomeColors.Muted,
                                        ),
                                    ) {
                                        Text("$label ($count)", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                            when (achievementFilter) {
                                "Unlocked" -> HomeAchievementGroup("UNLOCKED", earned, locked = false, onAchievement = onAchievement)
                                "Locked" -> HomeAchievementGroup("LOCKED", pending, locked = true, onAchievement = onAchievement)
                                else -> {
                                    HomeAchievementGroup("UNLOCKED", earned, locked = false, onAchievement = onAchievement)
                                    HomeAchievementGroup("LOCKED", pending, locked = true, onAchievement = onAchievement)
                                }
                            }
                        }
                    }
                }
                TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) { Text("Close", color = HomeColors.Mint, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun HomeStatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(20.dp), color = HomeColors.Card, border = androidx.compose.foundation.BorderStroke(1.dp, HomeColors.Line)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, color = HomeColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(label, color = HomeColors.Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HomeAchievementGroup(
    title: String,
    achievements: List<RetroAchievementsEarnedStore.Achievement>,
    locked: Boolean,
    onAchievement: (RetroAchievementsEarnedStore.Achievement) -> Unit,
) {
    Text("$title (${achievements.size})", color = HomeColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp, modifier = Modifier.padding(top = 5.dp))
    if (achievements.isEmpty()) {
        Text(if (locked) "All done!" else "Nothing earned yet", color = HomeColors.Muted, fontSize = 12.sp)
    } else {
        val rows = (achievements.size + 3) / 4
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().height((rows * 58 + (rows - 1) * 9).dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp), verticalArrangement = Arrangement.spacedBy(9.dp),
            userScrollEnabled = false,
        ) {
            items(achievements, key = { it.id }) { achievement ->
                val badgePath = if (locked) achievement.lockedBadgePath.ifBlank { achievement.badgePath } else achievement.badgePath
                Surface(
                    modifier = Modifier.size(58.dp).clip(RoundedCornerShape(13.dp)).combinedClickable(onClick = { onAchievement(achievement) }, onLongClick = null),
                    color = HomeColors.SurfaceVariant,
                    border = if (!locked) androidx.compose.foundation.BorderStroke(1.dp, HomeColors.Gold.copy(alpha = .5f)) else null,
                ) {
                    val badgeBackground = if (locked) {
                        Brush.linearGradient(listOf(Color.Gray.copy(alpha = .14f), Color.Gray.copy(alpha = .14f)))
                    } else {
                        Brush.linearGradient(listOf(HomeColors.Gold.copy(alpha = .38f), HomeColors.SurfaceVariant))
                    }
                    Box(Modifier.fillMaxSize().background(badgeBackground), contentAlignment = Alignment.Center) {
                        if (badgePath.isNotBlank() && File(badgePath).isFile) {
                            AsyncImage(model = File(badgePath), contentDescription = achievement.title, modifier = Modifier.fillMaxSize().padding(5.dp).clip(RoundedCornerShape(9.dp)), alpha = if (locked) .48f else 1f)
                        } else Text(if (locked) "◇" else "★", fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (locked) HomeColors.Muted else HomeColors.Gold)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeAchievementDetailDialog(
    game: Game,
    achievement: RetroAchievementsEarnedStore.Achievement,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val path = if (achievement.earned) achievement.badgePath else achievement.lockedBadgePath.ifBlank { achievement.badgePath }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = .78f)).pointerInput(onClose) { detectTapGestures(onTap = { onClose() }) },
            contentAlignment = Alignment.BottomCenter,
        ) {
            val interaction = remember { MutableInteractionSource() }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth(.96f).widthIn(max = 680.dp).heightIn(max = maxHeight * .88f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(HomeColors.CardVariant)
                    .clickable(interactionSource = interaction, indication = null) { }
                    .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .22f)))
                Box(Modifier.size(96.dp).clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(listOf(HomeColors.Gold.copy(alpha = .7f), HomeColors.SurfaceVariant))), contentAlignment = Alignment.Center) {
                    if (path.isNotBlank() && File(path).isFile) AsyncImage(model = File(path), contentDescription = "${achievement.title} badge", modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(22.dp)), alpha = if (achievement.earned) 1f else .48f) else Text(if (achievement.earned) "★" else "◇", color = HomeColors.Gold, fontSize = 38.sp)
                }
                Text(achievement.title, color = HomeColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Text("${game.displayName()} · ${game.systemId.uppercase()}", color = HomeColors.Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeChip("${achievement.points} pts", HomeColors.Gold)
                    HomeChip(if (achievement.earned) "✓ Unlocked" else "Locked", if (achievement.earned) HomeColors.Mint else HomeColors.Muted)
                    if (achievement.hardcoreEarnedAt > 0L) HomeChip("★ Hardcore", HomeColors.Gold)
                }
                HomeDetailSection("HOW TO EARN") { Text(achievement.description.ifBlank { "No description is available." }, color = HomeColors.Text, fontSize = 15.sp, lineHeight = 21.sp) }
                HomeDetailSection(if (achievement.earned) "UNLOCKED" else "STATUS") {
                    if (achievement.earned) {
                        val date = if (achievement.earnedAt > 0L) android.text.format.DateFormat.getMediumDateFormat(context).format(Date(achievement.earnedAt)) else "Date unavailable"
                        Text("$date · ${if (achievement.hardcoreEarnedAt > 0L) "Hardcore" else "Softcore"}", color = HomeColors.Text, fontSize = 14.sp)
                    } else Text("Not unlocked yet. Keep playing to earn ${achievement.points} points.", color = HomeColors.Muted, fontSize = 14.sp)
                }
                OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(99.dp)) { Text("Close") }
            }
        }
    }
}

@Composable
private fun HomeChip(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(99.dp), color = color.copy(alpha = .14f)) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RetroAchievementsSyncSheet(
    progress: RetroAchievementsSyncProgress.Progress,
    onClose: () -> Unit,
    onRetryFailed: () -> Unit,
) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.72f)).pointerInput(onClose) {
                detectTapGestures(onTap = { onClose() })
            },
            contentAlignment = Alignment.BottomCenter,
        ) {
            val interaction = remember { MutableInteractionSource() }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth(.97f).widthIn(max = 680.dp)
                    .heightIn(max = 720.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(HomeColors.CardVariant)
                    .clickable(interactionSource = interaction, indication = null) { }
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.width(40.dp).height(4.dp).align(Alignment.CenterHorizontally).clip(CircleShape).background(Color.White.copy(alpha = .22f)))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("RetroAchievements sync", color = HomeColors.Text, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        Text(
                            if (progress.running) "${progress.completeGames} / ${progress.games.size} games processed" else (progress.finishedMessage ?: "Sync status"),
                            color = HomeColors.Muted,
                            fontSize = 12.sp,
                        )
                    }
                    Text("${(progress.fraction * 100).toInt()}%", color = HomeColors.Mint, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                }
                LinearProgressIndicator(
                    progress = { progress.fraction },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(99.dp)),
                    color = HomeColors.Mint,
                    trackColor = Color.White.copy(alpha = .10f),
                )
                Column(
                    Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    if (progress.games.isEmpty()) {
                        Text("Preparing your library…", color = HomeColors.Muted, modifier = Modifier.padding(vertical = 12.dp))
                    }
                    progress.games.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = HomeColors.Card,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HomeColors.Line),
                        ) {
                            Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                                OminoyGameImage(game = item.game, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.game.displayName(), Modifier.weight(1f), color = HomeColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(
                                            if (item.total > 0) "${item.downloaded.coerceAtMost(item.total)} / ${item.total}" else "—",
                                            color = if (item.failed) Color(0xFFFF8A80) else if (item.complete) HomeColors.Mint else HomeColors.Muted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { item.fraction },
                                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(99.dp)),
                                        color = HomeColors.Mint,
                                        trackColor = Color.White.copy(alpha = .10f),
                                    )
                                    Text(
                                        item.state,
                                        color = if (item.failed) Color(0xFFFF8A80) else HomeColors.Muted,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
                if (!progress.running && progress.failedGames.isNotEmpty()) {
                    OutlinedButton(onClick = onRetryFailed, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(99.dp)) {
                        Text("Retry failed (${progress.failedGames.size})", color = HomeColors.Mint, fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(99.dp)) {
                    Text("Close", color = HomeColors.Mint, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HomeDetailSection(title: String, content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = HomeColors.Card, border = androidx.compose.foundation.BorderStroke(1.dp, HomeColors.Line)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = HomeColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
            content()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeRow(
    title: String,
    games: List<Game>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onInfoClick: (Game) -> Unit,
) {
    if (games.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        ) {
            items(games, key = { it.id }) { game ->
                OminoyGameCard(
                    modifier = Modifier.width(142.dp),
                    game = game,
                    onClick = { onGameClicked(game) },
                    onLongClick = { onGameLongClick(game) },
                    onInfoClick = { onInfoClick(game) },
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
    onAction: () -> Unit,
    enabled: Boolean = true,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(containerColor = HomeColors.SurfaceVariant.copy(alpha = .94f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(titleId), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = HomeColors.Text)
            Text(stringResource(messageId), style = MaterialTheme.typography.bodyMedium, color = HomeColors.Muted, lineHeight = 20.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onAction, enabled = enabled, shape = RoundedCornerShape(99.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(stringResource(actionId), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun energizeArtworkColor(color: Color): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[1] = hsv[1].coerceIn(0.66f, 0.92f)
    hsv[2] = hsv[2].coerceIn(0.74f, 0.96f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun shiftArtworkHue(
    color: Color,
    hueShift: Float,
): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[0] = (hsv[0] + hueShift) % 360f
    hsv[1] = hsv[1].coerceIn(0.70f, 0.90f)
    hsv[2] = hsv[2].coerceIn(0.78f, 0.94f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun fallbackArtworkColor(game: Game?): Color =
    when (game?.systemId) {
        "gba" -> Color(0xFF4658C7)
        "gbc" -> Color(0xFF72A832)
        "gb" -> Color(0xFF258F79)
        "psp", "psx" -> Color(0xFF8B4BC1)
        "n64", "ngc", "3ds" -> Color(0xFF2F829E)
        else -> Color(0xFF51406F)
    }

private suspend fun loadArtworkColor(context: Context, coverUrl: String): Color? =
    runCatching {
        val request = ImageRequest.Builder(context).data(coverUrl).allowHardware(false).build()
        val drawable = context.imageLoader.execute(request).drawable ?: return@runCatching null
        val bitmap =
            (drawable as? BitmapDrawable)?.bitmap
                ?: drawable.toBitmap(width = 48, height = 48, config = Bitmap.Config.ARGB_8888)

        var red = 0L
        var green = 0L
        var blue = 0L
        var count = 0L
        val stepX = max(1, bitmap.width / 10)
        val stepY = max(1, bitmap.height / 10)
        for (y in 0 until bitmap.height step stepY) {
            for (x in 0 until bitmap.width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = android.graphics.Color.alpha(pixel)
                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)
                val brightest = max(r, max(g, b))
                val darkest = min(r, min(g, b))
                if (alpha > 180 && brightest in 28..238 && brightest - darkest > 12) {
                    red += r
                    green += g
                    blue += b
                    count++
                }
            }
        }
        if (count == 0L) {
            Color(0xFF51406F)
        } else {
            Color(android.graphics.Color.rgb((red / count).toInt(), (green / count).toInt(), (blue / count).toInt()))
        }
    }.getOrNull()
