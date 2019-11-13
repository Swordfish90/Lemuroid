package com.codebutler.retrograde.app.feature.home

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.codebutler.retrograde.R

@EpoxyModelClass(layout = R.layout.layout_home_section)
abstract class EpoxyHomeSection : EpoxyModelWithHolder<EpoxyHomeSection.Holder>() {

    @EpoxyAttribute
    var title: Int? = null

    override fun bind(holder: Holder) {
        title?.let {
            holder.titleView?.setText(it)
        }
    }

    class Holder : EpoxyHolder() {
        var titleView: TextView? = null

        override fun bindView(itemView: View) {
            titleView = itemView.findViewById(R.id.text)
        }
    }
}
