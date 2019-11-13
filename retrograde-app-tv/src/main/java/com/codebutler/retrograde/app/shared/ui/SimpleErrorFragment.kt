/*
 * SimpleErrorFragment.kt
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

package com.codebutler.retrograde.app.shared.ui

import android.os.Bundle
import android.view.View
import androidx.leanback.app.ErrorSupportFragment
import com.codebutler.retrograde.lib.R

class SimpleErrorFragment : ErrorSupportFragment() {

    companion object {
        private const val ARG_MESSAGE = "message"

        fun create(message: String): SimpleErrorFragment {
            val fragment = SimpleErrorFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_MESSAGE, message)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageDrawable = resources.getDrawable(R.drawable.lb_ic_sad_cloud, null)
        message = arguments?.getString(ARG_MESSAGE)
        setDefaultBackground(true)

        buttonText = resources.getString(R.string.dismiss)
        buttonClickListener = View.OnClickListener {
            val fragmentManager = fragmentManager!!
            fragmentManager.beginTransaction()
                    .remove(this@SimpleErrorFragment)
                    .commit()
            fragmentManager.popBackStack()
        }
    }
}
