package com.gramflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.local.entity.UserEntity
import com.gramflow.app.data.repository.AuthRepository
import com.gramflow.app.data.repository.DashboardMetrics
import com.gramflow.app.data.repository.InventoryRepository
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
}
