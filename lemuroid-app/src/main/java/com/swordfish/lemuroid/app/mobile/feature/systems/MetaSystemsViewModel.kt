package com.swordfish.lemuroid.app.mobile.feature.systems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.metaSystemID
import io.reactivex.Observable

class MetaSystemsViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MetaSystemsViewModel(retrogradeDb) as T
        }
    }

    val availableMetaSystems: Observable<List<MetaSystemInfo>> = retrogradeDb.gameDao()
        .selectSystemsWithCount()
        .map { systemCounts ->
            systemCounts
                .asSequence()
                .filter { (_, count) -> count > 0 }
                .map { (systemId, count) -> GameSystem.findById(systemId).metaSystemID() to count }
                .groupBy { (metaSystemId, _) -> metaSystemId }
                .map { (metaSystemId, counts) -> MetaSystemInfo(metaSystemId, counts.sumBy { it.second }) }
                .sortedBy { it.metaSystem.sortOrder }
                .toList()
        }
}
