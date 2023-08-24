package com.swordfish.lemuroid.app.mobile.feature.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.mobile.feature.main.MainViewModel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameListRow
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidTopAppBarContainer
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidTopBarActions
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues
import com.swordfish.lemuroid.app.utils.android.compose.plus
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun SearchScreen(
    outerPadding: PaddingValues,
    navController: NavController,
    viewModel: SearchViewModel,
    mainUIState: MainViewModel.UiState,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit
) {
    val context = LocalContext.current
    val query = viewModel.queryString.collectAsState()
    val searchState = viewModel.searchState.collectAsState(SearchViewModel.UIState.Idle)
    val searchGames = viewModel.searchResults.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            LemuroidTopAppBarContainer(mainUIState.operationInProgress) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                    ) {
                        // Standard TextField has huge margins that can't be customized.
                        // We set the background to invisible and draw a surface behind
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            shape = RoundedCornerShape(100),
                            tonalElevation = 16.dp
                        ) { }
                        TextField(
                            value = query.value,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 8.dp, end = 8.dp),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            onValueChange = { viewModel.queryString.value = it },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    // TODO COMPOSE. Action have a slightly different color.
                    Box(modifier = Modifier.padding(4.dp)) {
                        LemuroidTopBarActions(
                            MainRoute.SEARCH,
                            navController,
                            context = context,
                            saveSyncEnabled = mainUIState.saveSyncEnabled
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val padding = outerPadding + innerPadding
        when {
            searchState.value == SearchViewModel.UIState.Idle -> { }
            searchState.value == SearchViewModel.UIState.Loading -> {
                SearchLoadingView(padding)
            }
            searchState.value == SearchViewModel.UIState.Ready && searchGames.itemCount == 0 -> {
                SearchEmptyView(padding)
            }
            else -> {
                SearchResultsView(
                    padding,
                    searchGames,
                    onGameClick,
                    onGameLongClick,
                    onGameFavoriteToggle
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsView(
    padding: MergedPaddingValues,
    games: LazyPagingItems<Game>,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit
) {
    LazyColumn(contentPadding = padding.asPaddingValues()) {
        items(games.itemCount, key = { games[it]?.id ?: -1 }) { index ->
            val game = games[index] ?: return@items

            LemuroidGameListRow(
                modifier = Modifier.animateItemPlacement(),
                game = game,
                onClick = { onGameClick(game) },
                onLongClick = { onGameLongClick(game) },
                onFavoriteToggle = { isFavorite ->
                    onGameFavoriteToggle(game, isFavorite)
                }
            )
        }
    }
}

@Composable
private fun SearchEmptyView(padding: MergedPaddingValues) {
    LemuroidEmptyView(modifier = Modifier.padding(padding.asPaddingValues()))
}

@Composable
private fun SearchLoadingView(padding: MergedPaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding.asPaddingValues()),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
