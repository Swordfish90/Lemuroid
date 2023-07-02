package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo

@Composable
fun SystemImage(system: MetaSystemInfo) {
    Image(
        painter = painterResource(id = system.metaSystem.imageResId),
        contentDescription = stringResource(id = system.metaSystem.titleResId),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.0f),
        contentScale = ContentScale.Inside
    )
}
