package com.swordfish.touchinput.utils

import android.view.GestureDetector
import android.view.MotionEvent

class DoubleTapListener(private val action: () -> Unit) : GestureDetector.SimpleOnGestureListener() {
    override fun onDoubleTap(e: MotionEvent?): Boolean {
        action()
        return true
    }
}
