package com.swordfish.lemuroid.app.mobile.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase

class HomeViewModel(appContext: Context, retrogradeDb: RetrogradeDatabase) : ViewModel() {

    companion object {
        const val CAROUSEL_MAX_ITEMS = 10
    }

    class Factory(val appContext: Context, val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(appContext, retrogradeDb) as T
        }
    }

    val favoriteGames = retrogradeDb.gameDao().selectFirstFavorites(CAROUSEL_MAX_ITEMS)

    val discoverGames = retrogradeDb.gameDao().selectFirstNotPlayed(CAROUSEL_MAX_ITEMS)

    val recentGames = retrogradeDb.gameDao().selectFirstUnfavoriteRecents(CAROUSEL_MAX_ITEMS)

    val indexingInProgress = PendingOperationsMonitor(appContext).anyLibraryOperationInProgress()
}
