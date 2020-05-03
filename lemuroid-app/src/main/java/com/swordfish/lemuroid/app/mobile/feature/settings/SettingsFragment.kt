package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.library.LibraryIndexWork
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject lateinit var settingsInteractor: SettingsInteractor
    @Inject lateinit var rxSharedPreferences: RxSharedPreferences

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.mobile_settings, rootKey)
    }

    override fun onResume() {
        super.onResume()

        val settingsViewModel = ViewModelProviders.of(this, SettingsViewModel.Factory(context!!, rxSharedPreferences))
                .get(SettingsViewModel::class.java)

        val currentDirectory: Preference? = findPreference(getString(R.string.pref_key_extenral_folder))
        val rescanPreference: Preference? = findPreference(getString(R.string.pref_key_rescan))

        settingsViewModel.currentFolder
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribe {
                    currentDirectory?.summary = getDisplayNameForFolderUri(Uri.parse(it)) ?: getString(R.string.none)
                }

        settingsViewModel.indexingInProgress.observe(this, Observer {
            rescanPreference?.isEnabled = !it
            currentDirectory?.isEnabled = !it
        })
    }

    private fun getDisplayNameForFolderUri(uri: Uri) = DocumentFile.fromTreeUri(context!!, uri)?.name

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            getString(R.string.pref_key_rescan) -> handleRescan()
            getString(R.string.pref_key_extenral_folder) -> handleChangeExternalFolder()
            getString(R.string.pref_key_clear_cores_cache) -> handleClearCacheCores()
            getString(R.string.pref_key_open_gamepad_bindings) -> handleOpenGamepadBindings()
            getString(R.string.pref_key_display_bios_info) -> handleDisplayBiosInfo()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun handleDisplayBiosInfo() {
        findNavController().navigate(R.id.navigation_settings_bios_info)
    }

    private fun handleOpenGamepadBindings() {
        findNavController().navigate(R.id.navigation_settings_gamepad)
    }

    private fun handleChangeExternalFolder() {
        settingsInteractor.changeLocalStorageFolder()
    }

    private fun handleRescan() {
        context?.let { LibraryIndexWork.enqueueUniqueWork(it) }
    }

    private fun handleClearCacheCores() {
        activity?.let {
            SettingsInteractor(it)
                    .clearCoresCache()
                    .doAfterTerminate { displayClearCoreCacheMessage() }
                    .autoDispose(scope())
                    .subscribe()
        }
    }

    private fun displayClearCoreCacheMessage() {
        Toast.makeText(activity, R.string.clear_cores_cache_success, Toast.LENGTH_SHORT).show()
    }

    @dagger.Module
    class Module
}
