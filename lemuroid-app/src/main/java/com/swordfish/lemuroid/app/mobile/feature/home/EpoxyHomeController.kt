package com.swordfish.lemuroid.app.mobile.feature.home

import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.carousel
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.withModelsFrom
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.kotlin.lazySequenceOf
import com.swordfish.lemuroid.lib.library.db.entity.Game

class EpoxyHomeController(
    private val gameInteractor: GameInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val coverLoader: CoverLoader
) : AsyncEpoxyController() {

    private var uiState = HomeViewModel.UIState()

    fun update(viewState: HomeViewModel.UIState) {
        uiState = viewState
        requestModelBuild()
    }

    override fun buildModels() {
        if (displayFavorites()) {
            addCarousel("favorites", R.string.favorites, uiState.favoritesGames)
        }

        if (displayRecents()) {
            addCarousel("recent", R.string.recent, uiState.recentGames)
        }

        if (displayDiscovery()) {
            addCarousel("discover", R.string.discover, uiState.discoveryGames)
        }

        if (displayEmptyView()) {
            addEmptyView()
        }
    }

    private fun displayDiscovery() = uiState.discoveryGames.isNotEmpty()

    private fun displayRecents() = uiState.recentGames.isNotEmpty()

    private fun displayFavorites() = uiState.favoritesGames.isNotEmpty()

    private fun displayEmptyView(): Boolean {
        val conditions = lazySequenceOf(
            { uiState.loading.not() },
            { uiState.recentGames.isEmpty() },
            { uiState.favoritesGames.isEmpty() },
            { uiState.discoveryGames.isEmpty() },
        )
        return conditions.all { it }
    }

    private fun addCarousel(id: String, titleId: Int, games: List<Game>) {
        epoxyHomeSection {
            id("section_$id")
            title(titleId)
        }
        carousel {
            id("carousel_$id")
            paddingRes(R.dimen.grid_spacing)
            withModelsFrom(games) { item ->
                EpoxyGameView_()
                    .id(item.id)
                    .game(item)
                    .gameInteractor(this@EpoxyHomeController.gameInteractor)
                    .coverLoader(this@EpoxyHomeController.coverLoader)
            }
        }
    }

    private fun addEmptyView() {
        epoxyEmptyViewAction {
            id("empty_home")
                .title(R.string.home_empty_title)
                .message(R.string.home_empty_message)
                .action(R.string.home_empty_action)
                .actionEnabled(!this@EpoxyHomeController.uiState.indexInProgress)
                .onClick { this@EpoxyHomeController.settingsInteractor.changeLocalStorageFolder() }
        }
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }
}
