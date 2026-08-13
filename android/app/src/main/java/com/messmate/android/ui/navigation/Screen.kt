package com.messmate.android.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object MealSelection : Screen("meal_selection")
    object BazarExpense : Screen("bazar_expense")
    object AdminDashboard : Screen("admin_dashboard")
    object Menu : Screen("menu")
    object AdminMenu : Screen("admin_menu")
    object AdminExpense : Screen("admin_expense")
    object AdminBill : Screen("admin_bill")
    object MealHistory : Screen("meal_history")
}
