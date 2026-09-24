package com.example.healthogram.finance

/**
 * Healthogram Step 18: Custom Actions & Backend Service Callables.
 * Implements the full suite of client actions and cloud function adapters
 * for secure financial ledger operations, owner withdrawals, seller payouts,
 * and financial reporting.
 */
object FinanceCustomActions {

    private val engine = FinancialLedgerEngine.getInstance()

    fun getOwnerFinancialSummary(
        country: String = "ALL",
        currency: String = "SAR"
    ): OwnerEarningsOverview {
        return engine.getOwnerEarningsOverview(country, currency)
    }

    fun getOwnerBalance(): OwnerFinancialAccount {
        return engine.ownerAccount.value
    }

    fun getOwnerLedger(): List<FinancialLedgerEntry> {
        return engine.ledgerEntries.value
    }

    fun getOwnerSellerPayouts(): List<SellerPayoutRequest> {
        return engine.sellerPayoutRequests.value
    }

    fun getOwnerWithdrawals(): List<OwnerWithdrawalRequest> {
        return engine.ownerWithdrawalRequests.value
    }

    fun createOwnerWithdrawalRequest(
        ownerUid: String,
        amount: Double,
        payoutAccountId: String,
        pin: String,
        currency: String = "SAR",
        country: String = "SA"
    ): OwnerWithdrawalRequest {
        return engine.requestOwnerWithdrawal(
            ownerUid = ownerUid,
            amount = amount,
            payoutAccountId = payoutAccountId,
            pin = pin,
            currency = currency,
            country = country
        )
    }

    fun getWithdrawalStatus(withdrawalId: String): OwnerWithdrawalStatus? {
        return engine.ownerWithdrawalRequests.value.firstOrNull { it.withdrawalId == withdrawalId }?.status
    }

    fun cancelOwnerWithdrawal(
        withdrawalId: String,
        reason: String,
        actorUid: String = "owner_root_001"
    ): OwnerWithdrawalRequest {
        return engine.failOrCancelOwnerWithdrawal(withdrawalId, reason, actorUid)
    }

    fun completeOwnerWithdrawal(
        withdrawalId: String,
        actorUid: String = "owner_root_001"
    ): OwnerWithdrawalRequest {
        return engine.completeOwnerWithdrawal(withdrawalId, actorUid)
    }

    fun approveSellerPayout(
        payoutId: String,
        actorUid: String = "owner_root_001"
    ): SellerPayoutRequest {
        return engine.approveSellerPayout(payoutId, actorUid)
    }

    fun completeSellerPayout(
        payoutId: String,
        actorUid: String = "owner_root_001"
    ): SellerPayoutRequest {
        return engine.completeSellerPayout(payoutId, actorUid)
    }

    fun runFinancialReconciliation(
        provider: String = "ALL_GATEWAYS",
        country: String = "SA"
    ): FinancialReconciliationRun {
        return engine.runFinancialReconciliation(provider, country)
    }

    fun getCountryFinancialSummary(): List<CountryFinancialSummary> {
        return engine.getCountryFinancialSummaries()
    }

    fun getSellerFinancialSummary(): List<SellerFinancialSummary> {
        return engine.getSellerFinancialSummaries()
    }

    fun getRevenueByCategory(): List<CategoryRevenueSummary> {
        return engine.getCategoryRevenueBreakdown()
    }

    fun getFinancialAlerts(): List<FinancialAlert> {
        return engine.financialAlerts.value
    }

    fun exportFinancialReport(
        reportName: String,
        format: ReportExportFormat,
        dateRange: String,
        country: String,
        currency: String
    ): FinancialExportJob {
        return engine.exportFinancialReport(reportName, format, dateRange, country, currency)
    }

    fun reverseLedgerEntry(
        entryId: String,
        actorUid: String,
        reason: String
    ): FinancialLedgerEntry {
        return engine.reverseLedgerEntry(entryId, actorUid, reason)
    }

    fun postOrderTransaction(
        orderId: String,
        paymentId: String,
        customerId: String,
        sellerId: String,
        country: String = "SA",
        currency: String = "SAR",
        productSubtotal: Double,
        deliveryFee: Double = 0.0,
        platformServiceFee: Double = 0.0,
        taxAmount: Double = 0.0,
        paymentGatewayFee: Double = 0.0,
        sellerType: String = "PHARMACY",
        productCategory: String = "MEDICINE",
        idempotencyKey: String? = null
    ): List<FinancialLedgerEntry> {
        return engine.postCustomerOrderTransaction(
            orderId = orderId,
            paymentId = paymentId,
            customerId = customerId,
            sellerId = sellerId,
            country = country,
            currency = currency,
            productSubtotal = productSubtotal,
            deliveryFee = deliveryFee,
            platformServiceFee = platformServiceFee,
            taxAmount = taxAmount,
            paymentGatewayFee = paymentGatewayFee,
            sellerType = sellerType,
            productCategory = productCategory,
            idempotencyKey = idempotencyKey
        )
    }

    fun processRefund(
        paymentId: String,
        orderId: String,
        sellerId: String,
        customerId: String,
        refundAmount: Double,
        currency: String = "SAR",
        reason: String
    ): FinancialRefund {
        return engine.processRefund(
            paymentId = paymentId,
            orderId = orderId,
            sellerId = sellerId,
            customerId = customerId,
            refundAmount = refundAmount,
            currency = currency,
            reason = reason
        )
    }

    fun runFinancialIntegrityCheck(): FinancialIntegrityCheck {
        return engine.runFinancialIntegrityCheck()
    }
}
