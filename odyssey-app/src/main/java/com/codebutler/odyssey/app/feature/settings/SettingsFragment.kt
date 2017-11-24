package com.codebutler.odyssey.app.feature.settings

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.v14.preference.PreferenceFragment
import android.support.v17.preference.LeanbackPreferenceFragment
import android.support.v17.preference.LeanbackSettingsFragment
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.support.v7.preference.PreferenceScreen
import android.util.TypedValue
import android.view.ContextThemeWrapper
import com.codebutler.odyssey.R
import com.codebutler.odyssey.lib.injection.PerChildFragment
import com.codebutler.odyssey.lib.storage.StorageProviderRegistry
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import javax.inject.Inject

class SettingsFragment : LeanbackSettingsFragment(), HasFragmentInjector {

    @Inject lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onAttach(context: Context?) {
        AndroidInjection.inject(this)
        super.onAttach(context)
    }

    override fun onPreferenceStartInitialScreen() {
        startPreferenceFragment(PrefFragment())
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragment, pref: PreferenceScreen): Boolean = false

    override fun onPreferenceStartFragment(caller: PreferenceFragment, pref: Preference): Boolean {
        val fragment = Class.forName(pref.fragment).newInstance() as Fragment
        startPreferenceFragment(fragment)
        return true
    }

    override fun fragmentInjector(): AndroidInjector<Fragment> = childFragmentInjector

    @dagger.Module
    abstract class Module {

        @PerChildFragment
        @ContributesAndroidInjector
        abstract fun prefFragment(): PrefFragment
    }

    class PrefFragment : LeanbackPreferenceFragment() {

        @Inject lateinit var storageProviderRegistry: StorageProviderRegistry

        override fun onAttach(context: Context?) {
            AndroidInjection.inject(this)
            super.onAttach(context)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.prefs)

            val themeTypedValue = TypedValue()
            activity.theme.resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true)
            val contextThemeWrapper = ContextThemeWrapper(activity, themeTypedValue.resourceId)

            val sourcesCategory = findPreference("sources") as PreferenceCategory
            for (provider in storageProviderRegistry.providers) {
                val prefsFragmentClass = provider.prefsFragmentClass
                if (prefsFragmentClass != null) {
                    val pref = preferenceManager.createPreferenceScreen(contextThemeWrapper)
                    pref.title = provider.name
                    pref.fragment = prefsFragmentClass.name
                    sourcesCategory.addPreference(pref)
                }
            }
        }
    }
}
