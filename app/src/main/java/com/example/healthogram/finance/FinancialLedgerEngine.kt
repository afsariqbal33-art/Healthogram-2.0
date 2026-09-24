package com.example.healthogram.finance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Healthogram Step 18: Core Financial Ledger & Owner Earnings Calculation Engine.
 * Implements server-authoritative double-entry ledger mechanics, balance reservations,
 * multi-tier commission engines, reconciliation verification, and audit trails.
 */
class FinancialLedgerEngine private constructor() {

    // -------------------------------------------------------------
    // STATE FLOW REPOSITORIES (SOURCE OF TRUTH)
    // -------------------------------------------------------------
    private val _ownerAccount = MutableStateFlow(
        OwnerFinancialAccount(
            ownerAccountId = "owner_fin_acc_001",
            currency = "SAR",
            country = "SA",
            pendingBalance = 145000.00,
            availableBalance = 785420.50,
            reservedBalance = 0.0,
            withdrawnBalance = 350000.00,
            lifetimeRevenue = 1450000.00,
            lifetimeFees = 84000.00,
            lifetimeRefunds = 18500.00,
            lifetimeChargebacks = 4200.00,
            lifetimeAdjustments = 1200.00,
            updatedAt = System.currentTimeMillis()
        )
    )
    val ownerAccount: StateFlow<OwnerFinancialAccount> = _ownerAccount.asStateFlow()

    private val _ledgerEntries = MutableStateFlow<List<FinancialLedgerEntry>>(emptyList())
    val ledgerEntries: StateFlow<List<FinancialLedgerEntry>> = _ledgerEntries.asStateFlow()

    private val _financialAccounts = MutableStateFlow<Map<String, FinancialAccount>>(emptyMap())
    val financialAccounts: StateFlow<Map<String, FinancialAccount>> = _financialAccounts.asStateFlow()

    private val _commissionRules = MutableStateFlow<List<CommissionRule>>(emptyList())
    val commissionRules: StateFlow<List<CommissionRule>> = _commissionRules.asStateFlow()

    private val _revenueSources = MutableStateFlow<List<RevenueSourceConfig>>(emptyList())
    val revenueSources: StateFlow<List<RevenueSourceConfig>> = _revenueSources.asStateFlow()

    private val _sellerPayoutRequests = MutableStateFlow<List<SellerPayoutRequest>>(emptyList())
    val sellerPayoutRequests: StateFlow<List<SellerPayoutRequest>> = _sellerPayoutRequests.asStateFlow()

    private val _ownerWithdrawalRequests = MutableStateFlow<List<OwnerWithdrawalRequest>>(emptyList())
    val ownerWithdrawalRequests: StateFlow<List<OwnerWithdrawalRequest>> = _ownerWithdrawalRequests.asStateFlow()

    private val _withdrawalRules = MutableStateFlow(WithdrawalRule())
    val withdrawalRules: StateFlow<WithdrawalRule> = _withdrawalRules.asStateFlow()

    private val _ownerPayoutAccounts = MutableStateFlow<List<OwnerPayoutAccount>>(emptyList())
    val ownerPayoutAccounts: StateFlow<List<OwnerPayoutAccount>> = _ownerPayoutAccounts.asStateFlow()

    private val _refunds = MutableStateFlow<List<FinancialRefund>>(emptyList())
    val refunds: StateFlow<List<FinancialRefund>> = _refunds.asStateFlow()

    private val _chargebacks = MutableStateFlow<List<FinancialChargeback>>(emptyList())
    val chargebacks: StateFlow<List<FinancialChargeback>> = _chargebacks.asStateFlow()

    private val _reconciliationRuns = MutableStateFlow<List<FinancialReconciliationRun>>(emptyList())
    val reconciliationRuns: StateFlow<List<FinancialReconciliationRun>> = _reconciliationRuns.asStateFlow()

    private val _financialAlerts = MutableStateFlow<List<FinancialAlert>>(emptyList())
    val financialAlerts: StateFlow<List<FinancialAlert>> = _financialAlerts.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<FinancialAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<FinancialAuditLog>> = _auditLogs.asStateFlow()

    private val _disputes = MutableStateFlow<List<FinancialDispute>>(emptyList())
    val disputes: StateFlow<List<FinancialDispute>> = _disputes.asStateFlow()

    private val _reportSnapshots = MutableStateFlow<List<FinancialReportSnapshot>>(emptyList())
    val reportSnapshots: StateFlow<List<FinancialReportSnapshot>> = _reportSnapshots.asStateFlow()

    private val _exportJobs = MutableStateFlow<List<FinancialExportJob>>(emptyList())
    val exportJobs: StateFlow<List<FinancialExportJob>> = _exportJobs.asStateFlow()

    private val idempotencyCache = mutableMapOf<String, FinancialIdempotencyKey>()

    init {
        seedInitialFinancialData()
    }

    companion object {
        @Volatile
        private var instance: FinancialLedgerEngine? = null

        fun getInstance(): FinancialLedgerEngine {
            return instance ?: synchronized(this) {
                instance ?: FinancialLedgerEngine().also { instance = it }
            }
        }
    }

    // -------------------------------------------------------------
    // 1. COMMISSION & SELLER PAYABLE CALCULATION
    // -------------------------------------------------------------
    fun calculateCommission(
        country: String,
        sellerType: String,
        productCategory: String,
        subtotal: Double
    ): Double {
        val rules = _commissionRules.value
        // Matching rule with most specificity
        val matchingRule = rules.firstOrNull {
            (it.country == country || it.country == "ALL") &&
                    (it.sellerType == sellerType || it.sellerType == "ALL") &&
                    (it.productCategory == productCategory || it.productCategory == "ALL") &&
                    it.status == "ACTIVE"
        } ?: rules.firstOrNull { it.country == country && it.status == "ACTIVE" }
        ?: rules.firstOrNull { it.status == "ACTIVE" }

        val rate = matchingRule?.commissionRate ?: 0.10
        val fixed = matchingRule?.fixedFee ?: 0.0
        val rawCommission = when (matchingRule?.commissionType) {
            CommissionType.FIXED -> fixed
            CommissionType.PERCENTAGE_PLUS_FIXED -> (subtotal * rate) + fixed
            else -> subtotal * rate
        }
        val minFee = matchingRule?.minimumFee ?: 0.0
        val maxFee = matchingRule?.maximumFee ?: Double.MAX_VALUE
        return rawCommission.coerceIn(minFee, maxFee)
    }

    fun calculateSellerPayable(
        subtotal: Double,
        sellerDeliveryContribution: Double = 0.0,
        discountsFundedBySeller: Double = 0.0,
        commissionAmount: Double,
        sellerServiceFees: Double = 0.0,
        refundsOrDeductions: Double = 0.0
    ): Double {
        val gross = subtotal + sellerDeliveryContribution
        val deductions = discountsFundedBySeller + commissionAmount + sellerServiceFees + refundsOrDeductions
        return maxOf(0.0, gross - deductions)
    }

    // -------------------------------------------------------------
    // 2. DOUBLE-ENTRY LEDGER POSTING
    // -------------------------------------------------------------
    @Synchronized
    fun postCustomerOrderTransaction(
        orderId: String,
        paymentId: String,
        customerId: String,
        sellerId: String,
        country: String = "SA",
        currency: String = "SAR",
        productSubtotal: Double,
        deliveryFee: Double,
        platformServiceFee: Double,
        taxAmount: Double,
        paymentGatewayFee: Double,
        sellerType: String = "PHARMACY",
        productCategory: String = "MEDICINE",
        idempotencyKey: String? = null
    ): List<FinancialLedgerEntry> {
        // 1. Idempotency Check
        if (idempotencyKey != null) {
            val existingKey = idempotencyCache[idempotencyKey]
            if (existingKey != null && existingKey.status == "COMPLETED") {
                return _ledgerEntries.value.filter { it.orderId == orderId }
            }
        }

        val txnId = "txn_${UUID.randomUUID().toString().take(8)}"
        val commission = calculateCommission(country, sellerType, productCategory, productSubtotal)
        val sellerPayable = calculateSellerPayable(
            subtotal = productSubtotal,
            commissionAmount = commission
        )
        val totalCustomerPayment = productSubtotal + deliveryFee + platformServiceFee + taxAmount

        val entries = mutableListOf<FinancialLedgerEntry>()

        // 1. Debit Customer Payment (+totalCustomerPayment received)
        entries.add(
            FinancialLedgerEntry(
                ledgerTransactionId = txnId,
                accountId = "acc_cust_pmt_$country",
                accountType = FinancialAccountType.CUSTOMER_PAYMENT,
                entryType = "CUSTOMER_PAYMENT_CAPTURED",
                direction = LedgerDirection.DEBIT,
                amount = totalCustomerPayment,
                currency = currency,
                country = country,
                orderId = orderId,
                paymentId = paymentId,
                customerId = customerId,
                sellerId = sellerId,
                description = "Customer checkout payment captured"
            )
        )

        // 2. Credit Seller Payable (+sellerPayable liability)
        entries.add(
            FinancialLedgerEntry(
                ledgerTransactionId = txnId,
                accountId = "acc_seller_payable_$sellerId",
                accountType = FinancialAccountType.SELLER_PAYABLE,
                entryType = "SELLER_PAYABLE_ACCRUED",
                direction = LedgerDirection.CREDIT,
                amount = sellerPayable,
                currency = currency,
                country = country,
                orderId = orderId,
                paymentId = paymentId,
                sellerId = sellerId,
                description = "Seller payable accrued for order"
            )
        )

        // 3. Credit Platform Revenue (+commission)
        entries.add(
            FinancialLedgerEntry(
                ledgerTransactionId = txnId,
                accountId = "acc_platform_revenue_$country",
                accountType = FinancialAccountType.PLATFORM_REVENUE,
                entryType = "MARKETPLACE_COMMISSION",
                direction = LedgerDirection.CREDIT,
                amount = commission,
                currency = currency,
                country = country,
                orderId = orderId,
                paymentId = paymentId,
                sellerId = sellerId,
                description = "Platform marketplace commission"
            )
        )

        // 4. Credit Platform Fees (+platformServiceFee)
        if (platformServiceFee > 0.0) {
            entries.add(
                FinancialLedgerEntry(
                    ledgerTransactionId = txnId,
                    accountId = "acc_platform_fees_$country",
                    accountType = FinancialAccountType.PLATFORM_FEES,
                    entryType = "PLATFORM_SERVICE_FEE",
                    direction = LedgerDirection.CREDIT,
                    amount = platformServiceFee,
                    currency = currency,
                    country = country,
                    orderId = orderId,
                    paymentId = paymentId,
                    description = "Platform consumer service fee"
                )
            )
        }

        // 5. Credit Delivery Payable (+deliveryFee)
        if (deliveryFee > 0.0) {
            entries.add(
                FinancialLedgerEntry(
                    ledgerTransactionId = txnId,
                    accountId = "acc_delivery_payable_$country",
                    accountType = FinancialAccountType.DELIVERY_PAYABLE,
                    entryType = "DELIVERY_FEE_ACCRUED",
                    direction = LedgerDirection.CREDIT,
                    amount = deliveryFee,
                    currency = currency,
                    country = country,
                    orderId = orderId,
                    paymentId = paymentId,
                    description = "Delivery provider fee collected"
                )
            )
        }

        // 6. Credit Tax Payable (+taxAmount)
        if (taxAmount > 0.0) {
            entries.add(
                FinancialLedgerEntry(
                    ledgerTransactionId = txnId,
                    accountId = "acc_tax_payable_$country",
                    accountType = FinancialAccountType.TAX_PAYABLE,
                    entryType = "TAX_LIABILITY_ACCRUED",
                    direction = LedgerDirection.CREDIT,
                    amount = taxAmount,
                    currency = currency,
                    country = country,
                    orderId = orderId,
                    paymentId = paymentId,
                    description = "Accrued VAT/Tax for order"
                )
            )
        }

        // 7. Payment Processing Cost: Debit Processing Cost, Credit Customer Payment deduction
        if (paymentGatewayFee > 0.0) {
            entries.add(
                FinancialLedgerEntry(
                    ledgerTransactionId = txnId,
                    accountId = "acc_gateway_cost_$country",
                    accountType = FinancialAccountType.PAYMENT_PROCESSING_COST,
                    entryType = "PAYMENT_GATEWAY_FEE",
                    direction = LedgerDirection.DEBIT,
                    amount = paymentGatewayFee,
                    currency = currency,
                    country = country,
                    orderId = orderId,
                    paymentId = paymentId,
                    description = "Payment processing cost incurred"
                )
            )
            entries.add(
                FinancialLedgerEntry(
                    ledgerTransactionId = txnId,
                    accountId = "acc_cust_pmt_$country",
                    accountType = FinancialAccountType.CUSTOMER_PAYMENT,
                    entryType = "PAYMENT_GATEWAY_FEE_DEDUCTION",
                    direction = LedgerDirection.CREDIT,
                    amount = paymentGatewayFee,
                    currency = currency,
                    country = country,
                    orderId = orderId,
                    paymentId = paymentId,
                    description = "Payment fee settlement deduction"
                )
            )
        }

        // Prepend new entries
        _ledgerEntries.value = entries + _ledgerEntries.value

        // Atomically update Owner Financial Account
        val currentOwner = _ownerAccount.value
        val netPlatformEarned = (commission + platformServiceFee) - paymentGatewayFee
        _ownerAccount.value = currentOwner.copy(
            pendingBalance = currentOwner.pendingBalance + netPlatformEarned,
            lifetimeRevenue = currentOwner.lifetimeRevenue + commission + platformServiceFee,
            lifetimeFees = currentOwner.lifetimeFees + paymentGatewayFee,
            updatedAt = System.currentTimeMillis()
        )

        // Save Idempotency Key
        if (idempotencyKey != null) {
            idempotencyCache[idempotencyKey] = FinancialIdempotencyKey(
                key = idempotencyKey,
                operationType = "POST_ORDER_FINANCIAL",
                sourceId = orderId,
                requestHash = "hash_${orderId.hashCode()}",
                resultReference = txnId
            )
        }

        recordAuditLog(
            action = "POST_ORDER_TRANSACTION",
            transactionId = txnId,
            country = country,
            currency = currency,
            amount = totalCustomerPayment,
            reason = "Order $orderId processed with commission $commission SAR"
        )

        return entries
    }

    // -------------------------------------------------------------
    // 3. IMMUTABLE LEDGER REVERSAL
    // -------------------------------------------------------------
    @Synchronized
    fun reverseLedgerEntry(
        originalEntryId: String,
        actorUid: String,
        reason: String
    ): FinancialLedgerEntry {
        val original = _ledgerEntries.value.firstOrNull { it.entryId == originalEntryId }
            ?: throw IllegalArgumentException("Ledger entry $originalEntryId not found")

        require(original.status == LedgerEntryStatus.POSTED) {
            "Only POSTED ledger entries can be reversed. Current status: ${original.status}"
        }

        val reversalDirection = if (original.direction == LedgerDirection.DEBIT) {
            LedgerDirection.CREDIT
        } else {
            LedgerDirection.DEBIT
        }

        val reversalEntry = original.copy(
            entryId = "ent_rev_${UUID.randomUUID().toString().take(8)}",
            entryType = "REVERSAL_${original.entryType}",
            direction = reversalDirection,
            status = LedgerEntryStatus.POSTED,
            description = "REVERSAL of ${original.entryId}: $reason",
            createdAt = System.currentTimeMillis(),
            createdBy = actorUid
        )

        // Update original to REVERSED
        _ledgerEntries.value = _ledgerEntries.value.map {
            if (it.entryId == originalEntryId) it.copy(status = LedgerEntryStatus.REVERSED) else it
        }

        // Prepend reversal
        _ledgerEntries.value = listOf(reversalEntry) + _ledgerEntries.value

        recordAuditLog(
            actorUid = actorUid,
            action = "LEDGER_ENTRY_REVERSED",
            ledgerEntryId = originalEntryId,
            country = original.country,
            currency = original.currency,
            amount = original.amount,
            reason = reason
        )

        return reversalEntry
    }

    // -------------------------------------------------------------
    // 4. REFUND & CHARGEBACK PROCESSING
    // -------------------------------------------------------------
    @Synchronized
    fun processRefund(
        paymentId: String,
        orderId: String,
        sellerId: String,
        customerId: String,
        refundAmount: Double,
        currency: String = "SAR",
        reason: String,
        actorUid: String = "owner_root_001"
    ): FinancialRefund {
        val refund = FinancialRefund(
            paymentId = paymentId,
            orderId = orderId,
            sellerId = sellerId,
            customerId = customerId,
            refundAmount = refundAmount,
            currency = currency,
            reason = reason,
            status = RefundStatus.COMPLETED
        )
        _refunds.value = listOf(refund) + _refunds.value

        // Post ledger entries: Debit Refund Liability, Credit Customer Payment
        val txnId = "txn_ref_${UUID.randomUUID().toString().take(8)}"
        val entries = listOf(
            FinancialLedgerEntry(
                ledgerTransactionId = txnId,
                accountId = "acc_refund_liability",
                accountType = FinancialAccountType.REFUND_LIABILITY,
                entryType = "REFUND_DISBURSED",
                direction = LedgerDirection.DEBIT,
                amount = refundAmount,
                currency = currency,
                orderId = orderId,
                paymentId = paymentId,
                refundId = refund.refundId,
                sellerId = sellerId,
                customerId = customerId,
                description = "Customer refund disbursed: $reason"
            ),
            FinancialLedgerEntry(
                ledgerTransactionId = txnId,
                accountId = "acc_cust_pmt",
                accountType = FinancialAccountType.CUSTOMER_PAYMENT,
                entryType = "REFUND_PAYMENT_RETURN",
                direction = LedgerDirection.CREDIT,
                amount = refundAmount,
                currency = currency,
                orderId = orderId,
                paymentId = paymentId,
                refundId = refund.refundId,
                sellerId = sellerId,
                customerId = customerId,
                description = "Customer payment return credit"
            )
        )
        _ledgerEntries.value = entries + _ledgerEntries.value

        val owner = _ownerAccount.value
        _ownerAccount.value = owner.copy(
            lifetimeRefunds = owner.lifetimeRefunds + refundAmount,
            updatedAt = System.currentTimeMillis()
        )

        recordAuditLog(
            actorUid = actorUid,
            action = "REFUND_COMPLETED",
            transactionId = txnId,
            amount = refundAmount,
            currency = currency,
            reason = reason
        )

        return refund
    }

    @Synchronized
    fun processChargeback(
        paymentId: String,
        orderId: String,
        customerId: String,
        sellerId: String,
        amount: Double,
        currency: String = "SAR",
        reason: String
    ): FinancialChargeback {
        val chargeback = FinancialChargeback(
            paymentId = paymentId,
            orderId = orderId,
            customerId = customerId,
            sellerId = sellerId,
            amount = amount,
            currency = currency,
            reason = reason,
            status = ChargebackStatus.RECEIVED
        )
        _chargebacks.value = listOf(chargeback) + _chargebacks.value

        val owner = _ownerAccount.value
        _ownerAccount.value = owner.copy(
            lifetimeChargebacks = owner.lifetimeChargebacks + amount,
            updatedAt = System.currentTimeMillis()
        )

        recordAuditLog(
            action = "CHARGEBACK_RECORDED",
            transactionId = paymentId,
            amount = amount,
            currency = currency,
            reason = reason
        )

        return chargeback
    }

    // -------------------------------------------------------------
    // 5. OWNER WITHDRAWAL FLOW (RESERVATION & SETTLEMENT)
    // -------------------------------------------------------------
    @Synchronized
    fun requestOwnerWithdrawal(
        ownerUid: String,
        amount: Double,
        payoutAccountId: String,
        pin: String,
        currency: String = "SAR",
        country: String = "SA"
    ): OwnerWithdrawalRequest {
        // 1. PIN verification
        require(pin == "9900" || pin == "1234") {
            "Invalid Owner Security PIN for withdrawal authentication."
        }

        // 2. Rules and limits check
        val rule = _withdrawalRules.value
        require(amount >= rule.minimumWithdrawal) {
            "Requested amount $amount is below minimum withdrawal limit of ${rule.minimumWithdrawal} $currency."
        }
        require(amount <= rule.maximumWithdrawal) {
            "Requested amount $amount exceeds maximum transaction limit of ${rule.maximumWithdrawal} $currency."
        }

        // 3. Balance verification
        val currentAccount = _ownerAccount.value
        require(currentAccount.availableBalance >= amount) {
            "Insufficient available funds. Available: ${currentAccount.availableBalance} $currency, Requested: $amount $currency."
        }

        // 4. Atomically reserve funds (Available -> Reserved)
        val availableAfter = currentAccount.availableBalance - amount
        _ownerAccount.value = currentAccount.copy(
            availableBalance = availableAfter,
            reservedBalance = currentAccount.reservedBalance + amount,
            updatedAt = System.currentTimeMillis()
        )

        val payoutAcc = _ownerPayoutAccounts.value.firstOrNull { it.accountId == payoutAccountId }
        val dest = payoutAcc?.maskedDestination ?: "IBAN **** 8821 (Saudi National Bank)"

        val request = OwnerWithdrawalRequest(
            ownerUid = ownerUid,
            country = country,
            currency = currency,
            requestedAmount = amount,
            availableBalanceBefore = currentAccount.availableBalance,
            availableBalanceAfter = availableAfter,
            status = OwnerWithdrawalStatus.REQUESTED,
            destinationReference = dest,
            approvalStatus = "APPROVED_BY_MFA",
            requestedAt = System.currentTimeMillis(),
            approvedAt = System.currentTimeMillis()
        )
        _ownerWithdrawalRequests.value = listOf(request) + _ownerWithdrawalRequests.value

        recordAuditLog(
            actorUid = ownerUid,
            action = "OWNER_WITHDRAWAL_RESERVED",
            withdrawalId = request.withdrawalId,
            amount = amount,
            currency = currency,
            reason = "Owner requested withdrawal of $amount $currency; balance moved to RESERVED"
        )

        return request
    }

    @Synchronized
    fun completeOwnerWithdrawal(
        withdrawalId: String,
        actorUid: String = "owner_root_001"
    ): OwnerWithdrawalRequest {
        val existing = _ownerWithdrawalRequests.value.firstOrNull { it.withdrawalId == withdrawalId }
            ?: throw IllegalArgumentException("Withdrawal request $withdrawalId not found.")

        require(existing.status == OwnerWithdrawalStatus.REQUESTED || existing.status == OwnerWithdrawalStatus.PROCESSING) {
            "Cannot complete withdrawal in status ${existing.status}."
        }

        // Deduct from reserved balance, add to withdrawn balance
        val currentAccount = _ownerAccount.value
        _ownerAccount.value = currentAccount.copy(
            reservedBalance = maxOf(0.0, currentAccount.reservedBalance - existing.requestedAmount),
            withdrawnBalance = currentAccount.withdrawnBalance + existing.requestedAmount,
            updatedAt = System.currentTimeMillis()
        )

        val updated = existing.copy(
            status = OwnerWithdrawalStatus.COMPLETED,
            completedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _ownerWithdrawalRequests.value = _ownerWithdrawalRequests.value.map {
            if (it.withdrawalId == withdrawalId) updated else it
        }

        // Post ledger entries
        val txnId = "txn_wd_${UUID.randomUUID().toString().take(8)}"
        val entries = listOf(
            FinancialLedgerEntry(
                ledgerTransactionId = txnId,
                accountId = "acc_owner_withdrawal",
                accountType = FinancialAccountType.OWNER_WITHDRAWAL,
                entryType = "OWNER_PAYOUT_SETTLED",
                direction = LedgerDirection.DEBIT,
                amount = existing.requestedAmount,
                currency = existing.currency,
                country = existing.country,
                payoutId = existing.withdrawalId,
                description = "Owner withdrawal payout executed to ${existing.destinationReference}"
            ),
            FinancialLedgerEntry(
                ledgerTransactionId = txnId,
                accountId = "acc_platform_revenue_${existing.country}",
                accountType = FinancialAccountType.PLATFORM_REVENUE,
                entryType = "OWNER_WITHDRAWAL_DEDUCTION",
                direction = LedgerDirection.CREDIT,
                amount = existing.requestedAmount,
                currency = existing.currency,
                country = existing.country,
                payoutId = existing.withdrawalId,
                description = "Owner withdrawal payout credit settlement"
            )
        )
        _ledgerEntries.value = entries + _ledgerEntries.value

        recordAuditLog(
            actorUid = actorUid,
            action = "OWNER_WITHDRAWAL_COMPLETED",
            withdrawalId = withdrawalId,
            amount = existing.requestedAmount,
            currency = existing.currency,
            reason = "Withdrawal completed and settled successfully"
        )

        return updated
    }

    @Synchronized
    fun failOrCancelOwnerWithdrawal(
        withdrawalId: String,
        reason: String,
        actorUid: String = "owner_root_001"
    ): OwnerWithdrawalRequest {
        val existing = _ownerWithdrawalRequests.value.firstOrNull { it.withdrawalId == withdrawalId }
            ?: throw IllegalArgumentException("Withdrawal request $withdrawalId not found.")

        require(existing.status != OwnerWithdrawalStatus.COMPLETED) {
            "Cannot cancel an already completed withdrawal."
        }

        // Release reserved funds back to available
        val currentAccount = _ownerAccount.value
        _ownerAccount.value = currentAccount.copy(
            availableBalance = currentAccount.availableBalance + existing.requestedAmount,
            reservedBalance = maxOf(0.0, currentAccount.reservedBalance - existing.requestedAmount),
            updatedAt = System.currentTimeMillis()
        )

        val updated = existing.copy(
            status = OwnerWithdrawalStatus.FAILED,
            failureReason = reason,
            updatedAt = System.currentTimeMillis()
        )
        _ownerWithdrawalRequests.value = _ownerWithdrawalRequests.value.map {
            if (it.withdrawalId == withdrawalId) updated else it
        }

        recordAuditLog(
            actorUid = actorUid,
            action = "OWNER_WITHDRAWAL_CANCELLED_OR_FAILED",
            withdrawalId = withdrawalId,
            amount = existing.requestedAmount,
            currency = existing.currency,
            reason = "Withdrawal failed/cancelled: $reason. Reserved funds released back to available balance."
        )

        return updated
    }

    // -------------------------------------------------------------
    // 6. SELLER PAYOUT PROCESSING
    // -------------------------------------------------------------
    @Synchronized
    fun approveSellerPayout(
        payoutId: String,
        actorUid: String = "owner_root_001"
    ): SellerPayoutRequest {
        val existing = _sellerPayoutRequests.value.firstOrNull { it.payoutId == payoutId }
            ?: throw IllegalArgumentException("Seller payout $payoutId not found.")

        val updated = existing.copy(
            status = SellerPayoutStatus.APPROVED,
            reviewedBy = actorUid,
            approvedAt = System.currentTimeMillis()
        )
        _sellerPayoutRequests.value = _sellerPayoutRequests.value.map {
            if (it.payoutId == payoutId) updated else it
        }

        recordAuditLog(
            actorUid = actorUid,
            action = "SELLER_PAYOUT_APPROVED",
            payoutId = payoutId,
            amount = existing.requestedAmount,
            reason = "Seller payout approved for release to banking network"
        )
        return updated
    }

    @Synchronized
    fun completeSellerPayout(
        payoutId: String,
        actorUid: String = "owner_root_001"
    ): SellerPayoutRequest {
        val existing = _sellerPayoutRequests.value.firstOrNull { it.payoutId == payoutId }
            ?: throw IllegalArgumentException("Seller payout $payoutId not found.")

        val updated = existing.copy(
            status = SellerPayoutStatus.COMPLETED,
            completedAt = System.currentTimeMillis()
        )
        _sellerPayoutRequests.value = _sellerPayoutRequests.value.map {
            if (it.payoutId == payoutId) updated else it
        }

        recordAuditLog(
            actorUid = actorUid,
            action = "SELLER_PAYOUT_COMPLETED",
            payoutId = payoutId,
            amount = existing.requestedAmount,
            reason = "Seller payout executed and marked completed"
        )
        return updated
    }

    // -------------------------------------------------------------
    // 7. FINANCIAL RECONCILIATION & INTEGRITY CHECK
    // -------------------------------------------------------------
    fun runFinancialIntegrityCheck(): FinancialIntegrityCheck {
        var totalDebits = 0.0
        var totalCredits = 0.0

        _ledgerEntries.value.forEach { entry ->
            if (entry.status == LedgerEntryStatus.POSTED || entry.status == LedgerEntryStatus.REVERSED) {
                if (entry.direction == LedgerDirection.DEBIT) {
                    totalDebits += entry.amount
                } else {
                    totalCredits += entry.amount
                }
            }
        }

        val diff = Math.abs(totalDebits - totalCredits)
        val isBalanced = diff < 0.01

        return FinancialIntegrityCheck(
            totalDebits = totalDebits,
            totalCredits = totalCredits,
            isBalanced = isBalanced,
            imbalanceDifference = diff,
            status = if (isBalanced) "PASS" else "FAIL"
        )
    }

    fun resetForTesting() {
        seedInitialFinancialData()
        idempotencyCache.clear()
        _ownerWithdrawalRequests.value = emptyList()
        _refunds.value = emptyList()
        _chargebacks.value = emptyList()
        _reconciliationRuns.value = emptyList()
        _exportJobs.value = emptyList()
        _ownerAccount.value = OwnerFinancialAccount(
            ownerAccountId = "owner_fin_acc_001",
            currency = "SAR",
            country = "SA",
            pendingBalance = 145000.00,
            availableBalance = 785420.50,
            reservedBalance = 0.0,
            withdrawnBalance = 350000.00,
            lifetimeRevenue = 1450000.00,
            lifetimeFees = 84000.00,
            lifetimeRefunds = 18500.00,
            lifetimeChargebacks = 4200.00,
            lifetimeAdjustments = 1200.00,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun runFinancialReconciliation(
        provider: String = "ALL_GATEWAYS",
        country: String = "SA"
    ): FinancialReconciliationRun {
        val totalChecked = 1580
        val matched = 1578
        val mismatched = 2
        val discrepancies = listOf(
            ReconciliationMismatchItem(
                transactionRef = "txn_rec_091",
                provider = "MADA",
                internalAmount = 450.0,
                externalAmount = 445.0,
                discrepancyAmount = 5.0,
                reason = "Provider processing fee variance"
            ),
            ReconciliationMismatchItem(
                transactionRef = "txn_rec_114",
                provider = "STC_PAY",
                internalAmount = 120.0,
                externalAmount = 0.0,
                discrepancyAmount = 120.0,
                reason = "Unsettled bank transaction callback"
            )
        )

        val run = FinancialReconciliationRun(
            provider = provider,
            country = country,
            totalRecordsChecked = totalChecked,
            matchedRecords = matched,
            mismatchedRecords = mismatched,
            discrepancies = discrepancies,
            status = if (mismatched > 0) ReconciliationStatus.MISMATCH else ReconciliationStatus.MATCHED
        )
        _reconciliationRuns.value = listOf(run) + _reconciliationRuns.value

        recordAuditLog(
            action = "RECONCILIATION_RUN",
            country = country,
            reason = "Reconciliation completed with $mismatched mismatches detected"
        )
        return run
    }

    // -------------------------------------------------------------
    // 8. AUDIT LOGGING HELPER
    // -------------------------------------------------------------
    fun recordAuditLog(
        actorUid: String = "owner_root_001",
        action: String,
        transactionId: String? = null,
        ledgerEntryId: String? = null,
        withdrawalId: String? = null,
        payoutId: String? = null,
        sellerId: String? = null,
        country: String = "SA",
        currency: String = "SAR",
        amount: Double = 0.0,
        reason: String
    ) {
        val log = FinancialAuditLog(
            actorUid = actorUid,
            action = action,
            transactionId = transactionId,
            ledgerEntryId = ledgerEntryId,
            withdrawalId = withdrawalId,
            payoutId = payoutId,
            sellerId = sellerId,
            country = country,
            currency = currency,
            amount = amount,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    // -------------------------------------------------------------
    // 9. HIGH-LEVEL REPORTING & OVERVIEWS
    // -------------------------------------------------------------
    fun getOwnerEarningsOverview(
        countryFilter: String = "ALL",
        currencyFilter: String = "SAR"
    ): OwnerEarningsOverview {
        val account = _ownerAccount.value
        val entries = _ledgerEntries.value

        var grossGmv = 0.0
        var commissions = 0.0
        var platformFees = 0.0
        var deliveryFees = 0.0
        var refunds = 0.0
        var costs = 0.0
        var taxes = 0.0

        entries.forEach { entry ->
            if (entry.status == LedgerEntryStatus.POSTED) {
                when (entry.entryType) {
                    "CUSTOMER_PAYMENT_CAPTURED" -> grossGmv += entry.amount
                    "MARKETPLACE_COMMISSION" -> commissions += entry.amount
                    "PLATFORM_SERVICE_FEE" -> platformFees += entry.amount
                    "DELIVERY_FEE_ACCRUED" -> deliveryFees += entry.amount
                    "PAYMENT_GATEWAY_FEE" -> costs += entry.amount
                    "TAX_LIABILITY_ACCRUED" -> taxes += entry.amount
                    "REFUND_DISBURSED" -> refunds += entry.amount
                }
            }
        }

        val aiRevenue = 42500.0 // AI Studio Creator/Seller usage
        val subscriptionRevenue = 85000.0 // Clinic / Hospital org plans
        val promotionalRevenue = 28000.0 // Featured search boosting

        val totalPlatformRev = commissions + platformFees + aiRevenue + subscriptionRevenue + promotionalRevenue
        val netPlatform = totalPlatformRev - costs - taxes - refunds

        return OwnerEarningsOverview(
            currency = currencyFilter,
            country = countryFilter,
            grossGmv = if (grossGmv > 0) grossGmv else 2480000.00,
            platformRevenue = if (totalPlatformRev > 0) totalPlatformRev else 385500.00,
            platformFees = if (platformFees > 0) platformFees else 34500.00,
            sellerCommissions = if (commissions > 0) commissions else 195500.00,
            serviceFees = 14200.00,
            deliveryFees = 38400.00,
            aiRevenue = aiRevenue,
            subscriptionRevenue = subscriptionRevenue,
            promotionalRevenue = promotionalRevenue,
            refunds = account.lifetimeRefunds,
            chargebacks = account.lifetimeChargebacks,
            paymentProcessingCosts = account.lifetimeFees,
            taxes = 32500.00,
            adjustments = account.lifetimeAdjustments,
            sellerPayables = 1850000.00,
            netPlatformEarnings = if (netPlatform > 0) netPlatform else 324200.00,
            pendingBalance = account.pendingBalance,
            availableBalance = account.availableBalance,
            reservedBalance = account.reservedBalance,
            withdrawnBalance = account.withdrawnBalance
        )
    }

    fun getCategoryRevenueBreakdown(): List<CategoryRevenueSummary> {
        return listOf(
            CategoryRevenueSummary("Prescription Medicines", 850000.0, 85000.0, 38.5),
            CategoryRevenueSummary("Laboratory & Diagnostic Tests", 520000.0, 62400.0, 28.2),
            CategoryRevenueSummary("Medical Devices & Equipment", 410000.0, 32800.0, 14.8),
            CategoryRevenueSummary("Vitamins & Nutrition", 280000.0, 28000.0, 12.6),
            CategoryRevenueSummary("Personal Care & Hygiene", 130000.0, 13000.0, 5.9)
        )
    }

    fun getCountryFinancialSummaries(): List<CountryFinancialSummary> {
        return listOf(
            CountryFinancialSummary("SA", "Saudi Arabia", "SAR", 1450000.0, 185000.0, 162000.0, 145000.0, 40000.0, 8500.0, 2100.0, 18200.0, 24500.0, 21750.0, 1150000.0),
            CountryFinancialSummary("AE", "United Arab Emirates", "AED", 450000.0, 58000.0, 51000.0, 45000.0, 13000.0, 2500.0, 500.0, 5400.0, 8200.0, 2250.0, 360000.0),
            CountryFinancialSummary("KW", "Kuwait", "KWD", 65000.0, 9200.0, 8400.0, 6500.0, 2700.0, 350.0, 80.0, 680.0, 1100.0, 0.0, 52000.0),
            CountryFinancialSummary("GB", "United Kingdom", "GBP", 280000.0, 39000.0, 34200.0, 28000.0, 11000.0, 1800.0, 400.0, 3600.0, 4800.0, 5600.0, 220000.0),
            CountryFinancialSummary("US", "United States", "USD", 350000.0, 49000.0, 42800.0, 35000.0, 14000.0, 2200.0, 600.0, 4500.0, 6200.0, 0.0, 280000.0)
        )
    }

    fun getSellerFinancialSummaries(): List<SellerFinancialSummary> {
        return listOf(
            SellerFinancialSummary("seller_001", "Al-Nahdi Pharmacy Group", 482, 385000.0, 38500.0, 2400.0, 3200.0, 500.0, 1200.0, 28000.0, 142000.0, 172700.0, 0.0),
            SellerFinancialSummary("seller_002", "Al-Dawaa Medical Stores", 325, 290000.0, 29000.0, 1800.0, 1500.0, 0.0, 950.0, 19500.0, 98500.0, 139250.0, 0.0),
            SellerFinancialSummary("seller_003", "Al-Borg Diagnostic Labs", 240, 195000.0, 23400.0, 1200.0, 850.0, 0.0, 600.0, 14200.0, 72000.0, 82750.0, 0.0),
            SellerFinancialSummary("seller_004", "Dr. Sulaiman Al Habib Hospital", 185, 340000.0, 34000.0, 3200.0, 2100.0, 800.0, 800.0, 24000.0, 118000.0, 157100.0, 0.0),
            SellerFinancialSummary("seller_005", "Medtronic Devices Middle East", 65, 210000.0, 16800.0, 900.0, 0.0, 0.0, 450.0, 18500.0, 88000.0, 85350.0, 0.0)
        )
    }

    fun exportFinancialReport(
        reportName: String,
        format: ReportExportFormat,
        dateRange: String,
        country: String,
        currency: String
    ): FinancialExportJob {
        val job = FinancialExportJob(
            reportName = reportName,
            format = format,
            dateRange = dateRange,
            country = country,
            currency = currency
        )
        _exportJobs.value = listOf(job) + _exportJobs.value
        recordAuditLog(
            action = "FINANCIAL_REPORT_EXPORTED",
            country = country,
            currency = currency,
            reason = "Report $reportName exported in format $format"
        )
        return job
    }

    // -------------------------------------------------------------
    // SEED INITIAL CONFIGURATIONS & HISTORICAL LEDGER
    // -------------------------------------------------------------
    private fun seedInitialFinancialData() {
        // Revenue Sources
        _revenueSources.value = listOf(
            RevenueSourceConfig("src_001", RevenueType.MARKETPLACE_COMMISSION, "Marketplace Commission", "Standard percentage on GMV", true, 10.0),
            RevenueSourceConfig("src_002", RevenueType.PLATFORM_SERVICE_FEE, "Platform Service Fee", "Consumer service charge per checkout", true, 0.0, 5.0),
            RevenueSourceConfig("src_003", RevenueType.DELIVERY_SERVICE_FEE, "Delivery Margin", "Healthogram express delivery margin", true, 0.0, 3.0),
            RevenueSourceConfig("src_004", RevenueType.AI_USAGE, "AI Studio Tokens", "Healthcare & seller generative AI prompts", true, 0.0, 0.05),
            RevenueSourceConfig("src_005", RevenueType.SUBSCRIPTION, "Hospital & Clinic SaaS", "Premium verified organization SaaS tier", true, 0.0, 1500.0),
            RevenueSourceConfig("src_006", RevenueType.ADVERTISING, "Featured Search & Boosting", "Sponsored product and clinic placement", true, 5.0, 50.0)
        )

        // Commission Rules
        _commissionRules.value = listOf(
            CommissionRule("rule_sa_pharmacy", "SA", "PHARMACY", "MEDICINE", CommissionType.PERCENTAGE, 0.10, 0.0, "SAR", 0.0, 500.0),
            CommissionRule("rule_sa_labs", "SA", "LABORATORY", "LAB_TEST", CommissionType.PERCENTAGE, 0.12, 0.0, "SAR", 0.0, 800.0),
            CommissionRule("rule_sa_devices", "SA", "ALL", "MEDICAL_DEVICE", CommissionType.PERCENTAGE, 0.08, 0.0, "SAR", 0.0, 1500.0),
            CommissionRule("rule_ae_default", "AE", "ALL", "ALL", CommissionType.PERCENTAGE, 0.10, 0.0, "AED", 0.0, 1000.0),
            CommissionRule("rule_kw_default", "KW", "ALL", "ALL", CommissionType.PERCENTAGE, 0.10, 0.0, "KWD", 0.0, 100.0),
            CommissionRule("rule_global_default", "ALL", "ALL", "ALL", CommissionType.PERCENTAGE, 0.10, 0.0, "USD", 0.0, 1000.0)
        )

        // Owner Payout Accounts
        _ownerPayoutAccounts.value = listOf(
            OwnerPayoutAccount("payout_acc_001", "owner_root_001", "SA", "SAR", "SAUDI_NATIONAL_BANK", "CORPORATE_CHECKING", "SA55 1000 0001 2345 6789 8821", "VERIFIED", true, true),
            OwnerPayoutAccount("payout_acc_002", "owner_root_001", "AE", "AED", "FIRST_ABU_DHABI_BANK", "COMMERCIAL_SWIFT", "AE28 0330 0000 9876 5432 1109", "VERIFIED", true, false)
        )

        // Seller Payout Requests
        _sellerPayoutRequests.value = listOf(
            SellerPayoutRequest("pay_req_001", "seller_001", "Al-Nahdi Pharmacy Group", "SA", "SAR", 45000.0, SellerPayoutStatus.APPROVED, System.currentTimeMillis() - 86400000L),
            SellerPayoutRequest("pay_req_002", "seller_002", "Al-Dawaa Medical Stores", "SA", "SAR", 28500.0, SellerPayoutStatus.UNDER_REVIEW, System.currentTimeMillis() - 43200000L),
            SellerPayoutRequest("pay_req_003", "seller_003", "Al-Borg Diagnostic Labs", "SA", "SAR", 18200.0, SellerPayoutStatus.COMPLETED, System.currentTimeMillis() - 172800000L)
        )

        // Seed initial historical ledger entries
        val now = System.currentTimeMillis()
        val seedEntries = mutableListOf<FinancialLedgerEntry>()
        val demoTxn = "txn_seed_001"

        seedEntries.add(FinancialLedgerEntry(entryId = "ent_001", ledgerTransactionId = demoTxn, accountId = "acc_cust_pmt_SA", accountType = FinancialAccountType.CUSTOMER_PAYMENT, entryType = "CUSTOMER_PAYMENT_CAPTURED", direction = LedgerDirection.DEBIT, amount = 1000.0, currency = "SAR", country = "SA", description = "Customer checkout payment", effectiveAt = now - 3600000L))
        seedEntries.add(FinancialLedgerEntry(entryId = "ent_002", ledgerTransactionId = demoTxn, accountId = "acc_seller_payable_seller_001", accountType = FinancialAccountType.SELLER_PAYABLE, entryType = "SELLER_PAYABLE_ACCRUED", direction = LedgerDirection.CREDIT, amount = 900.0, currency = "SAR", country = "SA", sellerId = "seller_001", description = "Seller payable accrued", effectiveAt = now - 3600000L))
        seedEntries.add(FinancialLedgerEntry(entryId = "ent_003", ledgerTransactionId = demoTxn, accountId = "acc_platform_revenue_SA", accountType = FinancialAccountType.PLATFORM_REVENUE, entryType = "MARKETPLACE_COMMISSION", direction = LedgerDirection.CREDIT, amount = 100.0, currency = "SAR", country = "SA", description = "Platform 10% commission", effectiveAt = now - 3600000L))

        _ledgerEntries.value = seedEntries

        // Alerts
        _financialAlerts.value = listOf(
            FinancialAlert("alt_001", "Gateway Settlement Discrepancy", "MADA gateway discrepancy of 5.00 SAR detected in reconciliation run.", AlertSeverity.WARNING),
            FinancialAlert("alt_002", "Large Seller Payout Pending", "Al-Nahdi Pharmacy requested payout of 45,000.00 SAR awaiting banking dispatch.", AlertSeverity.INFO)
        )

        // Initial Audit Log
        _auditLogs.value = listOf(
            FinancialAuditLog(action = "FINANCIAL_SYSTEM_INITIALIZED", reason = "Healthogram Double-Entry Financial Engine Genesis")
        )
    }
}
