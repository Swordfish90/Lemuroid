package com.swordfish.lemuroid.app.tv.main

import android.Manifest
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.tv.games.TVGamesFragment
import com.swordfish.lemuroid.app.tv.home.TVHomeFragment
import com.swordfish.lemuroid.app.tv.search.TVSearchFragment
import com.swordfish.lemuroid.app.tv.shared.BaseTVActivity
import com.swordfish.lemuroid.app.tv.shared.TVHelper
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.injection.PerFragment
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.android.schedulers.AndroidSchedulers

class MainTVActivity : BaseTVActivity() {

    var mainViewModel: MainTVViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_main)

        val factory = MainTVViewModel.Factory(applicationContext)
        mainViewModel = ViewModelProviders.of(this, factory)
            .get(MainTVViewModel::class.java)

        mainViewModel?.indexingInProgress?.observe(this) {
            findViewById<View>(R.id.tv_loading).setVisibleOrGone(it)
        }

        ensureLegacyStoragePermissionsIfNeeded()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (mainViewModel?.indexingInProgress?.value == true) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (mainViewModel?.indexingInProgress?.value == true) {
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
