package com.swordfish.touchinput.radial.utils

import androidx.compose.ui.geometry.Offset
import com.swordfish.lemuroid.common.graphics.GraphicsUtils.rotatePoint
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.jam.jampadcompose.anchors.Anchor
import gg.jam.jampadcompose.ids.KeyId
import gg.jam.jampadcompose.utils.GeometryUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

fun buildCentral6ButtonsAnchors(rotation: Float, id0: Int, id1: Int, id2: Int, id3: Int): List<Anchor<KeyId>> {
    val buttonSize = GeometryUtils.computeSizeOfItemsAroundCircumference(12)
    val d = 3f * buttonSize

    val rotationAngle = rotation * TouchControllerSettingsManager.MAX_ROTATION.toDouble()

    val delta = Offset(-tan(Math.toRadians(15.0)).toFloat(), 1f) * d * 1.25f
    val topLeftLine = Offset(0f, -1f - d)
    val topRightLine = Offset(sin(Math.toRadians(30.0)).toFloat(), -cos(Math.toRadians(30.0)).toFloat()) * (1f + d)

    val pointA = rotatePoint(topLeftLine + delta * 1.0f, rotationAngle)
    val pointB = rotatePoint(topLeftLine + delta * 2.0f, rotationAngle)
    val pointC = rotatePoint(topRightLine + delta * 1.0f, rotationAngle)
    val pointD = rotatePoint(topRightLine + delta * 2.0f, rotationAngle)

    val result = listOf(
        Anchor(pointA, setOf(KeyId(id0)), buttonSize),
        Anchor(pointB, setOf(KeyId(id1)), buttonSize),
        Anchor(pointC, setOf(KeyId(id2)), buttonSize),
        Anchor(pointD, setOf(KeyId(id3)), buttonSize),
    )

    return result
}
