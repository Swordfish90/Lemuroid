package com.swordfish.lemuroid.app.tv.search

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

class TVSearchViewModel(private val retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TVSearchViewModel(retrogradeDb) as T
        }
    }

    val queryString = MutableLiveData<String>()

    val searchResults: LiveData<PagingData<Game>> = Transformations.switchMap(queryString) {
        buildLiveDataPaging(20, viewModelScope) { retrogradeDb.gameSearchDao().search(it) }
    }
}
