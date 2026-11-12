package come.asg.ominoy.app.shared.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import come.asg.ominoy.app.mobile.shared.compose.ui.OminoyGameImage
import come.asg.ominoy.app.shared.game.viewmodel.GameViewModelRetroGameView
import come.asg.ominoy.lib.library.db.entity.Game

@Composable
fun BaseGameScreen(
    viewModel: BaseGameScreenViewModel,
    game: Game,
    gameScreen: @Composable (BaseGameScreenViewModel) -> Unit,
) {
    val gameState =
        viewModel.getGameState()
            .collectAsState(GameViewModelRetroGameView.GameState.Uninitialized)
            .value

    val isGameReady =
        gameState is GameViewModelRetroGameView.GameState.Loaded ||
            gameState is GameViewModelRetroGameView.GameState.Ready

    if (isGameReady) {
        gameScreen(viewModel)
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OminoyGameImage(
                    game = game,
                    modifier = Modifier.size(224.dp).clip(RoundedCornerShape(18.dp)),
                )
                Text(
                    text = game.displayName(),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )

                val message = if (gameState is GameViewModelRetroGameView.GameState.Loading) gameState.message else null
                AnimatedVisibility(message != null) {
                    Text(text = message!!, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
