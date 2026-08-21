package com.gramflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramflow.app.data.local.entity.StockBatchEntity
import com.gramflow.app.data.repository.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockViewModel(
    private val inventoryRepo: InventoryRepository
) : ViewModel() {

    val batches: StateFlow<List<StockBatchEntity>> = inventoryRepo.allBatchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStock: StateFlow<Double> = inventoryRepo.totalStockFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun ingestBatch(grams: Double, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = inventoryRepo.ingestBatch(grams)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun closeBatch(batchId: Long, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = inventoryRepo.closeBatch(batchId)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun updateBatch(batchId: Long, grams: Double, price: Double, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = inventoryRepo.updateBatch(batchId, grams, price)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteBatch(batchId: Long, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = inventoryRepo.deleteBatch(batchId)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }
}
