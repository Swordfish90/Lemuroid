package com.swordfish.lemuroid.app.mobile.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.swordfish.lemuroid.common.paging.buildFlowPaging
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel(private val retrogradeDb: RetrogradeDatabase) : ViewModel() {
    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(retrogradeDb) as T
        }
    }

    val queryString = MutableStateFlow("")

    enum class UIState { Idle, Loading, Ready }

    val searchResults =
        queryString
            .debounce(400.milliseconds)
            .flatMapLatest {
                buildFlowPaging(20, viewModelScope) {
                    retrogradeDb.gameSearchDao().search(it)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PagingData.empty())

    val searchState: Flow<UIState> =
        queryString
            .flatMapLatest { searchStatesForQuery(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UIState.Idle)

    private fun searchStatesForQuery(query: String): Flow<UIState> {
        if (query.isEmpty()) {
            return flowOf(UIState.Idle)
        }

        return flow {
            emit(UIState.Loading)
            delay(500)
            emit(UIState.Ready)
        }
    }
}
