package com.swordfish.lemuroid.app.mobile.feature.favorites

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.games.GameGridCard
import com.swordfish.lemuroid.app.mobile.feature.games.GameSortOrder
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameListRow
import com.swordfish.lemuroid.lib.library.db.entity.Game

private val GENRE_SPLIT_REGEX = Regex("[,|/•]")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel,
    searchQuery: String = "",
    sortOrder: GameSortOrder = GameSortOrder.NAME_ASC,
    isGridView: Boolean = true,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
) {
    LaunchedEffect(searchQuery) {
        viewModel.setSearchQuery(searchQuery)
    }

    LaunchedEffect(sortOrder) {
        viewModel.setSortOrder(sortOrder)
    }

    val allFavorites by viewModel.favorites.collectAsState()
    var selectedGenres by remember { mutableStateOf<Set<String>>(emptySet()) }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val availableGenres = remember(allFavorites) {
        allFavorites
            .flatMap { item ->
                item.genres.orEmpty()
                    .split(GENRE_SPLIT_REGEX)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }

    val filteredGames = remember(allFavorites, selectedGenres) {
        if (selectedGenres.isEmpty()) {
            allFavorites
        } else {
            val requiredLower = selectedGenres.map { it.lowercase() }
            allFavorites.filter { item ->
                val itemGenres = item.genres.orEmpty()
                    .split(GENRE_SPLIT_REGEX)
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() }
                    .toSet()
                requiredLower.all { req -> itemGenres.contains(req) }
            }
        }
    }

    var lastAppliedFilter by remember { mutableStateOf(Triple(sortOrder, selectedGenres, searchQuery)) }
    var pendingScrollOldIds by remember { mutableStateOf<List<Int>?>(null) }

    LaunchedEffect(sortOrder, selectedGenres, searchQuery) {
        val current = Triple(sortOrder, selectedGenres, searchQuery)
        if (current != lastAppliedFilter) {
            lastAppliedFilter = current
            pendingScrollOldIds = filteredGames.map { it.game.id }
        }
    }

    LaunchedEffect(filteredGames, pendingScrollOldIds, isGridView) {
        val oldIds = pendingScrollOldIds ?: return@LaunchedEffect
        val currentIds = filteredGames.map { it.game.id }
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

    if (allFavorites.isEmpty()) {
        LemuroidEmptyView()
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (availableGenres.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "fav_genre_all") {
                    FilterChip(
                        selected = selectedGenres.isEmpty(),
                        onClick = { selectedGenres = emptySet() },
                        label = { Text(text = stringResource(id = R.string.filter_genre_all)) },
                    )
                }
                items(
                    items = availableGenres,
                    key = { "fav_genre_$it" },
                ) { genre ->
                    val isSelected = selectedGenres.any { it.equals(genre, ignoreCase = true) }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedGenres = if (isSelected) {
                                selectedGenres.filterNot { it.equals(genre, ignoreCase = true) }.toSet()
                            } else {
                                selectedGenres + genre
                            }
                        },
                        label = { Text(text = genre) },
                    )
                }
            }
        }

        if (filteredGames.isEmpty()) {
            LemuroidEmptyView()
        } else if (isGridView) {
            LazyVerticalGrid(
                state = gridState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                columns = GridCells.Adaptive(112.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                gridItems(
                    items = filteredGames,
                    key = { it.game.id },
                    contentType = { "favorite_grid_card" },
                ) { item ->
                    val game = item.game
                    GameGridCard(
                        modifier = Modifier.animateItem(),
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
                    items = filteredGames,
                    key = { it.game.id },
                    contentType = { "favorite_list_row" },
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
