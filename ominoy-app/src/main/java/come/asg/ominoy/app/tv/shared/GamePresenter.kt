package come.asg.ominoy.app.tv.shared

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import come.asg.ominoy.app.shared.GameContextMenuListener
import come.asg.ominoy.app.shared.GameInteractor
import come.asg.ominoy.app.shared.covers.CoverUtils
import come.asg.ominoy.app.utils.games.GameUtils
import come.asg.ominoy.lib.library.db.entity.Game

class GamePresenter(
    private val cardSize: Int,
    private val gameInteractor: GameInteractor,
) : Presenter() {
    override fun onBindViewHolder(
        viewHolder: Presenter.ViewHolder?,
        item: Any?,
    ) {
        if (item == null || viewHolder !is ViewHolder) return
        val game = item as Game
        viewHolder.mCardView.titleText = game.displayName()
        viewHolder.mCardView.contentText = GameUtils.getGameSubtitle(viewHolder.mCardView.context, game)
        viewHolder.mCardView.setMainImageDimensions(cardSize, cardSize)
        viewHolder.updateCardViewImage(game)
        viewHolder.view.setOnCreateContextMenuListener(GameContextMenuListener(gameInteractor, game))
    }

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        (cardView.findViewById<View>(androidx.leanback.R.id.content_text) as TextView).setTextColor(Color.LTGRAY)
        return ViewHolder(cardView)
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {
        val viewHolder = viewHolder as ViewHolder
        viewHolder.mCardView.mainImage = null
        viewHolder.view.setOnCreateContextMenuListener(null)
    }

    class ViewHolder(view: ImageCardView) : Presenter.ViewHolder(view) {
        val mCardView: ImageCardView = view

        fun updateCardViewImage(game: Game) {
            CoverUtils.loadCover(game, mCardView.mainImageView)
        }
    }
}
