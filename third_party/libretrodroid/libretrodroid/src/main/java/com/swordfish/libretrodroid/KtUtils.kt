/*
 *     Copyright (C) 1  Filippo Scognamiglio
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.libretrodroid

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch

object KtUtils {
    fun runOnUIThread(runnable: () -> Unit) {
        if (isUIThread()) {
            runnable()
        } else {
            Handler(Looper.getMainLooper()).post(runnable)
        }
    }

    private fun isUIThread(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Looper.getMainLooper().isCurrentThread
        } else {
            Thread.currentThread() == Looper.getMainLooper().thread
        }
    }

    fun CountDownLatch.awaitUninterruptibly() {
        try {
            while (true) {
                try {
                    await()
                    return
                } catch (e: InterruptedException) {

                }
            }
        } finally {
        }
    }
}