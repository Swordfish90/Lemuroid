package com.swordfish.lemuroid.app.mobile.feature.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Settings
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TouchControlGroupSizesTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun portraitViewportLeavesRoomForTheTallerGroup() {
        var tallerLeft by mutableStateOf(false)
        compose.setContent {
            ConstraintLayout(
                constraintSet = GameScreenLayout.buildConstraintSet(false, true),
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(Modifier.layoutId(GameScreenLayout.CONSTRAINTS_GAME_VIEW).testTag("viewport"))
                Box(
                    Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_PAD)
                        .height(if (tallerLeft) 160.dp else 80.dp).testTag("left"),
                )
                Box(
                    Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_PAD)
                        .height(if (tallerLeft) 80.dp else 160.dp).testTag("right"),
                )
                Box(Modifier.layoutId(GameScreenLayout.CONSTRAINTS_BOTTOM_CONTAINER))
                Box(Modifier.layoutId(GameScreenLayout.CONSTRAINTS_GAME_CONTAINER))
            }
        }
        for (leftIsTaller in listOf(false, true)) {
            compose.runOnIdle { tallerLeft = leftIsTaller }
            val left = compose.onNodeWithTag("left").getUnclippedBoundsInRoot()
            val right = compose.onNodeWithTag("right").getUnclippedBoundsInRoot()
            val viewport = compose.onNodeWithTag("viewport").getUnclippedBoundsInRoot()
            assertEquals(minOf(left.top.value, right.top.value), viewport.bottom.value, 0.5f)
        }
    }

    @Test
    fun changingLeftSizePreservesRightSize() {
        var settings by mutableStateOf(Settings())
        compose.setContent {
            MaterialTheme {
                TouchControlGroupSizesEditor(settings, { settings = it })
            }
        }
        val label =
            InstrumentationRegistry.getInstrumentation().targetContext.getString(
                R.string.touch_customize_left_scale,
            )
        compose.onNodeWithContentDescription(label)
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0.7f) }
        compose.runOnIdle {
            assertEquals(0.7f, settings.leftScale)
            assertEquals(1f, settings.rightScale)
        }
    }
}
