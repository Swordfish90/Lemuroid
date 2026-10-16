package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

fun NavigationSuiteScope.mainNavigationItems(
    currentRoute: MainRoute,
    navController: NavHostController,
    showLabels: Boolean,
) {
    MainNavigationRoutes.entries.forEach { destination ->
        val isSelected = currentRoute.root == destination.route
        val iconDrawable = if (isSelected) destination.selectedIcon else destination.unselectedIcon

        item(
            icon = {
                Icon(
                    imageVector = iconDrawable,
                    contentDescription = stringResource(destination.titleId),
                )
            },
            label = if (showLabels) { { Text(stringResource(destination.titleId)) } } else null,
            selected = isSelected,
            onClick = {
                navController.navigate(destination.route.route) {
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }
            },
        )
    }
}
