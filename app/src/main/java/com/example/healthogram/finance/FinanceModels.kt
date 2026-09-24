package com.example.healthogram.finance

import java.util.UUID

/**
 * Healthogram Step 18: Owner Earnings, Financial Ledger & Financial Management Models.
 */

enum class LedgerDirection {
    DEBIT,
    CREDIT
}

enum class LedgerEntryStatus {
    PENDING,
    POSTED,
    REVERSED,
    VOIDED
}

enum class FinancialAccountType {
    PLATFORM_REVENUE,
    PLATFORM_FEES,
    SELLER_PAYABLE,
    DELIVERY_PAYABLE,
    PAYMENT_PROCESSING_COST,
    TAX_PAYABLE,
    REFUND_LIABILITY,
    CHARGEBACK_LIABILITY,
    OWNER_WITHDRAWAL,
    CUSTOMER_PAYMENT,
    ADJUSTMENT,
    PROMOTIONAL_CREDIT
}

enum class RevenueType {
    MARKETPLACE_COMMISSION,
    SELLER_LISTING_FEE,
    SELLER_SERVICE_FEE,
    PLATFORM_SERVICE_FEE,
    DELIVERY_SERVICE_FEE,
    SUBSCRIPTION,
    AI_USAGE,
    PROMOTIONAL_SERVICE,
    ADVERTISING,
    FEATURED_LISTING,
    OTHER_APPROVED_REVENUE
}

enum class CommissionType {
    PERCENTAGE,
    FIXED,
    PERCENTAGE_PLUS_FIXED
}

enum class SellerPayoutStatus {
    REQUESTED,
    UNDER_REVIEW,
    APPROVED,
    PROCESSING,
    SENT,
    COMPLETED,
    FAILED,
    CANCELLED,
    HELD,
    REVERSED
}

enum class OwnerWithdrawalStatus {
    REQUESTED,
    UNDER_REVIEW,
    APPROVED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    HELD
}

enum class RefundStatus {
    REQUESTED,
    APPROVED,
    PROCESSING,
    COMPLETED,
    FAILED,
    REVERSED
}

enum class ChargebackStatus {
    RECEIVED,
    UNDER_REVIEW,
    EVIDENCE_REQUIRED,
    EVIDENCE_SUBMITTED,
    WON,
    LOST,
    REVERSED
}

enum class AdjustmentType {
    TAX,
    WITHHOLDING,
    REGULATORY_ADJUSTMENT,
    ROUNDING_ADJUSTMENT,
    CURRENCY_ADJUSTMENT,
    PROMOTIONAL_ADJUSTMENT,
    MANUAL_ADJUSTMENT
}

enum class FinancialTransactionStatus {
    INITIATED,
    AUTHORIZED,
    CAPTURED,
    SETTLED,
    PENDING,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED,
    CHARGEBACK,
    DISPUTED,
    REVERSED
}

enum class ReconciliationStatus {
    MATCHED,
    MISMATCH,
    PENDING,
    RESOLVED
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

enum class DisputeStatus {
    OPEN,
    UNDER_REVIEW,
    EVIDENCE_REQUIRED,
    RESOLVED,
    REJECTED,
    ESCALATED
}

enum class ReportExportFormat {
    CSV,
    XLSX,
    PDF
}

enum class RiskStatus {
    LOW,
    MEDIUM,
    HIGH,
    BLOCKED
}

enum class TimePeriodFilter(val title: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    DAYS_7("7 Days"),
    DAYS_30("30 Days"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom")
}

// -------------------------------------------------------------
// 1. DOUBLE-ENTRY FINANCIAL LEDGER ENTRY
// -------------------------------------------------------------
data class FinancialLedgerEntry(
    val entryId: String = "ent_${UUID.randomUUID().toString().take(10)}",
    val ledgerTransactionId: String = "txn_${UUID.randomUUID().toString().take(10)}",
    val accountId: String,
    val accountType: FinancialAccountType,
    val entryType: String,
    val direction: LedgerDirection,
    val amount: Double,
    val currency: String = "SAR",
    val country: String = "SA",
    val sourceType: String = "ORDER",
    val sourceId: String = "",
    val orderId: String? = null,
    val paymentId: String? = null,
    val refundId: String? = null,
    val chargebackId: String? = null,
    val payoutId: String? = null,
    val sellerId: String? = null,
    val customerId: String? = null,
    val description: String = "",
    val status: LedgerEntryStatus = LedgerEntryStatus.POSTED,
    val effectiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system_financial_engine",
    val metadataVersion: Int = 1
)

// -------------------------------------------------------------
// 2. FINANCIAL ACCOUNTS & OWNER ACCOUNTS
// -------------------------------------------------------------
data class FinancialAccount(
    val accountId: String,
    val accountType: FinancialAccountType,
    val ownerType: String = "PLATFORM", // PLATFORM, SELLER, DELIVERY_PROVIDER, TAX_AUTHORITY
    val ownerId: String = "healthogram_platform",
    val country: String = "SA",
    val currency: String = "SAR",
    val status: String = "ACTIVE",
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val reservedBalance: Double = 0.0,
    val lifetimeCredit: Double = 0.0,
    val lifetimeDebit: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class OwnerFinancialAccount(
    val ownerAccountId: String = "owner_fin_acc_001",
    val currency: String = "SAR",
    val country: String = "SA",
    val pendingBalance: Double = 0.0,
    val availableBalance: Double = 0.0,
    val reservedBalance: Double = 0.0,
    val withdrawnBalance: Double = 0.0,
    val lifetimeRevenue: Double = 0.0,
    val lifetimeFees: Double = 0.0,
    val lifetimeRefunds: Double = 0.0,
    val lifetimeChargebacks: Double = 0.0,
    val lifetimeAdjustments: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// 3. REVENUE SOURCES & COMMISSION RULES
// -------------------------------------------------------------
data class RevenueSourceConfig(
    val sourceId: String,
    val revenueType: RevenueType,
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val defaultFeePercentage: Double = 0.0,
    val defaultFixedFee: Double = 0.0,
    val currency: String = "SAR",
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "owner_root_001"
)

data class CommissionRule(
    val ruleId: String = "com_rule_${UUID.randomUUID().toString().take(8)}",
    val country: String = "SA",
    val sellerType: String = "ALL", // INDIVIDUAL, PHARMACY, CLINIC, HOSPITAL, LAB, ALL
    val productCategory: String = "ALL", // MEDICINE, LAB_TEST, MEDICAL_DEVICE, SUPPLEMENTS, ALL
    val commissionType: CommissionType = CommissionType.PERCENTAGE,
    val commissionRate: Double = 0.10, // 10%
    val fixedFee: Double = 0.0,
    val currency: String = "SAR",
    val minimumFee: Double = 0.0,
    val maximumFee: Double = 1000.0,
    val effectiveFrom: Long = System.currentTimeMillis() - 86400000L * 30,
    val effectiveUntil: Long = System.currentTimeMillis() + 86400000L * 365,
    val status: String = "ACTIVE",
    val createdBy: String = "owner_root_001",
    val updatedBy: String = "owner_root_001",
    val createdAt: Long = System.currentTimeMillis() - 86400000L * 30,
    val updatedAt: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// 4. SELLER PAYOUTS
// -------------------------------------------------------------
data class SellerPayoutAccount(
    val sellerId: String,
    val bankName: String,
    val accountHolder: String,
    val ibanMasked: String,
    val swiftCode: String,
    val country: String = "SA",
    val currency: String = "SAR",
    val verified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SellerPayoutRequest(
    val payoutId: String = "pay_${UUID.randomUUID().toString().take(10)}",
    val sellerId: String,
    val sellerName: String,
    val country: String = "SA",
    val currency: String = "SAR",
    val requestedAmount: Double,
    val status: SellerPayoutStatus = SellerPayoutStatus.REQUESTED,
    val requestedAt: Long = System.currentTimeMillis(),
    val reviewedBy: String? = null,
    val approvedAt: Long? = null,
    val processedAt: Long? = null,
    val completedAt: Long? = null,
    val failureReason: String? = null,
    val holdReason: String? = null,
    val batchId: String? = null
)

// -------------------------------------------------------------
// 5. OWNER WITHDRAWAL & LIMITS
// -------------------------------------------------------------
data class OwnerWithdrawalRequest(
    val withdrawalId: String = "wd_${UUID.randomUUID().toString().take(10)}",
    val ownerUid: String,
    val country: String = "SA",
    val currency: String = "SAR",
    val requestedAmount: Double,
    val availableBalanceBefore: Double,
    val availableBalanceAfter: Double,
    val status: OwnerWithdrawalStatus = OwnerWithdrawalStatus.REQUESTED,
    val payoutMethod: String = "BANK_WIRE_SWIFT",
    val destinationReference: String = "IBAN **** 8821 (Saudi National Bank)",
    val riskStatus: RiskStatus = RiskStatus.LOW,
    val approvalStatus: String = "PENDING_MFA",
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val processedAt: Long? = null,
    val completedAt: Long? = null,
    val failureReason: String? = null,
    val createdBy: String = ownerUid,
    val updatedAt: Long = System.currentTimeMillis()
)

data class WithdrawalRule(
    val ruleId: String = "wd_rule_global",
    val country: String = "SA",
    val currency: String = "SAR",
    val minimumWithdrawal: Double = 100.0,
    val maximumWithdrawal: Double = 500000.0,
    val dailyLimit: Double = 250000.0,
    val weeklyLimit: Double = 1000000.0,
    val monthlyLimit: Double = 3000000.0,
    val approvalRequired: Boolean = true,
    val mfaRequired: Boolean = true,
    val riskReviewRequired: Boolean = true,
    val cooldownPeriodHours: Int = 12,
    val updatedAt: Long = System.currentTimeMillis()
)

data class OwnerPayoutAccount(
    val accountId: String = "payout_acc_001",
    val ownerUid: String = "owner_root_001",
    val country: String = "SA",
    val currency: String = "SAR",
    val provider: String = "SAUDI_NATIONAL_BANK",
    val accountType: String = "CORPORATE_CHECKING",
    val maskedDestination: String = "SA55 1000 0001 2345 6789 8821",
    val status: String = "VERIFIED",
    val verified: Boolean = true,
    val defaultAccount: Boolean = true,
    val createdAt: Long = System.currentTimeMillis() - 86400000L * 60,
    val updatedAt: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// 6. REFUNDS, CHARGEBACKS, COSTS, DELIVERY & TAX
// -------------------------------------------------------------
data class FinancialRefund(
    val refundId: String = "ref_${UUID.randomUUID().toString().take(10)}",
    val paymentId: String,
    val orderId: String,
    val sellerId: String,
    val customerId: String,
    val refundAmount: Double,
    val currency: String = "SAR",
    val reason: String = "Customer cancelled before shipment",
    val fundingSource: String = "SELLER_PAYABLE",
    val platformShare: Double = 0.0, // refunded commission
    val sellerShare: Double = refundAmount,
    val deliveryShare: Double = 0.0,
    val taxAdjustment: Double = 0.0,
    val status: RefundStatus = RefundStatus.COMPLETED,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = System.currentTimeMillis()
)

data class FinancialChargeback(
    val chargebackId: String = "chg_${UUID.randomUUID().toString().take(10)}",
    val paymentId: String,
    val orderId: String,
    val customerId: String,
    val sellerId: String,
    val amount: Double,
    val currency: String = "SAR",
    val reason: String = "Cardholder claimed fraud / unrecognized charge",
    val status: ChargebackStatus = ChargebackStatus.RECEIVED,
    val evidenceDeadline: Long = System.currentTimeMillis() + 86400000L * 7,
    val receivedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class PaymentProcessingCostRecord(
    val costId: String = "cost_${UUID.randomUUID().toString().take(10)}",
    val paymentId: String,
    val provider: String = "STC_PAY", // MADA, STC_PAY, APPLE_PAY, STRIPE, TABBY
    val country: String = "SA",
    val currency: String = "SAR",
    val grossPayment: Double,
    val providerFee: Double,
    val fixedFee: Double = 1.0,
    val percentageFee: Double = 0.015, // 1.5%
    val taxOnFee: Double = 0.15 * providerFee,
    val totalCost: Double = providerFee + (0.15 * providerFee),
    val createdAt: Long = System.currentTimeMillis()
)

data class DeliveryFinancialEvent(
    val eventId: String = "del_ev_${UUID.randomUUID().toString().take(10)}",
    val orderId: String,
    val shipmentId: String,
    val provider: String = "HEALTHOGRAM_EXPRESS",
    val country: String = "SA",
    val currency: String = "SAR",
    val customerDeliveryFee: Double = 25.0,
    val sellerDeliveryContribution: Double = 0.0,
    val platformDeliverySubsidy: Double = 0.0,
    val deliveryProviderCost: Double = 20.0,
    val netPlatformDeliveryMargin: Double = 5.0,
    val eventType: String = "DELIVERY_COMPLETED",
    val createdAt: Long = System.currentTimeMillis()
)

data class FinancialTaxEntry(
    val taxEntryId: String = "tax_${UUID.randomUUID().toString().take(10)}",
    val transactionId: String,
    val country: String = "SA",
    val currency: String = "SAR",
    val taxableAmount: Double,
    val taxRate: Double = 0.15, // 15% VAT
    val taxAmount: Double = taxableAmount * taxRate,
    val taxAuthority: String = "ZATCA_SAUDI_ARABIA",
    val status: String = "ACCRUED",
    val createdAt: Long = System.currentTimeMillis()
)

data class FinancialAdjustment(
    val adjustmentId: String = "adj_${UUID.randomUUID().toString().take(10)}",
    val adjustmentType: AdjustmentType,
    val amount: Double,
    val currency: String = "SAR",
    val targetAccount: FinancialAccountType,
    val reason: String,
    val supportingReference: String,
    val authorizedBy: String = "owner_root_001",
    val approved: Boolean = true,
    val approvedBy: String? = "owner_root_001",
    val createdAt: Long = System.currentTimeMillis()
)

data class FinancialIdempotencyKey(
    val key: String,
    val operationType: String,
    val sourceId: String,
    val requestHash: String,
    val status: String = "COMPLETED",
    val resultReference: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L
)

// -------------------------------------------------------------
// 7. RECONCILIATION & AUDIT & ALERTS
// -------------------------------------------------------------
data class ReconciliationMismatchItem(
    val mismatchId: String = "mis_${UUID.randomUUID().toString().take(8)}",
    val transactionRef: String,
    val provider: String,
    val internalAmount: Double,
    val externalAmount: Double,
    val discrepancyAmount: Double,
    val reason: String,
    val status: ReconciliationStatus = ReconciliationStatus.MISMATCH
)

data class FinancialReconciliationRun(
    val runId: String = "rec_${UUID.randomUUID().toString().take(10)}",
    val provider: String = "ALL_GATEWAYS",
    val country: String = "SA",
    val currency: String = "SAR",
    val totalRecordsChecked: Int = 1250,
    val matchedRecords: Int = 1248,
    val mismatchedRecords: Int = 2,
    val pendingRecords: Int = 0,
    val resolvedRecords: Int = 0,
    val discrepancies: List<ReconciliationMismatchItem> = emptyList(),
    val status: ReconciliationStatus = ReconciliationStatus.MISMATCH,
    val runAt: Long = System.currentTimeMillis()
)

data class FinancialAlert(
    val alertId: String = "alt_${UUID.randomUUID().toString().take(8)}",
    val title: String,
    val message: String,
    val severity: AlertSeverity = AlertSeverity.WARNING,
    val metricCategory: String = "RECONCILIATION",
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)

data class FinancialRiskEvent(
    val eventId: String = "risk_${UUID.randomUUID().toString().take(8)}",
    val entityType: String = "OWNER_WITHDRAWAL",
    val entityId: String,
    val score: Int = 25, // 0-100
    val riskStatus: RiskStatus = RiskStatus.LOW,
    val flags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class FinancialAuditLog(
    val logId: String = "fin_log_${UUID.randomUUID().toString().take(10)}",
    val actorUid: String = "owner_root_001",
    val actorRole: String = "PLATFORM_OWNER",
    val action: String,
    val transactionId: String? = null,
    val ledgerEntryId: String? = null,
    val withdrawalId: String? = null,
    val payoutId: String? = null,
    val sellerId: String? = null,
    val country: String = "SA",
    val currency: String = "SAR",
    val amount: Double = 0.0,
    val oldStatus: String? = null,
    val newStatus: String? = null,
    val reason: String = "",
    val ipReference: String = "192.168.1.1",
    val deviceReference: String = "Authorized Healthogram Hardware Node",
    val mfaVerified: Boolean = true,
    val reauthenticated: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class FinancialIntegrityCheck(
    val checkId: String = "chk_${UUID.randomUUID().toString().take(8)}",
    val totalDebits: Double,
    val totalCredits: Double,
    val isBalanced: Boolean,
    val imbalanceDifference: Double,
    val status: String = "PASS",
    val checkedAt: Long = System.currentTimeMillis()
)

data class FinancialDispute(
    val disputeId: String = "disp_${UUID.randomUUID().toString().take(8)}",
    val disputeType: String = "SELLER_COMMISSION",
    val complainantId: String,
    val respondentId: String = "platform_admin",
    val orderId: String? = null,
    val disputedAmount: Double,
    val currency: String = "SAR",
    val reason: String,
    val status: DisputeStatus = DisputeStatus.OPEN,
    val resolutionNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class FinancialExportJob(
    val jobId: String = "job_${UUID.randomUUID().toString().take(8)}",
    val reportName: String,
    val format: ReportExportFormat,
    val requesterUid: String = "owner_root_001",
    val dateRange: String,
    val country: String,
    val currency: String,
    val fileUri: String = "financial_reports_private/2026/09/${UUID.randomUUID()}.csv",
    val status: String = "COMPLETED",
    val createdAt: Long = System.currentTimeMillis()
)

data class FinancialReportSnapshot(
    val reportId: String = "rep_${UUID.randomUUID().toString().take(8)}",
    val periodStart: Long,
    val periodEnd: Long,
    val country: String = "SA",
    val currency: String = "SAR",
    val grossGmv: Double,
    val platformRevenue: Double,
    val netPlatformEarnings: Double,
    val commissions: Double,
    val platformFees: Double,
    val refunds: Double,
    val chargebacks: Double,
    val paymentCosts: Double,
    val deliveryCosts: Double,
    val taxes: Double,
    val sellerPayouts: Double,
    val ownerWithdrawals: Double,
    val generatedAt: Long = System.currentTimeMillis(),
    val generatedBy: String = "owner_root_001",
    val version: Int = 1
)

// -------------------------------------------------------------
// 8. HIGH-LEVEL AGGREGATED METRICS
// -------------------------------------------------------------
data class OwnerEarningsOverview(
    val currency: String = "SAR",
    val country: String = "SA",
    val grossGmv: Double = 0.0,
    val platformRevenue: Double = 0.0,
    val platformFees: Double = 0.0,
    val sellerCommissions: Double = 0.0,
    val serviceFees: Double = 0.0,
    val deliveryFees: Double = 0.0,
    val aiRevenue: Double = 0.0,
    val subscriptionRevenue: Double = 0.0,
    val promotionalRevenue: Double = 0.0,
    val refunds: Double = 0.0,
    val chargebacks: Double = 0.0,
    val paymentProcessingCosts: Double = 0.0,
    val taxes: Double = 0.0,
    val adjustments: Double = 0.0,
    val sellerPayables: Double = 0.0,
    val netPlatformEarnings: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val availableBalance: Double = 0.0,
    val reservedBalance: Double = 0.0,
    val withdrawnBalance: Double = 0.0
)

data class SellerFinancialSummary(
    val sellerId: String,
    val sellerName: String,
    val orderCount: Int,
    val grossSales: Double,
    val commissionsDeducted: Double,
    val feesDeducted: Double,
    val refundsDeducted: Double,
    val chargebacksDeducted: Double,
    val deliveryAdjustments: Double,
    val pendingBalance: Double,
    val availableBalance: Double,
    val paidBalance: Double,
    val heldBalance: Double,
    val currency: String = "SAR"
)

data class CountryFinancialSummary(
    val countryCode: String,
    val countryName: String,
    val currency: String,
    val grossGmv: Double,
    val platformRevenue: Double,
    val netEarnings: Double,
    val commissions: Double,
    val fees: Double,
    val refunds: Double,
    val chargebacks: Double,
    val paymentCosts: Double,
    val deliveryCosts: Double,
    val taxes: Double,
    val sellerPayouts: Double
)

data class CategoryRevenueSummary(
    val categoryName: String,
    val grossSales: Double,
    val commissionEarned: Double,
    val percentageOfRevenue: Double,
    val currency: String = "SAR"
)
