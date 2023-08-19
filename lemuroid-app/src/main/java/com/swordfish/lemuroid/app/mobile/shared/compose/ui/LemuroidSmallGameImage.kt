package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun LemuroidSmallGameImage(modifier: Modifier = Modifier, game: Game) {
    val painter = rememberImagePainter(data = game.coverFrontUrl) {
        val fallbackDrawable = CoverLoader.getFallbackDrawable(game)
        fallback(fallbackDrawable)
        error(fallbackDrawable)
    }

    Surface(
        modifier = modifier,
        tonalElevation = 2.dp
    ) {
        Image(
            painter,
            game.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0f),
            contentScale = ContentScale.Crop
        )
    }
}
