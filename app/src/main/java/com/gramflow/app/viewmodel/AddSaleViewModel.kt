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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SaleComputation(
    val grams: Double = 0.0,
    val gross: Double = 0.0,
    val discount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val amountReceived: Double = 0.0,
    val balance: Double = 0.0
)

data class LastSaleSummary(
    val customerName: String,
    val gramsSold: Double,
    val grossAmount: Double,
    val discount: Double,
    val finalAmount: Double,
    val amountReceived: Double,
    val balance: Double
)

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

    // Optional Dynamic Rate Overrides
    var overrideRatePerGram = MutableStateFlow("")
    var overrideSpecial025 = MutableStateFlow("")
    var overrideSpecial050 = MutableStateFlow("")

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _lastSaleSummary = MutableStateFlow<LastSaleSummary?>(null)
    val lastSaleSummary: StateFlow<LastSaleSummary?> = _lastSaleSummary.asStateFlow()

    // Real-time Reactive Computation Flow
    @Suppress("UNCHECKED_CAST")
    val computation: StateFlow<SaleComputation> = combine(
        gramsText,
        discountText,
        amountReceivedText,
        settings,
        overrideRatePerGram,
        overrideSpecial025,
        overrideSpecial050
    ) { params ->
        val gText = params[0] as String
        val dText = params[1] as String
        val rText = params[2] as String
        val rateSet = params[3] as RateSettings
        val ovrRate = params[4] as String
        val ovr025 = params[5] as String
        val ovr050 = params[6] as String

        val g = gText.toDoubleOrNull() ?: 0.0
        val d = dText.toDoubleOrNull() ?: 0.0
        val r = rText.toDoubleOrNull() ?: 0.0

        val rPerGram = ovrRate.toDoubleOrNull() ?: rateSet.ratePerGram
        val sp025 = ovr025.toDoubleOrNull() ?: rateSet.special025
        val sp050 = ovr050.toDoubleOrNull() ?: rateSet.special050

        val gross = when {
            g in 0.25..0.30 -> sp025
            g in 0.50..0.60 -> sp050
            else -> g * rPerGram
        }
        val finalAmount = maxOf(0.0, gross - d)
        val balance = maxOf(0.0, finalAmount - r)

        SaleComputation(
            grams = g,
            gross = gross,
            discount = d,
            finalAmount = finalAmount,
            amountReceived = r,
            balance = balance
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SaleComputation())

    fun submitSale(onComplete: (Boolean, String?) -> Unit) {
        val custId = selectedCustomerId.value ?: return onComplete(false, "Please select a customer")
        val currentComp = computation.value

        if (currentComp.grams <= 0.0) return onComplete(false, "Please enter a valid gram weight")
        val currentStock = totalStock.value
        if (currentComp.grams > currentStock) return onComplete(false, "Insufficient stock available in vault")

        val customer = customers.value.find { it.id == custId }
        val custName = customer?.name ?: "Customer #$custId"

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = inventoryRepo.recordSale(
                customerId = custId,
                gramsSold = currentComp.grams,
                grossAmount = currentComp.gross,
                discount = currentComp.discount,
                finalAmount = currentComp.finalAmount,
                amountReceived = currentComp.amountReceived,
                batchIdOverride = selectedBatchId.value,
                comments = commentsText.value.takeIf { it.isNotBlank() }
            )
            _isSubmitting.value = false

            if (result.isSuccess) {
                // Save complete snapshot for the success modal
                _lastSaleSummary.value = LastSaleSummary(
                    customerName = custName,
                    gramsSold = currentComp.grams,
                    grossAmount = currentComp.gross,
                    discount = currentComp.discount,
                    finalAmount = currentComp.finalAmount,
                    amountReceived = currentComp.amountReceived,
                    balance = currentComp.balance
                )

                // Reset form fields
                gramsText.value = ""
                discountText.value = ""
                amountReceivedText.value = ""
                commentsText.value = ""
                selectedBatchId.value = null
                overrideRatePerGram.value = ""
                overrideSpecial025.value = ""
                overrideSpecial050.value = ""

                onComplete(true, null)
            } else {
                onComplete(false, result.exceptionOrNull()?.message ?: "Failed to record sale")
            }
        }
    }
}
