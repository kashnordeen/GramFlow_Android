package com.gramflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_batches",
    indices = [Index(value = ["remainingGrams"]), Index(value = ["createdAt"])]
)
data class StockBatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val grams: Double,
    val remainingGrams: Double,
    val pricePerGram: Double,
    val totalCost: Double = grams * pricePerGram,
    val totalRevenue: Double = 0.0,
    val status: String = if (remainingGrams > 0) "active" else "depleted",
    val createdAt: Long = System.currentTimeMillis()
)
