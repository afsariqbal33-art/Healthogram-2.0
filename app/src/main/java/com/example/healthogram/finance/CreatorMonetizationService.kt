package com.example.healthogram.finance

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class CreatorMonetizationType {
    SUBSCRIPTION,
    TIP,
    PAID_CONTENT,
    DIGITAL_WORKSHOP
}

data class CreatorTransaction(
    val transactionId: String = UUID.randomUUID().toString(),
    val creatorUid: String,
    val supporterUid: String,
    val monetizationType: CreatorMonetizationType,
    val currency: String = "OMR",
    val grossAmountMinorUnits: Long,
    val platformFeeMinorUnits: Long,
    val paymentProcessingFeeMinorUnits: Long,
    val taxMinorUnits: Long,
    val netCreatorAmountMinorUnits: Long,
    val status: String = "COMPLETED", // PENDING, COMPLETED, REFUNDED
    val createdAt: Long = System.currentTimeMillis()
)

data class CreatorBalance(
    val creatorUid: String,
    val currency: String = "OMR",
    val availableBalanceMinorUnits: Long = 0L,
    val pendingBalanceMinorUnits: Long = 0L,
    val totalPaidOutMinorUnits: Long = 0L,
    val totalEarnedMinorUnits: Long = 0L
)

data class CreatorPayoutRecord(
    val payoutId: String = UUID.randomUUID().toString(),
    val creatorUid: String,
    val amountMinorUnits: Long,
    val currency: String,
    val destinationBankOrIban: String,
    val status: String = "INITIATED", // INITIATED, PROCESSING, PAID, REJECTED
    val referenceNumber: String = "PO-${UUID.randomUUID().toString().take(8).uppercase()}",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * CreatorMonetizationService 2.1
 * Manages creator earnings, tipping, subscription entitlements, and integer-math payouts.
 */
class CreatorMonetizationService private constructor(
    private val taxService: TaxService = TaxService.getInstance()
) {

    private val transactions = ConcurrentHashMap<String, MutableList<CreatorTransaction>>()
    private val balances = ConcurrentHashMap<String, CreatorBalance>()
    private val payouts = ConcurrentHashMap<String, MutableList<CreatorPayoutRecord>>()

    companion object {
        @Volatile
        private var instance: CreatorMonetizationService? = null

        fun getInstance(): CreatorMonetizationService {
            return instance ?: synchronized(this) {
                instance ?: CreatorMonetizationService().also { instance = it }
            }
        }

        const val DEFAULT_PLATFORM_FEE_BPS = 1000 // 10.00%
        const val DEFAULT_PAYMENT_FEE_BPS = 250    // 2.50%
    }

    /**
     * Process a creator monetization transaction (subscription, tip, or paid content)
     */
    fun processCreatorEarning(
        creatorUid: String,
        supporterUid: String,
        type: CreatorMonetizationType,
        grossAmountMinorUnits: Long,
        currency: String = "OMR",
        countryCode: String = "OM"
    ): CreatorTransaction {
        require(grossAmountMinorUnits > 0) { "Gross amount must be positive" }

        val platformFee = (grossAmountMinorUnits * DEFAULT_PLATFORM_FEE_BPS + 5000L) / 10000L
        val paymentFee = (grossAmountMinorUnits * DEFAULT_PAYMENT_FEE_BPS + 5000L) / 10000L

        val taxResult = taxService.calculateTax(
            countryCode = countryCode,
            amountMinorUnits = platformFee, // Tax applied on platform service fee
            isHealthcareService = false,
            isDigitalService = true
        )
        val taxFee = taxResult.taxAmountMinorUnits

        val netCreator = grossAmountMinorUnits - platformFee - paymentFee - taxFee

        val tx = CreatorTransaction(
            creatorUid = creatorUid,
            supporterUid = supporterUid,
            monetizationType = type,
            currency = currency,
            grossAmountMinorUnits = grossAmountMinorUnits,
            platformFeeMinorUnits = platformFee,
            paymentProcessingFeeMinorUnits = paymentFee,
            taxMinorUnits = taxFee,
            netCreatorAmountMinorUnits = netCreator,
            status = "COMPLETED"
        )

        val txList = transactions.computeIfAbsent(creatorUid) { mutableListOf() }
        synchronized(txList) {
            txList.add(tx)
        }

        // Update balance
        val currBalance = balances.computeIfAbsent(creatorUid) {
            CreatorBalance(creatorUid = creatorUid, currency = currency)
        }
        val updated = currBalance.copy(
            availableBalanceMinorUnits = currBalance.availableBalanceMinorUnits + netCreator,
            totalEarnedMinorUnits = currBalance.totalEarnedMinorUnits + netCreator
        )
        balances[creatorUid] = updated

        return tx
    }

    /**
     * Request payout of available creator balance
     */
    fun requestPayout(
        creatorUid: String,
        amountMinorUnits: Long,
        iban: String
    ): CreatorPayoutRecord {
        val currBalance = balances[creatorUid]
            ?: throw IllegalStateException("No balance record found for creator $creatorUid")

        require(amountMinorUnits > 0) { "Payout amount must be positive" }
        require(currBalance.availableBalanceMinorUnits >= amountMinorUnits) {
            "Insufficient available balance (Available: ${currBalance.availableBalanceMinorUnits}, Requested: $amountMinorUnits)"
        }

        val payout = CreatorPayoutRecord(
            creatorUid = creatorUid,
            amountMinorUnits = amountMinorUnits,
            currency = currBalance.currency,
            destinationBankOrIban = iban,
            status = "PROCESSING"
        )

        val list = payouts.computeIfAbsent(creatorUid) { mutableListOf() }
        synchronized(list) {
            list.add(payout)
        }

        balances[creatorUid] = currBalance.copy(
            availableBalanceMinorUnits = currBalance.availableBalanceMinorUnits - amountMinorUnits,
            totalPaidOutMinorUnits = currBalance.totalPaidOutMinorUnits + amountMinorUnits
        )

        return payout
    }

    fun getCreatorBalance(creatorUid: String): CreatorBalance {
        return balances.computeIfAbsent(creatorUid) {
            CreatorBalance(creatorUid = creatorUid)
        }
    }

    fun getTransactions(creatorUid: String): List<CreatorTransaction> {
        return transactions[creatorUid]?.toList() ?: emptyList()
    }

    fun getPayouts(creatorUid: String): List<CreatorPayoutRecord> {
        return payouts[creatorUid]?.toList() ?: emptyList()
    }

    fun clear() {
        transactions.clear()
        balances.clear()
        payouts.clear()
    }
}
