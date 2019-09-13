package com.codebutler.retrograde.app.feature.games

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.feature.game.GameLauncherActivity
import com.codebutler.retrograde.app.shared.DynamicGridLayoutManager
import com.codebutler.retrograde.app.shared.GameInteractor
import com.codebutler.retrograde.app.shared.GamesAdapter
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.dao.updateAsync
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class GamesFragment : Fragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private lateinit var gamesViewModel: GamesViewModel

    private var gamesAdapter: GamesAdapter? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_games, container, false)

        gamesAdapter = GamesAdapter(R.layout.layout_grid_game, gameInteractor)

        gamesViewModel = ViewModelProviders.of(this, GamesViewModel.Factory(retrogradeDb)).get(GamesViewModel::class.java)
        gamesViewModel.allGames.observe(this, Observer { pagedList ->
            gamesAdapter?.submitList(pagedList)
        })

        root.findViewById<RecyclerView>(R.id.games_recyclerview).apply {
            this.adapter = gamesAdapter
            this.layoutManager = DynamicGridLayoutManager(context, 2)
        }

        return root
    }

    @dagger.Module
    class Module
}
