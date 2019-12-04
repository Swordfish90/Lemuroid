package com.swordfish.lemuroid.app.feature.favorites

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.DynamicGridLayoutManager
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.GamesAdapter
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class FavoritesFragment : Fragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private lateinit var favoritesViewModel: FavoritesViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_favorites, container, false)

        favoritesViewModel = ViewModelProviders.of(this, FavoritesViewModel.Factory(retrogradeDb))
            .get(FavoritesViewModel::class.java)

        return root
    }

    override fun onResume() {
        super.onResume()

        val gamesAdapter = GamesAdapter(R.layout.layout_game, gameInteractor)
        favoritesViewModel.favorites.observe(this, Observer {
            gamesAdapter.submitList(it)
        })

        view?.findViewById<RecyclerView>(R.id.favorites_recyclerview)?.apply {
            this.adapter = gamesAdapter
            this.layoutManager = DynamicGridLayoutManager(context)
        }
    }

    @dagger.Module
    class Module
}
