package com.swordfish.lemuroid.app.mobile.feature.favorites

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.GameCard
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun FavoritesScreen(
    games: LazyPagingItems<Game>,
    onGameClicked: (Game) -> Unit
) {
    AppTheme {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(144.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(games.itemCount, key = { games[it]!!.id }) { index ->
                val game = games[index]!!
                GameCard(game) { onGameClicked(game) }
            }
        }
    }
}
