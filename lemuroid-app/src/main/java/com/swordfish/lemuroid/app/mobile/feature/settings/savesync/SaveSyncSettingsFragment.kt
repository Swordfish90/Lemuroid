package com.swordfish.lemuroid.app.mobile.feature.settings.savesync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SaveSyncSettingsFragment : Fragment() {

    @Inject
    lateinit var directoriesManager: DirectoriesManager

    @Inject
    lateinit var saveSyncManager: SaveSyncManager

    private val saveSyncSettingsViewModel: SaveSyncSettingsViewModel by viewModels {
        SaveSyncSettingsViewModel.Factory(requireActivity().application, saveSyncManager)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val saveSyncState = saveSyncSettingsViewModel.uiState.collectAsState()
                val indexingState = saveSyncSettingsViewModel.indexingInProgress.observeAsState(true)

                SaveSyncSettingsScreen(
                    indexingState.value,
                    saveSyncState.value,
                    onConfigureClicked = { handleSaveSyncConfigure() },
                    onManualSyncClicked = { handleSaveSyncRefresh() }
                )
            }
        }
    }

    private fun handleSaveSyncConfigure() {
        startActivity(
            Intent(activity, saveSyncManager.getSettingsActivity())
        )
    }

    private fun handleSaveSyncRefresh() {
        SaveSyncWork.enqueueManualWork(requireContext().applicationContext)
    }

    @dagger.Module
    class Module
}
