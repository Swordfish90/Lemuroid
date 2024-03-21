package com.swordfish.lemuroid.common.bitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

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
