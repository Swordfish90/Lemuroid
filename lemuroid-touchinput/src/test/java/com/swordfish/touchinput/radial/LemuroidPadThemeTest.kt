package com.swordfish.touchinput.radial

import org.junit.Assert.assertEquals
import org.junit.Test

class LemuroidPadThemeTest {
    @Test
    fun `default theme keeps the existing button colors`() {
        val theme = LemuroidPadTheme()
        assertEquals(0.5f, theme.foregroundFill(false).alpha, 0.01f)
        assertEquals(0f, theme.icons(false).red, 0.01f)
    }

    @Test
    fun `transparent backgrounds keep icons visible in both states`() {
        val theme = LemuroidPadTheme(transparentBackground = true)
        for (pressed in listOf(false, true)) {
            assertEquals(0.2f, theme.foregroundFill(pressed).alpha, 0.01f)
            assertEquals(1f, theme.icons(pressed).red, 0.01f)
            assertEquals(0.5f, theme.icons(pressed).alpha, 0.01f)
        }
    }
}
