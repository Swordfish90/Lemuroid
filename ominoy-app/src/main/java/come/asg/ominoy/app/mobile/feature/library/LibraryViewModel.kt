package come.asg.ominoy.app.mobile.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.filter
import come.asg.ominoy.common.paging.buildFlowPaging
import come.asg.ominoy.lib.library.SystemID
import come.asg.ominoy.lib.library.db.RetrogradeDatabase
import come.asg.ominoy.lib.library.db.entity.Game
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class LibraryViewModel(private val database: RetrogradeDatabase) : ViewModel() {
    class Factory(private val database: RetrogradeDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LibraryViewModel(database) as T
    }

    val filter = MutableStateFlow("All")
    val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val games: Flow<PagingData<Game>> =
        combine(filter, query.debounce(300)) { selectedFilter, searchQuery ->
            selectedFilter to searchQuery.trim()
        }.flatMapLatest { (selectedFilter, searchQuery) ->
            val results =
                if (searchQuery.isNotEmpty()) {
                    buildFlowPaging(30, viewModelScope) {
                        database.gameSearchDao().search(searchQuery)
                    }
                } else {
                    when (selectedFilter) {
                        "Favorites" -> buildFlowPaging(30, viewModelScope) { database.gameDao().selectFavorites() }
                        "GB" -> buildFlowPaging(30, viewModelScope) { database.gameDao().selectBySystem(SystemID.GB.dbname) }
                        "GBC" -> buildFlowPaging(30, viewModelScope) { database.gameDao().selectBySystem(SystemID.GBC.dbname) }
                        "GBA" -> buildFlowPaging(30, viewModelScope) { database.gameDao().selectBySystem(SystemID.GBA.dbname) }
                        else -> buildFlowPaging(30, viewModelScope) {
                            database.gameDao().selectBySystems(SystemID.values().map { it.dbname })
                        }
                    }
                }

            results.map { pagingData ->
                pagingData.filter { game ->
                    val matchesFilter =
                        when (selectedFilter) {
                            "Favorites" -> game.isFavorite
                            "GB" -> game.systemId == SystemID.GB.dbname
                            "GBC" -> game.systemId == SystemID.GBC.dbname
                            "GBA" -> game.systemId == SystemID.GBA.dbname
                            else -> true
                        }
                    val matchesQuery =
                        searchQuery.isEmpty() ||
                            game.displayName().contains(searchQuery, ignoreCase = true) ||
                            game.systemId.contains(searchQuery, ignoreCase = true)
                    matchesFilter && matchesQuery
                }
            }
        }

    fun selectFilter(value: String) {
        filter.value = value
    }

    fun updateQuery(value: String) {
        query.value = value
    }
}
