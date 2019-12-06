package com.swordfish.lemuroid.app.feature.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.GamesAdapter
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.jakewharton.rxbinding3.appcompat.queryTextChanges
import com.swordfish.lemuroid.app.utils.livedata.CombinedLiveData
import com.swordfish.lemuroid.lib.ui.updateVisibility
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchFragment : Fragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private lateinit var searchViewModel: SearchViewModel

    private lateinit var emptyView: View

    private var searchSubject: PublishSubject<String> = PublishSubject.create()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        setupSearchMenuItem(menu)
    }

    private fun setupSearchMenuItem(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        searchItem.expandActionView()
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                activity?.onBackPressed()
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem?) = true
        })

        val searchView = searchItem.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setQuery(searchViewModel.queryString.value, false)
        searchView.queryTextChanges()
                .debounce(1, TimeUnit.SECONDS)
                .map { it.toString() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(searchSubject)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        searchViewModel = ViewModelProviders.of(this, SearchViewModel.Factory(retrogradeDb))
            .get(SearchViewModel::class.java)

        emptyView = root.findViewById(R.id.search_empty_view)

        return root
    }

    override fun onResume() {
        super.onResume()

        val gamesAdapter = GamesAdapter(R.layout.layout_game_list, gameInteractor)
        searchViewModel.searchResults.observe(this, Observer {
            gamesAdapter.submitList(it)
        })

        searchViewModel.emptyViewVisible.observe(this, Observer {
            emptyView.updateVisibility(it)
        })

        searchSubject
            .distinctUntilChanged()
            .autoDisposable(scope())
            .subscribe { searchViewModel.queryString.postValue(it) }

        view?.findViewById<RecyclerView>(R.id.search_recyclerview)?.apply {
            this.adapter = gamesAdapter
            this.layoutManager = LinearLayoutManager(context)
        }
    }

    @dagger.Module
    class Module
}
