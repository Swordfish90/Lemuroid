package com.swordfish.lemuroid.app.shared.game.viewmodel

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.sensors.TiltSensor
import gg.padkit.inputstate.InputState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTilt(
    appContext: Context,
    private val settingsManager: SettingsManager,
) : DefaultLifecycleObserver {
    private val tiltSensor = TiltSensor(appContext)
    private val tiltConfiguration = MutableStateFlow<TiltConfiguration>(TiltConfiguration.Disabled)

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        tiltSensor.isAllowedToRun = true
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        tiltSensor.isAllowedToRun = false
    }

    fun changeTiltConfiguration(configuration: TiltConfiguration) {
        tiltConfiguration.value = configuration
    }

    fun getTiltConfiguration(): Flow<TiltConfiguration> {
        return tiltConfiguration
    }

    fun getSimulatedTiltEvents(): Flow<InputState> {
        return tiltConfiguration
            .flatMapLatest { config ->
                if (config is TiltConfiguration.Disabled) {
                    return@flatMapLatest flow { awaitCancellation() }
                }

                tiltSensor.setSensitivity(settingsManager.tiltSensitivity())
                tiltSensor.getTiltEvents()
                    .onStart { tiltSensor.shouldRun = true }
                    .onCompletion { tiltSensor.shouldRun = false }
                    .map { config.process(it) }
                    .distinctUntilChanged()
            }
    }
}
