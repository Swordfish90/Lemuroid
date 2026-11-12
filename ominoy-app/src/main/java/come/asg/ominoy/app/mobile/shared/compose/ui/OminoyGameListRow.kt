package come.asg.ominoy.app.mobile.shared.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import come.asg.ominoy.lib.library.db.entity.Game

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OminoyGameListRow(
    modifier: Modifier = Modifier,
    game: Game,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onInfoClick: (() -> Unit)? = null,
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
            OminoySmallGameImage(
                modifier =
                    Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .align(Alignment.CenterVertically),
                game = game,
            )
            OminoyGameTexts(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                game = game,
            )
            Box(
                modifier =
                    Modifier
                        .width(if (onInfoClick != null) 80.dp else 40.dp)
                        .height(40.dp)
                        .align(Alignment.CenterVertically),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onInfoClick != null) {
                        IconButton(onClick = onInfoClick) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = "${game.displayName()} play statistics",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    FavoriteToggle(isToggled = game.isFavorite, onFavoriteToggle = onFavoriteToggle)
                }
            }
        }
    }
}
