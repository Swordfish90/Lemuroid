/*
 * OnboardingFragment.kt
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.retrograde.app.feature.onboarding

import androidx.leanback.app.OnboardingSupportFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codebutler.retrograde.R

class OnboardingFragment : OnboardingSupportFragment() {

    override fun getPageTitle(pageIndex: Int): String = getString(R.string.onboarding_title)

    override fun getPageDescription(pageIndex: Int): String = getString(R.string.onboarding_description)

    override fun onCreateForegroundView(inflater: LayoutInflater, container: ViewGroup): View? = null

    override fun onCreateBackgroundView(inflater: LayoutInflater, container: ViewGroup): View {
        val bgView = View(activity)
        bgView.setBackgroundColor(resources.getColor(R.color.colorPrimaryLight, activity!!.theme))
        return bgView
    }

    override fun getPageCount() = 1

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View = View(activity)

    override fun onFinishFragment() {
        (activity as Listener).onOnboardingComplete()
    }

    interface Listener {
        fun onOnboardingComplete()
    }
}
