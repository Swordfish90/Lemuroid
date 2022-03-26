package com.swordfish.lemuroid.lib.controller

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import androidx.core.math.MathUtils
import com.dinuscxj.gesture.MultiTouchGestureDetector
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.touchinput.controller.R
import io.reactivex.Observable

class TouchControllerCustomizer {

    private lateinit var touchDetector: MultiTouchGestureDetector
    private var editControlsWindow: PopupWindow? = null

    sealed class Event {
        class Rotation(val value: Float) : Event()
        class Scale(val value: Float) : Event()
        class Margins(val x: Float, val y: Float) : Event()
        object Save : Event()
    }

    data class Settings(
        val scale: Float,
        val rotation: Float,
        val marginX: Float,
        val margin: Float
    )

    private fun getObservable(
        activity: Activity,
        layoutInflater: LayoutInflater,
        view: View,
        settings: Settings,
        insets: Rect
    ): Observable<Event> = Observable.create { emitter ->
        var (scale, rotation, marginX, marginY) = settings

        val contentView = layoutInflater.inflate(R.layout.layout_edit_touch_controls, null)
        editControlsWindow = PopupWindow(
            contentView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        editControlsWindow?.contentView?.findViewById<Button>(R.id.edit_control_reset)
            ?.setOnClickListener {
                scale = TouchControllerSettingsManager.DEFAULT_SCALE
                rotation = TouchControllerSettingsManager.DEFAULT_ROTATION
                marginX = TouchControllerSettingsManager.DEFAULT_MARGIN_X
                marginY = TouchControllerSettingsManager.DEFAULT_MARGIN_Y

                emitter.onNext(Event.Margins(marginX, marginY))
                emitter.onNext(Event.Rotation(rotation))
                emitter.onNext(Event.Scale(scale))
            }
        editControlsWindow?.contentView?.findViewById<Button>(R.id.edit_control_done)
            ?.setOnClickListener {
                emitter.onNext(Event.Save)
                hideCustomizationOptions()
                emitter.onComplete()
            }

        touchDetector = MultiTouchGestureDetector(
            activity,
            object : MultiTouchGestureDetector.SimpleOnMultiTouchGestureListener() {
                val moveScale: Float = GraphicsUtils.convertDpToPixel(
                    TouchControllerSettingsManager.MAX_MARGINS,
                    activity.applicationContext
                )

                val maxMarginY: Float = 1f
                val minMarginY: Float = -insets.bottom / moveScale

                val maxMarginX: Float = 1f
                val minMarginX: Float = -maxOf(insets.left, insets.right) / moveScale

                var invertXAxis: Float = 1f

                override fun onBegin(detector: MultiTouchGestureDetector): Boolean {
                    val popupWindowWidth = editControlsWindow?.contentView?.measuredWidth ?: 0
                    invertXAxis = if (detector.focusX < popupWindowWidth / 2) 1f else -1f
                    return super.onBegin(detector)
                }

                override fun onScale(detector: MultiTouchGestureDetector) {
                    scale = MathUtils.clamp(scale + (detector.scale - 1f) * 0.5f, 0f, 1f)
                    emitter.onNext(Event.Scale(scale))
                }

                override fun onMove(detector: MultiTouchGestureDetector) {
                    marginY = MathUtils.clamp(
                        marginY - detector.moveY / moveScale,
                        minMarginY,
                        maxMarginY
                    )
                    marginX = MathUtils.clamp(
                        marginX + invertXAxis * detector.moveX / moveScale,
                        minMarginX,
                        maxMarginX
                    )
                    emitter.onNext(Event.Margins(marginX, marginY))
                }

                override fun onRotate(detector: MultiTouchGestureDetector) {
                    val currentRotation = rotation * TouchControllerSettingsManager.MAX_ROTATION
                    val nextRotation = currentRotation - invertXAxis * detector.rotation
                    rotation = MathUtils.clamp(
                        nextRotation / TouchControllerSettingsManager.MAX_ROTATION,
                        0f,
                        1f
                    )
                    emitter.onNext(Event.Rotation(rotation))
                }
            }
        )

        editControlsWindow?.setOnDismissListener { emitter.onComplete() }

        editControlsWindow?.contentView?.setOnTouchListener { _, event ->
            touchDetector.onTouchEvent(event)
        }
        editControlsWindow?.isFocusable = false
        editControlsWindow?.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    fun displayCustomizationPopup(
        activity: Activity,
        layoutInflater: LayoutInflater,
        view: View,
        insets: Rect,
        settings: Settings
    ): Observable<Event> {
        val originalRequestedOrientation = activity.requestedOrientation
        return getObservable(activity, layoutInflater, view, settings, insets)
            .doOnSubscribe { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED }
            .doFinally { activity.requestedOrientation = originalRequestedOrientation }
            .doFinally { hideCustomizationOptions() }
    }

    private fun hideCustomizationOptions() {
        editControlsWindow?.dismiss()
        editControlsWindow?.contentView?.setOnTouchListener(null)
        editControlsWindow = null
    }
}
