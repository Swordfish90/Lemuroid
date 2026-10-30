package com.swordfish.lemuroid.app.mobile.feature.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TouchControlDragTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun draggingPersistsThePositionOncePerGestureWithoutPressingButtons() {
        var offset by mutableStateOf(Offset.Zero)
        var viewport by mutableStateOf(Size(1f, 1f))
        var writes = 0
        var presses = 0
        var firstOffset = Offset.Zero
        compose.setContent {
            MaterialTheme {
                Box(
                    modifier =
                        Modifier.fillMaxSize().onSizeChanged {
                            viewport = Size(it.width.toFloat(), it.height.toFloat())
                        },
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Box(
                        modifier =
                            Modifier.size(100.dp)
                                .editableTouchPad(true, offset, viewport) {
                                    offset = it
                                    writes++
                                }
                                .testTag("pad")
                                .clickable { presses++ },
                    )
                }
            }
        }
        compose.onNodeWithTag("pad").performTouchInput {
            swipe(center, center + Offset(100f, -100f), durationMillis = 500)
        }
        compose.runOnIdle {
            assertTrue(offset.x > 0f)
            assertTrue(offset.y < 0f)
            assertEquals(1, writes)
            assertEquals(0, presses)
            firstOffset = offset
        }
        compose.onNodeWithTag("pad").performTouchInput {
            swipe(center, center + Offset(100f, -100f), durationMillis = 500)
        }
        compose.runOnIdle {
            assertEquals(2, writes)
            assertEquals(0, presses)
            assertTrue(offset.x > firstOffset.x)
            assertTrue(offset.y < firstOffset.y)
        }
    }

    @Test
    fun controlsDoNotMoveOutsideEditMode() {
        var viewport by mutableStateOf(Size(1f, 1f))
        var writes = 0
        compose.setContent {
            MaterialTheme {
                Box(
                    modifier =
                        Modifier.fillMaxSize().onSizeChanged {
                            viewport = Size(it.width.toFloat(), it.height.toFloat())
                        },
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Box(
                        Modifier.size(
                            100.dp,
                        ).editableTouchPad(false, Offset.Zero, viewport) { writes++ }.testTag("pad"),
                    )
                }
            }
        }
        compose.onNodeWithTag("pad").performTouchInput {
            swipe(center, center + Offset(100f, -100f), durationMillis = 500)
        }
        compose.runOnIdle { assertEquals(0, writes) }
    }
}
