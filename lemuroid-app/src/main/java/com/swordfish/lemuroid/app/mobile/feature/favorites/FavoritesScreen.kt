package com.swordfish.lemuroid.app.mobile.feature.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.swordfish.lemuroid.app.mobile.shared.compose.layout.LazyWeightedVerticalGrid
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameCard
import com.swordfish.lemuroid.lib.library.CoverArtPolicy
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
) {
    val games = viewModel.favorites.collectAsLazyPagingItems()

    if (games.itemCount == 0) {
        LemuroidEmptyView(modifier = modifier)
        return
    }

    LazyWeightedVerticalGrid(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        spacing = 16.dp,
    ) {
        items(
            count = games.itemCount,
            key = games.itemKey { it.id },
            itemRowWeight = { index -> games.peek(index)?.let(CoverArtPolicy::aspectRatio) ?: 1f },
        ) { index ->
            val game = games[index]
            if (game == null) {
                Box(Modifier.fillMaxWidth().aspectRatio(1f))
                return@items
            }
            LemuroidGameCard(
                modifier = Modifier.animateItem(),
                game = game,
                onClick = { onGameClick(game) },
                onLongClick = { onGameLongClick(game) },
            )
        }
    }
}
