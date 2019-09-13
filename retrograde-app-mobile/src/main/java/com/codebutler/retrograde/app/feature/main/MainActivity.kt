package com.codebutler.retrograde.app.feature.main

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.feature.games.GamesFragment
import com.codebutler.retrograde.app.feature.settings.SettingsFragment
import com.codebutler.retrograde.lib.android.RetrogradeAppCompatActivity
import com.codebutler.retrograde.lib.injection.PerActivity
import com.codebutler.retrograde.lib.injection.PerFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.Provides
import dagger.android.ContributesAndroidInjector

class MainActivity : RetrogradeAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        val topLevelIds = setOf(R.id.navigation_home, R.id.navigation_games, R.id.navigation_settings)
        val appBarConfiguration = AppBarConfiguration(topLevelIds)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    @dagger.Module
    abstract class Module {

        @PerFragment
        @ContributesAndroidInjector(modules = [SettingsFragment.Module::class])
        abstract fun settingsFragment(): SettingsFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GamesFragment.Module::class])
        abstract fun gamesFragment(): GamesFragment

        /*@PerFragment
        @ContributesAndroidInjector(modules = [HomeFragment.Module::class])
        abstract fun homeFragment(): HomeFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GamesGridFragment.Module::class])
        abstract fun gamesGridFragment(): GamesGridFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GamesSearchFragment.Module::class])
        abstract fun gamesSearchFragment(): GamesSearchFragment*/

        @dagger.Module
        companion object {

            @Provides
            @PerActivity
            @JvmStatic
            fun rxPermissions(activity: MainActivity): RxPermissions = RxPermissions(activity)
        }
    }
}
