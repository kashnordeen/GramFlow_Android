package com.gramflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["createdAt"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val gramsSold: Double,
    val grossAmount: Double,
    val discount: Double = 0.0,
    val finalAmount: Double,
    val amountReceived: Double,
    val balance: Double = finalAmount - amountReceived,
    val comments: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
