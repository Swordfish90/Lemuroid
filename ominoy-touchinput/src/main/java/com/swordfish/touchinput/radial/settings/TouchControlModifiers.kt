package com.swordfish.touchinput.radial.settings

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import gg.padkit.ids.Id
import kotlin.math.hypot

data class TouchControlEditorState(
    val isEditing: Boolean = false,
    val selectedControlKey: String? = null,
    val onSelect: (String) -> Unit = {},
    val onDoubleTap: (String) -> Unit = {},
    val onDrag: (String, Float, Float) -> Unit = { _, _, _ -> },
    val onDragEnd: (String, Float, Float) -> Unit = { _, _, _ -> },
)

val LocalTouchControlEditor =
    staticCompositionLocalOf { TouchControlEditorState() }

/**
 * Hidden controls always stay composed so they keep their slot in the radial layouts (removing one
 * collapses/reflows the pad and resizes the game viewport). Hiding is done in [touchControlTransform]
 * (alpha 0) and by dropping the control's input events, so this is always true.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun isTouchControlVisible(id: Id): Boolean = true

/** Scales the stick vector's magnitude (not x/y independently) so diagonals keep their angle. */
fun applyStickSensitivity(
    direction: Offset,
    sensitivity: Float,
): Offset {
    if (sensitivity == 1f) return direction
    val magnitude = hypot(direction.x, direction.y)
    if (magnitude == 0f) return direction
    val factor = (magnitude * sensitivity).coerceAtMost(1f) / magnitude
    return Offset(direction.x * factor, direction.y * factor)
}

private class ControlFrame {
    var bounds: Rect = Rect.Zero
    var root: IntSize = IntSize.Zero

    fun update(coordinates: LayoutCoordinates) {
        if (!coordinates.isAttached) return
        val a = coordinates.localToRoot(Offset.Zero)
        val b = coordinates.localToRoot(Offset(coordinates.size.width.toFloat(), coordinates.size.height.toFloat()))
        bounds = Rect(minOf(a.x, b.x), minOf(a.y, b.y), maxOf(a.x, b.x), maxOf(a.y, b.y))
        root = coordinates.findRootCoordinates().size
    }

    /** Limits a drag (in px) so the control's visual bounds cannot leave the screen. */
    fun clamp(
        dx: Float,
        dy: Float,
    ): Pair<Float, Float> {
        if (root == IntSize.Zero) return dx to dy
        val minX = if (bounds.left < 0f) 0f else -bounds.left
        val maxX = if (bounds.right > root.width) 0f else root.width - bounds.right
        val minY = if (bounds.top < 0f) 0f else -bounds.top
        val maxY = if (bounds.bottom > root.height) 0f else root.height - bounds.bottom
        return dx.coerceIn(minX, maxX) to dy.coerceIn(minY, maxY)
    }
}

/** Applies saved visual settings and exposes the control to the direct-manipulation editor. */
@Composable
fun Modifier.touchControlTransform(id: Id): Modifier {
    val editor = LocalTouchControlEditor.current
    val key = TouchControllerSettingsManager.controlKey(id)
    val settings =
        LocalTouchControllerSettings.current.controls[key]
            ?: TouchControllerSettingsManager.ControlSettings()
    val density = LocalDensity.current.density
    val onSelect by rememberUpdatedState(editor.onSelect)
    val onDoubleTap by rememberUpdatedState(editor.onDoubleTap)
    val onDrag by rememberUpdatedState(editor.onDrag)
    val onDragEnd by rememberUpdatedState(editor.onDragEnd)
    val isEditing = editor.isEditing
    val isSelected = editor.selectedControlKey == key
    val hidden = !settings.visible && TouchControllerSettingsManager.isHideable(key)
    val frame = remember { ControlFrame() }
    var liveDrag by remember(key) { mutableStateOf(Offset.Zero) }

    // Order matters: offset first so the touch target moves with the control, pointer input next so
    // drag deltas are in unscaled screen space, then the scale/alpha layer.
    var result: Modifier = this.offset(settings.offsetX.dp, settings.offsetY.dp)
    if (isEditing) {
        result =
            result
                .pointerInput(key, density) {
                    detectDragGestures(
                        onDragStart = { onSelect(key) },
                        onDragEnd = {
                            onDragEnd(key, liveDrag.x / density, liveDrag.y / density)
                            liveDrag = Offset.Zero
                        },
                        onDragCancel = { liveDrag = Offset.Zero },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val accumulated = liveDrag + dragAmount
                            val (dx, dy) = frame.clamp(accumulated.x, accumulated.y)
                            liveDrag = Offset(dx, dy)
                        },
                    )
                }
                .pointerInput(key) {
                    detectTapGestures(
                        onPress = { onSelect(key) },
                        onDoubleTap = {
                            onSelect(key)
                            onDoubleTap(key)
                        },
                    )
                }
    }

    return result
        .graphicsLayer {
            // Render the in-progress drag locally. The shared settings flow is updated only once
            // when the gesture ends, avoiding a recomposition for every pointer event.
            translationX = liveDrag.x
            translationY = liveDrag.y
            scaleX = settings.scale
            scaleY = settings.scale
            alpha =
                when {
                    hidden && isEditing -> 0.30f
                    hidden -> 0f
                    else -> settings.opacity
                }
            // Without this, alpha < 1 renders into an offscreen layer clipped to the control's bounds,
            // which cut multi-part controls (D-pad, face-button clusters) down to a fragment.
            compositingStrategy = CompositingStrategy.ModulateAlpha
        }
        .onGloballyPositioned { frame.update(it) }
        .drawWithContent {
            drawContent()
            if (isEditing && isSelected) {
                drawRoundRect(
                    color = Color(0xFFFFB547),
                    topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                    size =
                        Size(
                            (size.width - 2.dp.toPx()).coerceAtLeast(0f),
                            (size.height - 2.dp.toPx()).coerceAtLeast(0f),
                        ),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx() / settings.scale),
                )
            }
        }
}
