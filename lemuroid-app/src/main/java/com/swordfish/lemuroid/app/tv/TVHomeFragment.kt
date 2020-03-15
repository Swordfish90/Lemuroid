package com.swordfish.lemuroid.app.tv

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.common.livedata.zipLiveDataWithNull
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class TVHomeFragment : BrowseSupportFragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()

        val homeViewModel =
                ViewModelProviders.of(this,
                        TVHomeViewModel.Factory(retrogradeDb)).get(TVHomeViewModel::class.java)

        zipLiveDataWithNull(
            homeViewModel.recentGames,
            homeViewModel.discoverGames,
            homeViewModel.systems
        ).observe(this, Observer {
            adapter = ArrayObjectAdapter(ListRowPresenter()).apply {
                it[0]?.let {
                    val games = it as List<Game>
                    if (games.isNotEmpty()) {
                        val items = ArrayObjectAdapter(GamePresenter(resources.getDimensionPixelSize(R.dimen.card_width)))
                        items.addAll(0, games)
                        this.add(ListRow(HeaderItem("Recents"), items))
                    }
                }

                it[1]?.let {
                    val games = it as List<Game>
                    if (games.isNotEmpty()) {
                        val items = ArrayObjectAdapter(GamePresenter(resources.getDimensionPixelSize(R.dimen.card_width)))
                        items.addAll(0, games)
                        this.add(ListRow(HeaderItem("Discover"), items))
                    }
                }

                it[2]?.let {
                    val systems = it as List<GameSystem>
                    if (systems.isNotEmpty()) {
                        val items = ArrayObjectAdapter(SystemPresenter(resources.getDimensionPixelSize(R.dimen.card_width)))
                        items.addAll(0, systems)
                        this.add(ListRow(HeaderItem("Systems"), items))
                    }
                }
            }
        })

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when(item) {
                is Game -> gameInteractor.onGamePlay(item)
                is GameSystem -> {
                    val action = TVHomeFragmentDirections.actionNavigationSystemsToNavigationGames(item.id.dbname)
                    findNavController().navigate(action)
                }
            }
        }
    }

    @dagger.Module
    class Module
}
