package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
) {
    Surface(
        modifier =
            modifier
                .wrapContentHeight()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    start = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp,
                    end = 16.dp,
                ),
        ) {
            LemuroidSmallGameImage(
                modifier =
                    Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .align(Alignment.CenterVertically),
                game = game,
            )
            LemuroidGameTexts(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                game = game,
            )
            Box(
                modifier =
                    Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .align(Alignment.CenterVertically),
            ) {
                FavoriteToggle(
                    isToggled = game.isFavorite,
                    onFavoriteToggle = onFavoriteToggle,
                )
            }
        }
    }
}
