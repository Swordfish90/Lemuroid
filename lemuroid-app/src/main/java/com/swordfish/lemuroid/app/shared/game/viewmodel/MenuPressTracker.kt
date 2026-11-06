package com.swordfish.lemuroid.app.shared.game.viewmodel

internal class MenuPressTracker(
    private val doubleTapTimeoutMillis: Long,
    private val longPressTimeoutMillis: Long,
) {
    private var pressedAt: Long? = null
    private var previousTapReleasedAt: Long? = null
    private var openedDuringPress = false

    fun press(now: Long): Boolean {
        if (pressedAt != null) return false
        pressedAt = now
        openedDuringPress = false
        return true
    }

    fun release(now: Long): Boolean {
        val start = pressedAt ?: return false
        pressedAt = null
        if (openedDuringPress) {
            previousTapReleasedAt = null
            return false
        }
        if (now - start >= longPressTimeoutMillis) {
            previousTapReleasedAt = null
            return true
        }
        val previousRelease = previousTapReleasedAt
        val doubleTap = previousRelease != null && now - previousRelease in 0..doubleTapTimeoutMillis
        previousTapReleasedAt = if (doubleTap) null else now
        return doubleTap
    }

    fun longPress(now: Long): Boolean {
        val start = pressedAt ?: return false
        if (openedDuringPress || now - start < longPressTimeoutMillis) return false
        openedDuringPress = true
        previousTapReleasedAt = null
        return true
    }

    fun reset() {
        pressedAt = null
        previousTapReleasedAt = null
        openedDuringPress = false
    }
}
