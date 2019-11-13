package com.codebutler.retrograde.app.feature.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.entity.Game

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
