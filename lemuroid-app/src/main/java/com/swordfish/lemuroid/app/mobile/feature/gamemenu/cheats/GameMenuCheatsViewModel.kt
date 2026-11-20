package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.lib.cheats.CheatType
import com.swordfish.lemuroid.lib.cheats.CheatValidator
import com.swordfish.lemuroid.lib.cheats.ValidationResult
import com.swordfish.lemuroid.lib.library.db.dao.CheatDao
import com.swordfish.lemuroid.lib.library.db.entity.Cheat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameMenuCheatsViewModel(
    private val gameId: Int,
    private val systemId: String,
    private val cheatDao: CheatDao,
) : ViewModel() {
    val cheats: StateFlow<List<Cheat>> =
        cheatDao.getCheatsForGame(gameId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterCodes: StateFlow<List<Cheat>> =
        cheatDao.getMasterCodesForGame(gameId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingCheat = MutableStateFlow<Cheat?>(null)
    val editingCheat: StateFlow<Cheat?> = _editingCheat.asStateFlow()

    fun prepareAddCheat() {
        _editingCheat.value = null
    }

    fun prepareEditCheat(cheat: Cheat) {
        _editingCheat.value = cheat
    }

    fun toggleCheat(cheat: Cheat) {
        viewModelScope.launch {
            val newEnabled = !cheat.enabled
            cheatDao.setCheatEnabled(cheat.id, newEnabled)
            if (cheat.isMasterCode && !newEnabled) {
                cheatDao.setDependentCheatsEnabled(cheat.id, false)
            }
        }
    }

    fun deleteCheat(cheat: Cheat) {
        viewModelScope.launch {
            if (cheat.isMasterCode) {
                cheatDao.clearMasterCodeReferences(cheat.id)
            }
            cheatDao.deleteCheat(cheat)
        }
    }

    fun saveCheat(
        title: String,
        description: String?,
        cheatType: CheatType,
        code: String,
        isMasterCode: Boolean,
        masterCodeId: Long?,
        enabled: Boolean,
    ): ValidationResult {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return ValidationResult.Invalid("Cheat name cannot be empty")
        }

        val validation = CheatValidator.validate(cheatType, code)
        if (validation is ValidationResult.Invalid) {
            return validation
        }

        viewModelScope.launch {
            val currentEditing = _editingCheat.value
            val cheatToSave =
                if (currentEditing != null) {
                    currentEditing.copy(
                        title = trimmedTitle,
                        description = description?.trim()?.ifEmpty { null },
                        cheatType = cheatType.id,
                        code = code.trim(),
                        isMasterCode = isMasterCode,
                        masterCodeId = if (isMasterCode) null else masterCodeId,
                        enabled = enabled,
                    )
                } else {
                    Cheat(
                        gameId = gameId,
                        title = trimmedTitle,
                        description = description?.trim()?.ifEmpty { null },
                        systemId = systemId,
                        cheatType = cheatType.id,
                        code = code.trim(),
                        isMasterCode = isMasterCode,
                        masterCodeId = if (isMasterCode) null else masterCodeId,
                        enabled = enabled,
                    )
                }

            if (currentEditing != null) {
                cheatDao.updateCheat(cheatToSave)
            } else {
                cheatDao.insertCheat(cheatToSave)
            }
            _editingCheat.value = null
        }

        return ValidationResult.Valid
    }

    class Factory(
        private val gameId: Int,
        private val systemId: String,
        private val cheatDao: CheatDao,
    ) : ViewModelProvider.Factory {
        constructor(gameMenuRequest: GameMenuActivity.GameMenuRequest, cheatDao: CheatDao) :
            this(gameMenuRequest.game.id, gameMenuRequest.game.systemId, cheatDao)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameMenuCheatsViewModel(gameId, systemId, cheatDao) as T
        }
    }
}
