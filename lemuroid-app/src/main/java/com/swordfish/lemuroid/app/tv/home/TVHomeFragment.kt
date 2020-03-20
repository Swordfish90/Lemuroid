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
import androidx.leanback.widget.DiffCallback
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.feature.library.LibraryIndexWork
import com.swordfish.lemuroid.app.feature.settings.StorageFrameworkPickerLauncher
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.tv.folderpicker.TVFolderPickerLauncher
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
import com.swordfish.lemuroid.app.tv.shared.TVHelper
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
            when (item) {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = createAdapter()
    }

    override fun onResume() {
        super.onResume()

        val factory = TVHomeViewModel.Factory(retrogradeDb)
        val homeViewModel = ViewModelProviders.of(this, factory).get(TVHomeViewModel::class.java)

        homeViewModel.recentGames.observe(this, Observer {
            updateRecentGames(it)
        })

        homeViewModel.systems.observe(this, Observer {
            updateSystems(it)
        })
    }

    private fun updateRecentGames(recentGames: List<Game>) {
        val recentsItems = (adapter.get(0) as ListRow).adapter as ArrayObjectAdapter
        recentsItems.setItems(recentGames, LEANBACK_GAME_DIFF_CALLBACK)
    }

    private fun updateSystems(systems: List<GameSystem>) {
        val systemsItems = (adapter.get(1) as ListRow).adapter as ArrayObjectAdapter
        systemsItems.setItems(systems, LEANBACK_SYSTEM_DIFF_CALLBACK)
    }

    private fun createAdapter(): ArrayObjectAdapter {
        val result = ArrayObjectAdapter(ListRowPresenter())

        val recentItems = ArrayObjectAdapter(GamePresenter(resources.getDimensionPixelSize(R.dimen.card_size)))
        result.add(ListRow(HeaderItem(resources.getString(R.string.tv_home_section_recents)), recentItems))

        val systemItems = ArrayObjectAdapter(
            SystemPresenter(
                resources.getDimensionPixelSize(R.dimen.card_size),
                resources.getDimensionPixelSize(R.dimen.card_padding)
            )
        )
        result.add(ListRow(HeaderItem(resources.getString(R.string.tv_home_section_systems)), systemItems))

        val settingsItems = ArrayObjectAdapter(
            SettingPresenter(
                resources.getDimensionPixelSize(R.dimen.card_size),
                resources.getDimensionPixelSize(R.dimen.card_padding)
            )
        )
        settingsItems.add(0, TVSetting.RESCAN)
        settingsItems.add(1, TVSetting.CHOOSE_DIRECTORY)
        result.add(ListRow(HeaderItem(resources.getString(R.string.tv_home_section_settings)), settingsItems))

        return result
    }

    private fun launchFolderPicker() {
        if (TVHelper.isSAFSupported(requireContext())) {
            StorageFrameworkPickerLauncher.pickFolder(requireContext())
        } else {
            TVFolderPickerLauncher.pickFolder(requireContext())
        }
    }

    companion object {
        val LEANBACK_GAME_DIFF_CALLBACK = object : DiffCallback<Game>() {
            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem.id == newItem.id
            }
        }

        val LEANBACK_SYSTEM_DIFF_CALLBACK = object : DiffCallback<GameSystem>() {
            override fun areContentsTheSame(oldItem: GameSystem, newItem: GameSystem): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: GameSystem, newItem: GameSystem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    @dagger.Module
    class Module
}
