package com.swordfish.lemuroid.app.mobile.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.GameListEntry
import com.swordfish.lemuroid.app.shared.GameInteractor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    gameInteractor: GameInteractor
) {
    val query = viewModel.queryString.collectAsState()
    val games = viewModel.searchResults.collectAsLazyPagingItems()

    AppTheme {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp,
                shadowElevation = 1.dp
            ) {
                // TODO COMPOSE FIX SEARCHBAR
                SearchBar(
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    query = query.value,
                    onQueryChange = { viewModel.queryString.value = it },
                    onSearch = { },
                    active = false,
                    onActiveChange = { },
                ) { }
            }
            LazyColumn {
                items(games.itemCount, key = { games[it]?.id ?: -1 }) { index ->
                    val game = games[index] ?: return@items

                    GameListEntry(
                        game = game,
                        onClick = { gameInteractor.onGamePlay(game) },
                        onFavoriteToggle = { isFavorite ->
                            gameInteractor.onFavoriteToggle(game, isFavorite)
                        }
                    )
                }
            }
        }
    }
}
