package com.swordfish.lemuroid.common.bitmap

import android.graphics.Bitmap

fun Bitmap.cropToSquare(): Bitmap {
    val newWidth = if (height > width) width else height
    val newHeight = if (height > width) height - (height - width) else height
    var cropW = (width - height) / 2

    cropW = if (cropW < 0) 0 else cropW
    var cropH = (height - width) / 2
    cropH = if (cropH < 0) 0 else cropH

    return Bitmap.createBitmap(this, cropW, cropH, newWidth, newHeight)
}
