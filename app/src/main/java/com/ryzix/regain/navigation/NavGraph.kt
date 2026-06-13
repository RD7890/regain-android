package com.ryzix.regain.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ryzix.regain.ui.screens.DialerScreen
import com.ryzix.regain.ui.screens.HistoryScreen
import com.ryzix.regain.ui.screens.HomeScreen
import com.ryzix.regain.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Dialer : Screen("dialer")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}

@Composable
fun RegainNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onOpenDialer = { navController.navigate(Screen.Dialer.route) },
                onNavigateHistory = { navController.navigate(Screen.History.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Dialer.route) {
            DialerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                onNavigateHistory = { navController.navigate(Screen.History.route) { launchSingleTop = true } }
            )
        }
    }
}
