package come.asg.ominoy.app.tv.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import come.asg.ominoy.R
import come.asg.ominoy.app.mobile.feature.shortcuts.ShortcutsGenerator
import come.asg.ominoy.app.shared.GameInteractor
import come.asg.ominoy.app.shared.game.BaseGameActivity
import come.asg.ominoy.app.shared.game.GameLauncher
import come.asg.ominoy.app.shared.main.BusyActivity
import come.asg.ominoy.app.shared.main.GameLaunchTaskHandler
import come.asg.ominoy.app.tv.channel.ChannelUpdateWork
import come.asg.ominoy.app.tv.favorites.TVFavoritesFragment
import come.asg.ominoy.app.tv.games.TVGamesFragment
import come.asg.ominoy.app.tv.home.TVHomeFragment
import come.asg.ominoy.app.tv.search.TVSearchFragment
import come.asg.ominoy.app.tv.shared.BaseTVActivity
import come.asg.ominoy.app.tv.shared.TVHelper
import come.asg.ominoy.common.coroutines.launchOnState
import come.asg.ominoy.common.coroutines.safeCollect
import come.asg.ominoy.common.coroutines.safeLaunch
import come.asg.ominoy.lib.injection.PerActivity
import come.asg.ominoy.lib.injection.PerFragment
import come.asg.ominoy.lib.library.covers.CustomCoverManager
import come.asg.ominoy.lib.library.db.RetrogradeDatabase
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class MainTVActivity :
    BaseTVActivity(),
    BusyActivity {
    @Inject
    lateinit var gameLaunchTaskHandler: GameLaunchTaskHandler

    var mainViewModel: MainTVViewModel? = null

    override fun activity(): Activity = this

    override fun isBusy(): Boolean = mainViewModel?.inProgress?.value ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_main)

        val factory = MainTVViewModel.Factory(applicationContext)
        mainViewModel = ViewModelProvider(this, factory).get(MainTVViewModel::class.java)

        launchOnState(Lifecycle.State.CREATED) {
            mainViewModel?.inProgress?.safeCollect {
                findViewById<View>(R.id.tv_loading).isVisible = it
            }
        }

        ensureLegacyStoragePermissionsIfNeeded()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BaseGameActivity.REQUEST_PLAY_GAME -> {
                GlobalScope.safeLaunch {
                    gameLaunchTaskHandler.handleGameFinish(false, this@MainTVActivity, resultCode, data)
                    ChannelUpdateWork.enqueue(applicationContext)
                }
            }
        }
    }

    private fun ensureLegacyStoragePermissionsIfNeeded() {
        if (TVHelper.isSAFSupported(this) || hasLegacyPermissions()) {
            return
        }

        val requestPermission = ActivityResultContracts.RequestPermission()
        val requestPermissionLauncher =
            registerForActivityResult(requestPermission) { isGranted ->
                if (!isGranted) {
                    finish()
                }
            }
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun hasLegacyPermissions(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
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
            fun gameInteractor(
                activity: MainTVActivity,
                retrogradeDb: RetrogradeDatabase,
                shortcutsGenerator: ShortcutsGenerator,
                gameLauncher: GameLauncher,
                customCoverManager: CustomCoverManager,
            ) = GameInteractor(activity, retrogradeDb, true, shortcutsGenerator, gameLauncher, customCoverManager)
        }
    }
}
