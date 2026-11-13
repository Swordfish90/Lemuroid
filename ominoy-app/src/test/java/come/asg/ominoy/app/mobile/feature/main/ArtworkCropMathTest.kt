package come.asg.ominoy.app.mobile.feature.main

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class ArtworkCropMathTest {
    @Test
    fun minimumScaleCoversSquareAtArbitraryRotation() {
        val transform =
            clampCropTransform(
                sourceWidth = 400,
                sourceHeight = 800,
                viewportSizePx = 300f,
                requested = CropTransform(scale = 0.2f, rotationDegrees = 45f),
            )

        assertEquals(sqrt(2f), transform.scale, 0.0001f)
        assertEquals(0f, transform.offsetX, 0.001f)
        assertEquals(0f, transform.offsetY, 0.001f)
        assertCropCornersStayInside(400, 800, 300f, transform)
    }

    @Test
    fun clampsScaleAndPanWithoutExposingBackground() {
        val transform =
            clampCropTransform(
                sourceWidth = 1600,
                sourceHeight = 900,
                viewportSizePx = 320f,
                requested = CropTransform(scale = 20f, offsetX = 50_000f, offsetY = -50_000f),
            )

        assertEquals(MAX_CROP_ZOOM, transform.scale, 0.0001f)
        assertCropCornersStayInside(1600, 900, 320f, transform)
    }

    @Test
    fun focalPointStaysUnderFingersWhileZoomingAndRotating() {
        val width = 1600
        val height = 900
        val viewport = 320f
        val initial =
            clampCropTransform(
                width,
                height,
                viewport,
                CropTransform(scale = 2f, offsetX = 0f, offsetY = 0f, rotationDegrees = 20f),
            )
        val startX = 12f
        val startY = -9f
        val endX = 18f
        val endY = -5f
        val sourcePoint = mapViewportPointToSource(width, height, viewport, initial, startX, startY)

        val updated =
            applyCropGesture(
                current = initial,
                sourceWidth = width,
                sourceHeight = height,
                viewportSizePx = viewport,
                focalStartX = startX,
                focalStartY = startY,
                focalEndX = endX,
                focalEndY = endY,
                zoomChange = 1.35f,
                rotationChangeDegrees = 12f,
            )
        val mappedBack = mapViewportPointToSource(width, height, viewport, updated, endX, endY)

        assertEquals(sourcePoint.x, mappedBack.x, 0.05f)
        assertEquals(sourcePoint.y, mappedBack.y, 0.05f)
        assertCropCornersStayInside(width, height, viewport, updated)
    }

    @Test
    fun ninetyDegreeRotationMapsCropCoordinatesBackToOriginalPixels() {
        val sourcePoint =
            mapViewportPointToSource(
                sourceWidth = 400,
                sourceHeight = 800,
                viewportSizePx = 200f,
                transform = CropTransform(rotationDegrees = 90f),
                viewportX = 100f,
                viewportY = 0f,
            )

        assertEquals(200f, sourcePoint.x, 0.01f)
        assertEquals(200f, sourcePoint.y, 0.01f)
    }

    @Test
    fun edgeResistanceRemainsCoveredAndCanSettleToTheExactLimit() {
        val width = 1600
        val height = 900
        val viewport = 320f
        val state = CropTransformState(CropTransform(scale = 2f))
        val resisted =
            applyCropGestureInto(
                state = state,
                sourceWidth = width,
                sourceHeight = height,
                viewportSizePx = viewport,
                focalStartX = 0f,
                focalStartY = 0f,
                focalEndX = 1_000f,
                focalEndY = 0f,
                zoomChange = 1f,
                rotationChangeDegrees = 0f,
                edgeResistancePx = 5f,
            )
        val resistedTransform = state.snapshot()
        val settledTransform = settleCropTransformToNearestEdge(width, height, viewport, resistedTransform)

        assertTrue(resisted)
        assertCropCornersStayInside(width, height, viewport, resistedTransform)
        assertCropCornersStayInside(width, height, viewport, settledTransform)
        assertEquals(5f, settledTransform.offsetX - resistedTransform.offsetX, 0.05f)
    }

    private fun assertCropCornersStayInside(
        sourceWidth: Int,
        sourceHeight: Int,
        viewport: Float,
        transform: CropTransform,
    ) {
        val half = viewport * 0.5f
        val corners =
            arrayOf(
                Offset(-half, -half),
                Offset(half, -half),
                Offset(half, half),
                Offset(-half, half),
            )
        val epsilon = 0.05f
        for (corner in corners) {
            val point =
                mapViewportPointToSource(
                    sourceWidth,
                    sourceHeight,
                    viewport,
                    transform,
                    corner.x,
                    corner.y,
                )
            assertTrue("x=${point.x} outside source", point.x >= -epsilon && point.x <= sourceWidth + epsilon)
            assertTrue("y=${point.y} outside source", point.y >= -epsilon && point.y <= sourceHeight + epsilon)
        }
    }
}
