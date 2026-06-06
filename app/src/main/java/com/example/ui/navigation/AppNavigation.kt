package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.CreateEditScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.CompletedScreen
import com.example.viewmodel.MainViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel,
    onExit: () -> Unit
) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(navController, viewModel, onExit)
        }
        composable("create") {
            CreateEditScreen(navController, viewModel, null)
        }
        composable(
            "edit/{reminderId}",
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getLong("reminderId")
            CreateEditScreen(navController, viewModel, reminderId)
        }
        composable("settings") {
            SettingsScreen(navController, viewModel)
        }
        composable("search") {
            SearchScreen(navController, viewModel)
        }
        composable("completed") {
            CompletedScreen(navController, viewModel)
        }
    }
}
