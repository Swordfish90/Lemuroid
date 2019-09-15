package com.codebutler.retrograde.app.feature.search

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.entity.Game

class SearchViewModel(private val retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SearchViewModel(retrogradeDb) as T
        }
    }

    val queryString = MutableLiveData<String>()

    val searchResults: LiveData<PagedList<Game>> = Transformations.switchMap(queryString) {
        LivePagedListBuilder(retrogradeDb.gameSearchDao().search(it), 20).build()
    }
}
