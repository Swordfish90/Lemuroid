package com.swordfish.lemuroid.app.mobile.feature.gamemenu.patchcodes

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.lib.cheats.PatchCodesManager
import com.swordfish.lemuroid.lib.library.db.entity.PatchCode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameMenuPatchCodesViewModel(
    private val gameId: Int,
    private val patchCodesManager: PatchCodesManager,
) : ViewModel() {

    val codes: StateFlow<List<PatchCode>> =
        patchCodesManager
            .getCodesForGame(gameId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _operationComplete = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val operationComplete: SharedFlow<Unit> = _operationComplete.asSharedFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun addCode(description: String, code: String) {
        viewModelScope.launch {
            patchCodesManager.saveCode(
                PatchCode(
                    gameId = gameId,
                    description = description.trim(),
                    code = code.trim().uppercase(),
                    enabled = true,
                ),
            )
            _operationComplete.emit(Unit)
        }
    }

    fun toggleCode(patch: PatchCode) {
        viewModelScope.launch {
            patchCodesManager.updateCode(patch.copy(enabled = !patch.enabled))
            _operationComplete.emit(Unit)
        }
    }

    fun deleteCode(patch: PatchCode) {
        viewModelScope.launch {
            patchCodesManager.deleteCode(patch)
            _operationComplete.emit(Unit)
        }
    }

    fun importFromFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val imported = patchCodesManager.importFromUri(context, uri, gameId)
                _importState.value = ImportState.Success(imported.size)
                _operationComplete.emit(Unit)
            } catch (e: PatchCodesManager.ImportException) {
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    fun clearImportState() {
        _importState.value = ImportState.Idle
    }

    sealed class ImportState {
        object Idle : ImportState()
        object Loading : ImportState()
        data class Success(val count: Int) : ImportState()
        data class Error(val message: String) : ImportState()
    }

    class Factory(
        private val gameId: Int,
        private val patchCodesManager: PatchCodesManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameMenuPatchCodesViewModel(gameId, patchCodesManager) as T
        }
    }
}
