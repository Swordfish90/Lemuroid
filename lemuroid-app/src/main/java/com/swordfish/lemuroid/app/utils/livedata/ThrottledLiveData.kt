package com.swordfish.lemuroid.app.utils.livedata

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * LiveData throttling value emissions so they don't happen more often than [delayMs].
 */
class ThrottledLiveData<T>(source: LiveData<T>, delayMs: Long) : MediatorLiveData<T>() {
    private val handler = Handler(Looper.getMainLooper())
    var delayMs = delayMs
        private set

    private var isValueDelayed = false
    private var delayedValue: T? = null
    private var delayRunnable: Runnable? = null
        set(value) {
            field?.let { handler.removeCallbacks(it) }
            value?.let { handler.postDelayed(it, delayMs) }
            field = value
        }
    private val objDelayRunnable = Runnable { if (consumeDelayedValue()) startDelay() }

    init {
        addSource(source) { newValue ->
            if (delayRunnable == null) {
                value = newValue
                startDelay()
            } else {
                isValueDelayed = true
                delayedValue = newValue
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        consumeDelayedValue()
    }

    // start counting the delay or clear it if conditions are not met
    private fun startDelay() {
        delayRunnable = if (delayMs > 0 && hasActiveObservers()) objDelayRunnable else null
    }

    private fun consumeDelayedValue(): Boolean {
        delayRunnable = null
        return if (isValueDelayed) {
            value = delayedValue
            delayedValue = null
            isValueDelayed = false
            true
        } else {
            false
        }
    }
}
