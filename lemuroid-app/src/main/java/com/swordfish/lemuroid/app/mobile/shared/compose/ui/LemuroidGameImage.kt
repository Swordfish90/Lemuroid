package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.swordfish.lemuroid.app.shared.covers.CoverUtils
import com.swordfish.lemuroid.lib.library.CoverArtPolicy
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun LemuroidGameImage(
    modifier: Modifier = Modifier,
    game: Game,
) {
    val context = LocalContext.current
    var failed by remember(game) {
        mutableStateOf(game.coverFrontUrl == null)
    }

    val imageModifier = modifier
        .fillMaxWidth()
        .aspectRatio(CoverArtPolicy.aspectRatio(game))

    if (failed) {
        val fallback = remember(context, game) {
            CoverUtils.getDetailedFallbackDrawable(context, game)
        }
        Image(
            painter = rememberDrawablePainter(drawable = fallback),
            contentDescription = game.title,
            modifier = imageModifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context).data(game.coverFrontUrl).build(),
            contentDescription = game.title,
            modifier = imageModifier,
            onError = { failed = true },
            contentScale = ContentScale.Crop,
        )
    }
}
