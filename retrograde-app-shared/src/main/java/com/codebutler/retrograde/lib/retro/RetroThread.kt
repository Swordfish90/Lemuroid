package com.codebutler.retrograde.lib.retro

import java.util.LinkedList
import kotlin.math.roundToLong
import kotlin.system.measureNanoTime

/** Handle periodic actions with improved accuracy. Core ideas taken from:
 *  https://github.com/LWJGL/lwjgl/blob/master/src/java/org/lwjgl/opengl/Sync.java */

class RetroThread(private val cyclePeriod: Long, private val runnable: () -> Unit) : Thread() {

    companion object {
        // We overestimate this value to make sure we are not sleeping too much
        const val SLEEP_SECURITY_THRESHOLD = 1_000_000

        const val NANOS_IN_SECOND = 1_000_000_000L

        const val MOVING_AVERAGE_SIZE = 10

        fun fromFPS(fps: Double, runnable: () -> Unit): RetroThread {
            val period = (1.0 / fps * NANOS_IN_SECOND).roundToLong()
            return RetroThread(period, runnable)
        }
    }

    private var lastSleepAverage = 0.0
    private var lastYieldAverage = 0.0
    private var lastMeasurement = 0L

    var sleepDurationAverage = initMovingAverage()
    var yieldDurationAverage = initMovingAverage()

    override fun run() {
        var startTime: Long

        kotlin.runCatching {
            while (true) {
                startTime = System.nanoTime()
                runnable()
                accurateSleep(startTime + cyclePeriod)
            }
        }
    }

    private fun accurateSleep(nextFrame: Long) {
        while (System.nanoTime() < nextFrame - lastSleepAverage) {
            lastMeasurement = measureNanoTime { sleep(1) }
            lastSleepAverage = sleepDurationAverage(lastMeasurement) + SLEEP_SECURITY_THRESHOLD
        }

        while (System.nanoTime() < nextFrame - lastYieldAverage) {
            lastMeasurement = measureNanoTime { yield() }
            lastYieldAverage = yieldDurationAverage(lastMeasurement)
        }
    }

    private fun initMovingAverage(): (Long) -> Double {
        val list = LinkedList<Long>()
        return {
            list.add(it)
            if (list.size > MOVING_AVERAGE_SIZE) list.removeFirst()
            list.average()
        }
    }
}
