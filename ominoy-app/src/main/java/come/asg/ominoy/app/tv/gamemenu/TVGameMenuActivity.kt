package come.asg.ominoy.app.tv.gamemenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import come.asg.ominoy.app.shared.GameMenuContract
import come.asg.ominoy.app.shared.coreoptions.OminoyCoreOption
import come.asg.ominoy.app.shared.input.InputDeviceManager
import come.asg.ominoy.app.tv.shared.TVBaseSettingsActivity
import come.asg.ominoy.lib.library.SystemCoreConfig
import come.asg.ominoy.lib.library.db.entity.Game
import come.asg.ominoy.lib.saves.StatesManager
import come.asg.ominoy.lib.saves.StatesPreviewManager
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
                ) as Array<OminoyCoreOption>?
                    ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS")

            val advancedOptions =
                intent.extras?.getSerializable(
                    GameMenuContract.EXTRA_ADVANCED_CORE_OPTIONS,
                ) as Array<OminoyCoreOption>?
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

            val fastForwardEnabled =
                intent.extras?.getBoolean(GameMenuContract.EXTRA_FAST_FORWARD)
                    ?: throw InvalidParameterException("Missing EXTRA_FAST_FORWARD")

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
                    fastForwardEnabled,
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
        private val coreOptions: Array<OminoyCoreOption>,
        private val advancedCoreOptions: Array<OminoyCoreOption>,
        private val numDisks: Int,
        private val currentDisk: Int,
        private val audioEnabled: Boolean,
        private val fastForwardEnabled: Boolean,
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
                fastForwardEnabled,
                fastForwardSupported,
            )
        }
    }
}
