package com.codebutler.retrograde.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LivePagedListBuilder
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase

class HomeViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModel(retrogradeDb) as T
        }
    }

    val favoriteGames = LivePagedListBuilder(retrogradeDb.gameDao().selectFavorites(), 20).build()

    val recentGames = retrogradeDb.gameDao().selectLastRecentlyPlayed(10)
}
