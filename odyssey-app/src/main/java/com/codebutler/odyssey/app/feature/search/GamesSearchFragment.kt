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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import com.codebutler.odyssey.R
import com.codebutler.odyssey.app.feature.home.GamePresenter
import com.codebutler.odyssey.app.feature.main.MainActivity
import com.codebutler.odyssey.app.shared.GameInteractionHandler
import com.codebutler.odyssey.lib.injection.PerFragment
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.ui.PagedListObjectAdapter
import com.jakewharton.rxrelay2.PublishRelay
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposeWith
import dagger.Provides
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GamesSearchFragment : SearchSupportFragment(),
        SearchSupportFragment.SearchResultProvider,
        OnItemViewClickedListener {

    companion object {
        fun create(): GamesSearchFragment = GamesSearchFragment()
    }

    private val queryTextChangeRelay = PublishRelay.create<String>()
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    private var lastQuery: String? = null

    @Inject lateinit var odysseyDb: OdysseyDatabase
    @Inject lateinit var gameInteractionHandler: GameInteractionHandler

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchResultProvider(this)

        queryTextChangeRelay
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposeWith(AndroidLifecycleScopeProvider.from(this))
                .subscribe(this::search)

        setOnItemViewClickedListener(this)

        gameInteractionHandler.onRefreshListener = cb@ {
            search(lastQuery ?: return@cb)
        }
    }

    override fun getResultsAdapter(): ObjectAdapter = rowsAdapter

    override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row) {
        when (item) {
            is Game -> gameInteractionHandler.onItemClick(item)
        }
    }

    override fun onQueryTextChange(newQuery: String): Boolean {
        queryTextChangeRelay.accept(newQuery)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        search(query)
        return true
    }

    private fun search(query: String) {
        lastQuery = query
        rowsAdapter.clear()
        odysseyDb.gameDao().search(query)
                .create(0, PagedList.Config.Builder()
                        .setPageSize(50)
                        .setPrefetchDistance(50)
                        .build())
                .observe(this, Observer { pagedList ->
                    val header = HeaderItem(getString(R.string.search_results, query))
                    val adapter = PagedListObjectAdapter(GamePresenter(gameInteractionHandler), Game.DIFF_CALLBACK)
                    adapter.pagedList = pagedList
                    rowsAdapter.add(ListRow(header, adapter))
                })
    }

    @dagger.Module
    class Module {

        @Provides
        @PerFragment
        fun gameInteractionHandler(activity: MainActivity, odysseyDb: OdysseyDatabase)
                = GameInteractionHandler(activity, odysseyDb)
    }
}
