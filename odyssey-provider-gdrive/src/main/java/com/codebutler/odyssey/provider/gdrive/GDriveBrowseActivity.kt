/*
 * GDriveBrowseActivity.kt
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.provider.gdrive

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.codebutler.odyssey.lib.android.OdysseyActivity
import dagger.Subcomponent
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class GDriveBrowseActivity : OdysseyActivity(), GDriveBrowseFragment.Listener, HasSupportFragmentInjector {

    companion object {
        const val BUNDLE_FOLDER_ID = "folder_id"
    }

    @Inject lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

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

    override fun onFolderSelected(folderId: String) {
        val data = Intent()
        data.putExtra(BUNDLE_FOLDER_ID, folderId)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector
}

@Subcomponent(modules = arrayOf(GDriveBrowseFragmentModule::class))
interface GDriveBrowseActivityComponent : AndroidInjector<GDriveBrowseActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<GDriveBrowseActivity>()
}
