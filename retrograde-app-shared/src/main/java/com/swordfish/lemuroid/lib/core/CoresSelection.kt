package com.swordfish.lemuroid.lib.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemID

class CoresSelection(appContext: Context) {
    private val sharedPreferences: SharedPreferences = getDefaultSharedPreferences(appContext)

    private fun getDefaultSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getCoreConfigForSystem(system: GameSystem): SystemCoreConfig {
        val setting = sharedPreferences.getString(computeSystemPreferenceKey(system.id), null)
        val chosen = system.systemCoreConfigs.firstOrNull { it.coreID.coreName == setting }
        return chosen ?: system.systemCoreConfigs.first()
    }

    companion object {
        private const val CORE_SELECTION_BINDING_PREFERENCE_BASE_KEY = "pref_key_core_selection"

        fun computeSystemPreferenceKey(systemID: SystemID) =
            "${CORE_SELECTION_BINDING_PREFERENCE_BASE_KEY}_${systemID.dbname}"
    }
}
