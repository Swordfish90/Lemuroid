package com.swordfish.touchinput.radial.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import kotlinx.coroutines.delay

@Composable
fun LemuroidButtonPressFeedback(
    pressed: Boolean,
    animationDurationMillis: Int,
    icon: Int,
) {
    Box(
        modifier = Modifier.size(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        var shouldShow by remember { mutableStateOf(false) }
        var progress by remember { mutableFloatStateOf(0f) }

        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec =
                tween(
                    durationMillis = animationDurationMillis,
                    easing = LinearEasing,
                ),
            label = "progress",
            finishedListener = {
                if (progress > 0.5) {
                    shouldShow = false
                }
            },
        )

        LaunchedEffect(pressed) {
            if (pressed) {
                shouldShow = true
                progress = 1f
            } else {
                progress = 0f
                delay(animationDurationMillis.toLong())
                shouldShow = false
            }
        }

        AnimatedVisibility(shouldShow, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                LemuroidControlBackground()
                LemuroidButtonForeground(
                    pressed = remember { mutableStateOf(false) },
                    icon = icon,
                )

                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .fillMaxSize(0.625f)
                            .align(Alignment.Center),
                    progress = { animatedProgress },
                    color = LocalLemuroidPadTheme.current.icons(false),
                    trackColor = Color.Transparent,
                )
            }
        }
    }
}
