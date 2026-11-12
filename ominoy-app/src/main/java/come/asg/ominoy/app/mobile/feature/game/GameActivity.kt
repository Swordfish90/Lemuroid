package come.asg.ominoy.app.mobile.feature.game

import androidx.compose.runtime.Composable
import come.asg.ominoy.app.mobile.feature.gamemenu.GameMenuActivity
import come.asg.ominoy.app.shared.game.BaseGameActivity
import come.asg.ominoy.app.shared.game.BaseGameScreenViewModel

class GameActivity : BaseGameActivity() {
    @Composable
    override fun GameScreen(viewModel: BaseGameScreenViewModel) {
        MobileGameScreen(viewModel)
    }

    override fun getDialogClass() = GameMenuActivity::class.java
}
