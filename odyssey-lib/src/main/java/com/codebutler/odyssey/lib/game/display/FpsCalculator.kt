/*
 * FpsCalculator.kt
 *
 * Copyright (C) 2017 Odyssey Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.lib.game.display

class FpsCalculator {

    companion object {
        private var size: Int = 100
    }

    private var lastUpdate = System.currentTimeMillis()
    private var lastFpsWrite = 0
    private var totalFps = 0L
    private val fpsHistory = LongArray(size)

    fun update() {
        val currentUpdate = System.currentTimeMillis()
        val deltaTime = currentUpdate - lastUpdate + 1 // Add 1 to make sure delta time is non-zero.
        val fps = 1000 / deltaTime

        totalFps -= fpsHistory[lastFpsWrite]
        totalFps += fps

        fpsHistory[lastFpsWrite++] = fps
        lastFpsWrite %= 100
        lastUpdate = currentUpdate
    }

    val fps: Long
        get() = totalFps / 100
}
