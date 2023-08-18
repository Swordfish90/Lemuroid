package com.swordfish.lemuroid.app.mobile.feature.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.mobile.feature.main.MainViewModel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameListRow
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidTopAppBarContainer
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidTopBarActions
import com.swordfish.lemuroid.app.shared.GameInteractor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel,
    gameInteractor: GameInteractor,
    mainUIState: MainViewModel.UiState
) {
    val context = LocalContext.current
    val query = viewModel.queryString.collectAsState()
    val games = viewModel.searchResults.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            LemuroidTopAppBarContainer(mainUIState.operationInProgress) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        query = query.value,
                        onQueryChange = { viewModel.queryString.value = it },
                        onSearch = { },
                        active = false,
                        onActiveChange = { },
                    ) { }

                    LemuroidTopBarActions(
                        MainRoute.SEARCH,
                        navController,
                        context = context,
                        saveSyncEnabled = mainUIState.saveSyncEnabled
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(games.itemCount, key = { games[it]?.id ?: -1 }) { index ->
                val game = games[index] ?: return@items

                LemuroidGameListRow(
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
