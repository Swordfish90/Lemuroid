package com.swordfish.lemuroid.app.mobile.feature.settings.general

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.app.utils.android.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.indexPreferenceState
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SettingsFragment : Fragment() {

    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    @Inject
    lateinit var directoriesManager: DirectoriesManager

    @Inject
    lateinit var saveSyncManager: SaveSyncManager

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            requireContext().applicationContext,
            saveSyncManager,
            FlowSharedPreferences(
                SharedPreferencesHelper.getLegacySharedPreferences(requireContext())
            )
        )
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun getDisplayNameForFolderUri(uri: String?): String {
        return runCatching {
            DocumentFile.fromTreeUri(requireContext(), Uri.parse(uri))?.name
        }.getOrNull() ?: getString(R.string.none)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val state = settingsViewModel.uiState.collectAsState(SettingsViewModel.State())
                val scanInProgress = settingsViewModel.directoryScanInProgress.observeAsState(false)
                val indexingInProgress = settingsViewModel.indexingInProgress.observeAsState(false)

                val directoryName = remember(state.value.currentDirectory) {
                    getDisplayNameForFolderUri(state.value.currentDirectory)
                }

                SettingsScreen(
                    currentDirectory = directoryName,
                    autoSave = booleanPreferenceState(R.string.pref_key_autosave, true),
                    hdMode = booleanPreferenceState(R.string.pref_key_hd_mode, false),
                    displayFilterIndex = indexPreferenceState(
                        R.string.pref_key_shader_filter,
                        "auto",
                        context.resources.getStringArray(R.array.pref_key_shader_filter_values)
                            .toList()
                    ),
                    hapticFeedbackModeIndex = indexPreferenceState(
                        R.string.pref_key_haptic_feedback_mode,
                        "press",
                        context.resources.getStringArray(R.array.pref_key_haptic_feedback_mode_values)
                            .toList()
                    ),
                    directoryScanInProgress = scanInProgress,
                    indexingInProgress = indexingInProgress,
                    isSaveSyncSupported = state.value.isSaveSyncSupported,
                    onChangeFolder = { handleChangeExternalFolder() },
                    onRescan = { rescanLibrary() },
                    onRescanStop = { stopRescanLibrary() },
                    onOpenGamePadSettings = { handleOpenGamePadSettings() },
                    onOpenSaveSyncSettings = { handleDisplaySaveSync() },
                    onOpenBiosSettings = { handleDisplayBiosInfo() },
                    onOpenCoresSelectionSettings = { handleDisplayCorePage() },
                    onOpenAdvancedSettings = { handleAdvancedSettings() }
                )
            }
        }
    }

    private fun handleAdvancedSettings() {
        findNavController().navigate(R.id.navigation_settings_advanced)
    }

    private fun handleDisplayBiosInfo() {
        findNavController().navigate(R.id.navigation_settings_bios_info)
    }

    private fun handleDisplayCorePage() {
        findNavController().navigate(R.id.navigation_settings_cores_selection)
    }

    private fun handleDisplaySaveSync() {
        findNavController().navigate(R.id.navigation_settings_save_sync)
    }

    private fun handleOpenGamePadSettings() {
        findNavController().navigate(R.id.navigation_settings_gamepad)
    }

    private fun handleChangeExternalFolder() {
        settingsInteractor.changeLocalStorageFolder()
    }

    private fun rescanLibrary() {
        context?.let { LibraryIndexScheduler.scheduleLibrarySync(it) }
    }

    private fun stopRescanLibrary() {
        context?.let { LibraryIndexScheduler.cancelLibrarySync(it) }
    }

    @dagger.Module
    class Module
}
