package com.codebutler.odyssey.lib.ui

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapterHelper
import android.support.v17.leanback.widget.ObjectAdapter
import android.support.v17.leanback.widget.Presenter
import android.support.v7.recyclerview.extensions.DiffCallback
import android.support.v7.recyclerview.extensions.ListAdapterConfig
import android.support.v7.util.ListUpdateCallback

class PagedListObjectAdapter<T>(presenter: Presenter, diffCallback: DiffCallback<T>)
    : ObjectAdapter(presenter) {

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {
            notifyItemRangeChanged(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyChanged()
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }
    }

    private val helper = PagedListAdapterHelper(listUpdateCallback, ListAdapterConfig.Builder<T>()
            .setDiffCallback(diffCallback)
            .build())

    var pagedList: PagedList<T>?
        get() = helper.currentList
        set(list) { helper.setList(list) }

    override fun size(): Int = helper.itemCount

    override fun get(position: Int): Any? = helper.getItem(position)
}
