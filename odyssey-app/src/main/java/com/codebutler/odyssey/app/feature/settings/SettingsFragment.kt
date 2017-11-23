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
import com.codebutler.odyssey.lib.library.provider.GameLibraryProviderRegistry
import dagger.Subcomponent
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import javax.inject.Inject

class SettingsFragment : LeanbackSettingsFragment() {

    override fun onPreferenceStartInitialScreen() {
        startPreferenceFragment(PrefFragment())
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragment, pref: PreferenceScreen): Boolean = false

    override fun onPreferenceStartFragment(caller: PreferenceFragment, pref: Preference): Boolean {
        val fragment = Class.forName(pref.fragment).newInstance() as Fragment
        startPreferenceFragment(fragment)
        return true
    }

    class PrefFragment : LeanbackPreferenceFragment() {

        @Inject lateinit var libraryProviderRegistry: GameLibraryProviderRegistry

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
            for (provider in libraryProviderRegistry.providers) {
                val prefsFragmentClass = provider.prefsFragmentClass
                if (prefsFragmentClass != null) {
                    val pref = preferenceManager.createPreferenceScreen(contextThemeWrapper)
                    pref.title = provider.name
                    pref.fragment = prefsFragmentClass.name
                    sourcesCategory.addPreference(pref)
                }
            }
        }

        @Subcomponent
        interface Component : AndroidInjector<PrefFragment> {

            @Subcomponent.Builder
            abstract class Builder : AndroidInjector.Builder<PrefFragment>()
        }
    }
}
