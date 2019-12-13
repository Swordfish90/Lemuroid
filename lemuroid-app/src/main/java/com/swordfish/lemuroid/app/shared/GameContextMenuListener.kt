package com.swordfish.lemuroid.app.shared

import android.view.ContextMenu
import android.view.View
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.library.db.entity.Game

class GameContextMenuListener(
    private val gameInteractor: GameInteractor,
    private val game: Game
) : View.OnCreateContextMenuListener {

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu.add(R.string.play).setOnMenuItemClickListener {
            gameInteractor.onGamePlay(game)
            true
        }

        menu.add(R.string.restart).setOnMenuItemClickListener {
            gameInteractor.onGameRestart(game)
            true
        }

        if (game.isFavorite) {
            menu.add(R.string.remove_from_favorites).setOnMenuItemClickListener {
                gameInteractor.onFavoriteToggle(game, false)
                true
            }
        } else {
            menu.add(R.string.add_to_favorites).setOnMenuItemClickListener {
                gameInteractor.onFavoriteToggle(game, true)
                true
            }
        }
    }
}
