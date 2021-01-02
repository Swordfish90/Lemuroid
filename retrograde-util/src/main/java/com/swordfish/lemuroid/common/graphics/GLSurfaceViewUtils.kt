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
            val outputScaling = maxResolution / maxOf(width, height).toFloat()
            val inputScaling = outputScaling * 2

            val inputBitmap = Bitmap.createBitmap(
                (width * inputScaling).roundToInt(),
                (height * inputScaling).roundToInt(),
                Bitmap.Config.ARGB_8888
            )

            val onCompleted = { result: Int ->
                if (result == PixelCopy.SUCCESS) {

                    // This rescaling limits the artifacts introduced by shaders.
                    val outputBitmap = Bitmap.createScaledBitmap(
                        inputBitmap,
                        (width * outputScaling).roundToInt(),
                        (height * outputScaling).roundToInt(),
                        true
                    )

                    emitter.onSuccess(outputBitmap)
                } else {
                    emitter.onError(RuntimeException("Cannot take screenshot. Error code: $result"))
                }
            }
            PixelCopy.request(this, inputBitmap, onCompleted, handler)
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }
}
