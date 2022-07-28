package com.swordfish.lemuroid.lib.core

import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import dagger.Lazy
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CoresSelection(private val sharedPreferences: Lazy<SharedPreferences>) {

    suspend fun coroutineCoreConfigForSystem(system: GameSystem) = withContext(Dispatchers.IO) {
        fetchSystemCoreConfig(system)
    }

    @Deprecated("Use the Coroutine variant")
    fun getCoreConfigForSystem(system: GameSystem): Single<SystemCoreConfig> {
        return Single.fromCallable { fetchSystemCoreConfig(system) }
            .subscribeOn(Schedulers.io())
    }

    private fun fetchSystemCoreConfig(system: GameSystem): SystemCoreConfig {
        val setting = sharedPreferences.get()
            .getString(computeSystemPreferenceKey(system.id), null)

        return system.systemCoreConfigs.firstOrNull { it.coreID.coreName == setting }
            ?: system.systemCoreConfigs.first()
    }

    companion object {
        private const val CORE_SELECTION_BINDING_PREFERENCE_BASE_KEY = "pref_key_core_selection"

        fun computeSystemPreferenceKey(systemID: SystemID) =
            "${CORE_SELECTION_BINDING_PREFERENCE_BASE_KEY}_${systemID.dbname}"
    }
}
