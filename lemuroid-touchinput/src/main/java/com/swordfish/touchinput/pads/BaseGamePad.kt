package com.swordfish.touchinput.pads

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.swordfish.lemuroid.common.rx.safeDispose
import com.swordfish.touchinput.events.PadBusEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.events.PadEvent
import com.swordfish.touchinput.interfaces.PadBusSource
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.merge
import io.reactivex.rxkotlin.plusAssign

abstract class BaseGamePad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    leftPadConfig: SemiPadConfig,
    rightPadConfig: SemiPadConfig
) : FrameLayout(context, attrs, defStyleAttr), LifecycleObserver {

    init {
        val constraintLayout = inflate(context, R.layout.base_layout_gamepad, this)
            .findViewById<ConstraintLayout>(R.id.gamepadcontainer)

        val gridUnitSize = context.resources.getDimensionPixelSize(R.dimen.pad_grid_size)

        val leftContainer = findViewById<FrameLayout>(R.id.leftcontainer)
        val leftLayoutParams = leftContainer.layoutParams as ConstraintLayout.LayoutParams
        leftLayoutParams.matchConstraintMaxWidth = leftPadConfig.cols * gridUnitSize
        leftContainer.layoutParams = leftLayoutParams

        val rightContainer = findViewById<FrameLayout>(R.id.rightcontainer)
        val rightLayoutParams = rightContainer.layoutParams as ConstraintLayout.LayoutParams
        rightLayoutParams.matchConstraintMaxWidth = rightPadConfig.cols * gridUnitSize
        rightContainer.layoutParams = rightLayoutParams

        val set = ConstraintSet().apply {
            clone(constraintLayout)
            setDimensionRatio(R.id.leftcontainer, "${leftPadConfig.cols}:${leftPadConfig.rows}")
            setDimensionRatio(R.id.rightcontainer, "${rightPadConfig.cols}:${rightPadConfig.rows}")
            setHorizontalWeight(R.id.rightcontainer, rightPadConfig.cols.toFloat() / leftPadConfig.cols.toFloat())
        }
        constraintLayout.setConstraintSet(set)

        inflate(context, leftPadConfig.layoutId, leftContainer)
        inflate(context, rightPadConfig.layoutId, rightContainer)
    }

    abstract fun getEvents(): Observable<PadEvent>

    fun setTiltSensitivity(tiltSensitivity: Float) {
        sendBusEvent(PadBusEvent.SetTiltSensitivity(tiltSensitivity))
    }

    private val busDisposable = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        busDisposable += getBusSourceViews()
            .map { it.getBusEvents() }
            .merge()
            .subscribe { sendBusEvent(it) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        busDisposable.safeDispose()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        sendBusEvent(PadBusEvent.OnPause)
    }

    private fun sendBusEvent(event: PadBusEvent) {
        getBusSourceViews()
            .filter { event.sourceId != it.getView().id }
            .forEach { it.onBusEvent(event) }
    }

    open fun getBusSourceIds(): List<Int> = emptyList()

    private fun getBusSourceViews(): List<PadBusSource> {
        return getBusSourceIds().map { findViewById<View>(it) as PadBusSource }
    }

    data class SemiPadConfig(val layoutId: Int, val cols: Int, val rows: Int)
}
