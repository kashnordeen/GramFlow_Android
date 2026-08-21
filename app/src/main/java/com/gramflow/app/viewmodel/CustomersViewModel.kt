package com.gramflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.local.entity.CustomerEntity
import com.gramflow.app.data.repository.CustomerLedgerStatement
import com.gramflow.app.data.repository.CustomerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomersViewModel(
    private val customerRepo: CustomerRepository
) : ViewModel() {

    val customers: StateFlow<List<CustomerEntity>> = customerRepo.allCustomersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalOutstandingLoan: StateFlow<Double?> = customerRepo.totalLoansFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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
