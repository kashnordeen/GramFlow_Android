package com.gramflow.app.data.repository

import androidx.room.withTransaction
import com.gramflow.app.data.local.AppDatabase
import com.gramflow.app.data.local.entity.CustomerEntity
import com.gramflow.app.data.local.entity.SaleBatchAssignmentEntity
import com.gramflow.app.data.local.entity.SaleEntity
import com.gramflow.app.data.local.entity.StockBatchEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

data class DashboardMetrics(
    val totalStock: Double,
    val salesTodayAmount: Double,
    val salesTodayCount: Int,
    val salesTodayGrams: Double,
    val totalLoan: Double,
    val totalProfit: Double,
    val customersWithLoans: List<CustomerEntity>
)

class InventoryRepository(private val db: AppDatabase) {
    private val customerDao = db.customerDao()
    private val stockBatchDao = db.stockBatchDao()
    private val saleDao = db.saleDao()
    private val settingsDao = db.settingsDao()

    val totalStockFlow: Flow<Double> = stockBatchDao.getTotalRemainingStockFlow().map { it ?: 0.0 }
    val allBatchesFlow: Flow<List<StockBatchEntity>> = stockBatchDao.getAllBatchesFlow()
    val allSalesFlow: Flow<List<SaleEntity>> = saleDao.getAllSalesFlow()

    fun getDashboardMetricsFlow(): Flow<DashboardMetrics> {
        val startOfDay = getStartOfDayTimestamp()

        val salesTodayFlow = combine(
            saleDao.getSalesTodayAmountFlow(startOfDay),
            saleDao.getSalesTodayCountFlow(startOfDay),
            saleDao.getSalesTodayGramsFlow(startOfDay)
        ) { amount, count, grams ->
            Triple(amount ?: 0.0, count, grams ?: 0.0)
        }

        val loansFlow = combine(
            customerDao.getTotalOutstandingLoansFlow(),
            customerDao.getCustomersWithLoansFlow()
        ) { total, list ->
            Pair(total ?: 0.0, list)
        }

        return combine(
            stockBatchDao.getTotalRemainingStockFlow(),
            salesTodayFlow,
            loansFlow,
            saleDao.getAllSalesFlow()
        ) { totalStock, salesToday, loans, allSales ->
            val (todayAmount, todayCount, todayGrams) = salesToday
            val (totalLoans, customersWithLoans) = loans
            val totalProfit = allSales.sumOf { it.finalAmount }
            DashboardMetrics(
                totalStock = totalStock ?: 0.0,
                salesTodayAmount = todayAmount,
                salesTodayCount = todayCount,
                salesTodayGrams = todayGrams,
                totalLoan = totalLoans,
                totalProfit = totalProfit,
                customersWithLoans = customersWithLoans
            )
        }
    }

    suspend fun recordSale(
        customerId: Long,
        gramsSold: Double,
        grossAmount: Double,
        discount: Double,
        finalAmount: Double,
        amountReceived: Double,
        batchIdOverride: Long? = null,
        comments: String? = null
    ): Result<Long> {
        if (gramsSold <= 0.0) return Result.failure(Exception("Grams sold must be greater than 0"))
        val totalAvailable = stockBatchDao.getTotalRemainingStock() ?: 0.0
        if (gramsSold > totalAvailable) return Result.failure(Exception("Insufficient stock available"))

        val customer = customerDao.getCustomerById(customerId)
            ?: return Result.failure(Exception("Customer not found"))

        val balance = maxOf(0.0, finalAmount - amountReceived)

        return try {
            val saleId = db.withTransaction {
                // 1. Create Sale Entity
                val sale = SaleEntity(
                    customerId = customerId,
                    customerName = customer.name,
                    gramsSold = gramsSold,
                    grossAmount = grossAmount,
                    discount = discount,
                    finalAmount = finalAmount,
                    amountReceived = amountReceived,
                    balance = balance,
                    comments = comments
                )
                val newSaleId = saleDao.insertSale(sale)

                // 2. Perform Batch Deductions
                var remainingToDeduct = gramsSold
                val assignments = mutableListOf<SaleBatchAssignmentEntity>()

                val candidateBatches = if (batchIdOverride != null) {
                    val batch = stockBatchDao.getBatchById(batchIdOverride)
                    if (batch == null || batch.remainingGrams < gramsSold) {
                        throw IllegalStateException("Selected batch does not have sufficient remaining grams")
                    }
                    listOf(batch)
                } else {
                    stockBatchDao.getActiveBatchesAscending()
                }

                for (batch in candidateBatches) {
                    if (remainingToDeduct <= 0.0) break

                    val deductFromThisBatch = minOf(batch.remainingGrams, remainingToDeduct)
                    val newRemaining = batch.remainingGrams - deductFromThisBatch
                    val revenueProportion = if (gramsSold > 0) (deductFromThisBatch / gramsSold) * finalAmount else 0.0

                    assignments.add(
                        SaleBatchAssignmentEntity(
                            saleId = newSaleId,
                            batchId = batch.id,
                            gramsDeducted = deductFromThisBatch,
                            costPricePerGram = batch.pricePerGram
                        )
                    )

                    val updatedBatch = batch.copy(
                        remainingGrams = newRemaining,
                        totalRevenue = batch.totalRevenue + revenueProportion,
                        status = if (newRemaining == 0.0) "depleted" else "active"
                    )
                    stockBatchDao.updateBatch(updatedBatch)

                    remainingToDeduct -= deductFromThisBatch
                }

                if (remainingToDeduct > 0.0001) {
                    throw IllegalStateException("Stock deduction could not be completed. Remainder: $remainingToDeduct")
                }

                // 3. Save deduction assignments
                saleDao.insertAssignments(assignments)

                // 4. Update customer total loan
                if (balance > 0) {
                    customerDao.updateCustomer(
                        customer.copy(totalLoan = customer.totalLoan + balance)
                    )
                }

                newSaleId
            }
            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rollbackSale(saleId: Long): Result<Unit> {
        return try {
            db.withTransaction {
                val sale = saleDao.getSaleById(saleId)
                    ?: throw IllegalArgumentException("Sale record #$saleId does not exist")

                val assignments = saleDao.getAssignmentsForSale(saleId)

                // 1. Restore grams to batches
                for (assignment in assignments) {
                    val batch = stockBatchDao.getBatchById(assignment.batchId)
                    if (batch != null) {
                        val restoredRemaining = minOf(batch.grams, batch.remainingGrams + assignment.gramsDeducted)
                        val revenueToDeduct = if (sale.gramsSold > 0) {
                            (assignment.gramsDeducted / sale.gramsSold) * sale.finalAmount
                        } else 0.0

                        val updatedBatch = batch.copy(
                            remainingGrams = restoredRemaining,
                            totalRevenue = maxOf(0.0, batch.totalRevenue - revenueToDeduct),
                            status = if (restoredRemaining > 0) "active" else "depleted"
                        )
                        stockBatchDao.updateBatch(updatedBatch)
                    }
                }

                // 2. Reduce customer loan if balance existed
                if (sale.balance > 0) {
                    val customer = customerDao.getCustomerById(sale.customerId)
                    if (customer != null) {
                        val newLoan = maxOf(0.0, customer.totalLoan - sale.balance)
                        customerDao.updateCustomer(customer.copy(totalLoan = newLoan))
                    }
                }

                // 3. Delete sale and assignments
                saleDao.deleteAssignmentsBySaleId(saleId)
                saleDao.deleteSaleById(saleId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ingestBatch(grams: Double): Result<Long> {
        if (grams <= 0.0) return Result.failure(Exception("Batch grams must be greater than 0"))

        val rateStr = settingsDao.getSettingValue("rate_per_gram") ?: "1000"
        val pricePerGram = rateStr.toDoubleOrNull() ?: 1000.0

        val batch = StockBatchEntity(
            grams = grams,
            remainingGrams = grams,
            pricePerGram = pricePerGram,
            totalCost = grams * pricePerGram,
            totalRevenue = 0.0,
            status = "active"
        )
        val id = stockBatchDao.insertBatch(batch)
        return Result.success(id)
    }

    suspend fun closeBatch(batchId: Long): Result<Unit> {
        val batch = stockBatchDao.getBatchById(batchId)
            ?: return Result.failure(Exception("Batch not found"))

        val updated = batch.copy(
            remainingGrams = 0.0,
            status = "depleted"
        )
        stockBatchDao.updateBatch(updated)
        return Result.success(Unit)
    }

    suspend fun updateBatch(batchId: Long, newGrams: Double, newPricePerGram: Double): Result<Unit> {
        val batch = stockBatchDao.getBatchById(batchId)
            ?: return Result.failure(Exception("Batch not found"))

        val soldAmount = batch.grams - batch.remainingGrams
        if (newGrams < soldAmount) {
            return Result.failure(Exception("Total grams cannot be less than already sold amount (${soldAmount}g)"))
        }

        val updated = batch.copy(
            grams = newGrams,
            remainingGrams = newGrams - soldAmount,
            pricePerGram = newPricePerGram,
            totalCost = newGrams * newPricePerGram,
            status = if ((newGrams - soldAmount) > 0) "active" else "depleted"
        )
        stockBatchDao.updateBatch(updated)
        return Result.success(Unit)
    }

    suspend fun deleteBatch(batchId: Long): Result<Unit> {
        val batch = stockBatchDao.getBatchById(batchId)
            ?: return Result.failure(Exception("Batch not found"))

        if (batch.grams != batch.remainingGrams) {
            return Result.failure(Exception("Cannot delete batch with fulfilled sales. Use close batch instead."))
        }

        stockBatchDao.deleteBatchById(batchId)
        return Result.success(Unit)
    }

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
