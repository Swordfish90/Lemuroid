package com.codebutler.odyssey.app.feature.settings

import android.os.Bundle
import com.codebutler.odyssey.R
import com.codebutler.odyssey.lib.android.OdysseyActivity
import dagger.android.ContributesAndroidInjector

class SettingsActivity : OdysseyActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    @dagger.Module
    abstract class Module {

        @ContributesAndroidInjector(modules = arrayOf(SettingsFragment.Module::class))
        abstract fun settingsFragment(): SettingsFragment
    }
}
