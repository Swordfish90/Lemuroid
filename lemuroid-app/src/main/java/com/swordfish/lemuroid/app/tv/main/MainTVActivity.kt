package com.swordfish.lemuroid.app.tv.main

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
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
import javax.inject.Inject

class MainTVActivity : BaseTVActivity() {

    @Inject lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_main)

        val mainViewModel = ViewModelProviders.of(this, MainTVViewModel.Factory(applicationContext))
                .get(MainTVViewModel::class.java)

        mainViewModel.indexingInProgress.observe(this, Observer {
            findViewById<View>(R.id.tv_loading).setVisibleOrGone(it)
        })

        ensureLegacyStoragePermissionsIfNeeded()
    }

    private fun ensureLegacyStoragePermissionsIfNeeded() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (!TVHelper.isSAFSupported(this)) {
            requestLegacyStoragePermissions(permissions)
        }
    }

    private fun requestLegacyStoragePermissions(permissions: Array<String>) {
        rxPermissions.request(*permissions)
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

            @Provides
            @PerActivity
            @JvmStatic
            fun rxPermissions(activity: MainTVActivity): RxPermissions = RxPermissions(activity)
        }
    }
}
