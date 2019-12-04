package com.swordfish.lemuroid.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase

class HomeViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    companion object {
        val CAROUSEL_MAX_ITEMS = 10
    }

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModel(retrogradeDb) as T
        }
    }

    val favoriteGames = retrogradeDb.gameDao().selectFirstFavorites(CAROUSEL_MAX_ITEMS)

    val discoverGames = retrogradeDb.gameDao().selectFirstNotPlayed(CAROUSEL_MAX_ITEMS)

    val recentGames = retrogradeDb.gameDao().selectFirstRecents(CAROUSEL_MAX_ITEMS)
}
