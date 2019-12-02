package com.swordfish.lemuroid.app.feature.home

import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.carousel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.withModelsFrom
import com.swordfish.lemuroid.lib.library.db.entity.Game

class EpoxyHomeController(private val gameInteractor: GameInteractor) : PagedListEpoxyController<Game>() {
    private var recentGames = listOf<Game>()

    fun updateRecents(games: List<Game>) {
        recentGames = games
        requestDelayedModelBuild(UPDATE_DELAY_TIME)
    }

    override fun buildItemModel(currentPosition: Int, item: Game?): EpoxyModel<*> {
        return if (item == null) {
            EpoxyGameView_()
                    .id(-currentPosition)
        } else {
            EpoxyGameView_()
                    .id(item.id)
                    .title(item.title)
                    .coverUrl(item.coverFrontUrl)
                    .favorite(item.isFavorite)
                    .onFavoriteChanged { gameInteractor.onFavoriteToggle(item, it) }
                    .onClick { gameInteractor.onGameClick(item) }
        }
    }

    override fun addModels(models: List<EpoxyModel<*>>) {
        epoxyHomeSection {
            id("section_recents")
            title(R.string.recent)
        }
        carousel {
            id("carousel")
            withModelsFrom(recentGames) { item ->
                EpoxyGameRecentView_()
                        .id(item.id)
                        .title(item.title)
                        .coverUrl(item.coverFrontUrl)
                        .favorite(item.isFavorite)
                        .onFavoriteChanged { gameInteractor.onFavoriteToggle(item, it) }
                        .onClick { gameInteractor.onGameClick(item) }
            }
        }
        epoxyHomeSection {
            id("section_favorites")
            title(R.string.favorites)
        }
        super.addModels(models)
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

    companion object {
        const val UPDATE_DELAY_TIME = 1000
    }
}
