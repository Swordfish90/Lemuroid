package com.swordfish.lemuroid.app.shared

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView

class SquaredMaterialCardView : MaterialCardView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
