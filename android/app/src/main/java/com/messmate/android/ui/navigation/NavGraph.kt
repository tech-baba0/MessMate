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
import com.messmate.android.ui.screens.bazar.AddBazarExpenseScreen
import com.messmate.android.ui.screens.admin.AdminDashboardScreen
import com.messmate.android.ui.screens.admin.AdminMenuScreen
import com.messmate.android.ui.screens.admin.AdminExpenseScreen
import com.messmate.android.ui.screens.admin.AdminBillScreen
import com.messmate.android.ui.screens.menu.MenuScreen
import com.messmate.android.ui.screens.meal.MealHistoryScreen

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
                    navController.navigate(Screen.AdminDashboard.route)
                },
                onNavigateToMenu = { navController.navigate(Screen.Menu.route) },
                onNavigateToMealHistory = { navController.navigate(Screen.MealHistory.route) },
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(Screen.MealHistory.route) }
            )
        }
        composable(Screen.MealHistory.route) {
            MealHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.BazarExpense.route) {
            BazarExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpense = { navController.navigate("add_bazar_expense") }
            )
        }
        composable("add_bazar_expense") {
            AddBazarExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdminMenu = { navController.navigate(Screen.AdminMenu.route) },
                onNavigateToAdminExpense = { navController.navigate(Screen.AdminExpense.route) },
                onNavigateToAdminBill = { navController.navigate(Screen.AdminBill.route) },
                onLogout = {
                    com.messmate.android.network.ApiClient.tokenManager.clearToken()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AdminExpense.route) {
            AdminExpenseScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.AdminBill.route) {
            AdminBillScreen(onNavigateBack = { navController.popBackStack() })
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
