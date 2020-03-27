package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.mobile.shared.GamesAdapter
import com.swordfish.lemuroid.app.mobile.shared.RecyclerViewFragment
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import javax.inject.Inject

class GamesFragment : RecyclerViewFragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private val args: GamesFragmentArgs by navArgs()

    private lateinit var gamesViewModel: GamesViewModel

    private var gamesAdapter: GamesAdapter? = null

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

        recyclerView?.apply {
            adapter = gamesAdapter
            layoutManager = LinearLayoutManager(context)
        }
        restoreRecyclerViewState()
    }

    @dagger.Module
    class Module
}
