package com.swordfish.lemuroid.app.mobile.feature.proinfo.tutorial

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.app.mobile.feature.settings.advanced.AdvancedSettingsViewModel
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.fromHtmlToSpanned
import com.swordfish.lemuroid.app.appextension.getHexColor
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.swordfish.lemuroid.app.appextension.convertSpannedToAnnotatedString

@Composable
fun ProTutorialScreen(
    modifier: Modifier = Modifier,
    viewModel: AdvancedSettingsViewModel,
    navController: NavHostController,
    onBuyProClick: () -> Unit
) {

    val title = fromHtmlToSpanned(
        stringResource(R.string.string_pro_tutorial_title)
            .replace(
                "%tutorialTextColor%",
                LocalContext.current.getHexColor(R.color.main_color)
            )
    )
    val annotatedTitle = remember(title) { convertSpannedToAnnotatedString(title) }

    val buttonText = stringResource(R.string.string_pro_buy_now)
    val limitedDealText = stringResource(R.string.string_pro_limited_deal)

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.pro_tutorial_animation))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .width(150.dp)
                .height(160.dp)
        )

        Text(
            text = annotatedTitle,
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            text = stringResource(R.string.string_pro_tutorial_disclamer),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 12.dp)
        )

//        Text(
//            text = limitedDealText,
//            fontSize = 18.sp,
//            modifier = Modifier.padding(top = 12.dp)
//        )

//        Image(
//            painter = painterResource(R.drawable.ic_limited_deal),
//            contentDescription = null,
//            modifier = Modifier.padding(top = 12.dp)
//        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBuyProClick,
            modifier = Modifier
                .padding(
                    top = 16.dp,
                ),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            Text(
                text = buttonText,
                color = colorResource(id = R.color.textColorPrimary)
            )
        }
    }
}
