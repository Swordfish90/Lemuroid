package com.swordfish.touchinput.radial

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleObserver
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.touchinput.controller.R
import io.reactivex.Observable

class BaseRadialPad @JvmOverloads constructor(
    leftConfig: RadialGamePadConfig,
    rightConfig: RadialGamePadConfig,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LifecycleObserver {

    private val leftPad: RadialGamePad
    private val rightPad: RadialGamePad

    init {
        inflate(context, R.layout.base_radial_gamepad, this)

        val leftContainer = findViewById<FrameLayout>(R.id.leftcontainer)
        val rightContainer = findViewById<FrameLayout>(R.id.rightcontainer)

        leftPad = RadialGamePad(leftConfig, context)
        leftContainer.addView(leftPad)

        rightPad = RadialGamePad(rightConfig, context)
        rightContainer.addView(rightPad)
    }

    fun getEvents(): Observable<Event> {
        return Observable.merge(leftPad.events(), rightPad.events())
    }
}
