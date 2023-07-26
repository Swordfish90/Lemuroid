package com.swordfish.lemuroid.app.utils.android

import androidx.annotation.ArrayRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

@Composable
@ReadOnlyComposable
fun stringListResource(@ArrayRes id: Int): List<String> {
    return LocalContext.current.resources
        .getStringArray(id)
        .toList()
}
