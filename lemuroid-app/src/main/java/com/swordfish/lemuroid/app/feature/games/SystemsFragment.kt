package com.swordfish.lemuroid.app.feature.games

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.DynamicGridLayoutManager
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SystemsFragment : Fragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase

    private var systemsAdapter: SystemsAdapter? = null

    private var systemsViewModel: SystemsViewModel? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_games, container, false)
    }

    override fun onResume() {
        super.onResume()

        systemsViewModel = ViewModelProviders.of(this, SystemsViewModel.Factory(retrogradeDb))
                .get(SystemsViewModel::class.java)

        systemsAdapter = SystemsAdapter { navigateToGames(it) }
        systemsViewModel?.availableSystems
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.autoDisposable(scope())
                ?.subscribeBy {
                    systemsAdapter?.submitList(it)
                }

        view?.findViewById<RecyclerView>(R.id.games_recyclerview)?.apply {
            this.adapter = systemsAdapter
            this.layoutManager = DynamicGridLayoutManager(context, 2)
        }
    }

    private fun navigateToGames(system: GameSystem) {
        val action = SystemsFragmentDirections.actionNavigationSystemsToNavigationGames(system.id)
        findNavController().navigate(action)
    }

    @dagger.Module
    class Module
}
