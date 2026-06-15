/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.app.tv.gamemenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.coreoptions.LemuroidCoreOption
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.tv.shared.TVBaseSettingsActivity
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import java.security.InvalidParameterException
import javax.inject.Inject

class TVGameMenuActivity : TVBaseSettingsActivity() {
    @Inject
    lateinit var statesManager: StatesManager

    @Inject
    lateinit var statesPreviewManager: StatesPreviewManager

    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val game =
                intent.extras?.getSerializable(GameMenuContract.EXTRA_GAME) as Game?
                    ?: throw InvalidParameterException("Missing EXTRA_GAME")

            val core =
                intent.extras?.getSerializable(
                    GameMenuContract.EXTRA_SYSTEM_CORE_CONFIG,
                ) as SystemCoreConfig?
                    ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_CORE_CONFIG")

            val options =
                intent.extras?.getSerializable(
                    GameMenuContract.EXTRA_CORE_OPTIONS,
                ) as Array<LemuroidCoreOption>?
                    ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS")

            val advancedOptions =
                intent.extras?.getSerializable(
                    GameMenuContract.EXTRA_ADVANCED_CORE_OPTIONS,
                ) as Array<LemuroidCoreOption>?
                    ?: throw InvalidParameterException("Missing EXTRA_ADVANCED_CORE_OPTIONS")

            val numDisks =
                intent.extras?.getInt(GameMenuContract.EXTRA_DISKS)
                    ?: throw InvalidParameterException("Missing EXTRA_DISKS")

            val currentDisk =
                intent.extras?.getInt(GameMenuContract.EXTRA_CURRENT_DISK)
                    ?: throw InvalidParameterException("Missing EXTRA_CURRENT_DISK")

            val audioEnabled =
                intent.extras?.getBoolean(GameMenuContract.EXTRA_AUDIO_ENABLED)
                    ?: throw InvalidParameterException("Missing EXTRA_AUDIO_ENABLED")

            val fastForwardSpeed =
                intent.extras?.getInt(GameMenuContract.EXTRA_FAST_FORWARD_SPEED, 1) ?: 1

            val fastForwardSupported =
                intent.extras?.getBoolean(GameMenuContract.EXTRA_FAST_FORWARD_SUPPORTED)
                    ?: throw InvalidParameterException("Missing EXTRA_FAST_FORWARD_SUPPORTED")

            val fragment =
                TVGameMenuFragmentWrapper(
                    statesManager,
                    statesPreviewManager,
                    inputDeviceManager,
                    game,
                    core,
                    options,
                    advancedOptions,
                    numDisks,
                    currentDisk,
                    audioEnabled,
                    fastForwardSpeed,
                    fastForwardSupported,
                )
            supportFragmentManager.beginTransaction().replace(android.R.id.content, fragment)
                .commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    class TVGameMenuFragmentWrapper(
        private val statesManager: StatesManager,
        private val statesPreviewManager: StatesPreviewManager,
        private val inputDeviceManager: InputDeviceManager,
        private val game: Game,
        private val systemCoreConfig: SystemCoreConfig,
        private val coreOptions: Array<LemuroidCoreOption>,
        private val advancedCoreOptions: Array<LemuroidCoreOption>,
        private val numDisks: Int,
        private val currentDisk: Int,
        private val audioEnabled: Boolean,
        private val fastForwardSpeed: Int,
        private val fastForwardSupported: Boolean,
    ) : BaseSettingsFragmentWrapper() {
        override fun createFragment(): Fragment {
            return TVGameMenuFragment(
                statesManager,
                statesPreviewManager,
                inputDeviceManager,
                game,
                systemCoreConfig,
                coreOptions,
                advancedCoreOptions,
                numDisks,
                currentDisk,
                audioEnabled,
                fastForwardSpeed,
                fastForwardSupported,
            )
        }
    }
}
