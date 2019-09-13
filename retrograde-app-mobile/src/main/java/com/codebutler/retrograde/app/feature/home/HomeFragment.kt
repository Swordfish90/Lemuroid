package com.codebutler.retrograde.app.feature.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.feature.game.GameLauncherActivity
import com.codebutler.retrograde.app.feature.main.MainActivity
import com.codebutler.retrograde.app.shared.GameInteractor
import com.codebutler.retrograde.app.shared.GamesAdapter
import com.codebutler.retrograde.lib.injection.PerFragment
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.dao.updateAsync
import dagger.Provides
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private var favoritesAdapter: GamesAdapter? = null
    private var recentsAdapter: GamesAdapter? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        homeViewModel = ViewModelProviders.of(this, HomeViewModel.Factory(retrogradeDb)).get(HomeViewModel::class.java)

        setupFavoriteView()

        setupRecentsView()

        root.findViewById<RecyclerView>(R.id.favorites_recyclerview).apply {
            this.adapter = favoritesAdapter
            this.layoutManager = GridLayoutManager(context!!, 2, GridLayoutManager.HORIZONTAL, false)
        }

        root.findViewById<RecyclerView>(R.id.recent_recyclerview).apply {
            this.adapter = recentsAdapter
            this.layoutManager = GridLayoutManager(context!!, 2, GridLayoutManager.HORIZONTAL, false)
        }

        return root
    }

    private fun setupRecentsView() {
        recentsAdapter = GamesAdapter(R.layout.layout_linear_game, gameInteractor)

        homeViewModel.recentGames.observe(this, Observer { pagedList ->
            recentsAdapter?.submitList(pagedList)
        })
    }

    private fun setupFavoriteView() {
        favoritesAdapter = GamesAdapter(R.layout.layout_linear_game, gameInteractor)

        homeViewModel.favoriteGames.observe(this, Observer { pagedList ->
            favoritesAdapter?.submitList(pagedList)
        })
    }

    @dagger.Module
    class Module
}
