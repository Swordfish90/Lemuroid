package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.swordfish.lemuroid.app.shared.covers.CoverAspectRatio
import com.swordfish.lemuroid.app.shared.covers.CoverUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun LemuroidGameImage(
    modifier: Modifier = Modifier,
    game: Game,
    frameAspectRatio: Float? = null,
    onAspectRatioChanged: (Float) -> Unit = {},
) {
    var aspectRatio by
        remember(game.systemId, game.displayCoverUrl()) {
            mutableFloatStateOf(CoverAspectRatio.forSystem(game.systemId))
        }
    val fallbackDrawable =
        remember(game) {
            CoverUtils.getFallbackDrawable(game)
        }

    val fallbackPainter = rememberDrawablePainter(drawable = fallbackDrawable)

    AsyncImage(
        model =
            ImageRequest.Builder(LocalContext.current)
                .data(game.displayCoverUrl())
                .build(),
        contentDescription = game.displayName(),
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(frameAspectRatio ?: aspectRatio),
        fallback = fallbackPainter,
        error = fallbackPainter,
        contentScale = ContentScale.Fit,
        onSuccess = { state ->
            aspectRatio =
                CoverAspectRatio.forSystem(
                    game.systemId,
                    state.result.drawable.intrinsicWidth,
                    state.result.drawable.intrinsicHeight,
                )
            onAspectRatioChanged(aspectRatio)
        },
    )
}
