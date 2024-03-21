package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

@Composable
fun MainNavigationBar(
    currentRoute: MainRoute?,
    navController: NavHostController
) {
    AnimatedVisibility(
        visible = currentRoute?.showBottomNavigation != false,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        NavigationBar {
            MainNavigationRoutes.values().forEach { destination ->
                val isSelected = currentRoute?.root == destination.route
                val iconDrawable = if (isSelected) destination.selectedIcon else destination.unselectedIcon

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = iconDrawable,
                            contentDescription = stringResource(destination.titleId)
                        )
                    },
                    label = { Text(stringResource(destination.titleId)) },
                    selected = isSelected,
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
}
