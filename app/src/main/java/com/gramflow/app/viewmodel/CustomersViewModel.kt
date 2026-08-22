package com.gramflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.local.entity.CustomerEntity
import com.gramflow.app.data.repository.CustomerLedgerStatement
import com.gramflow.app.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CustomerFilter {
    ALL, DEBT, SETTLED
}

class CustomersViewModel(
    private val customerRepo: CustomerRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow(CustomerFilter.ALL)

    val customers: StateFlow<List<CustomerEntity>> = customerRepo.allCustomersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalOutstandingLoan: StateFlow<Double?> = customerRepo.totalLoansFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val filteredCustomers: StateFlow<List<CustomerEntity>> = combine(
        customers,
        searchQuery,
        filterType
    ) { custList, query, filter ->
        custList.filter { c ->
            val matchesQuery = query.isBlank() ||
                    c.name.contains(query, ignoreCase = true) ||
                    (c.phone?.contains(query, ignoreCase = true) == true)

            val combinedDebt = c.oldLoan + c.totalLoan
            val matchesFilter = when (filter) {
                CustomerFilter.ALL -> true
                CustomerFilter.DEBT -> combinedDebt > 0
                CustomerFilter.SETTLED -> combinedDebt == 0.0
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun registerCustomer(name: String, phone: String?, oldLoan: Double, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = customerRepo.createCustomer(name, phone, oldLoan)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun updateCustomer(customerId: Long, name: String, phone: String?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = customerRepo.updateCustomerDetails(customerId, name, phone)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun recordPayment(customerId: Long, amount: Double, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = customerRepo.recordPayment(customerId, amount)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun overrideLoan(customerId: Long, oldLoan: Double, appLoan: Double, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = customerRepo.overrideCustomerLoan(customerId, oldLoan, appLoan)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteCustomer(customerId: Long, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = customerRepo.deleteCustomer(customerId)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun getLedgerStatement(customerId: Long, onResult: (CustomerLedgerStatement?) -> Unit) {
        viewModelScope.launch {
            val res = customerRepo.getCustomerLedger(customerId)
            onResult(res.getOrNull())
        }
    }
}
