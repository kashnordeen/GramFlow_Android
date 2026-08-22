package com.gramflow.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.local.entity.UserEntity
import com.gramflow.app.data.repository.AuthRepository
import com.gramflow.app.data.repository.DashboardMetrics
import com.gramflow.app.data.repository.InventoryRepository
import com.gramflow.app.util.DatabaseBackupManager
import com.gramflow.app.util.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val inventoryRepo: InventoryRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    val metrics: StateFlow<DashboardMetrics> = inventoryRepo.getDashboardMetricsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardMetrics(
                totalStock = 0.0,
                salesTodayAmount = 0.0,
                salesTodayCount = 0,
                salesTodayGrams = 0.0,
                totalLoan = 0.0,
                totalProfit = 0.0,
                customersWithLoans = emptyList()
            )
        )

    val activeUser: StateFlow<UserEntity?> = authRepo.activeUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    var isPrivacyVisible = MutableStateFlow(true)
        private set

    fun togglePrivacy() {
        isPrivacyVisible.value = !isPrivacyVisible.value
    }

    fun updateProfile(newName: String, currentPass: String, newPass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val user = activeUser.value ?: return@launch
            val res = authRepo.updateProfile(user.email, currentPass, newPass.takeIf { it.isNotBlank() }, newName)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun exportMasterPdf(context: Context) {
        viewModelScope.launch {
            val sales = inventoryRepo.getAllSales()
            val customers = inventoryRepo.getAllCustomers()
            val totalStock = inventoryRepo.getTotalStock()
            val totalDebt = customers.sumOf { it.oldLoan + it.totalLoan }
            val totalRevenue = sales.sumOf { it.amountReceived }
            PdfGenerator.generateMasterLedgerPdf(context, sales, customers, totalStock, totalDebt, totalRevenue)
        }
    }

    fun backupDatabase(context: Context) {
        DatabaseBackupManager.createEncryptedBackup(context, inventoryRepo.database)
    }
}
