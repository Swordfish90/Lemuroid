package com.swordfish.lemuroid.lib.controller

class TouchControllerCustomizer {
//    private lateinit var touchDetector: MultiTouchGestureDetector
//    private var editControlsWindow: PopupWindow? = null
//
//    sealed class Event {
//        class Rotation(val value: Float) : Event()
//
//        class Scale(val value: Float) : Event()
//
//        class Margins(val x: Float, val y: Float) : Event()
//
//        object Save : Event()
//
//        object Close : Event()
//
//        object Init : Event()
//    }
//
//    data class Settings(
//        val scale: Float,
//        val rotation: Float,
//        val marginX: Float,
//        val margin: Float,
//    )
//
//    private fun getEvents(
//        activity: Activity,
//        layoutInflater: LayoutInflater,
//        view: View,
//        settings: Settings,
//        insets: Rect,
//    ): SharedFlow<Event> {
//        val events = MutableStateFlow<Event>(Event.Init)
//
//        var (scale, rotation, marginX, marginY) = settings
//
//        val contentView = layoutInflater.inflate(R.layout.layout_edit_touch_controls, null)
//        editControlsWindow =
//            PopupWindow(
//                contentView,
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                true,
//            )
//        editControlsWindow?.contentView?.findViewById<Button>(R.id.edit_control_reset)
//            ?.setOnClickListener {
//                scale = TouchControllerSettingsManager.DEFAULT_SCALE
//                rotation = TouchControllerSettingsManager.DEFAULT_ROTATION
//                marginX = TouchControllerSettingsManager.DEFAULT_MARGIN_X
//                marginY = TouchControllerSettingsManager.DEFAULT_MARGIN_Y
//
//                events.value = Event.Margins(marginX, marginY)
//                events.value = Event.Rotation(rotation)
//                events.value = Event.Scale(scale)
//            }
//        editControlsWindow?.contentView?.findViewById<Button>(R.id.edit_control_done)
//            ?.setOnClickListener {
//                events.value = Event.Save
//                hideCustomizationOptions()
//                events.value = Event.Close
//            }
//
//        touchDetector =
//            MultiTouchGestureDetector(
//                activity,
//                object : MultiTouchGestureDetector.SimpleOnMultiTouchGestureListener() {
//                    val moveScale: Float =
//                        GraphicsUtils.convertDpToPixel(
//                            TouchControllerSettingsManager.MAX_MARGINS,
//                            activity.applicationContext,
//                        )
//
//                    val maxMarginY: Float = 1f
//                    val minMarginY: Float = -insets.bottom / moveScale
//
//                    val maxMarginX: Float = 1f
//                    val minMarginX: Float = -maxOf(insets.left, insets.right) / moveScale
//
//                    var invertXAxis: Float = 1f
//
//                    override fun onBegin(detector: MultiTouchGestureDetector): Boolean {
//                        val popupWindowWidth = editControlsWindow?.contentView?.measuredWidth ?: 0
//                        invertXAxis = if (detector.focusX < popupWindowWidth / 2) 1f else -1f
//                        return super.onBegin(detector)
//                    }
//
//                    override fun onScale(detector: MultiTouchGestureDetector) {
//                        scale = MathUtils.clamp(scale + (detector.scale - 1f) * 0.5f, 0f, 1f)
//                        events.value = Event.Scale(scale)
//                    }
//
//                    override fun onMove(detector: MultiTouchGestureDetector) {
//                        marginY =
//                            MathUtils.clamp(
//                                marginY - detector.moveY / moveScale,
//                                minMarginY,
//                                maxMarginY,
//                            )
//                        marginX =
//                            MathUtils.clamp(
//                                marginX + invertXAxis * detector.moveX / moveScale,
//                                minMarginX,
//                                maxMarginX,
//                            )
//                        events.value = Event.Margins(marginX, marginY)
//                    }
//
//                    override fun onRotate(detector: MultiTouchGestureDetector) {
//                        val currentRotation = rotation * TouchControllerSettingsManager.MAX_ROTATION
//                        val nextRotation = currentRotation - invertXAxis * detector.rotation
//                        rotation =
//                            MathUtils.clamp(
//                                nextRotation / TouchControllerSettingsManager.MAX_ROTATION,
//                                0f,
//                                1f,
//                            )
//                        events.value = Event.Rotation(rotation)
//                    }
//                },
//            )
//
//        editControlsWindow?.setOnDismissListener { events.value = Event.Close }
//
//        editControlsWindow?.contentView?.setOnTouchListener { _, event ->
//            touchDetector.onTouchEvent(event)
//        }
//        editControlsWindow?.isFocusable = false
//        editControlsWindow?.showAtLocation(view, Gravity.CENTER, 0, 0)
//        return events
//    }
//
//    fun displayCustomizationPopup(
//        activity: Activity,
//        layoutInflater: LayoutInflater,
//        view: View,
//        insets: Rect,
//        settings: Settings,
//    ): Flow<Event> {
//        val originalRequestedOrientation = activity.requestedOrientation
//        return getEvents(activity, layoutInflater, view, settings, insets)
//            .onSubscription { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED }
//            .onCompletion { activity.requestedOrientation = originalRequestedOrientation }
//            .onCompletion { hideCustomizationOptions() }
//    }
//
//    private fun hideCustomizationOptions() {
//        editControlsWindow?.dismiss()
//        editControlsWindow?.contentView?.setOnTouchListener(null)
//        editControlsWindow = null
//    }
}
