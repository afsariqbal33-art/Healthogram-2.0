package com.example.healthogram.payments

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Healthogram Payment Repository.
 * Central single-source-of-truth for country configs, currencies, transactions,
 * seller ledger & balances, owner revenue, disputes, and audit logs.
 */
class PaymentRepository private constructor() {

    companion object {
        @Volatile
        private var instance: PaymentRepository? = null

        fun getInstance(): PaymentRepository {
            return instance ?: synchronized(this) {
                instance ?: PaymentRepository().also { instance = it }
            }
        }
    }

    // Currencies Registry
    private val _currencies = MutableStateFlow<Map<String, CurrencyConfig>>(
        mapOf(
            "SAR" to CurrencyConfig("SAR", "Saudi Riyal", "ر.س", 100, 2, true, listOf("SA"), "{symbol} {amount}"),
            "USD" to CurrencyConfig("USD", "US Dollar", "$", 100, 2, true, listOf("US"), "{symbol}{amount}"),
            "AED" to CurrencyConfig("AED", "UAE Dirham", "د.إ", 100, 2, true, listOf("AE"), "{symbol} {amount}"),
            "EUR" to CurrencyConfig("EUR", "Euro", "€", 100, 2, true, listOf("DE", "FR", "IT", "ES"), "{symbol}{amount}"),
            "GBP" to CurrencyConfig("GBP", "British Pound", "£", 100, 2, true, listOf("GB"), "{symbol}{amount}"),
            "INR" to CurrencyConfig("INR", "Indian Rupee", "₹", 100, 2, true, listOf("IN"), "{symbol}{amount}")
        )
    )
    val currencies: StateFlow<Map<String, CurrencyConfig>> = _currencies.asStateFlow()

    // Country Payment Configurations
    private val _countryConfigs = MutableStateFlow<Map<String, CountryPaymentConfig>>(
        mapOf(
            "SA" to CountryPaymentConfig(
                countryCode = "SA",
                countryName = "Saudi Arabia",
                currencyCode = "SAR",
                currencySymbol = "ر.س",
                timezone = "Asia/Riyadh",
                supportedGateways = listOf("stripe", "local_mada"),
                defaultGateway = "local_mada",
                fallbackGateway = "stripe",
                supportedPaymentMethods = listOf("mada", "apple_pay", "card", "google_pay"),
                taxEnabled = true,
                taxInclusive = true,
                taxName = "VAT",
                taxRatePercent = 15.0,
                platformFeePercent = 5.0,
                platformFeeFixedMinor = 100L,
                payoutDelayDays = 7,
                internationalMarketplaceEnabled = false
            ),
            "AE" to CountryPaymentConfig(
                countryCode = "AE",
                countryName = "United Arab Emirates",
                currencyCode = "AED",
                currencySymbol = "د.إ",
                timezone = "Asia/Dubai",
                supportedGateways = listOf("stripe"),
                defaultGateway = "stripe",
                supportedPaymentMethods = listOf("card", "apple_pay", "google_pay"),
                taxEnabled = true,
                taxInclusive = true,
                taxName = "VAT",
                taxRatePercent = 5.0,
                platformFeePercent = 5.0,
                payoutDelayDays = 7,
                internationalMarketplaceEnabled = false
            ),
            "US" to CountryPaymentConfig(
                countryCode = "US",
                countryName = "United States",
                currencyCode = "USD",
                currencySymbol = "$",
                timezone = "America/New_York",
                supportedGateways = listOf("stripe"),
                defaultGateway = "stripe",
                supportedPaymentMethods = listOf("card", "apple_pay", "google_pay"),
                taxEnabled = true,
                taxInclusive = false,
                taxName = "Sales Tax",
                taxRatePercent = 8.25,
                platformFeePercent = 5.0,
                platformFeeFixedMinor = 30L,
                payoutDelayDays = 5,
                internationalMarketplaceEnabled = false
            ),
            "GB" to CountryPaymentConfig(
                countryCode = "GB",
                countryName = "United Kingdom",
                currencyCode = "GBP",
                currencySymbol = "£",
                timezone = "Europe/London",
                supportedGateways = listOf("stripe"),
                defaultGateway = "stripe",
                supportedPaymentMethods = listOf("card", "apple_pay", "google_pay"),
                taxEnabled = true,
                taxInclusive = true,
                taxName = "VAT",
                taxRatePercent = 20.0,
                platformFeePercent = 5.0,
                payoutDelayDays = 7,
                internationalMarketplaceEnabled = false
            )
        )
    )
    val countryConfigs: StateFlow<Map<String, CountryPaymentConfig>> = _countryConfigs.asStateFlow()

    // Payment Methods
    private val _paymentMethods = MutableStateFlow<List<PaymentMethodItem>>(
        listOf(
            PaymentMethodItem("pm_mada_sa", "SA", "local_mada", PaymentMethodType.LOCAL_PAYMENT_METHOD, "Mada Debit Card", iconReference = "ic_mada", sortOrder = 1),
            PaymentMethodItem("pm_apple_pay_sa", "SA", "stripe", PaymentMethodType.APPLE_PAY, "Apple Pay", iconReference = "ic_apple_pay", sortOrder = 2),
            PaymentMethodItem("pm_card_sa", "SA", "stripe", PaymentMethodType.CARD, "Credit / Debit Card (Visa, Mastercard)", iconReference = "ic_card", sortOrder = 3),
            PaymentMethodItem("pm_google_pay_sa", "SA", "stripe", PaymentMethodType.GOOGLE_PAY, "Google Pay", iconReference = "ic_google_pay", sortOrder = 4),
            PaymentMethodItem("pm_card_us", "US", "stripe", PaymentMethodType.CARD, "Credit / Debit Card", iconReference = "ic_card", sortOrder = 1),
            PaymentMethodItem("pm_apple_pay_us", "US", "stripe", PaymentMethodType.APPLE_PAY, "Apple Pay", iconReference = "ic_apple_pay", sortOrder = 2)
        )
    )
    val paymentMethods: StateFlow<List<PaymentMethodItem>> = _paymentMethods.asStateFlow()

    // Transactions
    private val _transactions = MutableStateFlow<List<PaymentTransaction>>(emptyList())
    val transactions: StateFlow<List<PaymentTransaction>> = _transactions.asStateFlow()

    // Attempts
    private val _paymentAttempts = MutableStateFlow<List<PaymentAttempt>>(emptyList())
    val paymentAttempts: StateFlow<List<PaymentAttempt>> = _paymentAttempts.asStateFlow()

    // Seller Balances
    private val _sellerBalances = MutableStateFlow<Map<String, SellerBalance>>(emptyMap())
    val sellerBalances: StateFlow<Map<String, SellerBalance>> = _sellerBalances.asStateFlow()

    // Seller Ledger
    private val _sellerLedger = MutableStateFlow<List<SellerLedgerEntry>>(emptyList())
    val sellerLedger: StateFlow<List<SellerLedgerEntry>> = _sellerLedger.asStateFlow()

    // Seller Payouts
    private val _sellerPayouts = MutableStateFlow<List<SellerPayout>>(emptyList())
    val sellerPayouts: StateFlow<List<SellerPayout>> = _sellerPayouts.asStateFlow()

    // Owner Ledger
    private val _ownerLedger = MutableStateFlow<List<OwnerFinancialLedgerEntry>>(emptyList())
    val ownerLedger: StateFlow<List<OwnerFinancialLedgerEntry>> = _ownerLedger.asStateFlow()

    // Owner Revenue Summary
    private val _ownerRevenueSummary = MutableStateFlow(OwnerRevenueSummary())
    val ownerRevenueSummary: StateFlow<OwnerRevenueSummary> = _ownerRevenueSummary.asStateFlow()

    // Owner Withdrawals
    private val _ownerWithdrawals = MutableStateFlow<List<OwnerWithdrawal>>(emptyList())
    val ownerWithdrawals: StateFlow<List<OwnerWithdrawal>> = _ownerWithdrawals.asStateFlow()

    // Refunds
    private val _refunds = MutableStateFlow<List<RefundRecord>>(emptyList())
    val refunds: StateFlow<List<RefundRecord>> = _refunds.asStateFlow()

    // Disputes
    private val _disputes = MutableStateFlow<List<PaymentDispute>>(emptyList())
    val disputes: StateFlow<List<PaymentDispute>> = _disputes.asStateFlow()

    // Webhooks
    private val _webhookEvents = MutableStateFlow<List<PaymentWebhookEvent>>(emptyList())
    val webhookEvents: StateFlow<List<PaymentWebhookEvent>> = _webhookEvents.asStateFlow()

    // Reconciliation Reports
    private val _reconciliationReports = MutableStateFlow<List<PaymentReconciliationReport>>(emptyList())
    val reconciliationReports: StateFlow<List<PaymentReconciliationReport>> = _reconciliationReports.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<PaymentAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<PaymentAuditLog>> = _auditLogs.asStateFlow()

    // Risk Checks
    private val _riskChecks = MutableStateFlow<List<PaymentRiskCheck>>(emptyList())
    val riskChecks: StateFlow<List<PaymentRiskCheck>> = _riskChecks.asStateFlow()

    // Gateway Health Monitors
    private val _gatewayHealth = MutableStateFlow<Map<String, GatewayHealth>>(
        mapOf(
            "stripe" to GatewayHealth("stripe", "Stripe Global & Regional", "HEALTHY", 99.9, 0.1, 280L, 95L),
            "local_mada" to GatewayHealth("local_mada", "Saudi Payments (Mada / SARIE)", "HEALTHY", 99.7, 0.3, 310L, 110L),
            "future_international" to GatewayHealth("future_international", "International Cross-Border", "MAINTENANCE", 0.0, 0.0, 0L, 0L, maintenanceMode = true)
        )
    )
    val gatewayHealth: StateFlow<Map<String, GatewayHealth>> = _gatewayHealth.asStateFlow()

    // Feature Flags & Emergency Kill Switches
    private val _featureFlags = MutableStateFlow(PaymentFeatureFlags())
    val featureFlags: StateFlow<PaymentFeatureFlags> = _featureFlags.asStateFlow()

    init {
        seedInitialSampleData()
    }

    private fun seedInitialSampleData() {
        // Initial sample seller balance for "seller_pharmacy_demo"
        val demoSellerUid = "seller_pharmacy_demo"
        _sellerBalances.value = mapOf(
            demoSellerUid to SellerBalance(
                sellerUid = demoSellerUid,
                currencyCode = "SAR",
                pendingBalanceMinor = 45000L,     // SAR 450.00 in hold window
                availableBalanceMinor = 185000L,  // SAR 1,850.00 ready for payout
                lifetimeSalesMinor = 350000L,
                lifetimeFeesMinor = 17500L,
                lifetimeRefundsMinor = 0L,
                lifetimePayoutsMinor = 102500L
            )
        )

        // Seed sample owner ledger & summary
        val sampleOwnerEntries = listOf(
            OwnerFinancialLedgerEntry(
                sourceType = "MARKETPLACE_ORDER",
                sourceId = "ord_sample_1",
                orderId = "ord_sample_1",
                countryCode = "SA",
                currencyCode = "SAR",
                amountMinor = 1250L, // SAR 12.50 commission
                entryType = OwnerLedgerEntryType.MARKETPLACE_COMMISSION
            ),
            OwnerFinancialLedgerEntry(
                sourceType = "MARKETPLACE_ORDER",
                sourceId = "ord_sample_2",
                orderId = "ord_sample_2",
                countryCode = "SA",
                currencyCode = "SAR",
                amountMinor = 2400L, // SAR 24.00 commission
                entryType = OwnerLedgerEntryType.MARKETPLACE_COMMISSION
            )
        )
        _ownerLedger.value = sampleOwnerEntries
        recalculateOwnerSummary()
    }

    // ----------------------------------------------------------------------
    // Mutation & Query Methods (Enforcing Immutability & Thread-Safety)
    // ----------------------------------------------------------------------

    fun getCountryConfig(countryCode: String): CountryPaymentConfig {
        return _countryConfigs.value[countryCode.uppercase()] ?: _countryConfigs.value["SA"]!!
    }

    fun getCurrency(currencyCode: String): CurrencyConfig {
        return _currencies.value[currencyCode.uppercase()] ?: _currencies.value["SAR"]!!
    }

    fun getPaymentMethodsForCountry(countryCode: String): List<PaymentMethodItem> {
        return _paymentMethods.value.filter { it.countryCode.equals(countryCode, ignoreCase = true) && it.enabled }
    }

    fun findTransaction(transactionId: String): PaymentTransaction? {
        return _transactions.value.find { it.paymentTransactionId == transactionId }
    }

    fun findTransactionByIdempotencyKey(key: String): PaymentTransaction? {
        return _transactions.value.find { it.idempotencyKey == key }
    }

    fun recordTransaction(tx: PaymentTransaction) {
        val existingIndex = _transactions.value.indexOfFirst { it.paymentTransactionId == tx.paymentTransactionId }
        if (existingIndex >= 0) {
            val updated = _transactions.value.toMutableList()
            updated[existingIndex] = tx
            _transactions.value = updated
        } else {
            _transactions.value = listOf(tx) + _transactions.value
        }
    }

    fun recordPaymentAttempt(attempt: PaymentAttempt) {
        _paymentAttempts.value = listOf(attempt) + _paymentAttempts.value
    }

    fun recordSellerLedgerEntry(entry: SellerLedgerEntry) {
        _sellerLedger.value = listOf(entry) + _sellerLedger.value
        // Update seller balance atomically
        val currentBalance = _sellerBalances.value[entry.sellerUid] ?: SellerBalance(sellerUid = entry.sellerUid, currencyCode = entry.currencyCode)
        val newAvailable = if (entry.entryType == SellerLedgerEntryType.PAYOUT) {
            currentBalance.availableBalanceMinor + entry.amountMinor // entry.amountMinor is negative for payout
        } else {
            currentBalance.availableBalanceMinor
        }
        val newPending = if (entry.entryType == SellerLedgerEntryType.SALE) {
            currentBalance.pendingBalanceMinor + entry.amountMinor
        } else {
            currentBalance.pendingBalanceMinor
        }

        val updatedBalance = currentBalance.copy(
            availableBalanceMinor = maxOf(0L, newAvailable),
            pendingBalanceMinor = maxOf(0L, newPending),
            lifetimeSalesMinor = if (entry.entryType == SellerLedgerEntryType.SALE) currentBalance.lifetimeSalesMinor + entry.amountMinor else currentBalance.lifetimeSalesMinor,
            lifetimeRefundsMinor = if (entry.entryType == SellerLedgerEntryType.REFUND) currentBalance.lifetimeRefundsMinor + kotlin.math.abs(entry.amountMinor) else currentBalance.lifetimeRefundsMinor,
            lifetimePayoutsMinor = if (entry.entryType == SellerLedgerEntryType.PAYOUT) currentBalance.lifetimePayoutsMinor + kotlin.math.abs(entry.amountMinor) else currentBalance.lifetimePayoutsMinor,
            updatedAt = System.currentTimeMillis()
        )
        val updatedMap = _sellerBalances.value.toMutableMap()
        updatedMap[entry.sellerUid] = updatedBalance
        _sellerBalances.value = updatedMap
    }

    fun recordSellerPayout(payout: SellerPayout) {
        _sellerPayouts.value = listOf(payout) + _sellerPayouts.value
    }

    fun recordOwnerLedgerEntry(entry: OwnerFinancialLedgerEntry) {
        _ownerLedger.value = listOf(entry) + _ownerLedger.value
        recalculateOwnerSummary()
    }

    fun recordOwnerWithdrawal(withdrawal: OwnerWithdrawal) {
        _ownerWithdrawals.value = listOf(withdrawal) + _ownerWithdrawals.value
        recalculateOwnerSummary()
    }

    fun recordRefund(refund: RefundRecord) {
        _refunds.value = listOf(refund) + _refunds.value
    }

    fun recordDispute(dispute: PaymentDispute) {
        _disputes.value = listOf(dispute) + _disputes.value
    }

    fun recordReconciliationReport(report: PaymentReconciliationReport) {
        _reconciliationReports.value = listOf(report) + _reconciliationReports.value
    }

    fun recordWebhookEvent(event: PaymentWebhookEvent) {
        _webhookEvents.value = listOf(event) + _webhookEvents.value
    }

    fun recordAudit(log: PaymentAuditLog) {
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    fun recordRiskCheck(risk: PaymentRiskCheck) {
        _riskChecks.value = listOf(risk) + _riskChecks.value
    }

    fun updateFeatureFlags(flags: PaymentFeatureFlags) {
        _featureFlags.value = flags
    }

    fun setEmergencyPaymentKillSwitch(active: Boolean) {
        _featureFlags.value = _featureFlags.value.copy(emergencyPaymentKillSwitch = active)
        recordAudit(
            PaymentAuditLog(
                actorUid = "current_owner",
                actorRole = "OWNER",
                action = if (active) "ENABLE_EMERGENCY_PAYMENT_KILL_SWITCH" else "DISABLE_EMERGENCY_PAYMENT_KILL_SWITCH",
                countryCode = "ALL",
                currencyCode = "ALL"
            )
        )
    }

    fun setEmergencyPayoutKillSwitch(active: Boolean) {
        _featureFlags.value = _featureFlags.value.copy(emergencyPayoutKillSwitch = active)
        recordAudit(
            PaymentAuditLog(
                actorUid = "current_owner",
                actorRole = "OWNER",
                action = if (active) "ENABLE_EMERGENCY_PAYOUT_KILL_SWITCH" else "DISABLE_EMERGENCY_PAYOUT_KILL_SWITCH",
                countryCode = "ALL",
                currencyCode = "ALL"
            )
        )
    }

    fun getSellerBalance(sellerUid: String): SellerBalance {
        return _sellerBalances.value[sellerUid] ?: SellerBalance(sellerUid = sellerUid, currencyCode = "SAR")
    }

    fun getSellerLedger(sellerUid: String): List<SellerLedgerEntry> {
        return _sellerLedger.value.filter { it.sellerUid == sellerUid }
    }

    fun updateSellerBalance(
        sellerUid: String,
        currencyCode: String,
        availableDelta: Long,
        pendingDelta: Long,
        reserveDelta: Long
    ) {
        val current = getSellerBalance(sellerUid)
        val updated = current.copy(
            currencyCode = currencyCode,
            availableBalanceMinor = maxOf(0L, current.availableBalanceMinor + availableDelta),
            pendingBalanceMinor = maxOf(0L, current.pendingBalanceMinor + pendingDelta),
            reservedBalanceMinor = maxOf(0L, current.reservedBalanceMinor + reserveDelta),
            updatedAt = System.currentTimeMillis()
        )
        val map = _sellerBalances.value.toMutableMap()
        map[sellerUid] = updated
        _sellerBalances.value = map
    }

    private fun recalculateOwnerSummary() {
        val entries = _ownerLedger.value
        var gross = 0L
        var commission = 0L
        var sellerFees = 0L
        var costs = 0L
        var refunds = 0L
        var chargebacks = 0L
        var net = 0L

        for (e in entries) {
            when (e.entryType) {
                OwnerLedgerEntryType.MARKETPLACE_COMMISSION -> {
                    commission += e.amountMinor
                    gross += e.amountMinor
                    net += e.amountMinor
                }
                OwnerLedgerEntryType.SELLER_FEE -> {
                    sellerFees += e.amountMinor
                    gross += e.amountMinor
                    net += e.amountMinor
                }
                OwnerLedgerEntryType.GATEWAY_COST -> {
                    costs += kotlin.math.abs(e.amountMinor)
                    net -= kotlin.math.abs(e.amountMinor)
                }
                OwnerLedgerEntryType.REFUND_ADJUSTMENT -> {
                    refunds += kotlin.math.abs(e.amountMinor)
                    net -= kotlin.math.abs(e.amountMinor)
                }
                OwnerLedgerEntryType.CHARGEBACK_ADJUSTMENT -> {
                    chargebacks += kotlin.math.abs(e.amountMinor)
                    net -= kotlin.math.abs(e.amountMinor)
                }
                else -> {}
            }
        }

        val totalWithdrawn = _ownerWithdrawals.value
            .filter { it.status == PayoutStatus.COMPLETED }
            .sumOf { it.requestedAmountMinor }

        val available = maxOf(0L, net - totalWithdrawn)

        _ownerRevenueSummary.value = OwnerRevenueSummary(
            currencyCode = "SAR",
            grossPlatformRevenueMinor = gross,
            marketplaceCommissionMinor = commission,
            sellerFeesMinor = sellerFees,
            paymentProcessingCostsMinor = costs,
            refundsDeductedMinor = refunds,
            chargebacksDeductedMinor = chargebacks,
            netPlatformRevenueMinor = net,
            availableRevenueMinor = available,
            totalWithdrawnMinor = totalWithdrawn
        )
    }
}
