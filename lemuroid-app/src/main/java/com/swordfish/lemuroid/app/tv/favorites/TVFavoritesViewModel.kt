package com.swordfish.lemuroid.app.tv.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.swordfish.lemuroid.common.paging.buildLiveDataPaging
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game

class TVFavoritesViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TVFavoritesViewModel(retrogradeDb) as T
        }
    }

    val favorites: LiveData<PagingData<Game>> = buildLiveDataPaging(20, viewModelScope) {
        retrogradeDb.gameDao().selectFavorites()
    }
}
