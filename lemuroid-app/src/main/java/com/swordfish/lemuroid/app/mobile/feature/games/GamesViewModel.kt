package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.GameListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GamesViewModel(
    private val retrogradeDb: RetrogradeDatabase,
    initialMetaSystem: MetaSystemID,
) : ViewModel() {
    class Factory(
        private val retrogradeDb: RetrogradeDatabase,
        private val initialMetaSystem: MetaSystemID,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GamesViewModel(retrogradeDb, initialMetaSystem) as T
        }
    }

    private val metaSystemId = MutableStateFlow(initialMetaSystem)
    private val searchQuery = MutableStateFlow("")
    private val selectedGenres = MutableStateFlow<Set<String>>(emptySet())
    private val sortOrder = MutableStateFlow(GameSortOrder.NAME_ASC)

    fun changeSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun changeSelectedGenres(genres: Set<String>) {
        selectedGenres.value = genres
    }

    fun changeSortOrder(order: GameSortOrder) {
        sortOrder.value = order
    }

    private val systemDbNames = metaSystemId.map { metaSystem -> metaSystem.systemIDs.map { it.dbname } }

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableGenres: StateFlow<List<String>> =
        systemDbNames
            .flatMapLatest { systemIds ->
                if (systemIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    retrogradeDb.gameDao().selectGenresForSystems(systemIds)
                        .map { rawGenres ->
                            rawGenres
                                .asSequence()
                                .flatMap { it.split(GENRE_SPLIT_REGEX).asSequence() }
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .distinctBy { it.lowercase() }
                                .sortedBy { it.lowercase() }
                                .toList()
                        }
                }
            }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val games: StateFlow<List<GameListItem>> =
        combine(
            systemDbNames,
            searchQuery.map { it.trim() },
            selectedGenres,
            sortOrder,
        ) { systemIds, query, genres, order ->
            FilterParams(systemIds, query, genres, order)
        }
            .flatMapLatest { (systemIds, query, genres, order) ->
                if (systemIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val selectedLower = genres.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
                    retrogradeDb.gameDao().selectBySystemsSorted(
                        systemIds = systemIds,
                        query = query,
                        sortOrder = order.name,
                    ).map { list ->
                        if (selectedLower.isEmpty()) {
                            list
                        } else {
                            list.filter { item ->
                                val itemGenres = item.genres
                                    ?.split(GENRE_SPLIT_REGEX)
                                    ?.map { it.trim().lowercase() }
                                    ?.toSet()
                                    ?: emptySet()
                                selectedLower.all { it in itemGenres }
                            }
                        }
                    }
                }
            }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private data class FilterParams(
        val systemIds: List<String>,
        val query: String,
        val genres: Set<String>,
        val sortOrder: GameSortOrder,
    )

    companion object {
        private val GENRE_SPLIT_REGEX = Regex("[,|/•]")
    }
}
