package com.swordfish.lemuroid.app.tv.search

import android.content.Context
import android.os.Bundle
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.paging.cachedIn
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TVSearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private val searchRelay: PublishRelay<String> = PublishRelay.create()

    private lateinit var rowsAdapter: ArrayObjectAdapter
    private lateinit var searchViewModel: TVSearchViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Game -> gameInteractor.onGamePlay(item)
            }
        }

        rowsAdapter = createAdapter()

        val factory = TVSearchViewModel.Factory(retrogradeDb)
        searchViewModel = ViewModelProviders.of(this, factory).get(TVSearchViewModel::class.java)

        searchViewModel.searchResults.cachedIn(lifecycle).observe(this) {
            val gamesAdapter = (rowsAdapter.get(0) as ListRow).adapter as PagingDataAdapter<Game>
            gamesAdapter.submitData(lifecycle, it)
        }

        searchRelay
            .debounce(1, TimeUnit.SECONDS)
            .distinctUntilChanged()
            .autoDispose(scope())
            .subscribeBy { searchViewModel.queryString.postValue(it) }

        setSearchResultProvider(this)
    }

    private fun createAdapter(): ArrayObjectAdapter {
        val searchAdapter = ArrayObjectAdapter(ListRowPresenter())

        val gamePresenter = GamePresenter(
            resources.getDimensionPixelSize(R.dimen.card_size),
            gameInteractor
        )

        val gamesAdapter = PagingDataAdapter(gamePresenter, Game.DIFF_CALLBACK)
        searchAdapter.add(
            ListRow(
                HeaderItem(resources.getString(R.string.tv_search_results)),
                gamesAdapter
            )
        )

        return searchAdapter
    }

    override fun getResultsAdapter(): ObjectAdapter {
        return rowsAdapter
    }

    override fun onQueryTextChange(query: String): Boolean {
        searchRelay.accept(query)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        searchRelay.accept(query)
        return true
    }

    @dagger.Module
    class Module
}
