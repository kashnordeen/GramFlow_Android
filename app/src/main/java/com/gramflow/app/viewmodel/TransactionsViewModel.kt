package com.gramflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.local.entity.SaleEntity
import com.gramflow.app.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TransactionFilter {
    ALL, PAID, LOAN
}

class TransactionsViewModel(
    private val inventoryRepo: InventoryRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow(TransactionFilter.ALL)

    val filteredSales: StateFlow<List<SaleEntity>> = combine(
        inventoryRepo.allSalesFlow,
        searchQuery,
        filterType
    ) { sales, query, filter ->
        sales.filter { sale ->
            val matchesQuery = query.isBlank() ||
                    sale.customerName.contains(query, ignoreCase = true) ||
                    (sale.comments?.contains(query, ignoreCase = true) == true)

            val matchesFilter = when (filter) {
                TransactionFilter.ALL -> true
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
