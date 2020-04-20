package com.swordfish.lemuroid.app.mobile.feature.main

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.games.GamesFragment
import com.swordfish.lemuroid.app.mobile.feature.systems.SystemsFragment
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
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

class MainActivity : RetrogradeAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeActivity()
    }

    private fun initializeActivity() {
        setSupportActionBar(findViewById(R.id.toolbar))

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

        mainViewModel.indexingInProgress.observe(this, Observer { isRunning ->
            findViewById<MaterialProgressBar>(R.id.progress).setVisibleOrGone(isRunning)
        })
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
        @ContributesAndroidInjector(modules = [SystemsFragment.Module::class])
        abstract fun systemsFragment(): SystemsFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [HomeFragment.Module::class])
        abstract fun homeFragment(): HomeFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [SearchFragment.Module::class])
        abstract fun searchFragment(): SearchFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [FavoritesFragment.Module::class])
        abstract fun favoritesFragment(): FavoritesFragment

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
