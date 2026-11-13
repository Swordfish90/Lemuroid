package come.asg.ominoy.app.mobile.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import come.asg.ominoy.app.mobile.feature.home.HomeAchievementDetailDialog
import come.asg.ominoy.app.mobile.feature.home.HomeGameStatsDialog
import come.asg.ominoy.app.mobile.feature.home.matchesCachedAchievementTitle
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.RetroAchievementsAccountBridge
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readGameplayCredentials
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.readWebApiCredentials
import come.asg.ominoy.app.mobile.shared.compose.ui.OminoyGameCard
import come.asg.ominoy.app.mobile.shared.compose.ui.OminoyGameListRow
import come.asg.ominoy.app.shared.game.RetroAchievementsEarnedStore
import come.asg.ominoy.app.shared.main.GamePlaytimeStats
import come.asg.ominoy.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

private val libraryFilters = listOf("All", "Favorites", "GB", "GBC", "GBA")

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
) {
    val selectedFilter by viewModel.filter.collectAsState()
    val query by viewModel.query.collectAsState()
    val games = viewModel.games.collectAsLazyPagingItems()
    var gridView by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val retroAchievementsAccount by RetroAchievementsAccountBridge.credentials.collectAsState()
    var statsGame by remember { mutableStateOf<Game?>(null) }
    var statsAchievement by remember { mutableStateOf<RetroAchievementsEarnedStore.Achievement?>(null) }
    var statsRefreshToken by remember { mutableStateOf(0) }
    val openStats = { game: Game ->
        statsGame = game
        statsRefreshToken++
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, top = 18.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Library", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.2).sp)
                Text("Your games, ready when you are", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { gridView = !gridView }) {
                Icon(
                    imageVector = if (gridView) Icons.Filled.ViewList else Icons.Filled.GridView,
                    contentDescription = if (gridView) "Show list" else "Show covers",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::updateQuery,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            placeholder = { Text("Search games or systems") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(libraryFilters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { viewModel.selectFilter(filter) },
                    label = { Text(filter) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text =
                    when (selectedFilter) {
                        "All" -> "All games"
                        "Favorites" -> "Favorites"
                        else -> "$selectedFilter games"
                    },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = if (games.itemCount == 1) "1 game" else "${games.itemCount}+ games",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        when {
            games.loadState.refresh is LoadState.Loading && games.itemCount == 0 -> {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                }
            }
            games.itemCount == 0 -> {
                Box(Modifier.fillMaxWidth().weight(1f).padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            "Nothing here yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            if (selectedFilter == "Favorites") {
                                "Tap the heart on a game to keep it here."
                            } else {
                                "Try another search, or add games from Settings."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            gridView -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 144.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(games.itemCount, key = { games[it]?.id ?: it }) { index ->
                        val game = games[index] ?: return@items
                        OminoyGameCard(
                            modifier = Modifier.fillMaxWidth(),
                            game = game,
                            onClick = { onGameClick(game) },
                            onLongClick = { onGameLongClick(game) },
                            onInfoClick = { openStats(game) },
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 144.dp),
                ) {
                    items(games.itemCount, key = { games[it]?.id ?: it }) { index ->
                        val game = games[index] ?: return@items
                        OminoyGameListRow(
                            game = game,
                            onClick = { onGameClick(game) },
                            onLongClick = { onGameLongClick(game) },
                            onFavoriteToggle = { isFavorite -> onGameFavoriteToggle(game, isFavorite) },
                            onInfoClick = { openStats(game) },
                        )
                    }
                }
            }
        }

        statsGame?.let { game ->
            val playStats =
                remember(game.id, statsRefreshToken) {
                    val totalMs = GamePlaytimeStats.totalMs(context, game.id)
                    val recordedLastPlayed = GamePlaytimeStats.lastPlayed(context, game.id)
                    val lastPlayed =
                        recordedLastPlayed.takeIf { it > 0L }?.let(::Date) ?: game.lastPlayedAt?.let(::Date)
                    Triple(totalMs, GamePlaytimeStats.sessions(context, game.id), lastPlayed)
                }
            val totalMinutes = playStats.first / 60_000L
            val played =
                if (totalMinutes >=
                    60
                ) {
                    "${totalMinutes / 60} hr ${totalMinutes % 60} min"
                } else {
                    "$totalMinutes min"
                }
            var cachedAchievements by remember(
                game.id,
                game.title,
                game.displayName(),
                game.fileName,
                statsRefreshToken,
            ) {
                mutableStateOf(
                    run {
                        val directlyLinked = RetroAchievementsEarnedStore.read(context, game.id)
                        val syncedByTitle =
                            RetroAchievementsEarnedStore.readAll(context).filter { cached ->
                                cached.localLibraryGameId == game.id ||
                                    game.matchesCachedAchievementTitle(cached.gameTitle)
                            }
                        (directlyLinked + syncedByTitle).distinctBy { it.raGameId to it.id }
                    },
                )
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
                            )
                        }
                    }
                    cachedAchievements =
                        withContext(Dispatchers.IO) {
                            val direct = RetroAchievementsEarnedStore.read(context, game.id)
                            val byTitle =
                                RetroAchievementsEarnedStore.readAll(context).filter { cached ->
                                    cached.localLibraryGameId == game.id ||
                                        game.matchesCachedAchievementTitle(cached.gameTitle)
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
                accountAvailable = retroAchievementsAccount != null || persistedAccount != null,
                onClose = { statsGame = null },
                onAchievement = { statsAchievement = it },
            )
        }
        statsAchievement?.let { achievement ->
            statsGame?.let { game ->
                HomeAchievementDetailDialog(game, achievement) { statsAchievement = null }
            }
        }
    }
}
