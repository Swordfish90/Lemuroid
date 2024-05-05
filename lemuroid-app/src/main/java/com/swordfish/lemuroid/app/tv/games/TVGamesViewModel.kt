package com.swordfish.lemuroid.app.tv.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.swordfish.lemuroid.common.paging.buildFlowPaging
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class TVGamesViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {
    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TVGamesViewModel(retrogradeDb) as T
        }
    }

    val metaSystemId = MutableStateFlow<MetaSystemID?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val games: Flow<PagingData<Game>> =
        metaSystemId
            .map { metaSystem -> metaSystem?.systemIDs ?: emptyList() }
            .map { systems -> systems.map { it.dbname } }
            .flatMapLatest {
                when (it.size) {
                    0 -> emptyFlow()
                    1 -> buildFlowPaging(20, viewModelScope) { retrogradeDb.gameDao().selectBySystem(it[0]) }
                    else -> buildFlowPaging(20, viewModelScope) { retrogradeDb.gameDao().selectBySystems(it) }
                }
            }
}
