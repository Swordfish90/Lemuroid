/*
 * BrowseFragment.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.retrograde.app.feature.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v17.leanback.app.BrowseFragment
import android.support.v17.leanback.app.BrowseSupportFragment
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v4.app.ActivityOptionsCompat
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.feature.home.HomeAdapterFactory.AboutItem
import com.codebutler.retrograde.app.feature.home.HomeAdapterFactory.AllGamesItem
import com.codebutler.retrograde.app.feature.home.HomeAdapterFactory.GameSystemItem
import com.codebutler.retrograde.app.feature.home.HomeAdapterFactory.RescanItem
import com.codebutler.retrograde.app.feature.home.HomeAdapterFactory.SettingsItem
import com.codebutler.retrograde.app.feature.main.MainActivity
import com.codebutler.retrograde.app.feature.search.GamesSearchFragment
import com.codebutler.retrograde.app.feature.settings.SettingsActivity
import com.codebutler.retrograde.app.shared.GameInteractionHandler
import com.codebutler.retrograde.lib.injection.PerFragment
import com.codebutler.retrograde.lib.library.GameLibrary
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.codebutler.retrograde.app.shared.ui.SimpleErrorFragment
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import dagger.Provides
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class HomeFragment : BrowseSupportFragment(),
        OnItemViewClickedListener {

    @Inject lateinit var adapterFactory: HomeAdapterFactory
    @Inject lateinit var gameLibrary: GameLibrary
    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractionHandler: GameInteractionHandler

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameInteractionHandler.onRefreshListener = {
            loadContents()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        title = getString(R.string.app_name)
        onItemViewClickedListener = this
        loadContents()
    }

    override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row) {
        when (item) {
            is Game -> gameInteractionHandler.onItemClick(item)
            is GameSystemItem -> fragmentManager!!.beginTransaction()
                    .replace(R.id.content, GamesGridFragment.create(GamesGridFragment.Mode.SYSTEM, item.system.id))
                    .addToBackStack(null)
                    .commit()
            is AllGamesItem -> fragmentManager!!.beginTransaction()
                    .replace(R.id.content, GamesGridFragment.create(GamesGridFragment.Mode.ALL))
                    .addToBackStack(null)
                    .commit()
            is RescanItem -> {
                progressBarManager.show()
                gameLibrary.indexGames()
                        .observeOn(AndroidSchedulers.mainThread())
                        .autoDisposable(scope())
                        .subscribe(
                                {
                                    loadContents()
                                    progressBarManager.hide()
                                },
                                { error ->
                                    Timber.e(error)
                                    progressBarManager.hide()
                                    val errorFragment = SimpleErrorFragment.create(error.toString())
                                    fragmentManager!!.beginTransaction()
                                            .replace(R.id.content, errorFragment)
                                            .addToBackStack(null)
                                            .commit()
                                })
            }
            is SettingsItem -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!)
                        .toBundle()
                startActivity(intent, bundle)
            }
            is AboutItem -> {}
        }
    }

    private fun loadContents() {
        retrogradeDb.gameDao().selectCounts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope())
                .subscribe { counts ->
                    val categoryRowAdapter = ArrayObjectAdapter(ListRowPresenter())
                    if (counts.favoritesCount > 0) {
                        categoryRowAdapter.add(ListRow(
                                HeaderItem(getString(R.string.favorites)),
                                adapterFactory.buildFavoritesAdapter()))
                    }
                    if (counts.recentsCount > 0) {
                        categoryRowAdapter.add(ListRow(
                                HeaderItem(getString(R.string.recently_played)),
                                adapterFactory.buildRecentsAdapter()))
                    }
                    categoryRowAdapter.add(ListRow(
                            HeaderItem(getString(R.string.library)),
                            adapterFactory.buildSystemsAdapter(counts)))
                    categoryRowAdapter.add(ListRow(
                            HeaderItem(getString(R.string.settings)),
                            adapterFactory.buildSettingsAdapter()))
                    adapter = categoryRowAdapter

                    if (counts.totalCount > 0) {
                        setOnSearchClickedListener {
                            fragmentManager!!.beginTransaction()
                                    .replace(R.id.content, GamesSearchFragment.create())
                                    .addToBackStack(null)
                                    .commit()
                        }
                    } else {
                        setOnSearchClickedListener(null)
                    }
                }
    }

    @dagger.Module
    class Module {

        @Provides
        @PerFragment
        fun gameInteractionHandler(activity: MainActivity, retrogradeDb: RetrogradeDatabase) =
                GameInteractionHandler(activity, retrogradeDb)

        @Provides
        @PerFragment
        fun adapterFactory(fragment: HomeFragment, retrogradeDb: RetrogradeDatabase, handler: GameInteractionHandler) =
                HomeAdapterFactory(fragment, retrogradeDb, handler)
    }
}
