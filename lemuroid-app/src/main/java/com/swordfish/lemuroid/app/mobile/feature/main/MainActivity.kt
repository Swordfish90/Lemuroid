package com.swordfish.lemuroid.app.mobile.feature.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.games.GamesFragment
import com.swordfish.lemuroid.app.mobile.feature.systems.MetaSystemsFragment
import com.swordfish.lemuroid.app.mobile.feature.home.HomeFragment
import com.swordfish.lemuroid.app.mobile.feature.search.SearchFragment
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsFragment
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.lib.android.RetrogradeAppCompatActivity
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.injection.PerFragment
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesFragment
import com.swordfish.lemuroid.app.mobile.feature.settings.BiosSettingsFragment
import com.swordfish.lemuroid.app.mobile.feature.settings.GamepadSettingsFragment
import com.swordfish.lemuroid.app.shared.game.GameLauncherActivity
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.Completable
import io.reactivex.rxkotlin.subscribeBy
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.concurrent.TimeUnit

class MainActivity : RetrogradeAppCompatActivity() {

    private val reviewManager = ReviewManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeActivity()
    }

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

        val mainViewModel = ViewModelProviders.of(this, MainViewModel.Factory(applicationContext))
            .get(MainViewModel::class.java)

        mainViewModel.indexingInProgress.observe(this) { isRunning ->
            findViewById<MaterialProgressBar>(R.id.progress).setVisibleOrGone(isRunning)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            GameLauncherActivity.REQUEST_PLAY_GAME -> {
                val duration = data?.extras?.getLong(GameLauncherActivity.PLAY_GAME_RESULT_SESSION_DURATION)
                displayReviewRequest(duration)
            }
        }
    }

    private fun displayReviewRequest(durationMillis: Long?) {
        if (durationMillis == null) return
        Completable.timer(500, TimeUnit.MILLISECONDS)
            .andThen { reviewManager.startReviewFlow(this, durationMillis) }
            .subscribeBy { }
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

        @dagger.Module
        companion object {

            @Provides
            @PerActivity
            @JvmStatic
            fun settingsInteractor(activity: MainActivity) =
                SettingsInteractor(activity)

            @Provides
            @PerActivity
            @JvmStatic
            fun gameInteractor(activity: MainActivity, retrogradeDb: RetrogradeDatabase) =
                GameInteractor(activity, retrogradeDb, false)
        }
    }
}
