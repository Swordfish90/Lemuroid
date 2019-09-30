package com.codebutler.retrograde.app.shared

import android.view.ContextMenu
import android.view.View
import com.codebutler.retrograde.R

class GameContextMenuListener(
        private val isFavorite: Boolean?,
        private val onPlaySelected: (() -> Unit)?,
        private val onFavoriteChanged: ((Boolean) -> Unit)?
): View.OnCreateContextMenuListener {

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu.add(R.string.play).setOnMenuItemClickListener {
            onPlaySelected?.invoke()
            true
        }
        when (isFavorite) {
            true -> {
                menu.add(R.string.remove_from_favorites).setOnMenuItemClickListener {
                    onFavoriteChanged?.invoke(false)
                    true
                }
            }
            false -> {
                menu.add(R.string.add_to_favorites).setOnMenuItemClickListener {
                    onFavoriteChanged?.invoke(true)
                    true
                }
            }
            else -> Unit // Do nothing if is favorite is null
        }
    }
}
