package com.example.healthogram.payments

import com.example.healthogram.payments.gateways.*

/**
 * Healthogram Payment Custom Actions & Integration Layer.
 * Exposes clean, high-level client-callable hooks for FlutterFlow and Jetpack Compose screens,
 * delegating all financial authority, pricing logic, and state transitions to the secure PaymentEngine.
 */
object PaymentCustomActions {

    private val engine = PaymentEngine.getInstance()
    private val repository = PaymentRepository.getInstance()

    /**
     * Preview calculation for checkout. (Read-only preview; non-authoritative).
     */
    fun calculateCheckoutPreview(
        countryCode: String,
        currencyCode: String,
        subtotalMinor: Long,
        shippingMinor: Long = 1500L,
        discountCode: String? = null
    ): PaymentEngine.CheckoutPricingBreakdown {
        return engine.calculateCheckoutTotal(countryCode, currencyCode, subtotalMinor, shippingMinor, discountCode)
    }

    /**
     * Initializes authoritative payment session on the backend.
     */
    fun createPaymentSession(
        orderId: String,
        customerUid: String,
        sellerUid: String,
        countryCode: String,
        currencyCode: String,
        paymentMethodType: PaymentMethodType,
        subtotalMinor: Long,
        shippingMinor: Long,
        discountCode: String?,
        idempotencyKey: String
    ): PaymentTransaction {
        return engine.createPaymentSession(
            orderId = orderId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            countryCode = countryCode,
            currencyCode = currencyCode,
            paymentMethodType = paymentMethodType,
            subtotalMinor = subtotalMinor,
            shippingMinor = shippingMinor,
            discountCode = discountCode,
            idempotencyKey = idempotencyKey
        )
    }

    /**
     * Confirms and captures payment.
     */
    fun confirmPayment(transactionId: String, actionVerificationToken: String? = null): PaymentTransaction {
        return engine.confirmAndCapturePayment(transactionId, actionVerificationToken)
    }

    /**
     * Retries payment using a new payment attempt.
     */
    fun retryPayment(
        failedTransactionId: String,
        paymentMethodType: PaymentMethodType,
        newIdempotencyKey: String
    ): PaymentTransaction {
        val failedTx = repository.findTransaction(failedTransactionId)
            ?: throw IllegalArgumentException("Previous transaction not found: $failedTransactionId")

        return engine.createPaymentSession(
            orderId = failedTx.orderId,
            customerUid = failedTx.customerUid,
            sellerUid = failedTx.sellerUid,
            countryCode = failedTx.countryCode,
            currencyCode = failedTx.currencyCode,
            paymentMethodType = paymentMethodType,
            subtotalMinor = failedTx.subtotalMinor,
            shippingMinor = failedTx.shippingMinor,
            discountCode = null,
            idempotencyKey = newIdempotencyKey
        )
    }

    /**
     * Retrieves status of a payment transaction.
     */
    fun getPaymentStatus(transactionId: String): PaymentTransactionStatus {
        return repository.findTransaction(transactionId)?.status ?: PaymentTransactionStatus.FAILED
    }

    /**
     * Retrieves available payment methods for user's country.
     */
    fun getAvailablePaymentMethods(countryCode: String): List<PaymentMethodItem> {
        return repository.getPaymentMethodsForCountry(countryCode)
    }

    /**
     * Retrieves country payment configuration.
     */
    fun getCountryPaymentConfig(countryCode: String): CountryPaymentConfig {
        return repository.getCountryConfig(countryCode)
    }

    /**
     * Submits a customer refund request.
     */
    fun requestRefund(
        transactionId: String,
        amountMinor: Long,
        refundType: RefundType,
        reason: String,
        customerUid: String
    ): RefundRecord {
        return engine.executeRefund(transactionId, amountMinor, refundType, reason, customerUid)
    }

    /**
     * Retrieves customer payment history.
     */
    fun getCustomerPaymentHistory(customerUid: String): List<PaymentTransaction> {
        return repository.transactions.value.filter { it.customerUid == customerUid }
    }

    /**
     * Retrieves seller balance.
     */
    fun getSellerBalance(sellerUid: String): SellerBalance {
        return repository.sellerBalances.value[sellerUid]
            ?: SellerBalance(sellerUid = sellerUid, currencyCode = "SAR")
    }

    /**
     * Retrieves seller payout history.
     */
    fun getSellerPayoutHistory(sellerUid: String): List<SellerPayout> {
        return repository.sellerPayouts.value.filter { it.sellerUid == sellerUid }
    }

    /**
     * Requests seller payout.
     */
    fun requestSellerPayout(
        sellerUid: String,
        amountMinor: Long,
        payoutMethod: String,
        actorUid: String,
        idempotencyKey: String
    ): SellerPayout {
        return engine.executeSellerPayout(sellerUid, amountMinor, payoutMethod, actorUid, idempotencyKey)
    }

    /**
     * Retrieves owner earnings summary.
     */
    fun getOwnerEarnings(): OwnerRevenueSummary {
        return repository.ownerRevenueSummary.value
    }

    /**
     * Submits owner withdrawal request.
     */
    fun requestOwnerWithdrawal(
        ownerUid: String,
        amountMinor: Long,
        payoutMethod: String,
        twoFactorCode: String,
        idempotencyKey: String
    ): OwnerWithdrawal {
        return engine.executeOwnerWithdrawal(ownerUid, amountMinor, payoutMethod, twoFactorCode, idempotencyKey)
    }
}
