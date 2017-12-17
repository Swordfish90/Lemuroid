/*
 * GDriveBrowseActivity.kt
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

package com.codebutler.retrograde.storage.gdrive

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.codebutler.retrograde.lib.android.RetrogradeActivity
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector

class GDriveBrowseActivity : RetrogradeActivity(), GDriveBrowseFragment.Listener {

    companion object {
        const val BUNDLE_FOLDER_ID = "folder_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.gdrive_activity_browse)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content, GDriveBrowseFragment())
                    .commitNow()
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.content) as GDriveBrowseFragment
        if (!fragment.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onGDriveFolderSelected(folderId: String) {
        val data = Intent()
        data.putExtra(BUNDLE_FOLDER_ID, folderId)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onGDriveError(error: Throwable) {
        val message = when (error) {
            is GoogleJsonResponseException -> error.details.message
            else -> error.message
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    @Subcomponent(modules = [Module::class])
    interface Component : AndroidInjector<GDriveBrowseActivity> {

        @Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<GDriveBrowseActivity>()
    }

    @dagger.Module
    abstract class Module {

        @ContributesAndroidInjector
        abstract fun browseFragment(): GDriveBrowseFragment
    }
}
