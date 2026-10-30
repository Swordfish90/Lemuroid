package com.swordfish.touchinput.radial.settings

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

fun constrainTouchControlOffset(
    offset: Offset,
    bounds: Rect,
    viewport: Size,
): Offset =
    Offset(
        offset.x.coerceIn(-bounds.left, maxOf(-bounds.left, viewport.width - bounds.right)),
        offset.y.coerceIn(-bounds.top, maxOf(-bounds.top, viewport.height - bounds.bottom)),
    )
