package com.swordfish.lemuroid.lib.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.lib.library.GameSystem
import io.reactivex.Single
import java.security.InvalidParameterException

class CoreVariablesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getCoreOptionsForSystem(system: GameSystem): Single<List<CoreVariable>> {
        val defaultMap = convertCoreVariablesToMap(system.defaultSettings)
        return retrieveCustomCoreVariables(system)
            .map { convertCoreVariablesToMap(it) }
            .map { defaultMap + it }
            .map { convertMapToCoreVariables(it) }
    }

    private fun convertMapToCoreVariables(variablesMap: Map<String, String>): List<CoreVariable> {
        return variablesMap.entries.map { CoreVariable(it.key, it.value) }
    }

    private fun convertCoreVariablesToMap(coreVariables: List<CoreVariable>): Map<String, String> {
        return coreVariables
            .map { it.key to it.value }
            .toMap()
    }

    private fun retrieveCustomCoreVariables(system: GameSystem) = Single.fromCallable {
        val requestedKeys = system.exposedSettings
                .map { computeSharedPreferenceKey(it, system.id.dbname) }

        sharedPreferences.all.filter { it.key in requestedKeys }
                .map { (key, value) ->
                    val value = when (value!!) {
                        is Boolean -> if (value as Boolean) "enabled" else "disabled"
                        is String -> value as String
                        else -> throw InvalidParameterException("Invalid setting in SharedPreferences")
                    }
                    CoreVariable(computeOriginalKey(key, system.id.dbname), value)
                }
    }

    companion object {
        private const val RETRO_OPTION_PREFIX = "cv"

        fun computeSharedPreferenceKey(retroVariableName: String, systemID: String): String {
            return "${computeSharedPreferencesPrefix(systemID)}$retroVariableName"
        }

        fun computeOriginalKey(sharedPreferencesKey: String, systemID: String): String {
            return sharedPreferencesKey.replace(computeSharedPreferencesPrefix(systemID), "")
        }

        private fun computeSharedPreferencesPrefix(systemID: String): String {
            return "${RETRO_OPTION_PREFIX}_${systemID}_"
        }
    }
}
