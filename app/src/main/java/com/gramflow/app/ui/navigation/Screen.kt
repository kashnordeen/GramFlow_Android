package com.gramflow.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Dashboard : Screen("dashboard")
    object AddSale : Screen("add_sale")
    object Customers : Screen("customers")
    object StockVault : Screen("stock_vault")
    object Transactions : Screen("transactions")
    object Settings : Screen("settings")
}
