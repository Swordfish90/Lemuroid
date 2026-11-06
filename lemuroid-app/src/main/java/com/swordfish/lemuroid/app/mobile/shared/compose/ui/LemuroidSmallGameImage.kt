package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun LemuroidSmallGameImage(
    modifier: Modifier = Modifier,
    game: Game,
) {
    LemuroidGameImage(
        game = game,
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    )
}
