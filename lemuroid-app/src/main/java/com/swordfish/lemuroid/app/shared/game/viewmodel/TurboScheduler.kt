package com.swordfish.lemuroid.app.shared.game.viewmodel

import android.view.KeyEvent
import com.swordfish.libretrodroid.GLRetroView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Drives software turbo fire.
 *
 * When a turbo trigger key is held down, [start] launches a coroutine that alternates
 * ACTION_DOWN / ACTION_UP events towards the emulated core at the requested frequency.
 * [stop] cancels the loop and always emits a trailing ACTION_UP so the target button
 * can never get "stuck" in the pressed state.
 *
 * Instances are keyed by (deviceId, triggerKeyCode) so that multiple gamepads and multiple
 * turbo buttons can fire concurrently without interfering with each other.
 */
class TurboScheduler(
    private val scope: CoroutineScope,
    private val retroGameViewProvider: () -> GLRetroView?,
) {
    private data class TurboKey(
        val deviceId: Int,
        val triggerKeyCode: Int,
    )

    private data class RunningTurbo(
        val job: Job,
        val targetKeyCode: Int,
        val port: Int,
    )

    private val running = mutableMapOf<TurboKey, RunningTurbo>()

    fun start(
        deviceId: Int,
        triggerKeyCode: Int,
        targetKeyCode: Int,
        port: Int,
        frequencyHz: Int,
    ) {
        val key = TurboKey(deviceId, triggerKeyCode)
        if (running.containsKey(key)) return

        val halfPeriodMillis = (1000L / frequencyHz.coerceIn(1, 60)) / 2
        val job =
            scope.launch {
                while (isActive) {
                    retroGameViewProvider()?.sendKeyEvent(KeyEvent.ACTION_DOWN, targetKeyCode, port)
                    delay(halfPeriodMillis)
                    retroGameViewProvider()?.sendKeyEvent(KeyEvent.ACTION_UP, targetKeyCode, port)
                    delay(halfPeriodMillis)
                }
            }

        running[key] = RunningTurbo(job, targetKeyCode, port)
    }

    fun stop(
        deviceId: Int,
        triggerKeyCode: Int,
    ) {
        val runningTurbo = running.remove(TurboKey(deviceId, triggerKeyCode)) ?: return
        runningTurbo.job.cancel()
        retroGameViewProvider()?.sendKeyEvent(KeyEvent.ACTION_UP, runningTurbo.targetKeyCode, runningTurbo.port)
    }

    /**
     * Stops turbo for any device that is no longer in [activeDeviceIds]. Guards against hot-unplug:
     * a disconnected gamepad never emits the trailing ACTION_UP, so its loop would otherwise run forever.
     */
    fun retainDevices(activeDeviceIds: Set<Int>) {
        running.keys
            .filter { it.deviceId !in activeDeviceIds }
            .forEach { stop(it.deviceId, it.triggerKeyCode) }
    }

    /** Cancels every running turbo and releases all target buttons. Used on lifecycle/background transitions. */
    fun stopAll() {
        running.values.forEach {
            it.job.cancel()
            retroGameViewProvider()?.sendKeyEvent(KeyEvent.ACTION_UP, it.targetKeyCode, it.port)
        }
        running.clear()
    }
}
