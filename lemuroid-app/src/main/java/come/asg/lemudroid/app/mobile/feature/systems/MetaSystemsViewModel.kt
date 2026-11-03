package come.asg.lemudroid.app.mobile.feature.systems

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import come.asg.lemudroid.app.shared.systems.MetaSystemInfo
import come.asg.lemudroid.lib.library.GameSystem
import come.asg.lemudroid.lib.library.db.RetrogradeDatabase
import come.asg.lemudroid.lib.library.metaSystemID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MetaSystemsViewModel(retrogradeDb: RetrogradeDatabase, appContext: Context) : ViewModel() {
    class Factory(
        val retrogradeDb: RetrogradeDatabase,
        val appContext: Context,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MetaSystemsViewModel(retrogradeDb, appContext) as T
        }
    }

    val availableMetaSystems: Flow<List<MetaSystemInfo>> =
        retrogradeDb.gameDao()
            .selectSystemsWithCount()
            .map { systemCounts ->
                systemCounts.asSequence()
                    .filter { (_, count) -> count > 0 }
                    .mapNotNull {
                            (systemId, count) ->
                        GameSystem.findByIdOrNull(systemId)?.metaSystemID()?.let { it to count }
                    }
                    .groupBy { (metaSystemId, _) -> metaSystemId }
                    .map { (metaSystemId, counts) -> MetaSystemInfo(metaSystemId, counts.sumBy { it.second }) }
                    .sortedBy { it.getName(appContext) }
                    .toList()
            }
}
