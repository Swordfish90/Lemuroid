package com.swordfish.lemuroid.app.tv.home

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.mobile.feature.systems.SystemInfo
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import io.reactivex.Observable

class TVHomeViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    companion object {
        const val CAROUSEL_MAX_ITEMS = 10
    }

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TVHomeViewModel(retrogradeDb) as T
        }
    }

    private val systemIds = retrogradeDb.gameDao().selectSystems()

    val recentGames = retrogradeDb.gameDao().selectFirstRecents(CAROUSEL_MAX_ITEMS)

    val availableSystems: Observable<List<SystemInfo>> = retrogradeDb.gameDao()
        .selectSystemsWithCount()
        .map { it.filter { (_, count) -> count > 0 }
        .map { (systemId, count) -> SystemInfo(GameSystem.findById(systemId), count) } }
}
