package com.swordfish.touchinput.radial.utils

import androidx.compose.ui.geometry.Offset
import com.swordfish.lemuroid.common.graphics.GraphicsUtils.rotatePoint
import com.swordfish.lemuroid.common.math.computeSizeOfItemsAroundCircumference
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.padkit.anchors.Anchor
import gg.padkit.ids.Id
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

fun buildCentral6ButtonsAnchors(
    rotation: Float,
    id0: Int,
    id1: Int,
    id2: Int,
    id3: Int,
): PersistentList<Anchor<Id.Key>> {
    val buttonSize = computeSizeOfItemsAroundCircumference(12)
    val distance = 3f * buttonSize

    val rotationAngle = rotation * TouchControllerSettingsManager.MAX_ROTATION.toDouble()

    val delta = Offset(-tan(Math.toRadians(15.0)).toFloat(), 1f) * distance * 1.25f
    val topLeftLine = Offset(0f, -1f - distance)
    val topRightLine = Offset(sin(Math.toRadians(30.0)).toFloat(), -cos(Math.toRadians(30.0)).toFloat())

    val pointA = rotatePoint(topLeftLine + delta * 1.0f, rotationAngle)
    val pointB = rotatePoint(topLeftLine + delta * 2.0f, rotationAngle)
    val pointC = rotatePoint(topRightLine * (1f + distance) + delta * 1.0f, rotationAngle)
    val pointD = rotatePoint(topRightLine * (1f + distance) + delta * 2.0f, rotationAngle)

    val result =
        persistentListOf(
            Anchor(pointA, persistentSetOf(Id.Key(id0)), buttonSize),
            Anchor(pointB, persistentSetOf(Id.Key(id1)), buttonSize),
            Anchor(pointC, persistentSetOf(Id.Key(id2)), buttonSize),
            Anchor(pointD, persistentSetOf(Id.Key(id3)), buttonSize),
        )

    return result
}
