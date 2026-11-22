package com.swordfish.lemuroid.app.mobile.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.mobile.feature.games.GameSortOrder
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.GameListItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModel(private val retrogradeDb: RetrogradeDatabase) : ViewModel() {
    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(retrogradeDb) as T
        }
    }

    private val sortOrderFlow = MutableStateFlow(GameSortOrder.NAME_ASC)
    private val searchQueryFlow = MutableStateFlow("")

    val favorites: StateFlow<List<GameListItem>> =
        combine(
            searchQueryFlow.map { it.trim() },
            sortOrderFlow,
        ) { query, sortOrder -> query to sortOrder }
            .flatMapLatest { (query, sortOrder) ->
                retrogradeDb.gameDao().selectFavoritesSorted(query, sortOrder.name)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    fun setSortOrder(sortOrder: GameSortOrder) {
        if (sortOrderFlow.value != sortOrder) {
            sortOrderFlow.value = sortOrder
        }
    }

    fun setSearchQuery(query: String) {
        if (searchQueryFlow.value != query) {
            searchQueryFlow.value = query
        }
    }
}
