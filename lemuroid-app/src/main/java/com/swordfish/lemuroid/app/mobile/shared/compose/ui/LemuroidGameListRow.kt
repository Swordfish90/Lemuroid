package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.lib.library.db.entity.Game

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LemuroidGameListRow(
    modifier: Modifier = Modifier,
    game: Game,
    rating: Float? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp,
                    end = 16.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LemuroidSmallGameImage(
            modifier = Modifier.size(40.dp),
            game = game,
        )
        LemuroidGameTexts(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            game = game,
            rating = rating,
        )
        FavoriteToggle(
            modifier = Modifier.size(40.dp),
            isToggled = game.isFavorite,
            onFavoriteToggle = onFavoriteToggle,
        )
    }
}
