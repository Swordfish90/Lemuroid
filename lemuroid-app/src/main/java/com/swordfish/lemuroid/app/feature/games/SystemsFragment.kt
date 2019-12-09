package com.swordfish.lemuroid.app.feature.games

import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.DynamicGridLayoutManager
import com.swordfish.lemuroid.app.shared.GridSpaceDecoration
import com.swordfish.lemuroid.app.shared.RecyclerViewFragment
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.ui.updateVisibility
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class SystemsFragment : RecyclerViewFragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase

    private var systemsAdapter: SystemsAdapter? = null

    private lateinit var systemsViewModel: SystemsViewModel

    override fun onResume() {
        super.onResume()

        systemsViewModel = ViewModelProviders.of(this, SystemsViewModel.Factory(retrogradeDb))
                .get(SystemsViewModel::class.java)

        systemsAdapter = SystemsAdapter { navigateToGames(it) }
        systemsViewModel.availableSystems
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribeBy {
                    systemsAdapter?.submitList(it)
                    emptyView?.updateVisibility(it.isEmpty())
                }

        recyclerView?.apply {
            this.adapter = systemsAdapter
            this.layoutManager = DynamicGridLayoutManager(context, 2)

            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            GridSpaceDecoration.setSingleGridSpaceDecoration(this, spacingInPixels)
        }
        restoreRecyclerViewState()
    }

    private fun navigateToGames(system: GameSystem) {
        val action = SystemsFragmentDirections.actionNavigationSystemsToNavigationGames(system.id)
        findNavController().navigate(action)
    }

    @dagger.Module
    class Module
}
