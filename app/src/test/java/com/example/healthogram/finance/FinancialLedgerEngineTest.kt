package com.example.healthogram.finance

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Healthogram Step 18: Verification Test Suite for Financial Ledger Engine,
 * Double-Entry Accounting, Commission Rules, Seller Settlements, and Owner Withdrawals.
 */
class FinancialLedgerEngineTest {

    private lateinit var engine: FinancialLedgerEngine

    @Before
    fun setup() {
        engine = FinancialLedgerEngine.getInstance()
        engine.resetForTesting()
    }

    @Test
    fun testCommissionCalculationByCountryAndCategory() {
        // Saudi Arabia Pharmacy Medicine rule: 10% commission
        val commission = engine.calculateCommission(
            country = "SA",
            sellerType = "PHARMACY",
            productCategory = "MEDICINE",
            subtotal = 1000.0
        )
        assertEquals(100.0, commission, 0.01)

        // Saudi Arabia Laboratory Lab Test rule: 12% commission
        val labCommission = engine.calculateCommission(
            country = "SA",
            sellerType = "LABORATORY",
            productCategory = "LAB_TEST",
            subtotal = 500.0
        )
        assertEquals(60.0, labCommission, 0.01)

        // Cap rule check: max fee 500 SAR on medicine
        val highSubtotalCommission = engine.calculateCommission(
            country = "SA",
            sellerType = "PHARMACY",
            productCategory = "MEDICINE",
            subtotal = 10000.0 // 10% would be 1000, capped at 500
        )
        assertEquals(500.0, highSubtotalCommission, 0.01)
    }

    @Test
    fun testSellerPayableCalculation() {
        val commission = 50.0
        val sellerPayable = engine.calculateSellerPayable(
            subtotal = 500.0,
            sellerDeliveryContribution = 0.0,
            discountsFundedBySeller = 20.0,
            commissionAmount = commission,
            sellerServiceFees = 10.0,
            refundsOrDeductions = 0.0
        )
        // 500 - 20 - 50 - 10 = 420.0
        assertEquals(420.0, sellerPayable, 0.01)
    }

    @Test
    fun testDoubleEntryPostingAndIntegrityCheck() {
        val initialEntriesCount = engine.ledgerEntries.value.size

        val entries = engine.postCustomerOrderTransaction(
            orderId = "order_test_101",
            paymentId = "pmt_test_101",
            customerId = "cust_001",
            sellerId = "seller_001",
            country = "SA",
            currency = "SAR",
            productSubtotal = 1000.0,
            deliveryFee = 50.0,
            platformServiceFee = 20.0,
            taxAmount = 30.0,
            paymentGatewayFee = 15.0,
            sellerType = "PHARMACY",
            productCategory = "MEDICINE",
            idempotencyKey = "idemp_test_101"
        )

        assertTrue(entries.isNotEmpty())
        assertEquals(initialEntriesCount + entries.size, engine.ledgerEntries.value.size)

        // Verify debits vs credits inside this transaction
        val totalDebits = entries.filter { it.direction == LedgerDirection.DEBIT }.sumOf { it.amount }
        val totalCredits = entries.filter { it.direction == LedgerDirection.CREDIT }.sumOf { it.amount }

        // Debits: Customer payment (1100) + Gateway fee (15) = 1115
        // Credits: Seller payable (900) + Platform commission (100) + Service fee (20) + Delivery (50) + Tax (30) + Gateway deduction (15) = 1115
        assertEquals(totalDebits, totalCredits, 0.01)

        // Verify overall system integrity check passes
        val integrity = engine.runFinancialIntegrityCheck()
        assertTrue("Double-entry bookkeeping must remain balanced", integrity.isBalanced)
        assertEquals("PASS", integrity.status)
    }

    @Test
    fun testFinancialIdempotencyPreventsDuplicatePosting() {
        val key = "idemp_duplicate_guard_key_999"
        val firstRunEntries = engine.postCustomerOrderTransaction(
            orderId = "order_idemp_999",
            paymentId = "pmt_idemp_999",
            customerId = "cust_002",
            sellerId = "seller_002",
            productSubtotal = 500.0,
            deliveryFee = 25.0,
            platformServiceFee = 10.0,
            taxAmount = 15.0,
            paymentGatewayFee = 5.0,
            idempotencyKey = key
        )

        val countAfterFirst = engine.ledgerEntries.value.size

        // Post again with the exact same idempotency key
        val secondRunEntries = engine.postCustomerOrderTransaction(
            orderId = "order_idemp_999",
            paymentId = "pmt_idemp_999",
            customerId = "cust_002",
            sellerId = "seller_002",
            productSubtotal = 500.0,
            deliveryFee = 25.0,
            platformServiceFee = 10.0,
            taxAmount = 15.0,
            paymentGatewayFee = 5.0,
            idempotencyKey = key
        )

        // No new entries should be added
        assertEquals(countAfterFirst, engine.ledgerEntries.value.size)
    }

    @Test
    fun testImmutableLedgerReversal() {
        val entries = engine.postCustomerOrderTransaction(
            orderId = "order_rev_001",
            paymentId = "pmt_rev_001",
            customerId = "cust_003",
            sellerId = "seller_003",
            productSubtotal = 200.0,
            deliveryFee = 0.0,
            platformServiceFee = 0.0,
            taxAmount = 0.0,
            paymentGatewayFee = 0.0
        )

        val commissionEntry = entries.first { it.entryType == "MARKETPLACE_COMMISSION" }
        assertEquals(LedgerEntryStatus.POSTED, commissionEntry.status)
        assertEquals(LedgerDirection.CREDIT, commissionEntry.direction)

        val reversal = engine.reverseLedgerEntry(
            originalEntryId = commissionEntry.entryId,
            actorUid = "owner_root_001",
            reason = "Customer cancelled order under compliance review"
        )

        // Reversal must be opposite direction
        assertEquals(LedgerDirection.DEBIT, reversal.direction)
        assertEquals(commissionEntry.amount, reversal.amount, 0.01)

        // Original status updated to REVERSED
        val updatedOriginal = engine.ledgerEntries.value.first { it.entryId == commissionEntry.entryId }
        assertEquals(LedgerEntryStatus.REVERSED, updatedOriginal.status)
    }

    @Test
    fun testOwnerWithdrawalReservationAndCompletion() {
        val initialAvailable = engine.ownerAccount.value.availableBalance
        val initialReserved = engine.ownerAccount.value.reservedBalance
        val initialWithdrawn = engine.ownerAccount.value.withdrawnBalance
        val withdrawAmount = 5000.0

        // Step 1: Request withdrawal (funds move to RESERVED)
        val request = engine.requestOwnerWithdrawal(
            ownerUid = "owner_root_001",
            amount = withdrawAmount,
            payoutAccountId = "payout_acc_001",
            pin = "9900"
        )

        assertEquals(OwnerWithdrawalStatus.REQUESTED, request.status)
        assertEquals(initialAvailable - withdrawAmount, engine.ownerAccount.value.availableBalance, 0.01)
        assertEquals(initialReserved + withdrawAmount, engine.ownerAccount.value.reservedBalance, 0.01)

        // Step 2: Complete withdrawal (funds move to WITHDRAWN, cleared from RESERVED)
        val completed = engine.completeOwnerWithdrawal(request.withdrawalId)
        assertEquals(OwnerWithdrawalStatus.COMPLETED, completed.status)
        assertEquals(initialReserved, engine.ownerAccount.value.reservedBalance, 0.01)
        assertEquals(initialWithdrawn + withdrawAmount, engine.ownerAccount.value.withdrawnBalance, 0.01)
    }

    @Test
    fun testOwnerWithdrawalCancellationReleasesReservation() {
        val initialAvailable = engine.ownerAccount.value.availableBalance
        val initialReserved = engine.ownerAccount.value.reservedBalance
        val withdrawAmount = 2500.0

        // Request withdrawal
        val request = engine.requestOwnerWithdrawal(
            ownerUid = "owner_root_001",
            amount = withdrawAmount,
            payoutAccountId = "payout_acc_001",
            pin = "9900"
        )

        assertEquals(initialAvailable - withdrawAmount, engine.ownerAccount.value.availableBalance, 0.01)

        // Cancel withdrawal
        val cancelled = engine.failOrCancelOwnerWithdrawal(
            withdrawalId = request.withdrawalId,
            reason = "Bank routing code discrepancy"
        )

        assertEquals(OwnerWithdrawalStatus.FAILED, cancelled.status)
        // Funds restored back to available
        assertEquals(initialAvailable, engine.ownerAccount.value.availableBalance, 0.01)
        assertEquals(initialReserved, engine.ownerAccount.value.reservedBalance, 0.01)
    }

    @Test
    fun testWithdrawalPINRequirementAndLimits() {
        // Invalid PIN
        assertThrows(IllegalArgumentException::class.java) {
            engine.requestOwnerWithdrawal(
                ownerUid = "owner_root_001",
                amount = 1000.0,
                payoutAccountId = "payout_acc_001",
                pin = "0000" // wrong PIN
            )
        }

        // Amount below minimum (min is 100)
        assertThrows(IllegalArgumentException::class.java) {
            engine.requestOwnerWithdrawal(
                ownerUid = "owner_root_001",
                amount = 25.0,
                payoutAccountId = "payout_acc_001",
                pin = "9900"
            )
        }
    }

    @Test
    fun testReconciliationRunDetectsDiscrepancies() {
        val run = engine.runFinancialReconciliation(provider = "ALL_GATEWAYS", country = "SA")
        assertNotNull(run)
        assertTrue(run.totalRecordsChecked > 0)
        assertTrue(run.discrepancies.isNotEmpty())
        assertEquals(ReconciliationStatus.MISMATCH, run.status)
    }
}
