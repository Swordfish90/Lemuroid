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

    val systemId = MutableLiveData<String>()

    val games: LiveData<PagedList<Game>> = Transformations.switchMap(systemId) {
        LivePagedListBuilder(retrogradeDb.gameDao().selectBySystem(it), 20).build()
    }
}
