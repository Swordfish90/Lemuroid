package com.swordfish.touchinput.pads

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.events.PadEvent
import io.reactivex.Observable

abstract class BaseGamePad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    leftPadConfig: SemiPadConfig,
    rightPadConfig: SemiPadConfig
) : FrameLayout(context, attrs, defStyleAttr) {

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

    data class SemiPadConfig(val layoutId: Int, val cols: Int, val rows: Int)

    abstract fun getEvents(): Observable<PadEvent>
}
