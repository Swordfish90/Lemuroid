package com.swordfish.lemuroid.app.mobile.feature.game

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.platform.app.InstrumentationRegistry
import com.swordfish.lemuroid.app.mobile.shared.compose.TouchControlsAppearanceEditor
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Appearance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TouchControlAppearanceTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun opacityDoesNotChangeScale() {
        var appearance by mutableStateOf(Appearance(scale = 0.8f))
        compose.setContent { MaterialTheme { TouchControlsAppearanceEditor(appearance, { appearance = it }) } }
        val label =
            InstrumentationRegistry.getInstrumentation().targetContext.getString(
                R.string.touch_customize_opacity,
            )
        compose.onNodeWithContentDescription(label).performSemanticsAction(SemanticsActions.SetProgress) { it(0.4f) }
        compose.runOnIdle {
            assertEquals(0.4f, appearance.opacity, 0.001f)
            assertEquals(0.8f, appearance.scale)
        }
    }

    @Test
    fun backgroundStyleDoesNotChangeOpacity() {
        var appearance by mutableStateOf(Appearance(opacity = 0.6f))
        compose.setContent { MaterialTheme { TouchControlsAppearanceEditor(appearance, { appearance = it }) } }
        val label =
            InstrumentationRegistry.getInstrumentation().targetContext.getString(
                R.string.touch_customize_transparent_background,
            )
        compose.onNodeWithContentDescription(label).performClick()
        compose.runOnIdle {
            assertTrue(appearance.transparentBackground)
            assertEquals(0.6f, appearance.opacity)
        }
    }
}
