package com.gramflow.app

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomerLedgerTest {

    @Test
    fun testPaymentDeductionLegacyFirst() {
        var oldLoan = 1500.0
        var appLoan = 2000.0
        val paymentAmount = 1800.0

        var remainingPayment = paymentAmount

        // 1. Deduct from Old Loan first
        if (oldLoan > 0) {
            val deductOld = minOf(oldLoan, remainingPayment)
            remainingPayment -= deductOld
            oldLoan -= deductOld
        }

        // 2. Deduct remaining from App Loan
        if (remainingPayment > 0 && appLoan > 0) {
            appLoan = maxOf(0.0, appLoan - remainingPayment)
        }

        // Old loan should be 0, App loan should be reduced by remaining 300 to 1700
        assertEquals(0.0, oldLoan, 0.01)
        assertEquals(1700.0, appLoan, 0.01)
        assertEquals(1700.0, oldLoan + appLoan, 0.01)
    }

    @Test
    fun testExcessPaymentCappedAtZero() {
        var oldLoan = 500.0
        var appLoan = 500.0
        val paymentAmount = 1500.0

        var remainingPayment = paymentAmount

        if (oldLoan > 0) {
            val deductOld = minOf(oldLoan, remainingPayment)
            remainingPayment -= deductOld
            oldLoan -= deductOld
        }

        if (remainingPayment > 0 && appLoan > 0) {
            appLoan = maxOf(0.0, appLoan - remainingPayment)
        }

        assertEquals(0.0, oldLoan, 0.01)
        assertEquals(0.0, appLoan, 0.01)
    }
}
