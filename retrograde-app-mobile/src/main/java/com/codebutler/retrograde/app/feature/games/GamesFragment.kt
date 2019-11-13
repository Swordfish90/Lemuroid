package com.codebutler.retrograde.app.feature.games

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.shared.GameInteractor
import com.codebutler.retrograde.app.shared.GamesAdapter
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class GamesFragment : Fragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private val args: GamesFragmentArgs by navArgs()

    private lateinit var gamesViewModel: GamesViewModel

    private var gamesAdapter: GamesAdapter? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_games, container, false)
    }

    override fun onResume() {
        super.onResume()

        gamesAdapter = GamesAdapter(R.layout.layout_game_list, gameInteractor)

        gamesViewModel = ViewModelProviders.of(this, GamesViewModel.Factory(retrogradeDb))
                .get(GamesViewModel::class.java)

        gamesViewModel.games.observe(this, Observer { pagedList ->
            gamesAdapter?.submitList(pagedList)
        })

        args.systemId?.let {
            gamesViewModel.systemId.value = it
        }

        view?.findViewById<RecyclerView>(R.id.games_recyclerview)?.apply {
            this.adapter = gamesAdapter
            this.layoutManager = LinearLayoutManager(context)
        }
    }

    @dagger.Module
    class Module
}
