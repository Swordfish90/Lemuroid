package com.swordfish.lemuroid.app.tv.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.swordfish.lemuroid.common.paging.buildLiveDataPaging
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game

class TVGamesViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TVGamesViewModel(retrogradeDb) as T
        }
    }

    val systemIds = MutableLiveData<List<String>>()

    val games: LiveData<PagingData<Game>> = Transformations.switchMap(systemIds) { systems ->
        if (systems.size == 1) {
            buildLiveDataPaging(20, viewModelScope) {
                retrogradeDb.gameDao().selectBySystem(systems[0])
            }
        } else {
            buildLiveDataPaging(20, viewModelScope) {
                retrogradeDb.gameDao().selectBySystems(systems)
            }
        }
    }
}
