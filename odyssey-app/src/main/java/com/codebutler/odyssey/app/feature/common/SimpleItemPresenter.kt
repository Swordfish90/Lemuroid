/*
 * SettingsPresenter.kt
 *
 * Copyright (C) 2017 Odyssey Project
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.app.feature.common

import android.graphics.Color
import android.support.annotation.StringRes
import android.support.v17.leanback.widget.Presenter
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.codebutler.odyssey.R

sealed class TextOrResource {
    class Text(val value: String) : TextOrResource()
    class Resource(@StringRes val value: Int) : TextOrResource()
}

open class SimpleItem(val title: TextOrResource) {
    constructor(text: String) : this(TextOrResource.Text(text))
    constructor(@StringRes resId: Int) : this (TextOrResource.Resource(resId))
}

class SimpleItemPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = TextView(parent.context)

        val res = parent.resources
        val width = res.getDimensionPixelSize(R.dimen.card_width)
        val height = res.getDimensionPixelSize(R.dimen.card_height)

        view.layoutParams = ViewGroup.LayoutParams(width, height)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.setBackgroundColor(ContextCompat.getColor(parent.context,
                R.color.default_background))
        view.setTextColor(Color.WHITE)
        view.gravity = Gravity.CENTER
        return Presenter.ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val textView = viewHolder.view as TextView
        val title = (item as SimpleItem).title
        textView.text = when (title) {
            is TextOrResource.Text -> title.value
            is TextOrResource.Resource -> textView.resources.getString(title.value)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) { }
}
