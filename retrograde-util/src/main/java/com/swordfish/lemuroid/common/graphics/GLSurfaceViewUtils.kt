package com.swordfish.lemuroid.common.graphics

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.view.PixelCopy
import io.reactivex.Maybe
import java.lang.RuntimeException
import kotlin.math.roundToInt

fun GLSurfaceView.takeScreenshot(maxResolution: Int): Maybe<Bitmap> = Maybe.create { emitter ->
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
        emitter.onComplete()
        return@create
    }

    queueEvent {
        try {
            val scaling = maxResolution / maxOf(width, height).toFloat()
            val bitmap: Bitmap = Bitmap.createBitmap(
                (width * scaling).roundToInt(),
                (height * scaling).roundToInt(),
                Bitmap.Config.ARGB_8888
            )

            val onCompleted = { result: Int ->
                if (result == PixelCopy.SUCCESS) {
                    emitter.onSuccess(bitmap)
                } else {
                    emitter.onError(RuntimeException("Cannot take screenshot. Error code: $result"))
                }
            }
            PixelCopy.request(this, bitmap, onCompleted, handler)
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }
}
