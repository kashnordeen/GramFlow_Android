package com.gramflow.app.data.repository

import androidx.room.withTransaction
import com.gramflow.app.data.local.AppDatabase
import com.gramflow.app.data.local.entity.CustomerEntity
import com.gramflow.app.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

data class CustomerLedgerStatement(
    val customer: CustomerEntity,
    val sales: List<SaleEntity>,
    val totalPurchasedGrams: Double,
    val totalPurchasedAmount: Double,
    val totalPaid: Double,
    val currentBalance: Double
)

class CustomerRepository(private val db: AppDatabase) {
    private val customerDao = db.customerDao()
    private val saleDao = db.saleDao()
    private val inventoryRepo = InventoryRepository(db)

    val allCustomersFlow: Flow<List<CustomerEntity>> = customerDao.getAllCustomersFlow()
    val totalLoansFlow: Flow<Double?> = customerDao.getTotalOutstandingLoansFlow()

    suspend fun createCustomer(name: String, phone: String?, oldLoan: Double = 0.0): Result<Long> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return Result.failure(Exception("Customer name cannot be empty"))

        val customer = CustomerEntity(
            name = trimmedName,
            phone = phone?.trim()?.takeIf { it.isNotBlank() },
            oldLoan = maxOf(0.0, oldLoan),
            totalLoan = 0.0
        )
        val id = customerDao.insertCustomer(customer)
        return Result.success(id)
    }

    suspend fun recordPayment(customerId: Long, paymentAmount: Double): Result<Unit> {
        if (paymentAmount <= 0.0) return Result.failure(Exception("Payment amount must be greater than 0"))

        val customer = customerDao.getCustomerById(customerId)
            ?: return Result.failure(Exception("Customer not found"))

        val combinedLoan = customer.oldLoan + customer.totalLoan
        if (combinedLoan <= 0.0) return Result.failure(Exception("Customer has no outstanding balance"))

        var remainingPayment = paymentAmount

        // 1. Deduct from Old Loan first
        val newOldLoan = if (customer.oldLoan > 0) {
            val deductOld = minOf(customer.oldLoan, remainingPayment)
            remainingPayment -= deductOld
            customer.oldLoan - deductOld
        } else {
            0.0
        }

        // 2. Deduct from Total Loan (App Debt) next
        val newTotalLoan = if (remainingPayment > 0 && customer.totalLoan > 0) {
            maxOf(0.0, customer.totalLoan - remainingPayment)
        } else {
            customer.totalLoan
        }

        customerDao.updateCustomer(
            customer.copy(
                oldLoan = newOldLoan,
                totalLoan = newTotalLoan
            )
        )
        return Result.success(Unit)
    }

    suspend fun overrideCustomerLoan(customerId: Long, oldLoan: Double, appLoan: Double): Result<Unit> {
        val customer = customerDao.getCustomerById(customerId)
            ?: return Result.failure(Exception("Customer not found"))

        customerDao.updateCustomer(
            customer.copy(
                oldLoan = maxOf(0.0, oldLoan),
                totalLoan = maxOf(0.0, appLoan)
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteCustomer(customerId: Long): Result<Unit> {
        return try {
            db.withTransaction {
                val customer = customerDao.getCustomerById(customerId)
                    ?: throw IllegalArgumentException("Customer does not exist")

                // Rollback any associated sales first so inventory is fully restored
                val sales = saleDao.getSalesForCustomer(customerId)
                for (sale in sales) {
                    inventoryRepo.rollbackSale(sale.id)
                }

                customerDao.deleteCustomerById(customerId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomerLedger(customerId: Long): Result<CustomerLedgerStatement> {
        val customer = customerDao.getCustomerById(customerId)
            ?: return Result.failure(Exception("Customer not found"))

        val sales = saleDao.getSalesForCustomer(customerId)
        val totalGrams = sales.sumOf { it.gramsSold }
        val totalPurchased = sales.sumOf { it.finalAmount }
        val totalPaid = sales.sumOf { it.amountReceived }
        val currentBalance = customer.oldLoan + customer.totalLoan

        return Result.success(
            CustomerLedgerStatement(
                customer = customer,
                sales = sales,
                totalPurchasedGrams = totalGrams,
                totalPurchasedAmount = totalPurchased,
                totalPaid = totalPaid,
                currentBalance = currentBalance
            )
        )
    }
}
