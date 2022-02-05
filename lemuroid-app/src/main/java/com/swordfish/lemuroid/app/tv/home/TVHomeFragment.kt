package com.swordfish.lemuroid.app.tv.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.DiffCallback
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import com.swordfish.lemuroid.app.shared.settings.StorageFrameworkPickerLauncher
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo
import com.swordfish.lemuroid.app.tv.folderpicker.TVFolderPickerLauncher
import com.swordfish.lemuroid.app.tv.settings.TVSettingsActivity
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
import com.swordfish.lemuroid.app.tv.shared.TVHelper
import com.swordfish.lemuroid.app.utils.livedata.toObservable
import com.swordfish.lemuroid.common.rx.RXUtils
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TVHomeFragment : BrowseSupportFragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor
    @Inject lateinit var coverLoader: CoverLoader
    @Inject lateinit var saveSyncManager: SaveSyncManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Game -> gameInteractor.onGamePlay(item)
                is MetaSystemInfo -> {
                    val systemIds = item.metaSystem.systemIDs
                        .map { it.dbname }
                        .toTypedArray()

                    val action = TVHomeFragmentDirections.actionNavigationSystemsToNavigationGames(systemIds)
                    findNavController().navigate(action)
                }
                is TVSetting -> {
                    when (item.type) {
                        TVSettingType.STOP_RESCAN -> LibraryIndexScheduler.cancelLibrarySync(
                            requireContext().applicationContext
                        )
                        TVSettingType.RESCAN -> LibraryIndexScheduler.scheduleLibrarySync(
                            requireContext().applicationContext
                        )
                        TVSettingType.CHOOSE_DIRECTORY -> launchFolderPicker()
                        TVSettingType.SETTINGS -> launchTVSettings()
                        TVSettingType.SHOW_ALL_FAVORITES -> launchFavorites()
                        TVSettingType.SAVE_SYNC -> SaveSyncWork.enqueueManualWork(
                            requireContext().applicationContext
                        )
                    }
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recreateAdapter(
            includeFavorites = false,
            includeRecentGames = false,
            includeSystems = false
        )
        setOnSearchClickedListener {
            findNavController().navigate(R.id.navigation_search)
        }

        val factory = TVHomeViewModel.Factory(retrogradeDb, requireContext().applicationContext)
        val homeViewModel = ViewModelProvider(this, factory).get(TVHomeViewModel::class.java)

        val indexingProgress = homeViewModel.indexingInProgress.toObservable(this)
        val directoryScanInProgress = homeViewModel.directoryScanInProgress.toObservable(this)

        val entriesObservable = RXUtils.combineLatest(
            homeViewModel.favoritesGames,
            homeViewModel.recentGames,
            homeViewModel.availableSystems,
            indexingProgress,
            directoryScanInProgress
        )

        entriesObservable
            .debounce(50, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(AndroidLifecycleScopeProvider.from(viewLifecycleOwner))
            .subscribeBy { (favoriteGames, recentGames, systems, indexInProgress, scanInProgress) ->
                update(favoriteGames, recentGames, systems, indexInProgress, scanInProgress)
            }
    }

    private fun update(
        favoritesGames: List<Game>,
        recentGames: List<Game>,
        metaSystems: List<MetaSystemInfo>,
        indexInProgress: Boolean,
        scanInProgress: Boolean
    ) {
        val adapterHasFavorites = findAdapterById<ObjectAdapter>(FAVORITES_ADAPTER) != null
        val adapterHasGames = findAdapterById<ObjectAdapter>(RECENTS_ADAPTER) != null
        val adapterHasSystems = findAdapterById<ObjectAdapter>(SYSTEM_ADAPTER) != null

        val favoritesChanged = adapterHasFavorites != favoritesGames.isNotEmpty()
        val recentsChanged = adapterHasGames != recentGames.isNotEmpty()
        val systemsChanged = adapterHasSystems != metaSystems.isNotEmpty()

        if (favoritesChanged || recentsChanged || systemsChanged) {
            recreateAdapter(favoritesGames.isNotEmpty(), recentGames.isNotEmpty(), metaSystems.isNotEmpty())
        }

        findAdapterById<ArrayObjectAdapter>(FAVORITES_ADAPTER)?.apply {
            if (favoritesGames.size <= TVHomeViewModel.CAROUSEL_MAX_ITEMS) {
                setItems(favoritesGames, LEANBACK_MULTI_DIFF_CALLBACK)
            } else {
                val allItems = favoritesGames.subList(0, TVHomeViewModel.CAROUSEL_MAX_ITEMS) +
                    listOf(TVSetting(TVSettingType.SHOW_ALL_FAVORITES))
                setItems(allItems, LEANBACK_MULTI_DIFF_CALLBACK)
            }
        }
        findAdapterById<ArrayObjectAdapter>(RECENTS_ADAPTER)?.setItems(recentGames, LEANBACK_GAME_DIFF_CALLBACK)
        findAdapterById<ArrayObjectAdapter>(SYSTEM_ADAPTER)?.setItems(metaSystems, LEANBACK_SYSTEM_DIFF_CALLBACK)
        findAdapterById<ArrayObjectAdapter>(SETTINGS_ADAPTER)?.setItems(
            buildSettingsRowItems(indexInProgress, scanInProgress),
            LEANBACK_SETTING_DIFF_CALLBACK
        )
    }

    private fun <T> findAdapterById(id: Long): T? {
        for (i: Int in 0 until adapter.size()) {
            val listRow = adapter.get(i) as ListRow
            if (listRow.headerItem.id == id) {
                return listRow.adapter as T
            }
        }
        return null
    }

    private fun recreateAdapter(
        includeFavorites: Boolean,
        includeRecentGames: Boolean,
        includeSystems: Boolean
    ) {
        val result = ArrayObjectAdapter(ListRowPresenter())
        val cardSize = resources.getDimensionPixelSize(R.dimen.card_size)
        val cardPadding = resources.getDimensionPixelSize(R.dimen.card_padding)

        if (includeFavorites) {
            val presenter = ClassPresenterSelector()
            presenter.addClassPresenter(Game::class.java, GamePresenter(cardSize, gameInteractor, coverLoader))
            presenter.addClassPresenter(TVSetting::class.java, SettingPresenter(cardSize, cardPadding))
            val favouritesItems = ArrayObjectAdapter(presenter)
            val title = resources.getString(R.string.tv_home_section_favorites)
            result.add(ListRow(HeaderItem(FAVORITES_ADAPTER, title), favouritesItems))
        }

        if (includeRecentGames) {
            val recentItems = ArrayObjectAdapter(GamePresenter(cardSize, gameInteractor, coverLoader))
            val title = resources.getString(R.string.tv_home_section_recents)
            result.add(ListRow(HeaderItem(RECENTS_ADAPTER, title), recentItems))
        }

        if (includeSystems) {
            val systemItems = ArrayObjectAdapter(SystemPresenter(cardSize, cardPadding))
            val title = resources.getString(R.string.tv_home_section_systems)
            result.add(ListRow(HeaderItem(SYSTEM_ADAPTER, title), systemItems))
        }

        val settingsItems = ArrayObjectAdapter(SettingPresenter(cardSize, cardPadding))
        settingsItems.setItems(
            buildSettingsRowItems(indexInProgress = false, scanInProgress = false),
            LEANBACK_SETTING_DIFF_CALLBACK
        )
        val settingsTitle = resources.getString(R.string.tv_home_section_settings)
        result.add(ListRow(HeaderItem(SETTINGS_ADAPTER, settingsTitle), settingsItems))

        adapter = result
    }

    private fun buildSettingsRowItems(
        indexInProgress: Boolean,
        scanInProgress: Boolean
    ): List<TVSetting> {
        return mutableListOf<TVSetting>().apply {
            if (scanInProgress) {
                add(TVSetting(TVSettingType.STOP_RESCAN, true))
            } else {
                add(TVSetting(TVSettingType.RESCAN, !indexInProgress))
            }

            add(TVSetting(TVSettingType.CHOOSE_DIRECTORY, !indexInProgress))
            add(TVSetting(TVSettingType.SETTINGS, !indexInProgress))
            if (saveSyncManager.isSupported() && saveSyncManager.isConfigured()) {
                add(TVSetting(TVSettingType.SAVE_SYNC, !indexInProgress))
            }
        }
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

    private fun launchFavorites() {
        findNavController().navigate(R.id.navigation_favorites)
    }

    companion object {
        const val RECENTS_ADAPTER = 1L
        const val SYSTEM_ADAPTER = 2L
        const val SETTINGS_ADAPTER = 3L
        const val FAVORITES_ADAPTER = 4L

        val LEANBACK_MULTI_DIFF_CALLBACK = object : DiffCallback<Any>() {
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    (oldItem is Game && newItem is Game) -> {
                        LEANBACK_GAME_DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)
                    }
                    (oldItem is TVSetting && newItem is TVSetting) -> {
                        LEANBACK_SETTING_DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)
                    }
                    else -> false
                }
            }

            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    (oldItem is Game && newItem is Game) -> {
                        LEANBACK_GAME_DIFF_CALLBACK.areItemsTheSame(oldItem, newItem)
                    }
                    (oldItem is TVSetting && newItem is TVSetting) -> {
                        LEANBACK_SETTING_DIFF_CALLBACK.areItemsTheSame(oldItem, newItem)
                    }
                    else -> false
                }
            }
        }

        val LEANBACK_GAME_DIFF_CALLBACK = object : DiffCallback<Game>() {
            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
                return oldItem.id == newItem.id
            }
        }

        val LEANBACK_SYSTEM_DIFF_CALLBACK = object : DiffCallback<MetaSystemInfo>() {
            override fun areContentsTheSame(oldInfo: MetaSystemInfo, newInfo: MetaSystemInfo): Boolean {
                return oldInfo == newInfo
            }

            override fun areItemsTheSame(oldInfo: MetaSystemInfo, newInfo: MetaSystemInfo): Boolean {
                return oldInfo.metaSystem.name == newInfo.metaSystem.name
            }
        }

        val LEANBACK_SETTING_DIFF_CALLBACK = object : DiffCallback<TVSetting>() {
            override fun areContentsTheSame(oldItem: TVSetting, newItem: TVSetting): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: TVSetting, newItem: TVSetting): Boolean {
                return oldItem.type == newItem.type
            }
        }
    }

    @dagger.Module
    class Module
}
