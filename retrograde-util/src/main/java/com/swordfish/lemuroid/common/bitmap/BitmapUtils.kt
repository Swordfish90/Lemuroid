/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.common.bitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import kotlin.math.roundToInt

fun Bitmap.cropToViewport(viewport: RectF): Bitmap {
    val x = (viewport.left * width).roundToInt().coerceIn(0, width - 1)
    val y = (viewport.top * height).roundToInt().coerceIn(0, height - 1)
    val w = ((viewport.right - viewport.left) * width).roundToInt().coerceIn(1, width - x)
    val h = ((viewport.bottom - viewport.top) * height).roundToInt().coerceIn(1, height - y)
    if (x == 0 && y == 0 && w == width && h == height) return this
    return Bitmap.createBitmap(this, x, y, w, h)
}

fun Bitmap.cropToSquare(): Bitmap {
    val newWidth = if (height > width) width else height
    val newHeight = if (height > width) height - (height - width) else height
    var cropW = (width - height) / 2

    cropW = if (cropW < 0) 0 else cropW
    var cropH = (height - width) / 2
    cropH = if (cropH < 0) 0 else cropH

    return Bitmap.createBitmap(this, cropW, cropH, newWidth, newHeight)
}

fun Drawable.toBitmap(
    width: Int,
    height: Int,
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)
    return bitmap
}
