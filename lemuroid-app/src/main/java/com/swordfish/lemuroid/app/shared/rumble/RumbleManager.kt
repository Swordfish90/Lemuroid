package com.swordfish.lemuroid.app.shared.rumble

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.InputDevice
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.common.coroutines.safeCollect
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.libretrodroid.RumbleEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.newSingleThreadContext
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class RumbleManager(
    applicationContext: Context,
    private val settingsManager: SettingsManager,
    private val inputDeviceManager: InputDeviceManager,
) {
    private val deviceVibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val rumbleContext = newSingleThreadContext("Rumble")

    suspend fun collectAndProcessRumbleEvents(
        systemCoreConfig: SystemCoreConfig,
        rumbleEventsObservable: Flow<RumbleEvent>,
    ) {
        val enableRumble = settingsManager.enableRumble()
        val rumbleSupported = systemCoreConfig.rumbleSupported

        if (!enableRumble && rumbleSupported) {
            return
        }

        inputDeviceManager.getEnabledInputsObservable()
            .map { getVibrators(it) }
            .flatMapLatest { vibrators ->
                rumbleEventsObservable
                    .onEach { kotlin.runCatching { vibrate(vibrators.forPort(it.port), it) } }
                    .onStart { stopAllVibrators(vibrators) }
                    .onCompletion { stopAllVibrators(vibrators) }
                    .flowOn(rumbleContext)
            }
            .safeCollect { }
    }

    private fun stopAllVibrators(vibrators: RumbleVibrators) {
        vibrators.all().forEach {
            kotlin.runCatching { it.cancel() }
        }
    }

    private suspend fun getVibrators(gamePads: List<InputDevice>): RumbleVibrators {
        val destination = settingsManager.rumbleDestination()
        val systemVibrator = deviceVibrator.takeIf { it.hasVibrator() }
        val controllerVibrators = if (destination == RumbleDestination.DEVICE) {
            emptyList()
        } else {
            gamePads.map { it.getSupportedVibrator() }
        }

        return RumbleVibrators(destination, controllerVibrators, systemVibrator)
    }

    private data class RumbleVibrators(
        private val destination: RumbleDestination,
        private val controllerVibrators: List<Vibrator?>,
        private val deviceVibrator: Vibrator?,
    ) {
        fun forPort(port: Int): Vibrator? = when (destination) {
            RumbleDestination.AUTO -> controllerVibrators.getOrNull(port) ?: deviceVibrator
            RumbleDestination.CONTROLLER -> controllerVibrators.getOrNull(port)
            RumbleDestination.DEVICE -> deviceVibrator
        }

        fun all(): List<Vibrator> = when (destination) {
            RumbleDestination.AUTO -> controllerVibrators + deviceVibrator
            RumbleDestination.CONTROLLER -> controllerVibrators
            RumbleDestination.DEVICE -> listOf(deviceVibrator)
        }.filterNotNull().distinct()
    }

    private fun InputDevice.getSupportedVibrator(): Vibrator? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = vibratorManager
            return vibratorManager.defaultVibrator.takeIf {
                vibratorManager.vibratorIds.isNotEmpty()
            }
        }

        return getLegacySupportedVibrator()
    }

    @Suppress("DEPRECATION")
    private fun InputDevice.getLegacySupportedVibrator(): Vibrator? =
        vibrator.takeIf { it.hasVibrator() }

    private fun vibrate(
        vibrator: Vibrator?,
        rumbleEvent: RumbleEvent,
    ) {
        if (vibrator == null) return

        vibrator.cancel()

        val amplitude = computeAmplitude(rumbleEvent)

        if (amplitude == 0) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createOneShot(MAX_RUMBLE_DURATION_MS, amplitude))
        } else if (amplitude > LEGACY_MIN_RUMBLE_STRENGTH) {
            vibrator.vibrate(MAX_RUMBLE_DURATION_MS)
        }
    }

    private fun computeAmplitude(rumbleEvent: RumbleEvent): Int {
        val strength = rumbleEvent.strengthStrong * 0.66f + rumbleEvent.strengthWeak * 0.33f
        return (DEFAULT_RUMBLE_STRENGTH * (strength) * 255).roundToInt()
    }

    companion object {
        const val MAX_RUMBLE_DURATION_MS = 1000L
        const val DEFAULT_RUMBLE_STRENGTH = 0.5f
        const val LEGACY_MIN_RUMBLE_STRENGTH = 100
    }
}
