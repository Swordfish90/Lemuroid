package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.android.RetrogradeAppCompatActivity
import com.swordfish.lemuroid.lib.injection.PerFragment
import dagger.android.ContributesAndroidInjector

class GameMenuActivity : RetrogradeAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty_navigation_overlay)
        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.mobile_game_menu, intent.extras)

        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    @dagger.Module
    abstract class Module {

        @PerFragment
        @ContributesAndroidInjector(modules = [GameMenuCoreOptionsFragment.Module::class])
        abstract fun coreOptionsFragment(): GameMenuCoreOptionsFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GameMenuFragment.Module::class])
        abstract fun gameMenuFragment(): GameMenuFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GameMenuLoadFragment.Module::class])
        abstract fun gameMenuLoadFragment(): GameMenuLoadFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GameMenuSaveFragment.Module::class])
        abstract fun gameMenuSaveFragment(): GameMenuSaveFragment
    }
}
