package com.swordfish.lemuroid.app.mobile.feature.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameContextMenuListener
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.PicassoWrapper
import com.swordfish.lemuroid.app.utils.games.GameUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game

@EpoxyModelClass(layout = R.layout.layout_game_recent)
abstract class EpoxyGameView : EpoxyModelWithHolder<EpoxyGameView.Holder>() {

    @EpoxyAttribute
    lateinit var game: Game

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var gameInteractor: GameInteractor

    override fun bind(holder: Holder) {
        holder.titleView?.text = game.title
        holder.subtitleView?.let { it.text = GameUtils.getGameSubtitle(it.context, game) }

        PicassoWrapper.loadCover(game, holder.coverView)

        holder.itemView?.setOnClickListener { gameInteractor.onGamePlay(game) }
        holder.itemView?.setOnCreateContextMenuListener(
            GameContextMenuListener(gameInteractor, game)
        )
    }

    override fun unbind(holder: Holder) {
        holder.itemView?.setOnClickListener(null)
        holder.coverView?.apply {
            PicassoWrapper.cancelRequest(this)
        }
        holder.itemView?.setOnCreateContextMenuListener(null)
    }

    class Holder : EpoxyHolder() {
        var itemView: View? = null
        var titleView: TextView? = null
        var subtitleView: TextView? = null
        var coverView: ImageView? = null

        override fun bindView(itemView: View) {
            this.itemView = itemView
            this.titleView = itemView.findViewById(R.id.text)
            this.subtitleView = itemView.findViewById(R.id.subtext)
            this.coverView = itemView.findViewById(R.id.image)
        }
    }
}
