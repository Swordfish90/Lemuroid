package com.swordfish.lemuroid.app.shared.covers

import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.library.db.entity.Game

object PicassoWrapper {
    fun loadCover(game: Game, imageView: ImageView?) {
        Picasso.get()
            .load(game.coverFrontUrl)
            .placeholder(R.drawable.ic_image_paceholder)
            .into(imageView)
    }

    fun loadResizeCover(game: Game, imageView: ImageView?, resizeSize: Int) {
        Picasso.get()
            .load(game.coverFrontUrl)
            .resize(resizeSize, resizeSize)
            .centerCrop()
            .placeholder(R.drawable.ic_image_paceholder)
            .into(imageView)
    }

    fun cancelRequest(imageView: ImageView) {
        Picasso.get().cancelRequest(imageView)
    }
}