package com.swordfish.lemuroid.app.tv.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.game.GameLauncherActivity
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.app.shared.main.PostGameHandler
import com.swordfish.lemuroid.app.tv.favorites.TVFavoritesFragment
import com.swordfish.lemuroid.app.tv.games.TVGamesFragment
import com.swordfish.lemuroid.app.tv.home.TVHomeFragment
import com.swordfish.lemuroid.app.tv.search.TVSearchFragment
import com.swordfish.lemuroid.app.tv.shared.BaseTVActivity
import com.swordfish.lemuroid.app.tv.shared.TVHelper
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.injection.PerFragment
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MainTVActivity : BaseTVActivity(), BusyActivity {

    @Inject lateinit var postGameHandler: PostGameHandler

    var mainViewModel: MainTVViewModel? = null

    override fun activity(): Activity = this
    override fun isBusy(): Boolean = mainViewModel?.inProgress?.value ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_main)

        val factory = MainTVViewModel.Factory(applicationContext)
        mainViewModel = ViewModelProviders.of(this, factory)
            .get(MainTVViewModel::class.java)

        mainViewModel?.inProgress?.observe(this) {
            findViewById<View>(R.id.tv_loading).setVisibleOrGone(it)
        }

        ensureLegacyStoragePermissionsIfNeeded()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            GameLauncherActivity.REQUEST_PLAY_GAME -> {
                val duration = data?.extras?.getLong(GameLauncherActivity.PLAY_GAME_RESULT_SESSION_DURATION)
                val game = data?.extras?.getSerializable(GameLauncherActivity.PLAY_GAME_RESULT_GAME) as Game?
                val leanback = data?.extras?.getBoolean(GameLauncherActivity.PLAY_GAME_RESULT_LEANBACK)
                postGameHandler.handleAfterGame(this, leanback!!, game!!, duration!!)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (mainViewModel?.inProgress?.value == true) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (mainViewModel?.inProgress?.value == true) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun ensureLegacyStoragePermissionsIfNeeded() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (!TVHelper.isSAFSupported(this)) {
            requestLegacyStoragePermissions(permissions)
        }
    }

    private fun requestLegacyStoragePermissions(permissions: Array<String>) {
        RxPermissions(this).request(*permissions)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { if (!it) finish() }
            .autoDispose(scope())
            .subscribe()
    }

    @dagger.Module
    abstract class Module {

        @PerFragment
        @ContributesAndroidInjector(modules = [TVHomeFragment.Module::class])
        abstract fun tvHomeFragment(): TVHomeFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [TVGamesFragment.Module::class])
        abstract fun tvGamesFragment(): TVGamesFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [TVSearchFragment.Module::class])
        abstract fun tvSearchFragment(): TVSearchFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [TVFavoritesFragment.Module::class])
        abstract fun tvFavoritesFragment(): TVFavoritesFragment

        @dagger.Module
        companion object {
            @Provides
            @PerActivity
            @JvmStatic
            fun gameInteractor(activity: MainTVActivity, retrogradeDb: RetrogradeDatabase) =
                GameInteractor(activity, retrogradeDb, true)
        }
    }
}
