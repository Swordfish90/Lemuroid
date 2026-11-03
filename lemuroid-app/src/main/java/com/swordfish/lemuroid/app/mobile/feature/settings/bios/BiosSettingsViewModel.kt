package com.swordfish.lemuroid.app.mobile.feature.settings.bios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.swordfish.lemuroid.lib.bios.BiosManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class BiosSettingsViewModel(private val biosManager: BiosManager) : ViewModel() {
    class Factory
        @Inject
        constructor(private val biosManager: BiosManager) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BiosSettingsViewModel(biosManager) as T
            }
        }

    private val _uiState = MutableStateFlow(BiosManager.BiosInfo(emptyList(), emptyList()))
    val uiState = _uiState

    init {
        refresh()
    }

    fun importPs2File(file: DocumentFile, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val imported = biosManager.importPs2File(file, contentResolver)
            refresh()
            importResult.value = imported
        }
    }

    fun importPs2Folders(root: DocumentFile, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val imported = biosManager.importPs2Folders(root, contentResolver)
            refresh()
            importResult.value = imported
        }
    }

    val importResult = MutableStateFlow<Boolean?>(null)

    private fun refresh() {
        viewModelScope.launch {
            _uiState.value = biosManager.getBiosInfoAsync()
        }
    }
}
