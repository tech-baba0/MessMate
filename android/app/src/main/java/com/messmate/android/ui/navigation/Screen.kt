package com.messmate.android.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object MealSelection : Screen("meal_selection")
    object BazarExpense : Screen("bazar_expense")
}
