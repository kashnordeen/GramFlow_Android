package com.gramflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gramflow.app.data.local.entity.SaleBatchAssignmentEntity
import com.gramflow.app.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSalesFlow(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    suspend fun getAllSales(): List<SaleEntity>

    @Query("SELECT * FROM sales ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSalesFlow(limit: Int = 10): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY createdAt DESC")
    suspend fun getSalesForCustomer(customerId: Long): List<SaleEntity>

    @Query("SELECT * FROM sale_batch_assignments WHERE saleId = :saleId")
    suspend fun getAssignmentsForSale(saleId: Long): List<SaleBatchAssignmentEntity>

    @Query("SELECT * FROM sale_batch_assignments WHERE batchId = :batchId")
    suspend fun getAssignmentsForBatch(batchId: Long): List<SaleBatchAssignmentEntity>

    @Query("SELECT SUM(finalAmount) FROM sales WHERE createdAt >= :startOfDayTimestamp")
    fun getSalesTodayAmountFlow(startOfDayTimestamp: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :startOfDayTimestamp")
    fun getSalesTodayCountFlow(startOfDayTimestamp: Long): Flow<Int>

    @Query("SELECT SUM(gramsSold) FROM sales WHERE createdAt >= :startOfDayTimestamp")
    fun getSalesTodayGramsFlow(startOfDayTimestamp: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<SaleBatchAssignmentEntity>)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Long)

    @Query("DELETE FROM sale_batch_assignments WHERE saleId = :saleId")
    suspend fun deleteAssignmentsBySaleId(saleId: Long)
}
