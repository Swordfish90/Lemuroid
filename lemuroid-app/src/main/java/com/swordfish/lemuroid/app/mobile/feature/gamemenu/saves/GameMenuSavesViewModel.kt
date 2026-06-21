package com.swordfish.lemuroid.app.mobile.feature.gamemenu.saves

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SaveStatesExporter
import com.swordfish.lemuroid.lib.saves.SaveStatesImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameMenuSavesViewModel(
    application: Application,
    private val game: Game,
    private val coreID: CoreID,
    private val exporter: SaveStatesExporter,
    private val importer: SaveStatesImporter,
) : AndroidViewModel(application) {

    sealed class TransferState {
        object Idle : TransferState()
        object Working : TransferState()
        data class Success(val message: String) : TransferState()
        data class Error(val message: String) : TransferState()
    }

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState: StateFlow<TransferState> = _transferState

    fun exportSaves(uri: Uri) {
        viewModelScope.launch {
            _transferState.value = TransferState.Working
            val result = exporter.exportToUri(getApplication(), game, coreID, uri)
            _transferState.value = result.fold(
                onSuccess = { count -> TransferState.Success(count.toString()) },
                onFailure = { e -> TransferState.Error(e.message ?: "Unknown error") },
            )
        }
    }

    fun importSaves(uri: Uri) {
        viewModelScope.launch {
            _transferState.value = TransferState.Working
            val result = importer.importFromUri(getApplication(), game, coreID, uri)
            _transferState.value = result.fold(
                onSuccess = { count -> TransferState.Success(count.toString()) },
                onFailure = { e -> TransferState.Error(e.message ?: "Unknown error") },
            )
        }
    }

    class Factory(
        private val application: Application,
        private val game: Game,
        private val coreID: CoreID,
        private val exporter: SaveStatesExporter,
        private val importer: SaveStatesImporter,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return GameMenuSavesViewModel(application, game, coreID, exporter, importer) as T
        }
    }
}
