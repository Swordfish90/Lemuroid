package come.asg.ominoy.app.tv.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import come.asg.ominoy.common.paging.buildFlowPaging
import come.asg.ominoy.lib.library.db.RetrogradeDatabase
import come.asg.ominoy.lib.library.db.entity.Game
import kotlinx.coroutines.flow.Flow

class TVFavoritesViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {
    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TVFavoritesViewModel(retrogradeDb) as T
        }
    }

    val favorites: Flow<PagingData<Game>> =
        buildFlowPaging(20, viewModelScope) { retrogradeDb.gameDao().selectFavorites() }
}
