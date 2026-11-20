package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.lib.cheats.CheatType
import com.swordfish.lemuroid.lib.cheats.CheatValidator
import com.swordfish.lemuroid.lib.cheats.ValidationResult
import com.swordfish.lemuroid.lib.cheats.cht.ChtExportManager
import com.swordfish.lemuroid.lib.cheats.cht.ChtImportManager
import com.swordfish.lemuroid.lib.cheats.cht.ImportMode
import com.swordfish.lemuroid.lib.cheats.cht.ImportPreview
import com.swordfish.lemuroid.lib.cheats.cht.ImportResult
import com.swordfish.lemuroid.lib.library.db.dao.CheatDao
import com.swordfish.lemuroid.lib.library.db.entity.Cheat
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class GameMenuCheatsViewModel(
    val game: Game,
    private val cheatDao: CheatDao,
) : ViewModel() {
    val gameId: Int get() = game.id
    val systemId: String get() = game.systemId

    val cheats: StateFlow<List<Cheat>> =
        cheatDao.getCheatsForGame(game.id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterCodes: StateFlow<List<Cheat>> =
        cheatDao.getMasterCodesForGame(game.id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingCheat = MutableStateFlow<Cheat?>(null)
    val editingCheat: StateFlow<Cheat?> = _editingCheat.asStateFlow()

    private val _importPreview = MutableStateFlow<ImportPreview?>(null)
    val importPreview: StateFlow<ImportPreview?> = _importPreview.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    private val _exportSuccess = MutableStateFlow<Int?>(null)
    val exportSuccess: StateFlow<Int?> = _exportSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _importTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val importTrigger: SharedFlow<Unit> = _importTrigger.asSharedFlow()

    private val _exportTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val exportTrigger: SharedFlow<Unit> = _exportTrigger.asSharedFlow()

    private val _clearAllTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val clearAllTrigger: SharedFlow<Unit> = _clearAllTrigger.asSharedFlow()

    fun triggerImport() {
        _importTrigger.tryEmit(Unit)
    }

    fun triggerExport() {
        _exportTrigger.tryEmit(Unit)
    }

    fun triggerClearAll() {
        _clearAllTrigger.tryEmit(Unit)
    }

    fun clearAllCheats() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cheatDao.deleteCheatsForGame(game.id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear cheats: ${e.message}"
            }
        }
    }

    private val importManager = ChtImportManager(cheatDao)
    private val exportManager = ChtExportManager()

    fun loadChtContentForImport(
        content: String,
        fileName: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existing = cheatDao.getCheatsForGameSync(game.id)
                val preview = importManager.generatePreview(content.byteInputStream(Charsets.UTF_8), fileName, game, existing)
                if (preview.candidates.isEmpty()) {
                    _errorMessage.value = "No cheat entries found in this file"
                } else {
                    _importPreview.value = preview
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to parse cheat file: ${e.message}"
            }
        }
    }

    fun loadChtForImport(
        inputStream: InputStream,
        fileName: String,
    ) {
        try {
            val content = inputStream.bufferedReader(Charsets.UTF_8).readText()
            loadChtContentForImport(content, fileName)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to parse cheat file: ${e.message}"
        }
    }

    fun confirmImport(mode: ImportMode) {
        val preview = _importPreview.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = importManager.executeImport(game, preview, mode)
                _importResult.value = result
                _importPreview.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Import failed: ${e.message}"
            }
        }
    }

    fun dismissImportPreview() {
        _importPreview.value = null
    }

    fun dismissImportResult() {
        _importResult.value = null
    }

    fun dismissExportSuccess() {
        _exportSuccess.value = null
    }

    fun dismissErrorMessage() {
        _errorMessage.value = null
    }

    fun exportToUri(
        uri: Uri,
        contentResolver: ContentResolver,
        cheatsToExport: List<Cheat>? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = cheatsToExport ?: cheatDao.getCheatsForGameSync(game.id)
                contentResolver.openOutputStream(uri)?.use { stream ->
                    exportManager.export(stream, list)
                } ?: throw java.io.IOException("Could not open destination file")
                _exportSuccess.value = list.size
            } catch (e: Exception) {
                _errorMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun exportCheats(
        outputStream: OutputStream,
        cheatsToExport: List<Cheat>? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = cheatsToExport ?: cheatDao.getCheatsForGameSync(game.id)
                exportManager.export(outputStream, list)
                _exportSuccess.value = list.size
            } catch (e: Exception) {
                _errorMessage.value = "Export failed: ${e.message}"
            }
        }
    }

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
                        gameId = game.id,
                        title = trimmedTitle,
                        description = description?.trim()?.ifEmpty { null },
                        systemId = game.systemId,
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
        private val game: Game,
        private val cheatDao: CheatDao,
    ) : ViewModelProvider.Factory {
        constructor(gameMenuRequest: GameMenuActivity.GameMenuRequest, cheatDao: CheatDao) :
            this(gameMenuRequest.game, cheatDao)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameMenuCheatsViewModel(game, cheatDao) as T
        }
    }
}
