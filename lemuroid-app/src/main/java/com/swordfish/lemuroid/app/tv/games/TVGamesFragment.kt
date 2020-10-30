package com.swordfish.lemuroid.app.tv.games

import android.content.Context
import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
import com.swordfish.lemuroid.app.tv.shared.PagedListObjectAdapter
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TVGamesFragment : VerticalGridSupportFragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private val args: TVGamesFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = 4
        setGridPresenter(gridPresenter)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()

        val factory = TVGamesViewModel.Factory(retrogradeDb)
        val gamesViewModel = ViewModelProviders.of(this, factory).get(TVGamesViewModel::class.java)

        gamesViewModel.games.observe(this) { pagedList ->
            val cardSize = resources.getDimensionPixelSize(R.dimen.card_size)
            val adapter = PagedListObjectAdapter(GamePresenter(cardSize, gameInteractor), Game.DIFF_CALLBACK)
            adapter.pagedList = pagedList
            this.adapter = adapter
        }

        args.systemIds.let {
            gamesViewModel.systemIds.value = listOf(*it)
        }

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Game -> gameInteractor.onGamePlay(item)
            }
        }
    }

    @dagger.Module
    class Module
}
