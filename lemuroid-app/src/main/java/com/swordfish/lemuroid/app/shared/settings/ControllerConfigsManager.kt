package com.swordfish.lemuroid.app.shared.settings

import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ControllerConfigsManager(private val sharedPreferences: Lazy<SharedPreferences>) {
    suspend fun getControllerConfigs(
        systemId: SystemID,
        systemCoreConfig: SystemCoreConfig,
    ): Map<Int, ControllerConfig> =
        withContext(Dispatchers.IO) {
            systemCoreConfig.controllerConfigs.entries
                .associate { (port, controllers) ->
                    val currentName =
                        sharedPreferences.get().getString(
                            getSharedPreferencesId(systemId.dbname, systemCoreConfig.coreID, port),
                            null,
                        )

                    val currentController =
                        controllers
                            .firstOrNull { it.name == currentName } ?: controllers.first()

                    port to currentController
                }
        }

    companion object {
        fun getSharedPreferencesId(
            systemId: String,
            coreID: CoreID,
            port: Int,
        ) = "pref_key_controller_type_${systemId}_${coreID.coreName}_$port"
    }
}
