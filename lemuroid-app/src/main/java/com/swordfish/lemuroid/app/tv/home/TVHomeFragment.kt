package com.swordfish.lemuroid.app.tv.home

import android.content.Context
import android.content.Intent
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
import com.swordfish.lemuroid.app.mobile.feature.systems.SystemInfo
import com.swordfish.lemuroid.app.shared.library.LibraryIndexWork
import com.swordfish.lemuroid.app.shared.settings.StorageFrameworkPickerLauncher
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.tv.folderpicker.TVFolderPickerLauncher
import com.swordfish.lemuroid.app.tv.settings.TVSettingsActivity
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
import com.swordfish.lemuroid.app.tv.shared.TVHelper
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
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
                is SystemInfo -> {
                    val systemId = item.system.id.dbname
                    val action = TVHomeFragmentDirections.actionNavigationSystemsToNavigationGames(systemId)
                    findNavController().navigate(action)
                }
                is TVSetting -> {
                    when (item) {
                        TVSetting.RESCAN -> LibraryIndexWork.enqueueUniqueWork(context!!.applicationContext)
                        TVSetting.CHOOSE_DIRECTORY -> launchFolderPicker()
                        TVSetting.SETTINGS -> launchTVSettings()
                    }
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = createAdapter()
        setOnSearchClickedListener {
            findNavController().navigate(R.id.navigation_search)
        }
    }

    override fun onResume() {
        super.onResume()

        val factory = TVHomeViewModel.Factory(retrogradeDb)
        val homeViewModel = ViewModelProviders.of(this, factory).get(TVHomeViewModel::class.java)

        homeViewModel.recentGames.observe(this, Observer {
            updateRecentGames(it)
        })

        homeViewModel
            .availableSystems
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy {
                updateSystems(it)
            }
    }

    private fun updateRecentGames(recentGames: List<Game>) {
        val recentsItems = (adapter.get(0) as ListRow).adapter as ArrayObjectAdapter
        recentsItems.setItems(recentGames, LEANBACK_GAME_DIFF_CALLBACK)
    }

    private fun updateSystems(systems: List<SystemInfo>) {
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
        settingsItems.add(2, TVSetting.SETTINGS)
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

    private fun launchTVSettings() {
        startActivity(Intent(requireContext(), TVSettingsActivity::class.java))
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

        val LEANBACK_SYSTEM_DIFF_CALLBACK = object : DiffCallback<SystemInfo>() {
            override fun areContentsTheSame(oldInfo: SystemInfo, newInfo: SystemInfo): Boolean {
                return oldInfo == newInfo
            }

            override fun areItemsTheSame(oldInfo: SystemInfo, newInfo: SystemInfo): Boolean {
                return oldInfo.system.id == newInfo.system.id
            }
        }
    }

    @dagger.Module
    class Module
}
