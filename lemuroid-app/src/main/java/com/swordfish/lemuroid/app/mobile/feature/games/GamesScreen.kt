package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameListRow
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun GamesScreen(
    viewModel: GamesViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit
) {
    val games = viewModel.games.collectAsLazyPagingItems()

    if (games.itemCount == 0) {
        LemuroidEmptyView()
        return
    }

    LazyColumn {
        items(games.itemCount, key = { games[it]?.id ?: -1 }) { index ->
            val game = games[index] ?: return@items

            LemuroidGameListRow(
                game = game,
                onClick = { onGameClick(game) },
                onLongClick = { onGameLongClick(game) },
                onFavoriteToggle = { isFavorite -> onGameFavoriteToggle(game, isFavorite) }
            )
        }
    }
}
