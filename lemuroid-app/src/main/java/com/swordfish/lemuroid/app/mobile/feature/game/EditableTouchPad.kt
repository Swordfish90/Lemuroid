package com.swordfish.lemuroid.app.mobile.feature.game

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.swordfish.touchinput.radial.settings.constrainTouchControlOffset
import kotlin.math.roundToInt

internal fun Modifier.editableTouchPad(
    editing: Boolean,
    offset: Offset,
    viewport: Size,
    onMoved: (Offset) -> Unit,
): Modifier =
    composed {
        var draft by remember(offset, viewport) { mutableStateOf(offset) }
        var bounds by remember(viewport) { mutableStateOf<Rect?>(null) }
        val placement = remember(viewport) { PadPlacement() }
        val currentOnMoved by rememberUpdatedState(onMoved)
        val currentOffset by rememberUpdatedState(offset)
        this
            .offset {
                val requested = Offset(draft.x * viewport.width, draft.y * viewport.height)
                val constrained = bounds?.let { constrainTouchControlOffset(requested, it, viewport) } ?: requested
                val result = IntOffset(constrained.x.roundToInt(), constrained.y.roundToInt())
                placement.translation = Offset(result.x.toFloat(), result.y.toFloat())
                result
            }
            .onGloballyPositioned {
                bounds = Rect(it.positionInRoot() - placement.translation, it.size.toSize())
            }
            .then(if (editing) Modifier.border(1.dp, MaterialTheme.colorScheme.primary) else Modifier)
            .pointerInput(editing, viewport) {
                if (!editing) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        draft =
                            Offset(placement.translation.x / viewport.width, placement.translation.y / viewport.height)
                    },
                    onDragEnd = {
                        currentOnMoved(draft)
                    },
                    onDragCancel = { draft = currentOffset },
                    onDrag = { change, delta ->
                        change.consume()
                        val requested = Offset(draft.x * viewport.width, draft.y * viewport.height) + delta
                        val constrained =
                            bounds?.let { constrainTouchControlOffset(requested, it, viewport) } ?: requested
                        draft = Offset(constrained.x / viewport.width, constrained.y / viewport.height)
                    },
                )
            }
    }

private class PadPlacement {
    var translation = Offset.Zero
}
