package com.swordfish.lemuroid.app.tv.gamemenu

import android.os.Bundle
import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.saves.SavesManager
import java.security.InvalidParameterException
import javax.inject.Inject

class TVGameMenuActivity : ImmersiveActivity() {

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

            val fragment = SettingsFragment(retrogradeDb, savesManager, gameId, systemID, options, numDisks)
            supportFragmentManager.beginTransaction().replace(android.R.id.content, fragment).commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    class SettingsFragment(
        private val retrogradeDb: RetrogradeDatabase,
        private val savesManager: SavesManager,
        private val gameId: Int,
        private val systemId: String,
        private val coreOptions: Array<CoreOption>,
        private val numDisks: Int
    ) : LeanbackSettingsFragmentCompat() {

        override fun onPreferenceStartInitialScreen() {
            startPreferenceFragment(
                    TVGameMenuFragment(retrogradeDb, savesManager, gameId, systemId, coreOptions, numDisks)
            )
        }

        override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
            val args = pref.extras
            val f = childFragmentManager.fragmentFactory.instantiate(
                    requireActivity().classLoader, pref.fragment)
            f.arguments = args
            f.setTargetFragment(caller, 0)
            if (f is PreferenceFragmentCompat || f is PreferenceDialogFragmentCompat) {
                startPreferenceFragment(f)
            } else {
                startImmersiveFragment(f)
            }
            return true
        }

        override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen): Boolean {
            val fragment = TVGameMenuFragment(retrogradeDb, savesManager, gameId, systemId, coreOptions, numDisks)
            val args = Bundle(1)
            args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.key)
            fragment.arguments = args
            startPreferenceFragment(fragment)
            return true
        }
    }
}
