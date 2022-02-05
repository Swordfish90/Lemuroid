package com.swordfish.lemuroid.app.mobile.feature.game

import android.view.View
import android.view.ViewConfiguration
import android.widget.ImageView
import android.widget.ProgressBar
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.common.view.animateProgress
import com.swordfish.lemuroid.common.view.animateVisibleOrGone
import com.swordfish.lemuroid.common.view.setVisibleOrGone
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

object VirtualLongPressHandler {

    private val APPEAR_ANIMATION = (ViewConfiguration.getLongPressTimeout() * 0.1f).toLong()
    private val DISAPPEAR_ANIMATION = (ViewConfiguration.getLongPressTimeout() * 2f).toLong()
    private val LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout().toLong()

    fun displayLoading(
        activity: GameActivity,
        iconId: Int,
        cancellation: Observable<Unit>
    ): Maybe<Unit> {
        return Observable.timer(LONG_PRESS_TIMEOUT, TimeUnit.MILLISECONDS)
            .takeUntil(cancellation)
            .firstElement()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                longPressView(activity).alpha = 0f
                longPressIconView(activity).setImageResource(iconId)
            }
            .doAfterSuccess {
                longPressView(activity).setVisibleOrGone(false)
            }
            .doOnSubscribe { displayLongPressView(activity) }
            .doAfterTerminate { hideLongPressView(activity) }
            .onErrorComplete()
            .map { Unit }
    }

    private fun longPressIconView(activity: GameActivity) =
        activity.findViewById<ImageView>(R.id.settings_loading_icon)

    private fun longPressProgressBar(activity: GameActivity) =
        activity.findViewById<ProgressBar>(R.id.settings_loading_progress)

    private fun longPressView(activity: GameActivity) =
        activity.findViewById<View>(R.id.settings_loading)

    private fun displayLongPressView(activity: GameActivity) {
        longPressView(activity).animateVisibleOrGone(true, APPEAR_ANIMATION)
        longPressProgressBar(activity).animateProgress(100, LONG_PRESS_TIMEOUT)
    }

    private fun hideLongPressView(activity: GameActivity) {
        longPressView(activity).animateVisibleOrGone(false, DISAPPEAR_ANIMATION)
        longPressProgressBar(activity).animateProgress(0, LONG_PRESS_TIMEOUT)
    }
}
