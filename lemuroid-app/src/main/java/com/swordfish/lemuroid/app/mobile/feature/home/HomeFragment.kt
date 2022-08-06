package com.swordfish.lemuroid.app.mobile.feature.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase
    @Inject
    lateinit var gameInteractor: GameInteractor
    @Inject
    lateinit var coverLoader: CoverLoader
    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = HomeViewModel.Factory(requireContext().applicationContext, retrogradeDb)
        val homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Disable snapping in carousel view
        Carousel.setDefaultGlobalSnapHelperFactory(null)

        val pagingController = EpoxyHomeController(gameInteractor, settingsInteractor, coverLoader)

        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recyclerview)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = pagingController.adapter

        launchOnState(Lifecycle.State.RESUMED) {
            homeViewModel.getViewStates().collect {
                pagingController.update(it)
            }
        }
    }

    @dagger.Module
    class Module
}
