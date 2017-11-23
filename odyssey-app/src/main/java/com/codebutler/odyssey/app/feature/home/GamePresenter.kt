/*
 * GamePresenter.kt
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.app.feature.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v17.leanback.widget.ImageCardView
import android.support.v17.leanback.widget.Presenter
import android.view.ViewGroup
import android.widget.ImageView
import com.codebutler.odyssey.R
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.squareup.picasso.Picasso

class GamePresenter : Presenter() {

    private lateinit var defaultCardImage: Drawable

    private var imageWidth: Int = -1
    private var imageHeight: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        defaultCardImage = ColorDrawable(Color.BLACK)

        val cardView = ImageCardView(parent.context)
        val res = cardView.resources
        imageWidth = res.getDimensionPixelSize(R.dimen.card_width)
        imageHeight = res.getDimensionPixelSize(R.dimen.card_height)

        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        cardView.setMainImageDimensions(imageWidth, imageHeight)

        return Presenter.ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any?) {
        if (item == null) {
            return
        }

        val game = item as Game

        val cardView = viewHolder.view as ImageCardView
        cardView.titleText = game.title
        cardView.contentText = game.developer

        if (game.coverFrontUrl != null) {
            Picasso.with(cardView.context)
                    .load(game.coverFrontUrl)
                    .error(defaultCardImage)
                    .resize(imageWidth, imageHeight)
                    .centerInside()
                    .into(cardView.mainImageView)
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        Picasso.with(cardView.context).cancelRequest(cardView.mainImageView)
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}
