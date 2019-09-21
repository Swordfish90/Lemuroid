package com.codebutler.retrograde.app.feature.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.shared.DynamicGridLayoutManager
import com.codebutler.retrograde.app.shared.GameInteractor
import com.codebutler.retrograde.app.shared.GamesAdapter
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.jakewharton.rxbinding3.appcompat.queryTextChanges
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable

import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchFragment : Fragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        val searchViewModel = ViewModelProviders.of(this, SearchViewModel.Factory(retrogradeDb))
            .get(SearchViewModel::class.java)

        val gamesAdapter = GamesAdapter(R.layout.layout_game, gameInteractor)
        searchViewModel.searchResults.observe(this, Observer {
            gamesAdapter.submitList(it)
        })

        root.findViewById<SearchView>(R.id.search_searchview).queryTextChanges()
                .debounce(1, TimeUnit.SECONDS)
                .map { it.toString() }
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope())
                .subscribe { searchViewModel.queryString.value = it }

        root.findViewById<RecyclerView>(R.id.search_recyclerview).apply {
            this.adapter = gamesAdapter
            this.layoutManager = DynamicGridLayoutManager(context, 2)
        }

        return root
    }

    @dagger.Module
    class Module
}
