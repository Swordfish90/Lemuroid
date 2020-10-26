package com.swordfish.lemuroid.app.tv.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game

class TVGamesViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TVGamesViewModel(retrogradeDb) as T
        }
    }

    val systemIds = MutableLiveData<List<String>>()

    val games: LiveData<PagedList<Game>> = Transformations.switchMap(systemIds) {
        if (it.size == 1) {
            LivePagedListBuilder(retrogradeDb.gameDao().selectBySystem(it[0]), 20).build()
        } else {
            LivePagedListBuilder(retrogradeDb.gameDao().selectBySystems(it), 20).build()
        }
    }
}
