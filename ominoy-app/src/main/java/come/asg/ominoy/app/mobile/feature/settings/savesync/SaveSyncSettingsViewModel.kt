package come.asg.ominoy.app.mobile.feature.settings.savesync

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import come.asg.ominoy.app.shared.library.PendingOperationsMonitor
import come.asg.ominoy.lib.library.CoreID
import come.asg.ominoy.lib.library.GameSystem
import come.asg.ominoy.lib.savesync.SaveSyncManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class SaveSyncSettingsViewModel(
    private val application: Application,
    private val saveSyncManager: SaveSyncManager,
) : ViewModel() {
    class Factory(
        private val application: Application,
        private val saveSyncManager: SaveSyncManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SaveSyncSettingsViewModel(application, saveSyncManager) as T
        }
    }

    val saveSyncInProgress = PendingOperationsMonitor(getContext()).anySaveOperationInProgress()

    data class State(
        val isConfigured: Boolean = false,
        val configInfo: String = "",
        val savesSpace: String = "",
        val lastSyncInfo: String = "",
        val coreNames: List<String> = emptyList(),
        val coreVisibleNames: List<String> = emptyList(),
        val provider: String = "",
        val settingsActivity: Class<out Activity>? = null,
    )

    val uiState =
        flow { emit(buildState()) }
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                State(),
            )

    private fun buildState(): State {
        return State(
            saveSyncManager.isConfigured(),
            saveSyncManager.getConfigInfo(),
            saveSyncManager.computeSavesSpace(),
            saveSyncManager.getLastSyncInfo(),
            computeCoreNames(),
            computeCoreVisibleNames(),
            saveSyncManager.getProvider(),
            saveSyncManager.getSettingsActivity(),
        )
    }

    private fun computeCoreNames(): List<String> {
        return supportedCoreIDs().map { it.coreName }
    }

    private fun computeCoreVisibleNames(): List<String> {
        val context = getContext()
        return supportedCoreIDs().map { saveSyncManager.getDisplayNameForCore(context, it) }
    }

    private fun supportedCoreIDs(): List<CoreID> =
        GameSystem.all().flatMap { it.systemCoreConfigs }.map { it.coreID }.distinct()

    private fun getContext(): Context {
        return application.applicationContext
    }
}
