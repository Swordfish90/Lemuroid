package com.codebutler.retrograde.app.shared.ui

import android.support.v17.leanback.widget.Presenter

interface ItemViewLongClickListener {

    fun onItemLongClicked(itemViewHolder: Presenter.ViewHolder, item: Any): Boolean
}
