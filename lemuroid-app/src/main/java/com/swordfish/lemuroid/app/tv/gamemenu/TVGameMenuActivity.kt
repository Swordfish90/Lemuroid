package com.swordfish.lemuroid.app.tv.gamemenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.tv.shared.TVBaseSettingsActivity
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.saves.SavesManager
import java.security.InvalidParameterException
import javax.inject.Inject

class TVGameMenuActivity : TVBaseSettingsActivity() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var savesManager: SavesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val gameId = intent.extras?.getInt(GameMenuContract.EXTRA_GAME_ID)
                    ?: throw InvalidParameterException("Missing EXTRA_GAME_ID")

            val options = intent.extras?.getSerializable(GameMenuContract.EXTRA_CORE_OPTIONS) as Array<CoreOption>?
                    ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS")

            val systemID = intent.extras?.getString(GameMenuContract.EXTRA_SYSTEM_ID)
                    ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_ID")

            val numDisks = intent.extras?.getInt(GameMenuContract.EXTRA_DISKS)
                    ?: throw InvalidParameterException("Missing EXTRA_DISKS")

            val fragment = TVGameMenuFragmentWrapper(retrogradeDb, savesManager, gameId, systemID, options, numDisks)
            supportFragmentManager.beginTransaction().replace(android.R.id.content, fragment).commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    class TVGameMenuFragmentWrapper(
        private val retrogradeDb: RetrogradeDatabase,
        private val savesManager: SavesManager,
        private val gameId: Int,
        private val systemId: String,
        private val coreOptions: Array<CoreOption>,
        private val numDisks: Int
    ) : BaseSettingsFragmentWrapper() {

        override fun createFragment(): Fragment {
            return TVGameMenuFragment(retrogradeDb, savesManager, gameId, systemId, coreOptions, numDisks)
        }
    }
}
