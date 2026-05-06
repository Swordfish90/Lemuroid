/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.appextension

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.swordfish.lemuroid.R

object InstallBrowserDialogBuilder {

	private var dialog: AlertDialog? = null

	fun show(context: Context, onPositiveClicked: () -> Unit) {
		val view = LayoutInflater.from(context)
			.inflate(R.layout.install_browser_dialog_layout, null)

		view.findViewById<TextView>(R.id.descriptionTextView).text =
			context.getString(R.string.install_browser_description).replace("\\n", "\n")

		val dialog = AlertDialog
			.Builder(context)
			.setView(view)
			.setPositiveButton(R.string.install_submit) { _, _ ->
				onPositiveClicked.invoke()
			}
			.setNegativeButton(R.string.rate_cancel) { _, _ -> }
			.create()

		dialog.setOnShowListener {
			dialog.getButton(AlertDialog.BUTTON_POSITIVE)
				?.setTextColor(ContextCompat.getColor(context, R.color.textColorAccent))
			dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
				?.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondary))
		}
		dialog.show()
	}

	private fun setButtonColor(buttonId: Int, color: Int) {
		dialog?.getButton(buttonId)?.setTextColor(color)
	}
}
