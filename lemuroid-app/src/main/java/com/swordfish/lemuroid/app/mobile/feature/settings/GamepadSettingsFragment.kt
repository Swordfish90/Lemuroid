package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.GamePadBindingsPreferences
import com.swordfish.lemuroid.app.shared.settings.GamePadManager
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class GamepadSettingsFragment : PreferenceFragmentCompat() {

    @Inject lateinit var gamePadBindingsPreferences: GamePadBindingsPreferences
    @Inject lateinit var gamePadManager: GamePadManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.empty_preference_screen, rootKey)
        gamePadManager.getGamePadsObservable()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { refreshBindings() }
    }

    private fun refreshBindings() {
        preferenceScreen.removeAll()
        gamePadBindingsPreferences.addGamePadsPreferencesToScreen(requireContext(), preferenceScreen)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.pref_key_reset_gamepad_bindings) -> handleResetBindings()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun handleResetBindings() {
        gamePadBindingsPreferences.resetAllBindings()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { refreshBindings() }
    }

    @dagger.Module
    class Module
}
