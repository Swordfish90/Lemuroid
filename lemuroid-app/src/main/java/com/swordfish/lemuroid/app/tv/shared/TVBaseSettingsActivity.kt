package com.swordfish.lemuroid.app.tv.shared

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.app.shared.ImmersiveActivity

abstract class TVBaseSettingsActivity : ImmersiveActivity() {
    abstract class BaseSettingsFragmentWrapper : LeanbackSettingsFragmentCompat() {
        override fun onPreferenceStartInitialScreen() {
            startPreferenceFragment(createFragment())
        }

        override fun onPreferenceStartFragment(
            caller: PreferenceFragmentCompat,
            pref: Preference,
        ): Boolean {
            val args = pref.extras
            val f =
                childFragmentManager.fragmentFactory.instantiate(
                    requireActivity().classLoader,
                    pref.fragment!!,
                )
            f.arguments = args
            f.setTargetFragment(caller, 0)
            if (f is PreferenceFragmentCompat || f is PreferenceDialogFragmentCompat) {
                startPreferenceFragment(f)
            } else {
                startImmersiveFragment(f)
            }
            return true
        }

        override fun onPreferenceStartScreen(
            caller: PreferenceFragmentCompat,
            pref: PreferenceScreen,
        ): Boolean {
            val fragment = createFragment()
            val args = Bundle(1)
            args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.key)
            fragment.arguments = args
            startPreferenceFragment(fragment)
            return true
        }

        abstract fun createFragment(): Fragment
    }
}
