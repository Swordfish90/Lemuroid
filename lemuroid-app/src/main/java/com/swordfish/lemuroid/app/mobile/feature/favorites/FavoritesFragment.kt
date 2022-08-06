package com.swordfish.lemuroid.app.mobile.feature.favorites

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.DynamicGridLayoutManager
import com.swordfish.lemuroid.app.mobile.shared.GamesAdapter
import com.swordfish.lemuroid.app.mobile.shared.GridSpaceDecoration
import com.swordfish.lemuroid.app.mobile.shared.RecyclerViewFragment
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.common.view.setVisibleOrGone
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import javax.inject.Inject

class FavoritesFragment : RecyclerViewFragment() {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase
    @Inject
    lateinit var gameInteractor: GameInteractor
    @Inject
    lateinit var coverLoader: CoverLoader

    private lateinit var favoritesViewModel: FavoritesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesViewModel = ViewModelProvider(
            this,
            FavoritesViewModel.Factory(retrogradeDb)
        )[FavoritesViewModel::class.java]

        val gamesAdapter = GamesAdapter(R.layout.layout_game_grid, gameInteractor, coverLoader)

        launchOnState(Lifecycle.State.RESUMED) {
            favoritesViewModel.favorites
                .collect { gamesAdapter.submitData(viewLifecycleOwner.lifecycle, it) }
        }

        gamesAdapter.addLoadStateListener {
            emptyView?.setVisibleOrGone(gamesAdapter.itemCount == 0)
        }

        recyclerView?.apply {
            this.adapter = gamesAdapter
            this.layoutManager = DynamicGridLayoutManager(context)

            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            GridSpaceDecoration.setSingleGridSpaceDecoration(this, spacingInPixels)
        }
    }

    @dagger.Module
    class Module
}
