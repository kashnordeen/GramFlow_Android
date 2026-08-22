package com.gramflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gramflow.app.ui.navigation.GramFlowNavGraph
import com.gramflow.app.ui.navigation.Screen
import com.gramflow.app.ui.theme.BgCanvas
import com.gramflow.app.ui.theme.GramFlowTheme
import com.gramflow.app.viewmodel.AddSaleViewModel
import com.gramflow.app.viewmodel.AuthViewModel
import com.gramflow.app.viewmodel.CustomersViewModel
import com.gramflow.app.viewmodel.DashboardViewModel
import com.gramflow.app.viewmodel.SettingsViewModel
import com.gramflow.app.viewmodel.StockViewModel
import com.gramflow.app.viewmodel.TransactionsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as GramFlowApp
        val authRepo = app.authRepository
        val invRepo = app.inventoryRepository
        val custRepo = app.customerRepository
        val setRepo = app.settingsRepository

        setContent {
            GramFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgCanvas
                ) {
                    val authVm: AuthViewModel = viewModel { AuthViewModel(authRepo) }
                    val dashboardVm: DashboardViewModel = viewModel { DashboardViewModel(invRepo, authRepo) }
                    val addSaleVm: AddSaleViewModel = viewModel { AddSaleViewModel(invRepo, custRepo, setRepo) }
                    val customersVm: CustomersViewModel = viewModel { CustomersViewModel(custRepo) }
                    val stockVm: StockViewModel = viewModel { StockViewModel(invRepo) }
                    val transVm: TransactionsViewModel = viewModel { TransactionsViewModel(invRepo) }
                    val settingsVm: SettingsViewModel = viewModel { SettingsViewModel(setRepo, invRepo) }

                    GramFlowNavGraph(
                        authViewModel = authVm,
                        dashboardViewModel = dashboardVm,
                        addSaleViewModel = addSaleVm,
                        customersViewModel = customersVm,
                        stockViewModel = stockVm,
                        transactionsViewModel = transVm,
                        settingsViewModel = settingsVm,
                        startDestination = Screen.Splash.route
                    )
                }
            }
        }
    }
}
