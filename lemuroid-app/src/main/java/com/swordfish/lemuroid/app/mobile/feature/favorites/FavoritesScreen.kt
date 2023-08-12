package com.swordfish.lemuroid.app.mobile.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.GameCard
import com.swordfish.lemuroid.app.shared.GameInteractor

// TODO COMPOSE... Get rid of GameInteractor leveraging activity
@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel, gameInteractor: GameInteractor) {
    val games = viewModel.favorites.collectAsLazyPagingItems()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(144.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(games.itemCount, key = { games[it]?.id ?: -1 }) { index ->
            val game = games[index] ?: return@items
            GameCard(
                game = game
            ) { gameInteractor.onGamePlay(game) }
        }
    }
}
