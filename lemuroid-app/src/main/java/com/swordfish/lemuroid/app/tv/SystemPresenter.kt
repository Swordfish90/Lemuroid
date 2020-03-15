package com.swordfish.lemuroid.app.tv

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.library.GameSystem

class SystemPresenter(private val cardSize: Int) : Presenter() {

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any) {
        val system = item as GameSystem
        (viewHolder as ViewHolder).mCardView.titleText = viewHolder.view.context.resources.getString(system.titleResId)
        viewHolder.mCardView.setMainImageDimensions(cardSize, cardSize)
        viewHolder.mCardView.mainImageView.setImageResource(system.imageResId)

        val padding = (cardSize * 0.15).toInt()
        viewHolder.mCardView.mainImageView.setPadding(padding, padding, padding, padding)
        viewHolder.mCardView.setMainImageScaleType(ImageView.ScaleType.FIT_CENTER)
    }

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        (cardView.findViewById<View>(R.id.content_text) as TextView).setTextColor(Color.LTGRAY)
        return ViewHolder(cardView)
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {
        // TODO FILIPPO... This should be fixed somehow
    }

    class ViewHolder(view: ImageCardView) : Presenter.ViewHolder(view) {
        val mCardView: ImageCardView = view
    }
}
