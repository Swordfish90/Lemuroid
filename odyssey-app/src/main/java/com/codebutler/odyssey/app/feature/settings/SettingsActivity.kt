package com.codebutler.odyssey.app.feature.settings

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import com.codebutler.odyssey.R
import dagger.Binds
import dagger.Subcomponent
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.FragmentKey
import dagger.android.HasFragmentInjector
import dagger.multibindings.IntoMap
import javax.inject.Inject

class SettingsActivity : Activity(), HasFragmentInjector {
    @Inject lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun fragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

    @Subcomponent(modules = arrayOf(Module::class))
    interface Component : AndroidInjector<SettingsActivity> {

        @Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<SettingsActivity>()
    }

    @dagger.Module(subcomponents = arrayOf(SettingsFragment.PrefFragment.Component::class))
    abstract class Module {
        @Binds
        @IntoMap
        @FragmentKey(SettingsFragment.PrefFragment::class)
        abstract fun settingsPrefFragmentInjectorFactory(builder: SettingsFragment.PrefFragment.Component.Builder):
                AndroidInjector.Factory<out Fragment>
    }
}
