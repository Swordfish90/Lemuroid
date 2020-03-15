package com.swordfish.lemuroid.app.tv

import android.content.Context
import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
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
        gridPresenter.numberOfColumns = 5
        setGridPresenter(gridPresenter)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()

        val factory = TVGamesViewModel.Factory(context!!.applicationContext, retrogradeDb)
        val gamesViewModel = ViewModelProviders.of(this, factory).get(TVGamesViewModel::class.java)

        gamesViewModel.games.observe(this, Observer { pagedList ->
            val adapter = PagedListObjectAdapter(GamePresenter(resources.getDimensionPixelSize(R.dimen.card_width)), Game.DIFF_CALLBACK)
            adapter.pagedList = pagedList
            this.adapter = adapter
        })

        args.systemId?.let {
            gamesViewModel.systemId.value = it
        }
    }

    @dagger.Module
    class Module
}
