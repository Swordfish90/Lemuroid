package com.swordfish.lemuroid.app.tv.search

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import javax.inject.Inject

@OptIn(FlowPreview::class)
class TVSearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {
    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    private val searchDebounce = MutableStateFlow("")

    private lateinit var rowsAdapter: ArrayObjectAdapter
    private lateinit var searchViewModel: TVSearchViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Game -> gameInteractor.onGamePlay(item)
            }
        }

        rowsAdapter = createAdapter()

        val factory = TVSearchViewModel.Factory(retrogradeDb)
        searchViewModel = ViewModelProvider(this, factory)[TVSearchViewModel::class.java]

        launchOnState(Lifecycle.State.RESUMED) {
            searchViewModel.searchResults
                .collect {
                    val gamesAdapter = (rowsAdapter.get(0) as ListRow).adapter as PagingDataAdapter<Game>
                    gamesAdapter.submitData(lifecycle, it)
                }
        }

        launchOnState(Lifecycle.State.RESUMED) {
            searchDebounce.debounce(1000)
                .collect { searchViewModel.queryString.value = it }
        }

        setSearchResultProvider(this)
    }

    private fun createAdapter(): ArrayObjectAdapter {
        val searchAdapter = ArrayObjectAdapter(ListRowPresenter())

        val gamePresenter =
            GamePresenter(
                resources.getDimensionPixelSize(com.swordfish.lemuroid.lib.R.dimen.card_size),
                gameInteractor,
            )

        val gamesAdapter = PagingDataAdapter(gamePresenter, Game.DIFF_CALLBACK)
        searchAdapter.add(
            ListRow(
                HeaderItem(resources.getString(R.string.tv_search_results)),
                gamesAdapter,
            ),
        )

        return searchAdapter
    }

    override fun getResultsAdapter(): ObjectAdapter {
        return rowsAdapter
    }

    override fun onQueryTextChange(query: String): Boolean {
        searchDebounce.value = query
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        searchDebounce.value = query
        return true
    }

    @dagger.Module
    class Module
}
