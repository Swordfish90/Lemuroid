package com.codebutler.retrograde.app.feature.home

import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.codebutler.retrograde.R
import com.airbnb.epoxy.EpoxyHolder
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import com.airbnb.epoxy.EpoxyAttribute
import com.squareup.picasso.Picasso


@EpoxyModelClass(layout = R.layout.layout_game)
abstract class EpoxyGameView : EpoxyModelWithHolder<EpoxyGameView.Holder>() {

    @EpoxyAttribute var title: String? = null
    @EpoxyAttribute var coverUrl: String? = null
    @EpoxyAttribute var toggled: Boolean? = null
    @EpoxyAttribute var onClick: (() -> Unit)? = null
    @EpoxyAttribute var onToggle: ((Boolean) -> Unit)? = null

    override fun bind(holder: Holder) {
        holder.titleView?.text = title
        Picasso.get().load(coverUrl).into(holder.coverView)
        holder.itemView?.setOnClickListener { onClick?.invoke() }
        holder.favoriteToggle?.isChecked = toggled ?: false
        holder.favoriteToggle?.setOnCheckedChangeListener { _, isChecked -> onToggle?.invoke(isChecked) }
    }

    override fun unbind(holder: Holder) {
        holder.favoriteToggle?.setOnCheckedChangeListener(null)
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
        var favoriteToggle: ToggleButton? = null

        override fun bindView(itemView: View) {
            this.itemView = itemView
            this.titleView = itemView.findViewById(R.id.text)
            this.coverView = itemView.findViewById(R.id.image)
            this.favoriteToggle = itemView.findViewById(R.id.favorite_toggle)
        }
    }
}
