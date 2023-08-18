package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameListRow
import com.swordfish.lemuroid.app.shared.GameInteractor

@Composable
fun GamesScreen(
    viewModel: GamesViewModel,
    gameInteractor: GameInteractor
) {
    val games = viewModel.games.collectAsLazyPagingItems()

    LazyColumn {
        items(games.itemCount, key = { games[it]?.id ?: -1 }) { index ->
            val game = games[index] ?: return@items

            LemuroidGameListRow(
                game = game,
                onClick = { gameInteractor.onGamePlay(game) },
                onFavoriteToggle = { isFavorite -> gameInteractor.onFavoriteToggle(game, isFavorite) }
            )
        }
    }
}
