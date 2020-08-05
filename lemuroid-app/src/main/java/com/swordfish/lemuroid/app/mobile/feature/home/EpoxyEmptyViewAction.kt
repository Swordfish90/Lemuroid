package com.swordfish.lemuroid.app.mobile.feature.home

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.swordfish.lemuroid.R

@EpoxyModelClass(layout = R.layout.layout_empty_action)
abstract class EpoxyEmptyViewAction : EpoxyModelWithHolder<EpoxyEmptyViewAction.Holder>() {

    @EpoxyAttribute
    var title: Int? = null

    @EpoxyAttribute
    var message: Int? = null

    @EpoxyAttribute
    var action: Int? = null

    @EpoxyAttribute
    var actionEnabled: Boolean? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onClick: (() -> Unit)? = null

    override fun bind(holder: Holder) {
        title?.let { holder.titleView?.setText(it) }
        message?.let { holder.messageView?.setText(it) }
        action?.let { holder.buttonView?.setText(it) }
        actionEnabled?.let { holder.buttonView?.isEnabled = it }

        holder.buttonView?.setOnClickListener { onClick?.invoke() }
    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        holder.buttonView?.setOnClickListener(null)
    }

    class Holder : EpoxyHolder() {
        var itemView: View? = null
        var messageView: TextView? = null
        var titleView: TextView? = null
        var buttonView: Button? = null

        override fun bindView(itemView: View) {
            this.itemView = itemView
            this.messageView = itemView.findViewById(R.id.message)
            this.titleView = itemView.findViewById(R.id.title)
            this.buttonView = itemView.findViewById(R.id.action)
        }
    }
}
