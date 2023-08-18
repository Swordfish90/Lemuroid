package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

@Composable
fun MainNavigationBar(
    currentRoute: MainRoute?,
    navController: NavHostController
) {
    // TODO COMPOSE Animate show and hide status
    if (currentRoute?.showBottomNavigation == false) {
        return
    }

    NavigationBar {
        MainNavigationRoutes.values().forEach { destination ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(destination.iconId),
                        contentDescription = stringResource(destination.titleId)
                    )
                },
                label = { Text(stringResource(destination.titleId)) },
                selected = (currentRoute?.parent
                    ?: currentRoute) == destination.route.startDestination,
                onClick = {
                    navController.navigate(destination.route.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = false
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = false
                    }
                }
            )
        }
    }
}
