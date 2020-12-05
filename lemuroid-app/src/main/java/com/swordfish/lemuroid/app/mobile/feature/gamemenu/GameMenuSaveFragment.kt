package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.gamemenu.GameMenuHelper
import com.swordfish.lemuroid.common.rx.toSingleAsOptional
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.security.InvalidParameterException
import javax.inject.Inject

class GameMenuSaveFragment : PreferenceFragmentCompat() {

    @Inject lateinit var statesManager: StatesManager
    @Inject lateinit var statesPreviewManager: StatesPreviewManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.empty_preference_screen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = activity?.intent?.extras

        val game = extras?.getSerializable(GameMenuContract.EXTRA_GAME) as Game?
            ?: throw InvalidParameterException("Missing EXTRA_GAME")

        val systemCoreConfig = extras?.getSerializable(GameMenuContract.EXTRA_SYSTEM_CORE_CONFIG) as SystemCoreConfig?
            ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_CORE_CONFIG")

        setupSavePreference(game, systemCoreConfig)
    }

    private fun setupSavePreference(game: Game, systemCoreConfig: SystemCoreConfig) {
        statesManager.getSavedSlotsInfo(game, systemCoreConfig.coreID)
            .toObservable()
            .flatMap {
                Observable.fromIterable(it.mapIndexed { index, saveInfo -> index to saveInfo })
            }
            .flatMapSingle { (index, saveInfo) ->
                GameMenuHelper.getSaveStateBitmap(
                    requireContext(),
                    statesPreviewManager,
                    game,
                    systemCoreConfig.coreID,
                    index
                )
                    .toSingleAsOptional()
                    .map { Triple(index, saveInfo, it) }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy { (index, saveInfo, bitmap) ->
                GameMenuHelper.addSavePreference(
                    preferenceScreen,
                    index,
                    saveInfo,
                    bitmap.toNullable()
                )
            }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return GameMenuHelper.onPreferenceTreeClicked(activity, preference)
    }

    @dagger.Module
    class Module
}
