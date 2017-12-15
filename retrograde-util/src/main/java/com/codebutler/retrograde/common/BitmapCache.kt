/*
 * BitmapCache.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.retrograde.common

import android.graphics.Bitmap

class BitmapCache {

    private var bitmap: Bitmap? = null

    fun getBitmap(width: Int, height: Int, videoBitmapConfig: Bitmap.Config): Bitmap {
        var bitmap = bitmap
        if (bitmap == null || bitmap.width != width || bitmap.height != height || bitmap.config != videoBitmapConfig) {
            bitmap = Bitmap.createBitmap(width, height, videoBitmapConfig)
            this@BitmapCache.bitmap = bitmap
        }
        return bitmap!!
    }
}
