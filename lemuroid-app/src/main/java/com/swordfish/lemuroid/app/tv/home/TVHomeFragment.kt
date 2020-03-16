package com.swordfish.lemuroid.app.tv.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.swordfish.lemuroid.app.feature.library.LibraryIndexWork
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
import com.swordfish.lemuroid.app.tv.folderpicker.TVFolderPickerLauncher
import com.swordfish.lemuroid.common.livedata.debounce
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when(item) {
                is Game -> gameInteractor.onGamePlay(item)
                is GameSystem -> {
                    val action = TVHomeFragmentDirections.actionNavigationSystemsToNavigationGames(item.id.dbname)
                    findNavController().navigate(action)
                }
                is TVSetting -> {
                    when (item) {
                        TVSetting.RESCAN -> LibraryIndexWork.enqueueUniqueWork(context!!.applicationContext)
                        TVSetting.CHOOSE_DIRECTORY -> launchFolderPicker()
                    }
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        val factory = TVHomeViewModel.Factory(retrogradeDb)
        val homeViewModel = ViewModelProviders.of(this, factory).get(TVHomeViewModel::class.java)

        val zippedLiveData = zipLiveDataWithNull(
            homeViewModel.recentGames,
            homeViewModel.systems
        )

        zippedLiveData.debounce().observe(this, Observer {
            adapter = buildAdapter(
                it[0] as List<Game>? ?: listOf(),
                it[1] as List<GameSystem>? ?: listOf()
            )
        })
    }

    private fun buildAdapter(recentGames: List<Game>, systems: List<GameSystem>) = ArrayObjectAdapter(ListRowPresenter()).apply {
        if (recentGames.isNotEmpty()) {
            val items = ArrayObjectAdapter(GamePresenter(resources.getDimensionPixelSize(R.dimen.card_width)))
            items.addAll(0, recentGames)
            this.add(ListRow(HeaderItem("Recents"), items))
        }

        if (systems.isNotEmpty()) {
            val items = ArrayObjectAdapter(
                    SystemPresenter(
                            resources.getDimensionPixelSize(R.dimen.card_width),
                            resources.getDimensionPixelSize(R.dimen.card_padding)
                    )
            )
            items.addAll(0, systems)
            this.add(ListRow(HeaderItem("Systems"), items))
        }

        val items = ArrayObjectAdapter(
                SettingPresenter(
                        resources.getDimensionPixelSize(R.dimen.card_width),
                        resources.getDimensionPixelSize(R.dimen.card_padding)
                )
        )
        items.add(0, TVSetting.RESCAN)
        items.add(1, TVSetting.CHOOSE_DIRECTORY)
        this.add(ListRow(HeaderItem("Settings"), items))
    }

    private fun launchFolderPicker() {
        activity?.let { TVFolderPickerLauncher.pickFolder(it) }
    }

    @dagger.Module
    class Module
}
