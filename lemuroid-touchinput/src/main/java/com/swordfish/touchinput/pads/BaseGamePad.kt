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
import com.swordfish.touchinput.events.PadEvent
import com.swordfish.touchinput.interfaces.PadBusSource
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.merge
import io.reactivex.rxkotlin.plusAssign

abstract class BaseGamePad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LifecycleObserver {

    abstract fun getEvents(): Observable<PadEvent>

    private val busDisposable = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        busDisposable += getBusSourceViews()
            .map { it.getBusEvents() }
            .merge()
            .subscribe { sendBusEvent(it) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        sendBusEvent(PadBusEvent.OnPause)
        busDisposable.safeDispose()
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
}
