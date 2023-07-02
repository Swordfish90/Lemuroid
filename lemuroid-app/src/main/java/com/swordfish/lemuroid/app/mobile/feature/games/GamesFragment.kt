package com.swordfish.lemuroid.app.mobile.feature.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.paging.compose.collectAsLazyPagingItems
import com.swordfish.lemuroid.app.mobile.shared.RecyclerViewFragment
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import javax.inject.Inject

class GamesFragment : RecyclerViewFragment() {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    @Inject
    lateinit var coverLoader: CoverLoader

    private val args: GamesFragmentArgs by navArgs()

    private val gamesViewModel: GamesViewModel by viewModels {
        GamesViewModel.Factory(retrogradeDb)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        gamesViewModel.systemIds.value = (listOf(*args.systemIds))
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val games = gamesViewModel.games.collectAsLazyPagingItems()
                GamesScreen(
                    games = games,
                    onGameClicked = { gameInteractor.onGamePlay(it) },
                    onFavoriteToggle = { game, isFavorite ->
                        gameInteractor.onFavoriteToggle(game, isFavorite)
                    }
                )
            }
        }
    }

    @dagger.Module
    class Module
}
