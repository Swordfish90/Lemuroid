package com.swordfish.lemuroid.common.view

import android.animation.ObjectAnimator
import android.view.View
import android.widget.ProgressBar
import androidx.core.animation.addListener

fun View.setVisibleOrGone(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.setVisibleOrInvisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

fun View.animateVisibleOrGone(visible: Boolean, durationInMs: Long) {
    val alpha = if (visible) 1.0f else 0.0f
    ObjectAnimator.ofFloat(this, "alpha", alpha).apply {
        duration = durationInMs
        setAutoCancel(true)
        addListener(
            onStart = {
                if (visible) setVisibleOrGone(true)
            },
            onEnd = {
                if (!visible) setVisibleOrGone(false)
            }
        )
        start()
    }
}

fun ProgressBar.animateProgress(progress: Int, durationInMs: Long) {
    ObjectAnimator.ofInt(this, "progress", progress).apply {
        duration = durationInMs
        setAutoCancel(true)
        start()
    }
}
