package com.gramflow.app.ui.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gramflow.app.ui.components.GramFlowBottomNav
import com.gramflow.app.ui.screens.addsale.AddSaleScreen
import com.gramflow.app.ui.screens.auth.LoginScreen
import com.gramflow.app.ui.screens.auth.SignupScreen
import com.gramflow.app.ui.screens.customers.CustomersScreen
import com.gramflow.app.ui.screens.dashboard.DashboardScreen
import com.gramflow.app.ui.screens.settings.SettingsScreen
import com.gramflow.app.ui.screens.splash.SplashScreen
import com.gramflow.app.ui.screens.stock.StockVaultScreen
import com.gramflow.app.ui.screens.transactions.TransactionsScreen
import com.gramflow.app.viewmodel.AddSaleViewModel
import com.gramflow.app.viewmodel.AuthViewModel
import com.gramflow.app.viewmodel.CustomersViewModel
import com.gramflow.app.viewmodel.DashboardViewModel
import com.gramflow.app.viewmodel.SettingsViewModel
import com.gramflow.app.viewmodel.StockViewModel
import com.gramflow.app.viewmodel.TransactionsViewModel
import kotlinx.coroutines.delay

@Composable
fun GramFlowNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    addSaleViewModel: AddSaleViewModel,
    customersViewModel: CustomersViewModel,
    stockViewModel: StockViewModel,
    transactionsViewModel: TransactionsViewModel,
    settingsViewModel: SettingsViewModel,
    startDestination: String = Screen.Splash.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val sessionUser by authViewModel.sessionUser.collectAsState()

    // 30-Minute Inactivity Auto-Logout Watcher
    LaunchedEffect(sessionUser) {
        if (sessionUser != null) {
            while (true) {
                delay(15_000) // Check every 15 seconds
                if (!authViewModel.isSessionValid()) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    break
                }
            }
        }
    }

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Transactions.route,
        Screen.Customers.route,
        Screen.StockVault.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GramFlowBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        authViewModel.recordActivity()
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        authViewModel.recordActivity()
                    }
                }
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onLoadingComplete = {
                            val nextRoute = if (authViewModel.isSessionValid()) {
                                Screen.Dashboard.route
                            } else {
                                Screen.Login.route
                            }
                            navController.navigate(nextRoute) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToSignup = {
                            navController.navigate(Screen.Signup.route)
                        }
                    )
                }

                composable(Screen.Signup.route) {
                    SignupScreen(
                        viewModel = authViewModel,
                        onSignupSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Signup.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToStock = { navController.navigate(Screen.StockVault.route) },
                        onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.AddSale.route) {
                    AddSaleScreen(
                        viewModel = addSaleViewModel,
                        onSaleCompleted = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Customers.route) {
                    CustomersScreen(
                        viewModel = customersViewModel
                    )
                }

                composable(Screen.StockVault.route) {
                    StockVaultScreen(
                        viewModel = stockViewModel
                    )
                }

                composable(Screen.Transactions.route) {
                    TransactionsScreen(
                        viewModel = transactionsViewModel
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
