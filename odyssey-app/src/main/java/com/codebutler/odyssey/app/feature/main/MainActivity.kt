/*
 * MainActivity.kt
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

package com.codebutler.odyssey.app.feature.main

import android.Manifest
import android.os.Bundle
import com.codebutler.odyssey.R
import com.codebutler.odyssey.app.OdysseyApplication
import com.codebutler.odyssey.app.feature.home.HomeFragment
import com.codebutler.odyssey.lib.android.OdysseyActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposeWith
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MainActivity : OdysseyActivity() {

    lateinit var component: MainComponent

    @Inject lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        component = DaggerMainComponent.builder()
                .appComponent(OdysseyApplication.get(this).component)
                .module(MainModule())
                .activity(this)
                .build()
        component.inject(this)

        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposeWith(AndroidLifecycleScopeProvider.from(this))
                .subscribe { granted ->
                    if (granted) {
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.content, HomeFragment())
                                .commit()

                    } else {
                        finish()
                    }
                }
    }
}
