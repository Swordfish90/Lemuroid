package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo

@Composable
fun LemuroidSystemImage(system: MetaSystemInfo) {
    Box(
        modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1.0f)
        .clip(MaterialTheme.shapes.medium)
        .background(Color(system.metaSystem.color()))
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = system.metaSystem.imageResId),
            contentDescription = stringResource(id = system.metaSystem.titleResId),
            contentScale = ContentScale.Inside
        )
    }
}
