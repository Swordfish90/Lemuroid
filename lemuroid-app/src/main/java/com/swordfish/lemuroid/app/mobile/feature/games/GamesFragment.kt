package com.swordfish.lemuroid.app.mobile.feature.games

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.GamesAdapter
import com.swordfish.lemuroid.app.mobile.shared.RecyclerViewFragment
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import kotlinx.coroutines.launch
import javax.inject.Inject

class GamesFragment : RecyclerViewFragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor
    @Inject lateinit var coverLoader: CoverLoader

    private val args: GamesFragmentArgs by navArgs()

    private lateinit var gamesViewModel: GamesViewModel

    private var gamesAdapter: GamesAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gamesAdapter = GamesAdapter(R.layout.layout_game_list, gameInteractor, coverLoader)

        gamesViewModel = ViewModelProvider(
            this,
            GamesViewModel.Factory(retrogradeDb)
        )[GamesViewModel::class.java]

        recyclerView?.apply {
            adapter = gamesAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                gamesViewModel.games
                    .collect { gamesAdapter?.submitData(lifecycle, it) }
            }
        }

        gamesViewModel.systemIds.value = (listOf(*args.systemIds))
    }

    @dagger.Module
    class Module
}
