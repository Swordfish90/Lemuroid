package com.codebutler.retrograde.app.feature.settings

import android.os.Bundle
import com.codebutler.retrograde.R
import com.codebutler.retrograde.lib.android.RetrogradeActivity
import com.codebutler.retrograde.lib.injection.PerFragment
import dagger.android.ContributesAndroidInjector

class SettingsActivity : RetrogradeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    @dagger.Module
    abstract class Module {

        @PerFragment
        @ContributesAndroidInjector(modules = [SettingsFragment.Module::class])
        abstract fun settingsFragment(): SettingsFragment
    }
}
