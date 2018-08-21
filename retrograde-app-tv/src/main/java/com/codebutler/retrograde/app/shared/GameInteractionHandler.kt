package com.codebutler.retrograde.app.shared

import android.app.Activity
import android.support.v17.leanback.widget.Presenter
import android.support.v7.widget.PopupMenu
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MenuItem
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.feature.game.GameLauncherActivity
import com.codebutler.retrograde.app.shared.ui.ItemViewLongClickListener
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.entity.Game
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg

class GameInteractionHandler(private val activity: Activity, private val retrogradeDb: RetrogradeDatabase) :
        ItemViewLongClickListener,
        PopupMenu.OnMenuItemClickListener,
        PopupMenu.OnDismissListener {

    var onRefreshListener: (() -> Unit)? = null

    private var gameForPopupMenu: Game? = null

    fun onItemClick(item: Game) {
        activity.startActivity(GameLauncherActivity.newIntent(activity, item))
    }

    override fun onItemLongClicked(itemViewHolder: Presenter.ViewHolder, item: Any): Boolean {
        return when (item) {
            is Game -> {
                gameForPopupMenu = item
                val popup = PopupMenu(
                        ContextThemeWrapper(activity, R.style.Theme_AppCompat_Light_DarkActionBar),
                        itemViewHolder.view,
                        Gravity.START or Gravity.BOTTOM)
                popup.setOnMenuItemClickListener(this)
                popup.setOnDismissListener(this)
                popup.inflate(R.menu.popup_game)
                val favoriteItem = popup.menu.findItem(R.id.toggle_favorite)
                if (item.isFavorite) {
                    favoriteItem.title = activity.getString(R.string.remove_from_favorites)
                } else {
                    favoriteItem.title = activity.getString(R.string.add_to_favorites)
                }
                popup.show()
                true
            }
            else -> false
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val game = gameForPopupMenu ?: return false
        return when (item.itemId) {
            R.id.play -> {
                onItemClick(game)
                true
            }
            R.id.toggle_favorite -> {
                async(UI) {
                    bg {
                        retrogradeDb.gameDao().update(game.copy(isFavorite = !game.isFavorite))
                    }.await()
                    onRefreshListener?.invoke()
                }
                true
            }
            else -> false
        }
    }

    override fun onDismiss(menu: PopupMenu) {
        gameForPopupMenu = null
    }
}
