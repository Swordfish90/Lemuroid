package com.swordfish.lemuroid.app.mobile.shared.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

/** Slides the full-height top bar out of view while releasing its space for content. */
@Stable
class SlideAwayTopBarScrollBehavior internal constructor() {
    private var height by mutableFloatStateOf(0f)
    private var offset by mutableFloatStateOf(0f)

    val nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val previousOffset = offset
                offset = (offset + available.y).coerceIn(-height, 0f)
                return Offset(x = 0f, y = offset - previousOffset)
            }
        }

    fun reset() {
        offset = 0f
    }

    fun Modifier.slideAwayTopBar(): Modifier =
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            height = placeable.height.toFloat()
            offset = offset.coerceIn(-height, 0f)

            layout(
                width = placeable.width,
                height = (placeable.height + offset).roundToInt(),
            ) {
                placeable.placeRelative(0, offset.roundToInt())
            }
        }
}

@Composable
fun rememberSlideAwayTopBarScrollBehavior(): SlideAwayTopBarScrollBehavior = remember { SlideAwayTopBarScrollBehavior() }
