package com.codebutler.retrograde.app.feature.main

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkInfo
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.feature.games.GamesFragment
import com.codebutler.retrograde.app.feature.games.SystemsFragment
import com.codebutler.retrograde.app.feature.home.HomeFragment
import com.codebutler.retrograde.app.feature.search.SearchFragment
import com.codebutler.retrograde.app.feature.settings.SettingsFragment
import com.codebutler.retrograde.app.shared.GameInteractor
import com.codebutler.retrograde.lib.android.RetrogradeAppCompatActivity
import com.codebutler.retrograde.lib.injection.PerActivity
import com.codebutler.retrograde.lib.injection.PerFragment
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

class MainActivity : RetrogradeAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        RxPermissions(this).request(*permissions)
                .autoDisposable(scope())
                .subscribe { granted ->
                    if (granted) {
                        initializeActivity()
                    } else {
                        finish()
                    }
                }
    }

    private fun initializeActivity() {
        setSupportActionBar(findViewById(R.id.toolbar))

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        val topLevelIds = setOf(
            R.id.navigation_home,
            R.id.navigation_search,
            R.id.navigation_systems,
            R.id.navigation_settings
        )
        val appBarConfiguration = AppBarConfiguration(topLevelIds)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val mainViewModel = ViewModelProviders.of(this, MainViewModel.Factory(applicationContext))
                .get(MainViewModel::class.java)

        mainViewModel.indexingInProgress.observe(this, Observer { workInfos ->
            if (workInfos != null) {
                val isRunning = workInfos
                    .map { it.state }
                    .any { it in listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED) }

                findViewById<MaterialProgressBar>(R.id.progress).visibility = if (isRunning) View.VISIBLE else View.GONE
            }
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

        /*@PerFragment
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
            fun gameInteractor(activity: MainActivity, retrogradeDb: RetrogradeDatabase) =
                    GameInteractor(activity, retrogradeDb)

            @Provides
            @PerActivity
            @JvmStatic
            fun rxPermissions(activity: MainActivity): RxPermissions = RxPermissions(activity)
        }
    }
}
