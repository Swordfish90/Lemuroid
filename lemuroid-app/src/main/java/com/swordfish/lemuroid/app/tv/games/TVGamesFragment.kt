package com.swordfish.lemuroid.app.tv.games

import android.content.Context
import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.paging.cachedIn
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
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

        val factory = TVGamesViewModel.Factory(retrogradeDb)
        val gamesViewModel = ViewModelProvider(this, factory).get(TVGamesViewModel::class.java)

        val cardSize = resources.getDimensionPixelSize(R.dimen.card_size)
        val pagingAdapter = PagingDataAdapter(GamePresenter(cardSize, gameInteractor), Game.DIFF_CALLBACK)

        this.adapter = pagingAdapter

        gamesViewModel.games.cachedIn(lifecycle).observe(this) { pagedList ->
            pagingAdapter.submitData(lifecycle, pagedList)
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

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    @dagger.Module
    class Module
}
