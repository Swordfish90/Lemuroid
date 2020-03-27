package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.lifecycle.Transformations
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game

class GamesViewModel(private val retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GamesViewModel(retrogradeDb) as T
        }
    }

    val systemId = MutableLiveData<String>()

    val games: LiveData<PagedList<Game>> = Transformations.switchMap(systemId) {
        LivePagedListBuilder(retrogradeDb.gameDao().selectBySystem(it), 20).build()
    }
}
