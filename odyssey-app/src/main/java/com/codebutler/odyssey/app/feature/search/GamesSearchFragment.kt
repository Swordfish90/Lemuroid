/*
 * SearchFragment.kt
 *
 * Copyright (C) 2017 Odyssey Project
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.app.feature.search

import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.content.Context
import android.os.Bundle
import android.support.v17.leanback.app.SearchSupportFragment
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.ObjectAdapter
import com.codebutler.odyssey.R
import com.codebutler.odyssey.app.feature.game.GameActivity
import com.codebutler.odyssey.app.feature.home.GamePresenter
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.ui.PagedListObjectAdapter
import com.jakewharton.rxrelay2.PublishRelay
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposeWith
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GamesSearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {

    companion object {
        fun create(): GamesSearchFragment = GamesSearchFragment()
    }

    private val queryTextChangeRelay = PublishRelay.create<String>()
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    @Inject lateinit var odysseyDb: OdysseyDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchResultProvider(this)

        queryTextChangeRelay
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposeWith(AndroidLifecycleScopeProvider.from(this))
                .subscribe(this::search)

        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Game -> startActivity(GameActivity.newIntent(context, item))
            }
        }
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun getResultsAdapter(): ObjectAdapter = rowsAdapter

    override fun onQueryTextChange(newQuery: String): Boolean {
        queryTextChangeRelay.accept(newQuery)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        search(query)
        return true
    }

    private fun search(query: String) {
        rowsAdapter.clear()
        odysseyDb.gameDao().search(query)
                .create(0, PagedList.Config.Builder()
                        .setPageSize(50)
                        .setPrefetchDistance(50)
                        .build())
                .observe(this, Observer { pagedList ->
                    val header = HeaderItem(getString(R.string.search_results, query))
                    val adapter = PagedListObjectAdapter(GamePresenter(), Game.DIFF_CALLBACK)
                    adapter.pagedList = pagedList
                    rowsAdapter.add(ListRow(header, adapter))
                })
    }

    @Subcomponent
    interface Component : AndroidInjector<GamesSearchFragment> {

        @Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<GamesSearchFragment>()
    }
}
