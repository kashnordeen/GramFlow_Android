package com.gramflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gramflow.app.data.local.entity.StockBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockBatchDao {
    @Query("SELECT * FROM stock_batches ORDER BY createdAt DESC")
    fun getAllBatchesFlow(): Flow<List<StockBatchEntity>>

    @Query("SELECT * FROM stock_batches ORDER BY createdAt ASC")
    suspend fun getAllBatchesAscending(): List<StockBatchEntity>

    @Query("SELECT * FROM stock_batches WHERE remainingGrams > 0 ORDER BY createdAt ASC")
    suspend fun getActiveBatchesAscending(): List<StockBatchEntity>

    @Query("SELECT * FROM stock_batches WHERE id = :id LIMIT 1")
    suspend fun getBatchById(id: Long): StockBatchEntity?

    @Query("SELECT SUM(remainingGrams) FROM stock_batches")
    fun getTotalRemainingStockFlow(): Flow<Double?>

    @Query("SELECT SUM(remainingGrams) FROM stock_batches")
    suspend fun getTotalRemainingStock(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: StockBatchEntity): Long

    @Update
    suspend fun updateBatch(batch: StockBatchEntity)

    @Update
    suspend fun updateBatches(batches: List<StockBatchEntity>)

    @Delete
    suspend fun deleteBatch(batch: StockBatchEntity)

    @Query("DELETE FROM stock_batches WHERE id = :id")
    suspend fun deleteBatchById(id: Long)
}
