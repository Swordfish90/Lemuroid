package com.swordfish.lemuroid.app.mobile.feature.systems

import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.DynamicGridLayoutManager
import com.swordfish.lemuroid.app.mobile.shared.GridSpaceDecoration
import com.swordfish.lemuroid.app.mobile.shared.RecyclerViewFragment
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MetaSystemsFragment : RecyclerViewFragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase

    private var metaSystemsAdapter: MetaSystemsAdapter? = null

    private lateinit var metaSystemsViewModel: MetaSystemsViewModel

    override fun onResume() {
        super.onResume()

        metaSystemsViewModel = ViewModelProviders.of(this, MetaSystemsViewModel.Factory(retrogradeDb))
            .get(MetaSystemsViewModel::class.java)

        metaSystemsAdapter = MetaSystemsAdapter { navigateToGames(it) }
        metaSystemsViewModel.availableMetaSystems
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy {
                metaSystemsAdapter?.submitList(it)
                emptyView?.setVisibleOrGone(it.isEmpty())
            }

        recyclerView?.apply {
            this.adapter = metaSystemsAdapter
            this.layoutManager = DynamicGridLayoutManager(context, 2)

            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            GridSpaceDecoration.setSingleGridSpaceDecoration(this, spacingInPixels)
        }
        restoreRecyclerViewState()
    }

    private fun navigateToGames(system: MetaSystemID) {
        val dbNames = system.systemIDs
            .map { it.dbname }
            .toTypedArray()

        val action = MetaSystemsFragmentDirections.actionNavigationSystemsToNavigationGames(dbNames)
        findNavController().navigate(action)
    }

    @dagger.Module
    class Module
}
