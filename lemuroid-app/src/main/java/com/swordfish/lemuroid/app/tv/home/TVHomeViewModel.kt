package com.swordfish.lemuroid.app.tv.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.toObservable
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.metaSystemID
import io.reactivex.Observable

class TVHomeViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    companion object {
        const val CAROUSEL_MAX_ITEMS = 10
        const val PAGE_SIZE = 10
    }

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TVHomeViewModel(retrogradeDb) as T
        }
    }

    val recentGames = retrogradeDb.gameDao().rxSelectFirstUnfavoriteRecents(CAROUSEL_MAX_ITEMS)

    val favoritesGames = retrogradeDb.gameDao().selectFavorites().toObservable(PAGE_SIZE)

    val availableSystems: Observable<List<MetaSystemInfo>> = retrogradeDb.gameDao()
        .selectSystemsWithCount()
        .map { systemCounts ->
            systemCounts.filter { (_, count) -> count > 0 }
                .map { (systemId, count) -> GameSystem.findById(systemId).metaSystemID() to count }
                .groupBy { (metaSystemId, _) -> metaSystemId }
                .map { (metaSystemId, counts) -> MetaSystemInfo(metaSystemId, counts.sumBy { it.second }) }
        }
}
