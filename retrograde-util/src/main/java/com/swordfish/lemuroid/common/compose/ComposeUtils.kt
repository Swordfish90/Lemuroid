package com.swordfish.lemuroid.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun Int.pxToDp() = this.pxToDp(LocalDensity.current)

fun Int.pxToDp(density: Density) = with(density) { this@pxToDp.toDp() }

@Composable
fun Dp.textUnit(): TextUnit {
    val sizeInDp = this
    return with(LocalDensity.current) { sizeInDp.toSp() }
}
