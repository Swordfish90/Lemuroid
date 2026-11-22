package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.swordfish.lemuroid.app.shared.covers.CoverUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun LemuroidSmallGameImage(
    modifier: Modifier = Modifier,
    game: Game,
) {
    val context = LocalContext.current
    val coverUrl = game.displayCoverUrl()

    val fallbackDrawable =
        remember(game.id, game.title) {
            CoverUtils.getFallbackDrawable(game)
        }

    val fallbackPainter = rememberDrawablePainter(fallbackDrawable)

    val imageRequest =
        remember(coverUrl) {
            ImageRequest.Builder(context)
                .data(coverUrl)
                .size(128)
                .crossfade(false)
                .build()
        }

    AsyncImage(
        model = imageRequest,
        contentDescription = game.displayName(),
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1.0f)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        fallback = fallbackPainter,
        error = fallbackPainter,
        contentScale = ContentScale.Crop,
    )
}
