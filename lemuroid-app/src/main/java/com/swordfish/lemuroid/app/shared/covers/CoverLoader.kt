package com.swordfish.lemuroid.app.shared.covers

import android.widget.ImageView
import coil.load
import com.swordfish.lemuroid.common.drawable.TextDrawable
import com.swordfish.lemuroid.common.graphics.ColorUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game

object CoverLoader {

    fun loadCover(game: Game, imageView: ImageView?) {
        if (imageView == null) return

        imageView.load(game.coverFrontUrl) {
            crossfade(true)

            val fallbackDrawable = getFallbackDrawable(game)
            fallback(fallbackDrawable)
            error(fallbackDrawable)
        }
    }

    fun getFallbackRemoteUrl(game: Game): String {
        val color = Integer.toHexString(computeColor(game)).substring(2)
        val title = computeTitle(game)
        return "https://fakeimg.pl/512x512/$color/fff/?font=bebas&text=$title"
    }

    fun getFallbackDrawable(game: Game) =
        TextDrawable(computeTitle(game), computeColor(game))

    private fun computeTitle(game: Game): String {
        val sanitizedName = game.title
            .replace(Regex("\\(.*\\)"), "")

        return sanitizedName.asSequence()
            .filter { it.isDigit() or it.isUpperCase() or (it == '&') }
            .take(3)
            .joinToString("")
            .ifBlank { game.title.first().toString() }
            .capitalize()
    }

    private fun computeColor(game: Game): Int {
        return ColorUtils.randomColor(game.title)
    }

    fun cancelRequest(imageView: ImageView) {
        // coil-kt automatically does that for us.
    }
}
