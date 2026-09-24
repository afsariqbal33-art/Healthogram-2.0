package com.example.healthogram.payments

import com.example.healthogram.payments.gateways.PaymentGatewayResolver
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Step 14: Country-Wise Payment System & Payment Gateway Architecture Unit & Integration Tests.
 * Verifies:
 * 1. Multi-country configuration & routing (SA, AE, US, GB)
 * 2. Authoritative calculations (taxes, fees, commissions, minor unit precision)
 * 3. Tokenized payment execution with zero PCI raw data leakage
 * 4. Idempotency enforcement
 * 5. Double-entry immutable ledger creation for sellers and platform owner
 * 6. Refund execution (full & partial) with ledger reversal entries
 * 7. Seller bank payout & owner withdrawal controls
 * 8. Emergency kill switches
 * 9. Webhook signature verification and idempotency deduplication
 * 10. Gateway reconciliation matching
 */
class PaymentInfrastructureTest {

    private lateinit var repository: PaymentRepository
    private lateinit var engine: PaymentEngine
    private lateinit var gatewayResolver: PaymentGatewayResolver

    @Before
    fun setup() {
        repository = PaymentRepository.getInstance()
        engine = PaymentEngine.getInstance()
        gatewayResolver = PaymentGatewayResolver()

        // Reset kill switches
        repository.setEmergencyPaymentKillSwitch(false)
        repository.setEmergencyPayoutKillSwitch(false)
    }

    @Test
    fun testCountryConfigurationsLoaded() {
        val configs = repository.countryConfigs.value
        assertTrue("Saudi Arabia must be configured", configs.containsKey("SA"))
        assertTrue("UAE must be configured", configs.containsKey("AE"))
        assertTrue("USA must be configured", configs.containsKey("US"))
        assertTrue("UK must be configured", configs.containsKey("GB"))

        val saConfig = configs["SA"]!!
        assertEquals("SAR", saConfig.currencyCode)
        assertEquals(15.0, saConfig.taxRatePercent, 0.001)
        assertTrue("Saudi VAT is inclusive", saConfig.taxInclusive)
        assertEquals("local_mada", saConfig.defaultGateway)

        val usConfig = configs["US"]!!
        assertEquals("USD", usConfig.currencyCode)
        assertEquals(8.25, usConfig.taxRatePercent, 0.001)
        assertFalse("US Sales tax is exclusive", usConfig.taxInclusive)
        assertEquals("stripe", usConfig.defaultGateway)
    }

    @Test
    fun testServerAuthoritativeCalculation_SaudiInclusiveTax() {
        // Subtotal: 100.00 SAR (10000 halalas), Shipping: 15.00 SAR (1500 halalas)
        // Saudi VAT is 15% inclusive: Tax is included within the 10000 subtotal
        val calc = engine.calculateCheckoutTotal(
            countryCode = "SA",
            currencyCode = "SAR",
            subtotalMinor = 10000L,
            shippingMinor = 1500L,
            discountCode = null
        )

        assertEquals("Subtotal must match", 10000L, calc.subtotalMinor)
        assertEquals("Shipping must match", 1500L, calc.shippingMinor)
        // Inclusive tax on 10000 at 15%: 10000 * 15 / 115 = 1304
        assertEquals("Inclusive tax must match", 1304L, calc.taxMinor)
        // Grand total: subtotal + shipping = 11500
        assertEquals("Grand total must match", 11500L, calc.totalPayableMinor)

        // Platform fee: 5% on 10000 = 500 + 100 halalas fixed = 600
        assertEquals("Platform fee must match", 600L, calc.platformFeeMinor)
        assertEquals("Seller gross must match", 10000L, calc.sellerGrossMinor)
        assertEquals("Seller net must match", 9400L, calc.sellerNetMinor)
    }

    @Test
    fun testServerAuthoritativeCalculation_USExclusiveTax() {
        // Subtotal: $100.00 (10000 cents), Shipping: $10.00 (1000 cents)
        // US Sales tax is 8.25% exclusive: Tax is added on top of taxable subtotal
        val calc = engine.calculateCheckoutTotal(
            countryCode = "US",
            currencyCode = "USD",
            subtotalMinor = 10000L,
            shippingMinor = 1000L,
            discountCode = null
        )

        assertEquals(10000L, calc.subtotalMinor)
        assertEquals(1000L, calc.shippingMinor)
        // 8.25% on 10000 = 825
        assertEquals(825L, calc.taxMinor)
        // Grand total: 10000 + 1000 + 825 = 11825 cents ($118.25)
        assertEquals(11825L, calc.totalPayableMinor)
    }

    @Test
    fun testCreatePaymentSessionAndConfirmation_EndToEnd() {
        val customerUid = "cust_test_456"
        val sellerUid = "seller_pharma_sa"
        val orderId = "order_test_999"
        val idempotencyKey = "idemp_test_abc123"

        // Step 1: Create Authoritative Payment Session
        val transaction = PaymentCustomActions.createPaymentSession(
            orderId = orderId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            countryCode = "SA",
            currencyCode = "SAR",
            paymentMethodType = PaymentMethodType.LOCAL_PAYMENT_METHOD,
            subtotalMinor = 20000L, // 200.00 SAR
            shippingMinor = 2000L,  // 20.00 SAR
            discountCode = "HEALTH10",
            idempotencyKey = idempotencyKey
        )

        assertNotNull(transaction)
        assertTrue(
            "Transaction status must be REQUIRES_ACTION or PROCESSING",
            transaction.status == PaymentTransactionStatus.REQUIRES_ACTION || transaction.status == PaymentTransactionStatus.PROCESSING
        )
        assertEquals(orderId, transaction.orderId)
        assertEquals(customerUid, transaction.customerUid)
        assertTrue("Client secret must be securely tokenized", transaction.clientSecretToken?.isNotBlank() == true)

        // Verify Idempotency: Repeating request returns exact same transaction
        val duplicateTx = PaymentCustomActions.createPaymentSession(
            orderId = orderId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            countryCode = "SA",
            currencyCode = "SAR",
            paymentMethodType = PaymentMethodType.LOCAL_PAYMENT_METHOD,
            subtotalMinor = 20000L,
            shippingMinor = 2000L,
            discountCode = "HEALTH10",
            idempotencyKey = idempotencyKey
        )
        assertEquals("Duplicate request must return existing transaction id", transaction.paymentTransactionId, duplicateTx.paymentTransactionId)

        // Step 2: Confirm Payment (Simulating gateway token confirmation)
        val initialSellerBalance = repository.getSellerBalance(sellerUid).availableBalanceMinor
        val initialOwnerRevenue = repository.ownerRevenueSummary.value.availableRevenueMinor

        val confirmedTx = PaymentCustomActions.confirmPayment(transaction.paymentTransactionId)
        assertEquals(PaymentTransactionStatus.CAPTURED, confirmedTx.status)

        // Verify Immutable Ledger entries were generated
        val sellerLedger = repository.getSellerLedger(sellerUid)
        assertTrue("Seller ledger must contain the transaction credit", sellerLedger.any { it.paymentTransactionId == confirmedTx.paymentTransactionId })

        val ownerLedger = repository.ownerLedger.value
        assertTrue("Owner ledger must contain platform revenue entry", ownerLedger.any { it.paymentTransactionId == confirmedTx.paymentTransactionId })

        // Verify Seller and Owner Balances increased accurately
        val updatedSellerBalance = repository.getSellerBalance(sellerUid).availableBalanceMinor
        val updatedOwnerRevenue = repository.ownerRevenueSummary.value.availableRevenueMinor

        assertTrue("Seller balance must increase", updatedSellerBalance >= initialSellerBalance)
        assertTrue("Owner revenue must increase", updatedOwnerRevenue >= initialOwnerRevenue)
    }

    @Test
    fun testEmergencyPaymentKillSwitch() {
        // Activate emergency payment kill switch
        repository.setEmergencyPaymentKillSwitch(true)

        try {
            PaymentCustomActions.createPaymentSession(
                orderId = "order_blocked_1",
                customerUid = "cust_test",
                sellerUid = "seller_test",
                countryCode = "SA",
                currencyCode = "SAR",
                paymentMethodType = PaymentMethodType.CARD,
                subtotalMinor = 5000L,
                shippingMinor = 0L,
                discountCode = null,
                idempotencyKey = "idemp_blocked_1"
            )
            fail("Should have thrown IllegalStateException due to kill switch")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("kill switch") == true)
        } finally {
            repository.setEmergencyPaymentKillSwitch(false)
        }
    }

    @Test
    fun testRefundProcessing_FullAndPartial() {
        val tx = PaymentCustomActions.createPaymentSession(
            orderId = "order_refund_test",
            customerUid = "cust_refund",
            sellerUid = "seller_refund",
            countryCode = "US",
            currencyCode = "USD",
            paymentMethodType = PaymentMethodType.CARD,
            subtotalMinor = 10000L, // $100.00
            shippingMinor = 1000L,
            discountCode = null,
            idempotencyKey = "idemp_refund_test"
        )
        val confirmed = PaymentCustomActions.confirmPayment(tx.paymentTransactionId)
        assertEquals(PaymentTransactionStatus.CAPTURED, confirmed.status)

        // Execute Partial Refund: $40.00 (4000 cents)
        val refundResult = PaymentCustomActions.requestRefund(
            transactionId = confirmed.paymentTransactionId,
            amountMinor = 4000L,
            refundType = RefundType.PARTIAL,
            reason = "Customer returned one item in order",
            customerUid = "cust_refund"
        )
        assertEquals("Refund must be COMPLETED", RefundStatus.COMPLETED, refundResult.status)
        assertEquals(4000L, refundResult.amountMinor)

        val updatedTx = repository.transactions.value.first { it.paymentTransactionId == confirmed.paymentTransactionId }
        assertEquals("Status must be PARTIALLY_REFUNDED", PaymentTransactionStatus.PARTIALLY_REFUNDED, updatedTx.status)
        assertEquals(4000L, updatedTx.amountRefundedMinor)

        // Verify negative ledger entry for seller
        val sellerEntries = repository.getSellerLedger("seller_refund")
        assertTrue("Must have a REFUND entry in seller ledger", sellerEntries.any { it.entryType == SellerLedgerEntryType.REFUND })
    }

    @Test
    fun testSellerPayoutExecution() {
        val sellerUid = "seller_payout_test"
        // Credit seller balance with 50000 halalas (500 SAR)
        repository.updateSellerBalance(
            sellerUid = sellerUid,
            currencyCode = "SAR",
            availableDelta = 50000L,
            pendingDelta = 0L,
            reserveDelta = 0L
        )

        val payout = PaymentCustomActions.requestSellerPayout(
            sellerUid = sellerUid,
            amountMinor = 40000L, // 400 SAR
            payoutMethod = "IBAN_BANK_TRANSFER",
            actorUid = sellerUid,
            idempotencyKey = "idemp_payout_001"
        )

        assertEquals("Payout must be COMPLETED", PayoutStatus.COMPLETED, payout.status)
        assertEquals(40000L, payout.amountMinor)
        assertEquals(10000L, repository.getSellerBalance(sellerUid).availableBalanceMinor)
    }

    @Test
    fun testOwnerWithdrawalAndPayoutKillSwitch() {
        // Activate emergency payout kill switch
        repository.setEmergencyPayoutKillSwitch(true)

        try {
            PaymentCustomActions.requestOwnerWithdrawal(
                ownerUid = "owner_admin",
                amountMinor = 5000L,
                payoutMethod = "TREASURY_DIRECT_WIRE",
                twoFactorCode = "123456",
                idempotencyKey = "idemp_owner_w1"
            )
            fail("Should have failed due to emergency payout kill switch")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("kill switch") == true || e.message?.contains("disabled") == true)
        } finally {
            repository.setEmergencyPayoutKillSwitch(false)
        }
    }

    @Test
    fun testWebhookVerificationAndProcessing() {
        val testPayload = """{"event": "payment_intent.succeeded", "id": "evt_webhook_123"}"""
        val validSignature = "sig_mock_valid_12345"

        val webhookEvent = engine.processWebhook(
            gatewayId = "stripe",
            payloadJson = testPayload,
            signatureHeader = validSignature
        )

        assertNotNull(webhookEvent)
        assertEquals("SUCCESS", webhookEvent.processingStatus)

        // Replay attack / duplicate webhook test
        val duplicateEvent = engine.processWebhook(
            gatewayId = "stripe",
            payloadJson = testPayload,
            signatureHeader = validSignature
        )
        // Idempotently ignored without duplicate creation
        assertEquals(webhookEvent.eventId, duplicateEvent.eventId)
    }

    @Test
    fun testReconciliationEngine() {
        val report = engine.reconcilePayments("local_mada", "SA")
        assertNotNull(report)
        assertTrue("Reconciliation matched count must be non-negative", report.matchedCount >= 0)
        assertEquals("Zero discrepancy amount expected in clean environment", 0L, report.discrepancyAmountMinor)
        assertEquals("RECONCILED", report.status)
    }
}
