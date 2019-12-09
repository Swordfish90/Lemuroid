package com.swordfish.lemuroid.app.feature.favorites

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.DynamicGridLayoutManager
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.GamesAdapter
import com.swordfish.lemuroid.app.shared.GridSpaceDecoration
import com.swordfish.lemuroid.app.shared.RecyclerViewFragment
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.ui.updateVisibility
import javax.inject.Inject

class FavoritesFragment : RecyclerViewFragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private lateinit var favoritesViewModel: FavoritesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesViewModel = ViewModelProviders.of(this, FavoritesViewModel.Factory(retrogradeDb))
                .get(FavoritesViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()

        val gamesAdapter = GamesAdapter(R.layout.layout_game_grid, gameInteractor)
        favoritesViewModel.favorites.observe(this, Observer {
            gamesAdapter.submitList(it)
            emptyView?.updateVisibility(it.isEmpty())
        })

        recyclerView?.apply {
            this.adapter = gamesAdapter
            this.layoutManager = DynamicGridLayoutManager(context)

            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            GridSpaceDecoration.setSingleGridSpaceDecoration(this, spacingInPixels)
        }
        restoreRecyclerViewState()
    }

    @dagger.Module
    class Module
}
