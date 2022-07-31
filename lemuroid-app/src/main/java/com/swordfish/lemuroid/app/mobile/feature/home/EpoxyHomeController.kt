package com.swordfish.lemuroid.app.mobile.feature.home

import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.carousel
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.withModelsFrom
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.library.db.entity.Game

class EpoxyHomeController(
    private val gameInteractor: GameInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val coverLoader: CoverLoader
) : AsyncEpoxyController() {

    private var homeViewState = HomeViewModel.HomeViewState()

    fun update(viewState: HomeViewModel.HomeViewState) {
        homeViewState = viewState
        requestModelBuild()
    }

    override fun buildModels() {
        if (homeViewState.favoritesGames.isNotEmpty()) {
            addCarousel("favorites", R.string.favorites, homeViewState.favoritesGames)
        }

        if (homeViewState.recentGames.isNotEmpty()) {
            addCarousel("recent", R.string.recent, homeViewState.recentGames)
        }

        if (homeViewState.discoveryGames.isNotEmpty()) {
            addCarousel("discover", R.string.discover, homeViewState.discoveryGames)
        }

        if (homeViewState.recentGames.isEmpty() && homeViewState.favoritesGames.isEmpty() && homeViewState.discoveryGames.isEmpty()) {
            addEmptyView()
        }
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
                .actionEnabled(!this@EpoxyHomeController.homeViewState.indexInProgress)
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
