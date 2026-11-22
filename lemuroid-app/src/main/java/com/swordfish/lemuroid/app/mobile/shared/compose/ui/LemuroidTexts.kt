package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.utils.games.GameUtils
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.util.Locale

@Composable
fun LemuroidGameTexts(
    modifier: Modifier = Modifier,
    game: Game,
    rating: Float? = null,
    titleMaxLines: Int = 1,
    titleMinLines: Int = 1,
    subtitleTopSpacingDp: Int = 0,
    compact: Boolean = false,
) {
    val context = LocalContext.current
    val validRating = rating?.takeIf { it > 0f }

    if (validRating == null) {
        val subtitle =
            remember(game.id, game.developer) {
                GameUtils.getGameSubtitle(context, game)
            }
        LemuroidTexts(
            modifier = modifier,
            title = game.displayName(),
            subtitle = subtitle,
            titleMaxLines = titleMaxLines,
            titleMinLines = titleMinLines,
            subtitleTopSpacingDp = subtitleTopSpacingDp,
            compact = compact,
        )
    } else {
        val systemName =
            remember(game.systemId) {
                val resId = GameSystem.findById(game.systemId).shortTitleResId
                context.getString(resId)
            }
        val formattedRating = remember(validRating) {
            String.format(Locale.US, "%.1f", validRating)
        }
        val developer = game.developer?.takeIf { it.isNotBlank() }

        val titleStyle = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp, lineHeight = 21.sp)
        val subtitleStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, lineHeight = 16.sp)

        Column(
            modifier = modifier.padding(8.dp),
        ) {
            Text(
                text = game.displayName(),
                style = titleStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$systemName • ",
                    style = subtitleStyle,
                    maxLines = 1,
                )
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = formattedRating,
                    style = subtitleStyle,
                    maxLines = 1,
                )
                if (developer != null) {
                    Text(
                        text = " • $developer",
                        style = subtitleStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun LemuroidTexts(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    titleMaxLines: Int = 1,
    titleMinLines: Int = 1,
    subtitleTopSpacingDp: Int = 0,
    compact: Boolean = false,
) {
    val titleFontSize = if (compact) 13.sp else 16.sp
    val titleLineHeight = if (compact) 17.sp else 21.sp
    val subtitleFontSize = if (compact) 11.sp else 12.sp
    val subtitleLineHeight = if (compact) 14.sp else 16.sp

    Column(
        modifier = modifier.padding(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = titleFontSize,
                lineHeight = titleLineHeight,
            ),
            maxLines = titleMaxLines,
            minLines = titleMinLines,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitleTopSpacingDp > 0) {
            Spacer(modifier = Modifier.height(subtitleTopSpacingDp.dp))
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = subtitleFontSize,
                lineHeight = subtitleLineHeight,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
