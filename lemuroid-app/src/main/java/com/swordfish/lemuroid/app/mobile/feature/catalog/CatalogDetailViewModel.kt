package com.swordfish.lemuroid.app.mobile.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CatalogDetailViewModel(
    private val gameId: Int,
    private val retrogradeDb: RetrogradeDatabase,
) : ViewModel() {

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game

    init {
        viewModelScope.launch {
            _game.value = retrogradeDb.gameDao().selectById(gameId)
        }
    }

    class Factory(
        private val gameId: Int,
        private val retrogradeDb: RetrogradeDatabase,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CatalogDetailViewModel(gameId, retrogradeDb) as T
        }
    }
}
