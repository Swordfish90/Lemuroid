package com.swordfish.lemuroid.app.mobile.feature.games

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

class GamesViewModel(private val retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GamesViewModel(retrogradeDb) as T
        }
    }

    val systemIds = MutableLiveData<List<String>>()

    val games: LiveData<PagingData<Game>> = Transformations.switchMap(systemIds) {
        if (it.size == 1) {
            buildLiveDataPaging(20, viewModelScope) { retrogradeDb.gameDao().selectBySystem(it[0]) }
        } else {
            buildLiveDataPaging(20, viewModelScope) { retrogradeDb.gameDao().selectBySystems(it) }
        }
    }
}
