package come.asg.ominoy.app.mobile.feature.main

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Scale is relative to cover-fit at 0/90/180/270 degrees; offsets are viewport pixels. */
internal data class CropTransform(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotationDegrees: Float = 0f,
)

/** Mutable hot-path state; the revision is read only inside graphicsLayer, not by composition. */
@Stable
internal class CropTransformState(initial: CropTransform = CropTransform()) {
    private val revisionState = mutableIntStateOf(0)

    var scale: Float = initial.scale
        private set
    var offsetX: Float = initial.offsetX
        private set
    var offsetY: Float = initial.offsetY
        private set
    var rotationDegrees: Float = initial.rotationDegrees
        private set

    val graphicsRevision: Int
        get() = revisionState.intValue

    fun set(scale: Float, offsetX: Float, offsetY: Float, rotationDegrees: Float) {
        this.scale = scale
        this.offsetX = offsetX
        this.offsetY = offsetY
        this.rotationDegrees = rotationDegrees
        revisionState.intValue += 1
    }

    fun snapshot(): CropTransform = CropTransform(scale, offsetX, offsetY, rotationDegrees)
}

/**
 * Clamp the transform so every corner of the square aperture remains inside the rotated image.
 * This mutates the supplied state without creating an object, making it suitable for pointer and
 * animation-frame updates.
 */
internal fun clampCropTransformInto(
    state: CropTransformState,
    sourceWidth: Int,
    sourceHeight: Int,
    viewportSizePx: Float,
    requestedScale: Float,
    requestedOffsetX: Float,
    requestedOffsetY: Float,
    requestedRotationDegrees: Float,
    maxScale: Float = MAX_CROP_ZOOM,
    edgeResistancePx: Float = 0f,
): Boolean {
    val width = max(1, sourceWidth).toFloat()
    val height = max(1, sourceHeight).toFloat()
    val viewport = if (viewportSizePx.isFinite()) max(1f, viewportSizePx) else 1f
    val rotation = if (requestedRotationDegrees.isFinite()) requestedRotationDegrees else 0f
    val radians = Math.toRadians(rotation.toDouble())
    val cosine = cos(radians).toFloat()
    val sine = sin(radians).toFloat()
    val absCosine = abs(cosine)
    val absSine = abs(sine)
    val minScale = max(1f, absCosine + absSine)
    val safeMaxScale = if (maxScale.isFinite()) max(minScale, maxScale) else max(minScale, MAX_CROP_ZOOM)
    val safeRequestedScale = if (requestedScale.isFinite()) requestedScale else minScale
    val scale = safeRequestedScale.coerceIn(minScale, safeMaxScale)
    val minSide = max(1f, min(width, height))
    val baseScale = viewport / minSide

    // In image-local coordinates, the square's rotated half-extent is equal on both axes.
    val cropCornerExtent = viewport * 0.5f * (absCosine + absSine)
    val imageHalfWidth = width * baseScale * scale * 0.5f
    val imageHalfHeight = height * baseScale * scale * 0.5f
    val localPanLimitX = max(0f, imageHalfWidth - cropCornerExtent)
    val localPanLimitY = max(0f, imageHalfHeight - cropCornerExtent)
    val offsetX = if (requestedOffsetX.isFinite()) requestedOffsetX else 0f
    val offsetY = if (requestedOffsetY.isFinite()) requestedOffsetY else 0f

    // Translation is applied in viewport coordinates, so rotate it back before clamping.
    val localPanX = cosine * offsetX + sine * offsetY
    val localPanY = -sine * offsetX + cosine * offsetY
    val clampedLocalX = localPanX.coerceIn(-localPanLimitX, localPanLimitX)
    val clampedLocalY = localPanY.coerceIn(-localPanLimitY, localPanLimitY)
    val resistance = if (edgeResistancePx.isFinite()) max(0f, edgeResistancePx) else 0f
    val didResist = resistance > 0f &&
        ((localPanX > localPanLimitX && localPanLimitX > 0f) ||
            (localPanX < -localPanLimitX && localPanLimitX > 0f) ||
            (localPanY > localPanLimitY && localPanLimitY > 0f) ||
            (localPanY < -localPanLimitY && localPanLimitY > 0f))
    val resistedLocalX = when {
        resistance <= 0f || localPanLimitX <= 0f -> clampedLocalX
        localPanX > localPanLimitX -> localPanLimitX - min(localPanLimitX, resistance)
        localPanX < -localPanLimitX -> -localPanLimitX + min(localPanLimitX, resistance)
        else -> clampedLocalX
    }
    val resistedLocalY = when {
        resistance <= 0f || localPanLimitY <= 0f -> clampedLocalY
        localPanY > localPanLimitY -> localPanLimitY - min(localPanLimitY, resistance)
        localPanY < -localPanLimitY -> -localPanLimitY + min(localPanLimitY, resistance)
        else -> clampedLocalY
    }
    val clampedOffsetX = cosine * resistedLocalX - sine * resistedLocalY
    val clampedOffsetY = sine * resistedLocalX + cosine * resistedLocalY

    state.set(scale, clampedOffsetX, clampedOffsetY, rotation)
    return didResist
}

/** Pure wrapper for unit tests and non-interactive target calculations. */
internal fun clampCropTransform(
    sourceWidth: Int,
    sourceHeight: Int,
    viewportSizePx: Float,
    requested: CropTransform,
    maxScale: Float = MAX_CROP_ZOOM,
): CropTransform {
    val state = CropTransformState()
    clampCropTransformInto(
        state = state,
        sourceWidth = sourceWidth,
        sourceHeight = sourceHeight,
        viewportSizePx = viewportSizePx,
        requestedScale = requested.scale,
        requestedOffsetX = requested.offsetX,
        requestedOffsetY = requested.offsetY,
        requestedRotationDegrees = requested.rotationDegrees,
        maxScale = maxScale,
    )
    return state.snapshot()
}

/** Move a resisted transform the last few pixels to the nearest exact, still-covered boundary. */
internal fun settleCropTransformToNearestEdge(
    sourceWidth: Int,
    sourceHeight: Int,
    viewportSizePx: Float,
    current: CropTransform,
    settleDistancePx: Float = 6f,
): CropTransform {
    val width = max(1, sourceWidth).toFloat()
    val height = max(1, sourceHeight).toFloat()
    val viewport = if (viewportSizePx.isFinite()) max(1f, viewportSizePx) else 1f
    val radians = Math.toRadians(current.rotationDegrees.toDouble())
    val cosine = cos(radians).toFloat()
    val sine = sin(radians).toFloat()
    val absCosine = abs(cosine)
    val absSine = abs(sine)
    val minScale = max(1f, absCosine + absSine)
    val scale = max(minScale, current.scale)
    val cropCornerExtent = viewport * 0.5f * (absCosine + absSine)
    val baseScale = viewport / max(1f, min(width, height))
    val panLimitX = max(0f, width * baseScale * scale * 0.5f - cropCornerExtent)
    val panLimitY = max(0f, height * baseScale * scale * 0.5f - cropCornerExtent)
    val localPanX = cosine * current.offsetX + sine * current.offsetY
    val localPanY = -sine * current.offsetX + cosine * current.offsetY
    val settleDistance = if (settleDistancePx.isFinite()) max(0f, settleDistancePx) else 0f
    val targetLocalX = when {
        localPanX > 0f && panLimitX - abs(localPanX) <= settleDistance -> panLimitX
        localPanX < 0f && panLimitX - abs(localPanX) <= settleDistance -> -panLimitX
        else -> localPanX
    }
    val targetLocalY = when {
        localPanY > 0f && panLimitY - abs(localPanY) <= settleDistance -> panLimitY
        localPanY < 0f && panLimitY - abs(localPanY) <= settleDistance -> -panLimitY
        else -> localPanY
    }
    return clampCropTransform(
        sourceWidth = sourceWidth,
        sourceHeight = sourceHeight,
        viewportSizePx = viewport,
        requested = CropTransform(
            scale = scale,
            offsetX = cosine * targetLocalX - sine * targetLocalY,
            offsetY = sine * targetLocalX + cosine * targetLocalY,
            rotationDegrees = current.rotationDegrees,
        ),
    )
}

/** Map a point relative to the crop aperture's center back into original source-image pixels. */
internal fun mapViewportPointToSource(
    sourceWidth: Int,
    sourceHeight: Int,
    viewportSizePx: Float,
    transform: CropTransform,
    viewportX: Float,
    viewportY: Float,
): Offset = mapViewportPointToSource(
    sourceWidth = sourceWidth,
    sourceHeight = sourceHeight,
    viewportSizePx = viewportSizePx,
    scale = transform.scale,
    offsetX = transform.offsetX,
    offsetY = transform.offsetY,
    rotationDegrees = transform.rotationDegrees,
    viewportX = viewportX,
    viewportY = viewportY,
)

private fun mapViewportPointToSource(
    sourceWidth: Int,
    sourceHeight: Int,
    viewportSizePx: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotationDegrees: Float,
    viewportX: Float,
    viewportY: Float,
): Offset {
    val width = max(1, sourceWidth).toFloat()
    val height = max(1, sourceHeight).toFloat()
    val viewport = if (viewportSizePx.isFinite()) max(1f, viewportSizePx) else 1f
    val radians = Math.toRadians(rotationDegrees.toDouble())
    val cosine = cos(radians).toFloat()
    val sine = sin(radians).toFloat()
    val scalePxPerSourcePixel = viewport / max(1f, min(width, height)) * max(0.0001f, scale)
    val translatedX = viewportX - offsetX
    val translatedY = viewportY - offsetY
    val localX = cosine * translatedX + sine * translatedY
    val localY = -sine * translatedX + cosine * translatedY
    return Offset(
        x = width * 0.5f + localX / scalePxPerSourcePixel,
        y = height * 0.5f + localY / scalePxPerSourcePixel,
    )
}

/** Apply a zoom/rotate/pan gesture while keeping the source point under the pinch centroid fixed. */
internal fun applyCropGestureInto(
    state: CropTransformState,
    sourceWidth: Int,
    sourceHeight: Int,
    viewportSizePx: Float,
    focalStartX: Float,
    focalStartY: Float,
    focalEndX: Float,
    focalEndY: Float,
    zoomChange: Float,
    rotationChangeDegrees: Float,
    maxScale: Float = MAX_CROP_ZOOM,
    edgeResistancePx: Float = 0f,
): Boolean {
    val width = max(1, sourceWidth).toFloat()
    val height = max(1, sourceHeight).toFloat()
    val viewport = if (viewportSizePx.isFinite()) max(1f, viewportSizePx) else 1f
    val oldScale = state.scale
    val oldOffsetX = state.offsetX
    val oldOffsetY = state.offsetY
    val oldRotation = state.rotationDegrees
    val focalSource = mapViewportPointToSource(
        sourceWidth = sourceWidth,
        sourceHeight = sourceHeight,
        viewportSizePx = viewport,
        scale = oldScale,
        offsetX = oldOffsetX,
        offsetY = oldOffsetY,
        rotationDegrees = oldRotation,
        viewportX = focalStartX,
        viewportY = focalStartY,
    )

    val nextRotation = oldRotation + if (rotationChangeDegrees.isFinite()) rotationChangeDegrees else 0f
    val radians = Math.toRadians(nextRotation.toDouble())
    val cosine = cos(radians).toFloat()
    val sine = sin(radians).toFloat()
    val minScale = max(1f, abs(cosine) + abs(sine))
    val safeZoomChange = if (zoomChange.isFinite() && zoomChange > 0f) zoomChange else 1f
    val safeMaxScale = if (maxScale.isFinite()) max(minScale, maxScale) else max(minScale, MAX_CROP_ZOOM)
    val nextScale = (oldScale * safeZoomChange).coerceIn(minScale, safeMaxScale)
    val effectiveScale = viewport / max(1f, min(width, height)) * nextScale
    val sourceDeltaX = focalSource.x - width * 0.5f
    val sourceDeltaY = focalSource.y - height * 0.5f
    val rotatedSourceX = cosine * sourceDeltaX - sine * sourceDeltaY
    val rotatedSourceY = sine * sourceDeltaX + cosine * sourceDeltaY
    val nextOffsetX = focalEndX - rotatedSourceX * effectiveScale
    val nextOffsetY = focalEndY - rotatedSourceY * effectiveScale

    return clampCropTransformInto(
        state = state,
        sourceWidth = sourceWidth,
        sourceHeight = sourceHeight,
        viewportSizePx = viewport,
        requestedScale = nextScale,
        requestedOffsetX = nextOffsetX,
        requestedOffsetY = nextOffsetY,
        requestedRotationDegrees = nextRotation,
        maxScale = safeMaxScale,
        edgeResistancePx = edgeResistancePx,
    )
}

/** Pure wrapper used by tests; the editor uses the in-place version above. */
internal fun applyCropGesture(
    current: CropTransform,
    sourceWidth: Int,
    sourceHeight: Int,
    viewportSizePx: Float,
    focalStartX: Float,
    focalStartY: Float,
    focalEndX: Float,
    focalEndY: Float,
    zoomChange: Float,
    rotationChangeDegrees: Float,
    maxScale: Float = MAX_CROP_ZOOM,
): CropTransform {
    val state = CropTransformState(current)
    applyCropGestureInto(
        state = state,
        sourceWidth = sourceWidth,
        sourceHeight = sourceHeight,
        viewportSizePx = viewportSizePx,
        focalStartX = focalStartX,
        focalStartY = focalStartY,
        focalEndX = focalEndX,
        focalEndY = focalEndY,
        zoomChange = zoomChange,
        rotationChangeDegrees = rotationChangeDegrees,
        maxScale = maxScale,
        edgeResistancePx = 0f,
    )
    return state.snapshot()
}

internal const val MAX_CROP_ZOOM = 6f
