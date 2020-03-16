package com.swordfish.lemuroid.app.tv

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.feature.library.LibraryIndexWork
import com.swordfish.lemuroid.app.feature.main.MainViewModel
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.injection.PerFragment
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.ui.setVisibleOrInvisible
import dagger.Provides
import dagger.android.ContributesAndroidInjector

class MainTVActivity : RetrogradeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_main)

        val mainViewModel = ViewModelProviders.of(this, MainTVViewModel.Factory(applicationContext))
                .get(MainTVViewModel::class.java)

        mainViewModel.indexingInProgress.observe(this, Observer {
            findViewById<View>(R.id.tv_loading).setVisibleOrGone(it)
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.view?.apply {
                this.isEnabled = !it
            }
        })

/*        val metrics = resources.displayMetrics
        metrics.density = 0.75f * metrics.density
        metrics.scaledDensity = 0.75f * metrics.scaledDensity
        resources.displayMetrics.setTo(metrics)*/
    }

    @dagger.Module
    abstract class Module {

        @PerFragment
        @ContributesAndroidInjector(modules = [TVHomeFragment.Module::class])
        abstract fun tvHomeFragment(): TVHomeFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [TVGamesFragment.Module::class])
        abstract fun tvGamesFragment(): TVGamesFragment

        @dagger.Module
        companion object {
            @Provides
            @PerActivity
            @JvmStatic
            fun gameInteractor(activity: MainTVActivity, retrogradeDb: RetrogradeDatabase) =
                    GameInteractor(activity, retrogradeDb)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}
