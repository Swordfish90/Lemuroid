package com.codebutler.odyssey.app.shared

import android.support.v17.leanback.widget.Presenter

interface ItemViewLongClickListener {

    fun onItemLongClicked(itemViewHolder: Presenter.ViewHolder, item: Any): Boolean
}
