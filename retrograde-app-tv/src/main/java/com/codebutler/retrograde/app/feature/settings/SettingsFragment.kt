package com.codebutler.retrograde.app.feature.settings

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.leanback.preference.LeanbackPreferenceFragment
import androidx.leanback.preference.LeanbackSettingsFragment
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceScreen
import com.codebutler.retrograde.R
import com.codebutler.retrograde.lib.injection.PerChildFragment
import com.codebutler.retrograde.lib.storage.StorageProviderRegistry
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
        startPreferenceFragment(PrefScreenFragment())
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragment, pref: PreferenceScreen): Boolean {
        val fragment = PrefScreenFragment()
        fragment.arguments = Bundle().apply {
            putString(LeanbackPreferenceFragment.ARG_PREFERENCE_ROOT, pref.key)
        }
        startPreferenceFragment(fragment)
        return true
    }

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
        abstract fun prefScreenFragment(): PrefScreenFragment
    }

    class PrefScreenFragment : LeanbackPreferenceFragment() {

        @Inject lateinit var storageProviderRegistry: StorageProviderRegistry

        override fun onAttach(context: Context?) {
            AndroidInjection.inject(this)
            super.onAttach(context)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs, rootKey)

            if (rootKey == null) {
                val themeTypedValue = TypedValue()
                activity.theme.resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true)
                val contextThemeWrapper = ContextThemeWrapper(activity, themeTypedValue.resourceId)

                val sourcesCategory = findPreference(getString(R.string.pref_key_sources)) as PreferenceCategory
                sourcesCategory.preferenceManager.sharedPreferencesName = StorageProviderRegistry.PREF_NAME

                val providers = storageProviderRegistry.providers.sortedBy { it.name }
                for (provider in providers) {
                    val pref = MasterSwitchPreference(contextThemeWrapper)
                    pref.setDefaultValue(provider.enabledByDefault)
                    pref.key = provider.id
                    pref.title = provider.name
                    val prefsFragmentClass = provider.prefsFragmentClass
                    if (prefsFragmentClass != null) {
                        pref.fragment = prefsFragmentClass.name
                    }
                    sourcesCategory.addPreference(pref)
                }
            }
        }

        override fun onResume() {
            super.onResume()
            if (preferenceScreen.key == null) {
                val defaultPrefs = getDefaultSharedPreferences(activity)
                val showLogPref = findPreference(getString(R.string.pref_key_advanced_log))
                val loggingEnabledKey = getString(R.string.pref_key_flags_logging)
                showLogPref.isVisible = defaultPrefs.getBoolean(loggingEnabledKey, false)
            }
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            return when (preference.key) {
                getString(R.string.pref_key_advanced_log) -> {
                    startActivity(Intent(activity, DebugLogActivity::class.java))
                    true
                }
                else -> super.onPreferenceTreeClick(preference)
            }
        }
    }
}
