package com.gramflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.local.entity.SaleBatchAssignmentEntity
import com.gramflow.app.data.local.entity.SaleEntity
import com.gramflow.app.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TransactionFilter {
    ALL, TODAY, PAID, LOAN
}

data class TransactionSummaryMetrics(
    val totalSalesCount: Int = 0,
    val totalGramsSold: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalLoansIssued: Double = 0.0
)

class TransactionsViewModel(
    private val inventoryRepo: InventoryRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow(TransactionFilter.ALL)

    val allSales: StateFlow<List<SaleEntity>> = inventoryRepo.allSalesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summaryMetrics: StateFlow<TransactionSummaryMetrics> = allSales.map { sales ->
        TransactionSummaryMetrics(
            totalSalesCount = sales.size,
            totalGramsSold = sales.sumOf { it.gramsSold },
            totalRevenue = sales.sumOf { it.amountReceived },
            totalLoansIssued = sales.sumOf { it.balance }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionSummaryMetrics())

    val filteredSales: StateFlow<List<SaleEntity>> = combine(
        allSales,
        searchQuery,
        filterType
    ) { sales, query, filter ->
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        sales.filter { sale ->
            val matchesQuery = query.isBlank() ||
                    sale.customerName.contains(query, ignoreCase = true) ||
                    (sale.comments?.contains(query, ignoreCase = true) == true) ||
                    sale.gramsSold.toString().contains(query) ||
                    sale.finalAmount.toString().contains(query)

            val matchesFilter = when (filter) {
                TransactionFilter.ALL -> true
                TransactionFilter.TODAY -> sale.createdAt >= startOfToday
                TransactionFilter.PAID -> sale.balance == 0.0
                TransactionFilter.LOAN -> sale.balance > 0.0
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun rollbackSale(saleId: Long, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = inventoryRepo.rollbackSale(saleId)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteSale(saleId: Long, onResult: (Boolean, String?) -> Unit) = rollbackSale(saleId, onResult)
}
