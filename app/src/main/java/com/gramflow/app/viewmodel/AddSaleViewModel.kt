package com.gramflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.local.entity.CustomerEntity
import com.gramflow.app.data.local.entity.StockBatchEntity
import com.gramflow.app.data.repository.CustomerRepository
import com.gramflow.app.data.repository.InventoryRepository
import com.gramflow.app.data.repository.RateSettings
import com.gramflow.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddSaleViewModel(
    private val inventoryRepo: InventoryRepository,
    private val customerRepo: CustomerRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    val customers: StateFlow<List<CustomerEntity>> = customerRepo.allCustomersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<StockBatchEntity>> = inventoryRepo.allBatchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStock: StateFlow<Double> = inventoryRepo.totalStockFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val settings: StateFlow<RateSettings> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RateSettings())

    var selectedCustomerId = MutableStateFlow<Long?>(null)
    var gramsText = MutableStateFlow("")
    var discountText = MutableStateFlow("")
    var amountReceivedText = MutableStateFlow("")
    var selectedBatchId = MutableStateFlow<Long?>(null)
    var commentsText = MutableStateFlow("")

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _saleSuccessEvent = MutableStateFlow(false)
    val saleSuccessEvent: StateFlow<Boolean> = _saleSuccessEvent.asStateFlow()

    fun calculateComputation(): Triple<Double, Double, Double> {
        val g = gramsText.value.toDoubleOrNull() ?: 0.0
        val d = discountText.value.toDoubleOrNull() ?: 0.0
        val r = amountReceivedText.value.toDoubleOrNull() ?: 0.0
        val currentSettings = settings.value

        val gross = when {
            g in 0.25..0.30 -> currentSettings.special025
            g in 0.50..0.60 -> currentSettings.special050
            else -> g * currentSettings.ratePerGram
        }
        val finalAmount = maxOf(0.0, gross - d)
        val balance = maxOf(0.0, finalAmount - r)
        return Triple(gross, finalAmount, balance)
    }

    fun submitSale(onComplete: (Boolean, String?) -> Unit) {
        val custId = selectedCustomerId.value ?: return onComplete(false, "Please select a customer")
        val g = gramsText.value.toDoubleOrNull() ?: return onComplete(false, "Invalid gram weight")
        val d = discountText.value.toDoubleOrNull() ?: 0.0
        val r = amountReceivedText.value.toDoubleOrNull() ?: return onComplete(false, "Enter amount received")

        val (gross, finalAmount, _) = calculateComputation()

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = inventoryRepo.recordSale(
                customerId = custId,
                gramsSold = g,
                grossAmount = gross,
                discount = d,
                finalAmount = finalAmount,
                amountReceived = r,
                batchIdOverride = selectedBatchId.value,
                comments = commentsText.value.takeIf { it.isNotBlank() }
            )
            _isSubmitting.value = false

            if (result.isSuccess) {
                _saleSuccessEvent.value = true
                gramsText.value = ""
                discountText.value = ""
                amountReceivedText.value = ""
                commentsText.value = ""
                selectedBatchId.value = null
                onComplete(true, null)
            } else {
                onComplete(false, result.exceptionOrNull()?.message)
            }
        }
    }

    fun resetSuccessEvent() {
        _saleSuccessEvent.value = false
    }
}
