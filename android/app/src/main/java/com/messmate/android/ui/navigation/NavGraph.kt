package com.messmate.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.messmate.android.ui.screens.auth.LoginScreen
import com.messmate.android.ui.screens.auth.RegisterScreen
import com.messmate.android.ui.screens.dashboard.DashboardScreen
import com.messmate.android.ui.screens.meal.MealSelectionScreen
import com.messmate.android.ui.screens.bazar.BazarExpenseScreen
import com.messmate.android.ui.screens.admin.AdminDashboardScreen
import com.messmate.android.ui.screens.admin.AdminMenuScreen
import com.messmate.android.ui.screens.menu.MenuScreen

@Composable
fun MessMateNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToMeal = { navController.navigate(Screen.MealSelection.route) },
                onNavigateToBazar = { navController.navigate(Screen.BazarExpense.route) },
                onNavigateToAdmin = { 
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToMenu = { navController.navigate(Screen.Menu.route) },
                onLogout = {
                    com.messmate.android.network.ApiClient.tokenManager.clearToken()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.MealSelection.route) {
            MealSelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.BazarExpense.route) {
            BazarExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdminMenu = { navController.navigate(Screen.AdminMenu.route) },
                onLogout = {
                    com.messmate.android.network.ApiClient.tokenManager.clearToken()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AdminMenu.route) {
            AdminMenuScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Menu.route) {
            MenuScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
