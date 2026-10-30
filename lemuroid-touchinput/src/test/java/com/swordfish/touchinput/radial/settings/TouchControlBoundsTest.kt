package com.swordfish.touchinput.radial.settings

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Test

class TouchControlBoundsTest {
    @Test
    fun `movement within the viewport is unchanged`() {
        assertOffsetEquals(
            Offset(30f, -40f),
            constrainTouchControlOffset(Offset(30f, -40f), Rect(0f, 500f, 200f, 700f), Size(400f, 800f)),
        )
    }

    @Test
    fun `left group cannot leave the top or left edge`() {
        assertOffsetEquals(
            Offset(0f, -500f),
            constrainTouchControlOffset(Offset(-100f, -900f), Rect(0f, 500f, 200f, 700f), Size(400f, 800f)),
        )
    }

    @Test
    fun `right group cannot leave the bottom or right edge`() {
        assertOffsetEquals(
            Offset(0f, 100f),
            constrainTouchControlOffset(Offset(100f, 900f), Rect(200f, 500f, 400f, 700f), Size(400f, 800f)),
        )
    }

    @Test
    fun `a saved position is clamped after the viewport shrinks`() {
        assertOffsetEquals(
            Offset(-100f, -300f),
            constrainTouchControlOffset(Offset(0f, 0f), Rect(200f, 500f, 400f, 700f), Size(300f, 400f)),
        )
    }

    @Test
    fun `oversized controls stay anchored to the visible area`() {
        assertOffsetEquals(
            Offset(-20f, -30f),
            constrainTouchControlOffset(Offset(500f, 500f), Rect(20f, 30f, 620f, 930f), Size(400f, 800f)),
        )
    }

    private fun assertOffsetEquals(
        expected: Offset,
        actual: Offset,
    ) {
        assertEquals(expected.x, actual.x, 0.001f)
        assertEquals(expected.y, actual.y, 0.001f)
    }
}
