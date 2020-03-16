/*
 * PagedListObjectAdapter.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.tv.shared

import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.Presenter
import androidx.paging.AsyncPagedListDiffer
import androidx.paging.PagedList
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback

class PagedListObjectAdapter<T>(
    presenter: Presenter,
    diffCallback: DiffUtil.ItemCallback<T>
) : ObjectAdapter(presenter) {

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

    private val differ = AsyncPagedListDiffer<T>(
            listUpdateCallback,
            AsyncDifferConfig.Builder<T>(diffCallback)
                    .build()
    )

    var pagedList: PagedList<T>?
        get() = differ.currentList
        set(list) { differ.submitList(list) }

    override fun size(): Int = differ.itemCount

    override fun get(position: Int): Any? = differ.getItem(position)
}
