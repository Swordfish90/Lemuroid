package come.asg.lemudroid.app.mobile.feature.home

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import come.asg.lemudroid.R
import come.asg.lemudroid.app.mobile.shared.compose.ui.LemuroidGameCard
import come.asg.lemudroid.app.mobile.shared.compose.ui.LemuroidGameImage
import come.asg.lemudroid.app.shared.main.GamePlaytimeStats
import come.asg.lemudroid.app.shared.settings.DataFolderPickerLauncher
import come.asg.lemudroid.app.utils.android.ComposableLifecycle
import come.asg.lemudroid.common.displayDetailsSettingsScreen
import come.asg.lemudroid.lib.library.db.entity.Game
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

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

    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) viewModel.updatePermissions(applicationContext)
    }

    val permissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) context.displayDetailsSettingsScreen()
        }

    val state = viewModel.getViewStates().collectAsState(HomeViewModel.UIState()).value
    HomeScreen(
        modifier = modifier,
        state = state,
        onGameClicked = onGameClick,
        onGameLongClick = onGameLongClick,
        onGameFavoriteToggle = onGameFavoriteToggle,
        onOpenCoreSelection = onOpenCoreSelection,
        onOpenSearch = onOpenSearch,
        onHelpPressed = onHelpPressed,
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
    onEnableNotificationsClicked: () -> Unit,
    onEnableMicrophoneClicked: () -> Unit,
    onSetDirectoryClicked: () -> Unit,
    onSetDataFolderClicked: () -> Unit,
) {
    val games = remember(state.recentGames, state.favoritesGames, state.discoveryGames) {
        (state.recentGames + state.favoritesGames + state.discoveryGames).distinctBy { it.id }.take(12)
    }
    var selectedGameId by remember(games) { mutableStateOf(games.firstOrNull()?.id) }
    val carouselState = rememberLazyListState()
    val selectedGame = games.firstOrNull { it.id == selectedGameId } ?: games.firstOrNull()
    val context = LocalContext.current
    var statsGame by remember { mutableStateOf<Game?>(null) }
    var artworkColor by remember(selectedGame?.id) { mutableStateOf(fallbackArtworkColor(selectedGame)) }
    val animatedArtworkColor by animateColorAsState(
        targetValue = artworkColor,
        animationSpec = tween(durationMillis = 720),
        label = "artworkBackgroundColor",
    )

    LaunchedEffect(selectedGame?.id, selectedGame?.displayCoverUrl()) {
        val game = selectedGame ?: return@LaunchedEffect
        val cover = game.displayCoverUrl()
        artworkColor = if (cover == null) fallbackArtworkColor(game) else {
            withContext(Dispatchers.IO) { loadArtworkColor(context, cover) } ?: fallbackArtworkColor(game)
        }
    }

    LaunchedEffect(carouselState, games) {
        snapshotFlow { carouselState.firstVisibleItemIndex }
            .map { games.getOrNull(it)?.id }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { selectedGameId = it }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedGame != null) {
            LemuroidGameImage(
                game = selectedGame,
                modifier = Modifier.fillMaxSize().blur(42.dp).graphicsLayer { alpha = 0.30f },
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        animatedArtworkColor.copy(alpha = 0.36f),
                        animatedArtworkColor.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                    ),
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
                IconButton(onClick = onHelpPressed, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = "Help and app information", tint = Color(0xFF10131A))
                }
            }
        }

        AnimatedVisibility(state.showNoNotificationPermissionCard) {
            HomeNotification(R.string.home_notification_title, R.string.home_notification_message, R.string.home_notification_action, onEnableNotificationsClicked)
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
            LazyRow(
                state = carouselState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 56.dp),
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
                        LemuroidGameImage(
                            game = game,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)),
                        )
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.52f),
                        ) {
                            IconButton(
                                onClick = { statsGame = game },
                                modifier = Modifier.size(38.dp),
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                            ) {
                                Icon(Icons.Filled.Info, contentDescription = "${game.displayName()} play statistics")
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
                        "Start your library",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("Choose your ROM folder in Settings to bring your games here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        HomeRow("Recently played", state.recentGames, onGameClicked, onGameLongClick) { statsGame = it }
        HomeRow("Favorites", state.favoritesGames, onGameClicked, onGameLongClick) { statsGame = it }
        HomeRow("Discover", state.discoveryGames, onGameClicked, onGameLongClick) { statsGame = it }
        Spacer(Modifier.height(8.dp))
        }
        statsGame?.let { game ->
            val totalMs = GamePlaytimeStats.totalMs(context, game.id)
            val totalMinutes = totalMs / 60_000L
            val played = if (totalMinutes >= 60) "${totalMinutes / 60} hr ${totalMinutes % 60} min" else "$totalMinutes min"
            AlertDialog(
                onDismissRequest = { statsGame = null },
                title = { Text(game.displayName(), fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Total playtime  ·  $played")
                        Text("Sessions  ·  ${GamePlaytimeStats.sessions(context, game.id)}")
                        Text(if (game.lastPlayedAt != null) "Last opened  ·  ${android.text.format.DateFormat.getMediumDateFormat(context).format(game.lastPlayedAt)}" else "Not played yet")
                    }
                },
                confirmButton = { TextButton(onClick = { statsGame = null }) { Text("Done") } },
                shape = RoundedCornerShape(28.dp),
            )
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
                LemuroidGameCard(
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
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(titleId), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(messageId), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(modifier = Modifier.align(Alignment.End), onClick = onAction, enabled = enabled) {
                Text(stringResource(actionId))
            }
        }
    }
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
