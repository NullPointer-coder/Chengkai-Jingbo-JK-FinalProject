package com.example.smartreciperecommenderapp.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val routes = listOf(
        Triple(NavRoutes.HOME, "Home", NavRoutes.icons[NavRoutes.HOME]),
        Triple(NavRoutes.INGREDIENTS, "Ingredients", NavRoutes.icons[NavRoutes.INGREDIENTS]),
        Triple(NavRoutes.PROFILE, "Profile", NavRoutes.icons[NavRoutes.PROFILE])
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        routes.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    icon?.let {
                        Icon(imageVector = it, contentDescription = null)
                    }
                },
                label = { Text(label) }
            )
        }
    }
}
