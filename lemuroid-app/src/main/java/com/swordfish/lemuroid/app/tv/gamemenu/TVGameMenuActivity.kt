package com.swordfish.lemuroid.app.tv.gamemenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.tv.shared.TVBaseSettingsActivity
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SavesManager
import java.security.InvalidParameterException
import javax.inject.Inject

class TVGameMenuActivity : TVBaseSettingsActivity() {

    @Inject lateinit var savesManager: SavesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val game = intent.extras?.getSerializable(GameMenuContract.EXTRA_GAME) as Game?
                    ?: throw InvalidParameterException("Missing EXTRA_GAME")

            val options = intent.extras?.getSerializable(GameMenuContract.EXTRA_CORE_OPTIONS) as Array<CoreOption>?
                    ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS")

            val numDisks = intent.extras?.getInt(GameMenuContract.EXTRA_DISKS)
                    ?: throw InvalidParameterException("Missing EXTRA_DISKS")

            val fragment = TVGameMenuFragmentWrapper(savesManager, game, options, numDisks)
            supportFragmentManager.beginTransaction().replace(android.R.id.content, fragment).commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    class TVGameMenuFragmentWrapper(
        private val savesManager: SavesManager,
        private val game: Game,
        private val coreOptions: Array<CoreOption>,
        private val numDisks: Int
    ) : BaseSettingsFragmentWrapper() {

        override fun createFragment(): Fragment {
            return TVGameMenuFragment(savesManager, game, coreOptions, numDisks)
        }
    }
}
