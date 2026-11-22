package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameListRow
import com.swordfish.lemuroid.app.shared.covers.CoverUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GamesScreen(
    modifier: Modifier = Modifier,
    viewModel: GamesViewModel,
    searchQuery: String = "",
    selectedGenres: Set<String> = emptySet(),
    onGenresChange: (Set<String>) -> Unit = {},
    sortOrder: GameSortOrder = GameSortOrder.NAME_ASC,
    isGridView: Boolean = false,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
) {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val games by viewModel.games.collectAsState()
    val availableGenres by viewModel.availableGenres.collectAsState()

    var lastAppliedFilter by remember { mutableStateOf(Triple(sortOrder, selectedGenres, searchQuery)) }
    var pendingScrollOldIds by remember { mutableStateOf<List<Int>?>(null) }

    LaunchedEffect(searchQuery) {
        viewModel.changeSearchQuery(searchQuery)
    }
    LaunchedEffect(selectedGenres) {
        viewModel.changeSelectedGenres(selectedGenres)
    }
    LaunchedEffect(sortOrder) {
        viewModel.changeSortOrder(sortOrder)
    }

    LaunchedEffect(sortOrder, selectedGenres, searchQuery) {
        val current = Triple(sortOrder, selectedGenres, searchQuery)
        if (current != lastAppliedFilter) {
            lastAppliedFilter = current
            pendingScrollOldIds = games.map { it.game.id }
        }
    }

    LaunchedEffect(games, pendingScrollOldIds, isGridView) {
        val oldIds = pendingScrollOldIds ?: return@LaunchedEffect
        val currentIds = games.map { it.game.id }
        if (currentIds != oldIds) {
            if (isGridView) gridState.scrollToItem(0) else listState.scrollToItem(0)
            pendingScrollOldIds = null
        } else {
            kotlinx.coroutines.delay(120)
            if (pendingScrollOldIds != null) {
                if (isGridView) gridState.scrollToItem(0) else listState.scrollToItem(0)
                pendingScrollOldIds = null
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (availableGenres.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "genre_all") {
                    FilterChip(
                        selected = selectedGenres.isEmpty(),
                        onClick = { onGenresChange(emptySet()) },
                        label = { Text(text = stringResource(R.string.filter_genre_all)) },
                    )
                }
                items(availableGenres, key = { it }) { genre ->
                    val isSelected = selectedGenres.any { it.equals(genre, ignoreCase = true) }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = if (isSelected) {
                                selectedGenres.filterNot { it.equals(genre, ignoreCase = true) }.toSet()
                            } else {
                                selectedGenres + genre
                            }
                            onGenresChange(updated)
                        },
                        label = { Text(text = genre) },
                    )
                }
            }
        }

        if (games.isEmpty()) {
            LemuroidEmptyView(modifier = Modifier.weight(1f))
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 112.dp),
                state = gridState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                gridItems(
                    items = games,
                    key = { it.game.id },
                    contentType = { "game_grid_card" },
                ) { item ->
                    val game = item.game
                    GameGridCard(
                        game = game,
                        rating = item.rating,
                        onClick = { onGameClick(game) },
                        onLongClick = { onGameLongClick(game) },
                        onFavoriteToggle = { isFavorite -> onGameFavoriteToggle(game, isFavorite) },
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                items(
                    items = games,
                    key = { it.game.id },
                    contentType = { "game_row" },
                ) { item ->
                    val game = item.game

                    LemuroidGameListRow(
                        game = game,
                        rating = item.rating,
                        onClick = { onGameClick(game) },
                        onLongClick = { onGameLongClick(game) },
                        onFavoriteToggle = { isFavorite -> onGameFavoriteToggle(game, isFavorite) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameGridCard(
    game: Game,
    rating: Float?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coverUrl = game.displayCoverUrl()
    val fallbackDrawable = remember(game.id, game.title) { CoverUtils.getFallbackDrawable(game) }
    val fallbackPainter = rememberDrawablePainter(fallbackDrawable)
    val imageRequest = remember(coverUrl) {
        ImageRequest.Builder(context)
            .data(coverUrl)
            .size(360)
            .crossfade(false)
            .build()
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = game.displayName(),
                    modifier = Modifier.fillMaxSize(),
                    fallback = fallbackPainter,
                    error = fallbackPainter,
                    contentScale = ContentScale.Crop,
                )

                rating?.takeIf { it > 0f }?.let { score ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.72f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(11.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", score),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.62f))
                        .clickable { onFavoriteToggle(!game.isFavorite) }
                        .padding(5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.favorites),
                        tint = if (game.isFavorite) Color(0xFFFF5252) else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            Text(
                text = game.displayName(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}
