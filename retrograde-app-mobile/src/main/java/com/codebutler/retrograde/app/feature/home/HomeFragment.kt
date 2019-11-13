package com.codebutler.retrograde.app.feature.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.shared.DynamicGridLayoutManager
import com.codebutler.retrograde.app.shared.GameInteractor
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onResume() {
        super.onResume()

        val homeViewModel =
                ViewModelProviders.of(this, HomeViewModel.Factory(retrogradeDb)).get(HomeViewModel::class.java)

        // Disable snapping in carousel view
        Carousel.setDefaultGlobalSnapHelperFactory(null)

        val pagingController = EpoxyHomeController(gameInteractor)

        val recyclerView = view!!.findViewById<RecyclerView>(R.id.home_recyclerview)
        val layoutManager = DynamicGridLayoutManager(context!!, 2)

        // Sets the first three rows to full width. Two headers and carousel view.
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position < 3) layoutManager.spanCount else 1
            }
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = pagingController.adapter

        homeViewModel.recentGames.observe(this, Observer {
            pagingController.updateRecents(it)
        })

        homeViewModel.favoriteGames.observe(this, Observer {
            pagingController.submitList(it)
        })
    }

    @dagger.Module
    class Module
}
