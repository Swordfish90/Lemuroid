package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.GameListEntry
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun GamesScreen(
    games: LazyPagingItems<Game>,
    onGameClicked: (Game) -> Unit,
    onFavoriteToggle: (Game, Boolean) -> Unit
) {
    AppTheme {
        LazyColumn {
            items(games.itemCount, key = { games[it]?.id ?: -1 }) { index ->
                val game = games[index] ?: return@items

                GameListEntry(
                    game = game,
                    onClick = { onGameClicked(game) },
                    onFavoriteToggle = { onFavoriteToggle(game, it) }
                )
            }
        }
    }
}
