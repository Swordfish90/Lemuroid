package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R

@Composable
fun FavoriteToggle(isToggled: Boolean, onFavoriteToggle: (Boolean) -> Unit) {
    val drawableId = if (isToggled) {
        R.drawable.game_list_favorite_toggled
    } else {
        R.drawable.game_list_favorite_untoggled
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        onClick = { onFavoriteToggle(!isToggled) }
    ) {
        Image(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp),
            painter = painterResource(id = drawableId),
            contentDescription = "",
            contentScale = ContentScale.FillBounds
        )
    }
}
