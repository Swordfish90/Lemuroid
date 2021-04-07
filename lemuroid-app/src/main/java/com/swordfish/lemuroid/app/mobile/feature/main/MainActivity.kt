package com.swordfish.lemuroid.app.mobile.feature.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesFragment
import com.swordfish.lemuroid.app.mobile.feature.games.GamesFragment
import com.swordfish.lemuroid.app.mobile.feature.home.HomeFragment
import com.swordfish.lemuroid.app.mobile.feature.search.SearchFragment
import com.swordfish.lemuroid.app.mobile.feature.settings.BiosSettingsFragment
import com.swordfish.lemuroid.app.mobile.feature.settings.CoresSelectionFragment
import com.swordfish.lemuroid.app.mobile.feature.settings.GamepadSettingsFragment
import com.swordfish.lemuroid.app.mobile.feature.settings.SaveSyncFragment
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsFragment
import com.swordfish.lemuroid.app.mobile.feature.shortcuts.ShortcutsGenerator
import com.swordfish.lemuroid.app.mobile.feature.systems.MetaSystemsFragment
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.app.shared.main.PostGameHandler
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.ext.feature.donate.IAPHandler
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.android.RetrogradeAppCompatActivity
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.injection.PerFragment
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MainActivity : RetrogradeAppCompatActivity(), BusyActivity {

    @Inject lateinit var postGameHandler: PostGameHandler

    private val reviewManager = ReviewManager()
    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeActivity()
    }

    override fun activity(): Activity = this
    override fun isBusy(): Boolean = mainViewModel?.displayProgress?.value ?: false

    private fun initializeActivity() {
        setSupportActionBar(findViewById(R.id.toolbar))

        reviewManager.initialize(applicationContext)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        val topLevelIds = setOf(
            R.id.navigation_home,
            R.id.navigation_favorites,
            R.id.navigation_search,
            R.id.navigation_systems,
            R.id.navigation_settings
        )
        val appBarConfiguration = AppBarConfiguration(topLevelIds)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        mainViewModel = ViewModelProviders.of(this, MainViewModel.Factory(applicationContext))
            .get(MainViewModel::class.java)

        mainViewModel?.displayProgress?.observe(this) { isRunning ->
            findViewById<ProgressBar>(R.id.progress).setVisibleOrGone(isRunning)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            BaseGameActivity.REQUEST_PLAY_GAME -> {
                val duration = data?.extras?.getLong(BaseGameActivity.PLAY_GAME_RESULT_SESSION_DURATION)
                val game = data?.extras?.getSerializable(BaseGameActivity.PLAY_GAME_RESULT_GAME) as Game
                postGameHandler.handleAfterGame(this, true, game, duration!!)
                    .subscribeBy { }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (IAPHandler.IS_SUPPORTED) {
            menuInflater.inflate(R.menu.menu_mobile_donate, menu)
        }
        menuInflater.inflate(R.menu.menu_mobile_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_options_support -> {
                IAPHandler.launchDonateScreen(this)
                true
            }
            R.id.menu_options_help -> {
                displayLemuroidHelp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayLemuroidHelp() {
        val systemFolders = SystemID.values()
            .map { it.dbname }
            .map { "<i>$it</i>" }
            .joinToString(", ")

        val message = getString(R.string.lemuroid_help_content).replace("\$SYSTEMS", systemFolders)
        AlertDialog.Builder(this)
            .setMessage(Html.fromHtml(message))
            .show()
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()

    @dagger.Module
    abstract class Module {

        @PerFragment
        @ContributesAndroidInjector(modules = [SettingsFragment.Module::class])
        abstract fun settingsFragment(): SettingsFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GamesFragment.Module::class])
        abstract fun gamesFragment(): GamesFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [MetaSystemsFragment.Module::class])
        abstract fun systemsFragment(): MetaSystemsFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [HomeFragment.Module::class])
        abstract fun homeFragment(): HomeFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [SearchFragment.Module::class])
        abstract fun searchFragment(): SearchFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [FavoritesFragment.Module::class])
        abstract fun favoritesFragment(): FavoritesFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GamepadSettingsFragment.Module::class])
        abstract fun gamepadSettings(): GamepadSettingsFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [BiosSettingsFragment.Module::class])
        abstract fun biosInfoFragment(): BiosSettingsFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [SaveSyncFragment.Module::class])
        abstract fun saveSyncFragment(): SaveSyncFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [CoresSelectionFragment.Module::class])
        abstract fun coresSelectionFragment(): CoresSelectionFragment

        @dagger.Module
        companion object {

            @Provides
            @PerActivity
            @JvmStatic
            fun settingsInteractor(activity: MainActivity, directoriesManager: DirectoriesManager) =
                SettingsInteractor(activity, directoriesManager)

            @Provides
            @PerActivity
            @JvmStatic
            fun gameInteractor(
                activity: MainActivity,
                retrogradeDb: RetrogradeDatabase,
                shortcutsGenerator: ShortcutsGenerator,
                gameLauncher: GameLauncher
            ) =
                GameInteractor(activity, retrogradeDb, false, shortcutsGenerator, gameLauncher)
        }
    }
}
