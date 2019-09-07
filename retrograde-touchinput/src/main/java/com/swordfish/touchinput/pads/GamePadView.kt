package com.swordfish.touchinput.pads

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.swordfish.touchinput.data.PadEvent
import io.reactivex.Observable

abstract class GamePadView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    abstract fun getEvents(): Observable<PadEvent>
}
