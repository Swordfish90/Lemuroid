package come.asg.ominoy.app.mobile.feature.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import come.asg.ominoy.R
import come.asg.ominoy.app.mobile.shared.compose.ui.OminoyEmptyView
import come.asg.ominoy.app.mobile.shared.compose.ui.OminoyGameListRow
import come.asg.ominoy.lib.library.db.entity.Game

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel,
    searchQuery: String,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
    onResetSearchQuery: () -> Unit,
) {
    val searchState = viewModel.searchState.collectAsState(SearchViewModel.UIState.Idle)
    val searchGames = viewModel.searchResults.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        onResetSearchQuery()
    }

    LaunchedEffect(key1 = searchQuery) {
        viewModel.queryString.value = searchQuery
    }

    AnimatedContent(
        targetState = searchState.value,
        label = "SearchContent",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { state ->
        when {
            state == SearchViewModel.UIState.Idle -> {
                SearchEmptyView(modifier, stringResource(R.string.game_page_search_suggestion))
            }

            state == SearchViewModel.UIState.Loading -> {
                SearchLoadingView(modifier)
            }

            state == SearchViewModel.UIState.Ready && searchGames.itemCount == 0 -> {
                SearchEmptyView(modifier, stringResource(id = R.string.empty_view_default))
            }

            else -> {
                SearchResultsView(
                    modifier,
                    searchGames,
                    onGameClick,
                    onGameLongClick,
                    onGameFavoriteToggle,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsView(
    modifier: Modifier,
    games: LazyPagingItems<Game>,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 144.dp)) {
        items(games.itemCount, key = { games[it]?.id ?: it }) { index ->
            val game = games[index] ?: return@items

            OminoyGameListRow(
                modifier = Modifier.animateItem(),
                game = game,
                onClick = { onGameClick(game) },
                onLongClick = { onGameLongClick(game) },
                onFavoriteToggle = { isFavorite ->
                    onGameFavoriteToggle(game, isFavorite)
                },
            )
        }
    }
}

@Composable
private fun SearchEmptyView(
    modifier: Modifier,
    text: String,
) {
    OminoyEmptyView(
        modifier = modifier,
        text = text,
    )
}

@Composable
private fun SearchLoadingView(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
