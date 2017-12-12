/*
 * SimpleItemPresenter.kt
 *
 * Copyright (C) 2017 Odyssey Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.lib.ui

import android.content.res.Resources
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v17.leanback.widget.ImageCardView
import android.support.v17.leanback.widget.Presenter
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.ImageView
import com.codebutler.odyssey.lib.R

sealed class TextOrResource {
    class Text(val value: String) : TextOrResource()
    class Resource(@StringRes val value: Int) : TextOrResource()
    fun getText(resources: Resources): String = when (this) {
        is Text -> value
        is Resource -> resources.getString(value)
    }
}

open class SimpleItem private constructor (val title: TextOrResource, @DrawableRes val image: Int?) {
    constructor(text: String, @DrawableRes image: Int? = 0) : this(TextOrResource.Text(text), image)
    constructor(@StringRes resId: Int, @DrawableRes image: Int = 0) : this (TextOrResource.Resource(resId), image)
}

class SimpleItemPresenter : Presenter() {

    private var imageWidth: Int = -1
    private var imageHeight: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(ContextThemeWrapper(parent.context, R.style.SimpleImageCardTheme))
        val res = cardView.resources
        imageWidth = res.getDimensionPixelSize(R.dimen.card_width)
        imageHeight = res.getDimensionPixelSize(R.dimen.card_height)

        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        cardView.setMainImageDimensions(imageWidth, imageHeight)

        return Presenter.ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        when (item) {
            is SimpleItem -> {
                val resources = viewHolder.view.resources
                val cardView = viewHolder.view as ImageCardView
                cardView.titleText = item.title.getText(resources)
                if (item.image != null && item.image != 0) {
                    cardView.mainImage = resources.getDrawable(item.image)
                } else {
                    cardView.mainImage = null
                }
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) { }
}
