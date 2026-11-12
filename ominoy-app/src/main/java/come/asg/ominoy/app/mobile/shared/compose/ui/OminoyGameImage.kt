package come.asg.ominoy.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import come.asg.ominoy.app.shared.covers.CoverUtils
import come.asg.ominoy.lib.library.db.entity.Game

@Composable
fun OminoyGameImage(
    modifier: Modifier = Modifier,
    game: Game,
) {
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
        placeholder = fallbackPainter,
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1.0f),
        fallback = fallbackPainter,
        error = fallbackPainter,
        contentScale = ContentScale.Crop,
    )
}
