package com.example.healthogram.payments

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class GatewayRoutingRule(
    val countryCode: String,
    val primaryGateway: String, // THAWANI, OMAN_NET, MADA, STRIPE_GLOBAL
    val supportedMethods: List<String>, // DEBIT_CARD, CREDIT_CARD, WALLET, BANK_TRANSFER
    val localCurrency: String
)

data class PaymentTransactionRecord(
    val transactionId: String = UUID.randomUUID().toString(),
    val idempotencyKey: String,
    val payerUid: String,
    val payeeUid: String?,
    val amountMinorUnits: Long,
    val currency: String,
    val countryCode: String,
    val gatewayUsed: String,
    val paymentMethod: String,
    val status: String = "SUCCESS", // PENDING, SUCCESS, FAILED, REFUNDED
    val referenceId: String = "GW-${UUID.randomUUID().toString().take(10).uppercase()}",
    val timestamp: Long = System.currentTimeMillis()
)

data class GlobalPaymentRefundRecord(
    val refundId: String = UUID.randomUUID().toString(),
    val originalTransactionId: String,
    val refundAmountMinorUnits: Long,
    val reason: String,
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * GlobalPaymentService 2.1
 * Multi-country gateway routing, integer minor unit conversions, and idempotent transaction processing.
 */
class GlobalPaymentService private constructor() {

    private val routingRules = ConcurrentHashMap<String, GatewayRoutingRule>()
    private val processedTransactions = ConcurrentHashMap<String, PaymentTransactionRecord>()
    private val idempotencyIndex = ConcurrentHashMap<String, String>() // key -> transactionId
    private val refunds = ConcurrentHashMap<String, MutableList<GlobalPaymentRefundRecord>>()

    init {
        routingRules["OM"] = GatewayRoutingRule(
            countryCode = "OM",
            primaryGateway = "THAWANI",
            supportedMethods = listOf("OMAN_NET_DEBIT", "VISA_MASTERCARD", "WALLET"),
            localCurrency = "OMR"
        )
        routingRules["SA"] = GatewayRoutingRule(
            countryCode = "SA",
            primaryGateway = "MADA",
            supportedMethods = listOf("MADA_DEBIT", "APPLE_PAY", "CREDIT_CARD"),
            localCurrency = "SAR"
        )
        routingRules["AE"] = GatewayRoutingRule(
            countryCode = "AE",
            primaryGateway = "STRIPE_GLOBAL",
            supportedMethods = listOf("APPLE_PAY", "VISA_MASTERCARD"),
            localCurrency = "AED"
        )
        routingRules["US"] = GatewayRoutingRule(
            countryCode = "US",
            primaryGateway = "STRIPE_GLOBAL",
            supportedMethods = listOf("CREDIT_CARD", "GOOGLE_PAY", "APPLE_PAY"),
            localCurrency = "USD"
        )
    }

    companion object {
        @Volatile
        private var instance: GlobalPaymentService? = null

        fun getInstance(): GlobalPaymentService {
            return instance ?: synchronized(this) {
                instance ?: GlobalPaymentService().also { instance = it }
            }
        }
    }

    fun getGatewayForCountry(countryCode: String): GatewayRoutingRule {
        return routingRules[countryCode.uppercase()] ?: GatewayRoutingRule(
            countryCode = countryCode,
            primaryGateway = "STRIPE_GLOBAL",
            supportedMethods = listOf("CREDIT_CARD"),
            localCurrency = "USD"
        )
    }

    /**
     * Process an idempotent payment transaction
     */
    fun processPayment(
        idempotencyKey: String,
        payerUid: String,
        payeeUid: String?,
        amountMinorUnits: Long,
        currency: String,
        countryCode: String,
        paymentMethod: String
    ): PaymentTransactionRecord {
        require(amountMinorUnits > 0) { "Payment amount must be greater than zero" }

        // Idempotency check: if key seen, return existing transaction
        idempotencyIndex[idempotencyKey]?.let { existingTxId ->
            processedTransactions[existingTxId]?.let { return it }
        }

        val rule = getGatewayForCountry(countryCode)
        val tx = PaymentTransactionRecord(
            idempotencyKey = idempotencyKey,
            payerUid = payerUid,
            payeeUid = payeeUid,
            amountMinorUnits = amountMinorUnits,
            currency = currency,
            countryCode = countryCode,
            gatewayUsed = rule.primaryGateway,
            paymentMethod = paymentMethod,
            status = "SUCCESS"
        )

        processedTransactions[tx.transactionId] = tx
        idempotencyIndex[idempotencyKey] = tx.transactionId
        return tx
    }

    /**
     * Process refund with status update
     */
    fun processRefund(
        originalTxId: String,
        reason: String
    ): GlobalPaymentRefundRecord {
        val original = processedTransactions[originalTxId]
            ?: throw IllegalArgumentException("Original transaction not found: $originalTxId")
        require(original.status == "SUCCESS") { "Cannot refund non-successful transaction" }

        val refund = GlobalPaymentRefundRecord(
            originalTransactionId = originalTxId,
            refundAmountMinorUnits = original.amountMinorUnits,
            reason = reason,
            status = "COMPLETED"
        )

        val updated = original.copy(status = "REFUNDED")
        processedTransactions[originalTxId] = updated

        val list = refunds.computeIfAbsent(originalTxId) { mutableListOf() }
        synchronized(list) {
            list.add(refund)
        }
        return refund
    }

    fun getTransaction(txId: String): PaymentTransactionRecord? = processedTransactions[txId]

    fun clear() {
        processedTransactions.clear()
        idempotencyIndex.clear()
        refunds.clear()
    }
}
