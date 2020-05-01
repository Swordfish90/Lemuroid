package com.swordfish.lemuroid.app.tv.settings

import android.content.Context
import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.GamePadBindingsPreferences
import com.swordfish.lemuroid.app.shared.settings.GamePadManager
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class TVSettingsFragment : LeanbackPreferenceFragmentCompat() {

    @Inject lateinit var gamePadBindingsPreferences: GamePadBindingsPreferences
    @Inject lateinit var gamePadManager: GamePadManager

    private lateinit var gamePadBindingsPreferenceScreen: PreferenceScreen

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.tv_settings, rootKey)

        gamePadBindingsPreferenceScreen = findPreference(resources.getString(R.string.pref_key_open_gamepad_bindings))!!

        gamePadManager.getGamePadsObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribe { refreshGamePadBindingsScreen() }
    }

    private fun refreshGamePadBindingsScreen() {
        gamePadBindingsPreferenceScreen.removeAll()
        gamePadBindingsPreferences.addGamePadsPreferencesToScreen(requireContext(), gamePadBindingsPreferenceScreen)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            getString(R.string.pref_key_clear_cores_cache) -> handleClearCacheCores()
            getString(R.string.pref_key_reset_gamepad_bindings) -> handleResetGamePadBindings()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun handleResetGamePadBindings() {
        gamePadBindingsPreferences.resetAllBindings()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { refreshGamePadBindingsScreen() }
    }

    private fun handleClearCacheCores() {
        SettingsInteractor(requireContext())
            .clearCoresCache()
            .doAfterTerminate { activity?.finish() }
            .autoDispose(scope())
            .subscribe()
    }

    @dagger.Module
    class Module
}
