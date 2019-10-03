package com.codebutler.retrograde.app.feature.home

import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.codebutler.retrograde.R
import com.airbnb.epoxy.EpoxyHolder
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.codebutler.retrograde.app.shared.GameContextMenuListener
import com.squareup.picasso.Picasso

@EpoxyModelClass(layout = R.layout.layout_game)
abstract class EpoxyGameView : EpoxyModelWithHolder<EpoxyGameView.Holder>() {

    @EpoxyAttribute var title: String? = null
    @EpoxyAttribute var coverUrl: String? = null
    @EpoxyAttribute var favorite: Boolean? = null
    @EpoxyAttribute var onClick: (() -> Unit)? = null
    @EpoxyAttribute var onFavoriteChanged: ((Boolean) -> Unit)? = null

    override fun bind(holder: Holder) {
        holder.titleView?.text = title
        Picasso.get().load(coverUrl).into(holder.coverView)
        holder.itemView?.setOnClickListener { onClick?.invoke() }
        holder.itemView?.setOnCreateContextMenuListener(GameContextMenuListener(favorite, onClick, onFavoriteChanged))
    }

    override fun unbind(holder: Holder) {
        holder.itemView?.setOnClickListener(null)
        holder.coverView?.apply {
            Picasso.get().cancelRequest(this)
        }
        holder.itemView?.setOnCreateContextMenuListener(null)
    }

    class Holder : EpoxyHolder() {
        var itemView: View? = null
        var titleView: TextView? = null
        var coverView: ImageView? = null

        override fun bindView(itemView: View) {
            this.itemView = itemView
            this.titleView = itemView.findViewById(R.id.text)
            this.coverView = itemView.findViewById(R.id.image)
        }
    }
}
