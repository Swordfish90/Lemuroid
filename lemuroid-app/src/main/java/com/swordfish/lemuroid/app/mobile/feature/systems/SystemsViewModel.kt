package com.swordfish.lemuroid.app.mobile.feature.systems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.shared.systems.SystemInfo
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import io.reactivex.Observable

class SystemsViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SystemsViewModel(retrogradeDb) as T
        }
    }

    val availableSystems: Observable<List<SystemInfo>> = retrogradeDb.gameDao()
            .selectSystemsWithCount()
            .map { it.filter { (_, count) -> count > 0 }
            .map { (systemId, count) -> SystemInfo(GameSystem.findById(systemId), count) } }
}
