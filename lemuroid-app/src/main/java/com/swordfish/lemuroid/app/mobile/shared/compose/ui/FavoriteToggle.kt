package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R

@Composable
fun FavoriteToggle(
    isToggled: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
) {
    IconToggleButton(
        checked = isToggled,
        onCheckedChange = onFavoriteToggle,
        modifier = Modifier.fillMaxSize(),
    ) {
        val image =
            if (isToggled) {
                Icons.Default.Favorite
            } else {
                Icons.Default.FavoriteBorder
            }
        Icon(
            image,
            contentDescription = stringResource(R.string.favorites),
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp),
        )
    }
}
