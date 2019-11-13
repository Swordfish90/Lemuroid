package com.codebutler.retrograde.app.shared.ui

import androidx.leanback.widget.Presenter

interface ItemViewLongClickListener {

    fun onItemLongClicked(itemViewHolder: Presenter.ViewHolder, item: Any): Boolean
}
