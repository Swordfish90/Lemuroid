package com.codebutler.retrograde.app.feature.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codebutler.retrograde.lib.library.GameSystem
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase

class SystemsViewModel(retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SystemsViewModel(retrogradeDb) as T
        }
    }

    val availableSystems = retrogradeDb.gameDao()
            .selectSystems()
            .map { ids -> ids.map { GameSystem.findById(it) } }
            .map { it.filterNotNull() }
}
