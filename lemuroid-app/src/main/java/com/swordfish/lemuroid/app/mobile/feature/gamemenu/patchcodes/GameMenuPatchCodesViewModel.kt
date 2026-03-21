package com.swordfish.lemuroid.app.mobile.feature.gamemenu.patchcodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.lib.cheats.PatchCodesManager
import com.swordfish.lemuroid.lib.library.db.entity.PatchCode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
        }
    }

    fun toggleCode(patch: PatchCode) {
        viewModelScope.launch {
            patchCodesManager.updateCode(patch.copy(enabled = !patch.enabled))
        }
    }

    fun deleteCode(patch: PatchCode) {
        viewModelScope.launch {
            patchCodesManager.deleteCode(patch)
        }
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
