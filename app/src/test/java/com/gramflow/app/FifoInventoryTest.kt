package com.gramflow.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

data class MockBatch(
    val id: Long,
    val grams: Double,
    var remainingGrams: Double,
    val pricePerGram: Double,
    var totalRevenue: Double = 0.0
)

class FifoInventoryTest {

    @Test
    fun testFifoDeductionAcrossBatches() {
        val batch1 = MockBatch(id = 1, grams = 10.0, remainingGrams = 10.0, pricePerGram = 1000.0)
        val batch2 = MockBatch(id = 2, grams = 15.0, remainingGrams = 15.0, pricePerGram = 1050.0)
        val batches = listOf(batch1, batch2)

        val gramsToSell = 14.0
        val finalBill = 14000.0
        var remainingToDeduct = gramsToSell

        for (batch in batches) {
            if (remainingToDeduct <= 0.0) break
            val deduct = minOf(batch.remainingGrams, remainingToDeduct)
            batch.remainingGrams -= deduct
            batch.totalRevenue += (deduct / gramsToSell) * finalBill
            remainingToDeduct -= deduct
        }

        // Batch 1 should be completely depleted
        assertEquals(0.0, batch1.remainingGrams, 0.001)
        assertEquals(10000.0, batch1.totalRevenue, 0.01)

        // Batch 2 should have 11.0g remaining
        assertEquals(11.0, batch2.remainingGrams, 0.001)
        assertEquals(4000.0, batch2.totalRevenue, 0.01)
        assertEquals(0.0, remainingToDeduct, 0.001)
    }

    @Test
    fun testPricingBrackets() {
        fun computePrice(grams: Double, ratePerGram: Double, sp25: Double, sp50: Double): Double {
            return when {
                grams in 0.25..0.30 -> sp25
                grams in 0.50..0.60 -> sp50
                else -> grams * ratePerGram
            }
        }

        // Test special 0.25g bracket
        assertEquals(250.0, computePrice(0.30, 1000.0, 250.0, 500.0), 0.01)

        // Test special 0.50g bracket
        assertEquals(500.0, computePrice(0.55, 1000.0, 250.0, 500.0), 0.01)

        // Test standard rate
        assertEquals(2000.0, computePrice(2.0, 1000.0, 250.0, 500.0), 0.01)
    }
}
