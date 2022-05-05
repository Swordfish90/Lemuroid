package com.swordfish.lemuroid.app.shared.settings

import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import dagger.Lazy

class CustomCoreOptions(private val sharedPreferences: Lazy<SharedPreferences>) {

    fun getPixelArtUpscaling(systemId: SystemID, systemCoreConfig: SystemCoreConfig): Single<Boolean> {
        return Single.just(systemCoreConfig.supportPixelArtUpscaling)
            .filter { it }
            .map {
                val key = pixelArtUpscalingPreferenceId(systemId.dbname, systemCoreConfig.coreID)
                sharedPreferences.get().getBoolean(key, false)
            }
            .toSingle(false)
            .subscribeOn(Schedulers.io())
    }

    fun getControllerConfigs(
        systemId: SystemID,
        systemCoreConfig: SystemCoreConfig
    ): Single<Map<Int, ControllerConfig>> = Single.fromCallable {
        systemCoreConfig.controllerConfigs.entries
            .map { (port, controllers) ->
                val currentName = sharedPreferences.get().getString(
                    controllersPreferenceId(systemId.dbname, systemCoreConfig.coreID, port),
                    null
                )

                val currentController = controllers
                    .firstOrNull { it.name == currentName } ?: controllers.first()

                port to currentController
            }
            .toMap()
    }.subscribeOn(Schedulers.io())

    companion object {
        fun controllersPreferenceId(systemId: String, coreID: CoreID, port: Int) =
            "pref_key_controller_type_${systemId}_${coreID.coreName}_$port"

        fun pixelArtUpscalingPreferenceId(systemId: String, coreID: CoreID) =
            "pref_key_pixel_art_upscaling_${systemId}_${coreID.coreName}"
    }
}
