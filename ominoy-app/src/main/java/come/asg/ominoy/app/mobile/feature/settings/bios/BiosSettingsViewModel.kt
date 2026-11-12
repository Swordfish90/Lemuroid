package come.asg.ominoy.app.mobile.feature.settings.bios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import come.asg.ominoy.lib.bios.BiosManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BiosSettingsViewModel(private val biosManager: BiosManager) : ViewModel() {
    class Factory
        @Inject
        constructor(private val biosManager: BiosManager) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BiosSettingsViewModel(biosManager) as T
            }
        }

    private val mutableUiState = MutableStateFlow(BiosManager.BiosInfo(emptyList(), emptyList()))
    val uiState = mutableUiState.asStateFlow()

    private var refreshJob: Job? = null
    private val biosObserver =
        biosManager.observeBiosChanges { refresh() }.apply {
            startWatching()
        }

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob =
            viewModelScope.launch {
                mutableUiState.value = biosManager.getBiosInfoAsync()
            }
    }

    override fun onCleared() {
        biosObserver.stopWatching()
        super.onCleared()
    }
}
