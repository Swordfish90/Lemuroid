package come.asg.ominoy.lib.core

import android.content.SharedPreferences
import androidx.core.content.edit
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import come.asg.ominoy.lib.library.CoreID
import come.asg.ominoy.lib.library.GameSystem
import come.asg.ominoy.lib.library.SystemCoreConfig
import come.asg.ominoy.lib.library.SystemID
import come.asg.ominoy.lib.migration.DesmumeMigrationHandler
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CoresSelection(
    private val sharedPreferencesFactory: Lazy<SharedPreferences>,
    private val desmumeMigrationHandler: DesmumeMigrationHandler,
) {
    private val sharedPreferences by lazy { sharedPreferencesFactory.get() }

    private val flowSharedPreferences by lazy { FlowSharedPreferences(sharedPreferences) }

    data class SelectedCore(
        val system: GameSystem,
        val coreConfig: SystemCoreConfig,
    )

    fun getSelectedCores(): Flow<List<SelectedCore>> {
        // This build has one bundled mGBA core for GB, GBC, and GBA. Keep all
        // supported systems visible to consumers even though there is no
        // user-selectable core anymore.
        val configurableSystems = GameSystem.all()

        val configurationFlows =
            configurableSystems.map { system ->
                getSelectedCoreConfigForSystem(system)
                    .map { SelectedCore(system, it) }
            }

        return combine(configurationFlows) { it.toList() }
    }

    suspend fun updateCoreConfigForSystem(
        system: GameSystem,
        coreID: CoreID,
    ) = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putString(computeSystemPreferenceKey(system.id), coreID.coreName)
            .commit()
    }

    suspend fun getCoreConfigForSystem(system: GameSystem): SystemCoreConfig {
        return getSelectedCoreConfigForSystem(system).first()
    }

    private fun getSelectedCoreConfigForSystem(system: GameSystem): Flow<SystemCoreConfig> {
        return getSelectedCoreNameForSystem(system)
            .map { coreName ->
                system.systemCoreConfigs.firstOrNull { it.coreID.coreName == coreName }
                    ?: system.systemCoreConfigs.first()
            }
    }

    private fun getSelectedCoreNameForSystem(system: GameSystem): Flow<String> {
        return flow {
            val preferenceKey = computeSystemPreferenceKey(system.id)
            val currentValue = flowSharedPreferences.sharedPreferences.getString(preferenceKey, null)
            var defaultValue = currentValue

            if (currentValue == null) {
                defaultValue = getDefaultCoreForSystem(system)
                flowSharedPreferences.sharedPreferences.edit(true) {
                    putString(preferenceKey, defaultValue)
                }
            }

            val preference =
                flowSharedPreferences.getString(
                    preferenceKey,
                    defaultValue,
                )
            emitAll(preference.asFlow())
        }.flowOn(Dispatchers.IO)
    }

    // TODO Also get rid of this when desmume is gone
    private fun getDefaultCoreForSystem(system: GameSystem): String {
        if (system.id == SystemID.NDS) {
            return if (desmumeMigrationHandler.hasPendingDesmumeSaves()) {
                CoreID.DESMUME.coreName
            } else {
                CoreID.MELONDS.coreName
            }
        }
        return system.systemCoreConfigs.firstOrNull()?.coreID?.coreName.orEmpty()
    }

    companion object {
        private const val CORE_SELECTION_BINDING_PREFERENCE_BASE_KEY = "pref_key_core_selection"

        fun computeSystemPreferenceKey(systemID: SystemID) =
            "${CORE_SELECTION_BINDING_PREFERENCE_BASE_KEY}_${systemID.dbname}"
    }
}
