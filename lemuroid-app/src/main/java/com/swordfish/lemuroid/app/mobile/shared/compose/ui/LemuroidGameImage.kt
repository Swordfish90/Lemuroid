package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun LemuroidGameImage(game: Game) {
    val painter = rememberImagePainter(data = game.coverFrontUrl) {
        val fallbackDrawable = CoverLoader.getFallbackDrawable(game)
        fallback(fallbackDrawable)
        error(fallbackDrawable)
    }

    Image(
        painter,
        game.title,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.Crop
    )
}
