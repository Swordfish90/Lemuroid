package com.swordfish.lemuroid.app.tv

import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.swordfish.lemuroid.R

class SettingPresenter(private val cardSize: Int) : Presenter() {

    enum class Setting(val icon: Int, val text: Int) {
        RESCAN(R.drawable.ic_refresh_white_64dp, R.string.rescan)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any) {
        val setting = item as Setting
        (viewHolder as ViewHolder).mCardView.titleText = viewHolder.view.context.resources.getString(setting.text)
        viewHolder.mCardView.setMainImageDimensions(cardSize, cardSize)
        viewHolder.mCardView.mainImageView.setImageResource(setting.icon)

        val padding = (cardSize * 0.15).toInt()
        viewHolder.mCardView.mainImageView.setPadding(padding, padding, padding, padding)
        viewHolder.mCardView.setMainImageScaleType(ImageView.ScaleType.FIT_CENTER)
    }

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {
        // TODO FILIPPO... This should be fixed somehow
    }

    class ViewHolder(view: ImageCardView) : Presenter.ViewHolder(view) {
        val mCardView: ImageCardView = view
    }
}
