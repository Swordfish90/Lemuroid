package com.swordfish.lemuroid.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
fun Int.pxToDp() = this.pxToDp(LocalDensity.current)

fun Int.pxToDp(density: Density) = with(density) { this@pxToDp.toDp() }
