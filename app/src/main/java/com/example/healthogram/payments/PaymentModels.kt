package com.example.healthogram.payments

import java.util.UUID

/**
 * Healthogram Country-wise Payment System & Gateway Architecture Models.
 * Strictly uses integer minor units (e.g. cents/halalas) to prevent floating-point imprecision.
 * Enforces strict PCI-DSS scope minimization: NO raw card numbers, CVV, or PINs stored.
 */

// ==========================================
// 1. CURRENCIES & LOCALIZATION
// ==========================================

data class CurrencyConfig(
    val currencyCode: String,        // e.g. "SAR", "USD", "EUR", "AED", "GBP", "INR"
    val currencyName: String,        // e.g. "Saudi Riyal", "US Dollar"
    val symbol: String,              // e.g. "ر.س", "$", "€", "د.إ", "£", "₹"
    val minorUnit: Int = 100,        // 100 for 2 decimal places, 1 for JPY
    val decimalDigits: Int = 2,
    val active: Boolean = true,
    val supportedCountries: List<String> = listOf("SA", "US", "AE", "GB", "DE", "IN"),
    val displayFormat: String = "{symbol} {amount}",
    val roundingMode: String = "HALF_EVEN"
) {
    /**
     * Formats integer minor units into human readable localized string.
     * e.g., 2499 with SAR -> "SAR 24.99" or "ر.س 24.99"
     */
    fun formatMinor(amountMinor: Long): String {
        val major = amountMinor / minorUnit
        val minor = kotlin.math.abs(amountMinor % minorUnit)
        val formattedNumber = if (decimalDigits > 0) {
            val paddedMinor = minor.toString().padStart(decimalDigits, '0')
            "$major.$paddedMinor"
        } else {
            major.toString()
        }
        return displayFormat.replace("{symbol}", symbol).replace("{amount}", formattedNumber)
    }
}

// ==========================================
// 2. COUNTRY PAYMENT CONFIGURATION
// ==========================================

enum class CountryPaymentStatus {
    ACTIVE,
    INACTIVE,
    MAINTENANCE,
    BETA,
    COMING_SOON
}

enum class PlatformFeeType {
    PERCENTAGE,
    FIXED,
    PERCENTAGE_PLUS_FIXED
}

data class CountryPaymentConfig(
    val countryCode: String,                         // e.g. "SA", "US", "AE", "GB", "IN"
    val countryName: String,                         // e.g. "Saudi Arabia"
    val currencyCode: String,                        // e.g. "SAR"
    val currencySymbol: String,                      // e.g. "ر.س"
    val currencyDecimals: Int = 2,
    val timezone: String = "Asia/Riyadh",
    val paymentEnabled: Boolean = true,
    val marketplacePaymentEnabled: Boolean = true,
    val sellerPayoutEnabled: Boolean = true,
    val ownerWithdrawalEnabled: Boolean = true,
    val supportedGateways: List<String> = listOf("stripe", "local_mada"),
    val defaultGateway: String = "stripe",
    val fallbackGateway: String? = "local_mada",
    val supportedPaymentMethods: List<String> = listOf("card", "apple_pay", "mada", "google_pay"),
    val minimumOrderAmountMinor: Long = 500L,        // e.g. SAR 5.00
    val maximumOrderAmountMinor: Long = 10000000L,   // e.g. SAR 100,000.00
    val minimumRefundAmountMinor: Long = 100L,       // e.g. SAR 1.00
    val sellerPayoutFrequency: String = "WEEKLY",    // DAILY, WEEKLY, MONTHLY
    val payoutDelayDays: Int = 7,                    // Hold period for risk & returns
    val platformFeeType: PlatformFeeType = PlatformFeeType.PERCENTAGE_PLUS_FIXED,
    val platformFeePercent: Double = 5.0,            // 5%
    val platformFeeFixedMinor: Long = 100L,          // SAR 1.00
    val taxEnabled: Boolean = true,
    val taxInclusive: Boolean = true,
    val taxName: String = "VAT",
    val taxRatePercent: Double = 15.0,               // 15% in KSA, 5% UAE, 20% UK
    val taxRegistrationRequired: Boolean = true,
    val internationalMarketplaceEnabled: Boolean = false, // Mandate: initially FALSE
    val shippingPaymentEnabled: Boolean = true,
    val walletEnabled: Boolean = true,
    val buyNowPayLaterEnabled: Boolean = false,
    val threeDsRequired: Boolean = true,
    val riskLevel: String = "STANDARD",
    val status: CountryPaymentStatus = CountryPaymentStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 3. PAYMENT METHODS & TYPES
// ==========================================

enum class PaymentMethodType {
    CARD,
    APPLE_PAY,
    GOOGLE_PAY,
    BANK_TRANSFER,
    LOCAL_WALLET,
    LOCAL_PAYMENT_METHOD,    // e.g. Mada in Saudi, Benefit in Bahrain, Fawry in Egypt
    CASH_ON_DELIVERY,
    BUY_NOW_PAY_LATER
}

data class PaymentMethodItem(
    val paymentMethodId: String,
    val countryCode: String,
    val gateway: String,
    val methodType: PaymentMethodType,
    val displayName: String,
    val enabled: Boolean = true,
    val customerEnabled: Boolean = true,
    val sellerEnabled: Boolean = true,
    val currencyCodes: List<String> = listOf("SAR", "USD", "AED", "EUR"),
    val minimumAmountMinor: Long = 100L,
    val maximumAmountMinor: Long = 10000000L,
    val requiresRedirect: Boolean = false,
    val requiresThreeDs: Boolean = true,
    val sortOrder: Int = 0,
    val iconReference: String = "ic_card"
)

// ==========================================
// 4. PAYMENT STATE MACHINE & TRANSACTIONS
// ==========================================

enum class PaymentTransactionStatus {
    CREATED,
    PROCESSING,
    PAYMENT_PENDING,
    REQUIRES_ACTION,    // 3D Secure OTP verification
    AUTHORIZED,
    CAPTURED,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    PARTIALLY_REFUNDED,
    REFUNDED,
    DISPUTED,
    CHARGEBACK,
    REVERSED,
    SETTLED
}

enum class RiskAction {
    ALLOW,
    REVIEW,
    HOLD,
    DECLINE
}

data class PaymentTransaction(
    val paymentTransactionId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val customerUid: String,
    val sellerUid: String,
    val countryCode: String,
    val currencyCode: String,
    val gateway: String,
    val paymentMethod: PaymentMethodType,
    val gatewayCustomerId: String? = null,
    val gatewayPaymentId: String? = null,
    val gatewaySessionId: String? = null,
    val idempotencyKey: String,
    val subtotalMinor: Long,
    val shippingMinor: Long,
    val discountMinor: Long,
    val taxMinor: Long,
    val platformFeeMinor: Long,
    val sellerGrossMinor: Long,
    val sellerNetMinor: Long,
    val totalMinor: Long,
    val amountAuthorizedMinor: Long = 0L,
    val amountCapturedMinor: Long = 0L,
    val amountRefundedMinor: Long = 0L,
    val amountChargebackMinor: Long = 0L,
    val status: PaymentTransactionStatus = PaymentTransactionStatus.CREATED,
    val paymentStatusReason: String? = null,
    val gatewayStatus: String? = null,
    val failureCode: String? = null,
    val failureMessageSafe: String? = null,
    val riskStatus: RiskAction = RiskAction.ALLOW,
    val clientSecretToken: String? = null, // Ephemeral client secret for tokenized SDK
    val redirectActionUrl: String? = null, // For 3DS / Hosted page redirect
    val createdAt: Long = System.currentTimeMillis(),
    val authorizedAt: Long? = null,
    val capturedAt: Long? = null,
    val failedAt: Long? = null,
    val refundedAt: Long? = null,
    val settledAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class PaymentAttempt(
    val attemptId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val paymentTransactionId: String,
    val customerUid: String,
    val gateway: String,
    val paymentMethod: PaymentMethodType,
    val currency: String,
    val amountMinor: Long,
    val idempotencyKey: String,
    val attemptNumber: Int = 1,
    val status: PaymentTransactionStatus = PaymentTransactionStatus.CREATED,
    val failureCode: String? = null,
    val safeFailureReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

// ==========================================
// 5. IMMUTABLE FINANCIAL LEDGER & BALANCES
// ==========================================

enum class SellerLedgerEntryType {
    SALE,
    PLATFORM_FEE,
    PAYMENT_FEE,
    SHIPPING_ADJUSTMENT,
    TAX_ADJUSTMENT,
    REFUND,
    CHARGEBACK,
    PAYOUT,
    PAYOUT_REVERSAL,
    MANUAL_ADJUSTMENT
}

data class SellerLedgerEntry(
    val ledgerEntryId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val orderId: String? = null,
    val paymentTransactionId: String? = null,
    val entryType: SellerLedgerEntryType,
    val amountMinor: Long, // Positive for credits, negative for debits
    val currencyCode: String,
    val balanceBeforeMinor: Long,
    val balanceAfterMinor: Long,
    val availableAt: Long, // Available after payout delay period
    val status: String = "POSTED",
    val referenceId: String? = null,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "SYSTEM_PAYMENT_ENGINE",
    val immutable: Boolean = true
)

data class SellerBalance(
    val sellerUid: String,
    val currencyCode: String,
    val pendingBalanceMinor: Long = 0L,
    val availableBalanceMinor: Long = 0L,
    val reservedBalanceMinor: Long = 0L,
    val lifetimeSalesMinor: Long = 0L,
    val lifetimeFeesMinor: Long = 0L,
    val lifetimeRefundsMinor: Long = 0L,
    val lifetimeChargebacksMinor: Long = 0L,
    val lifetimePayoutsMinor: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PayoutStatus {
    REQUESTED,
    RISK_REVIEW,
    APPROVED,
    PROCESSING,
    COMPLETED,
    FAILED,
    ON_HOLD
}

data class SellerPayout(
    val payoutId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val countryCode: String,
    val currencyCode: String,
    val amountMinor: Long,
    val payoutMethod: String = "IBAN_BANK_TRANSFER",
    val payoutProvider: String = "STRIPE_CONNECT",
    val providerPayoutId: String? = null,
    val status: PayoutStatus = PayoutStatus.REQUESTED,
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val processingAt: Long? = null,
    val completedAt: Long? = null,
    val failedAt: Long? = null,
    val failureReasonSafe: String? = null,
    val availableBalanceBefore: Long = 0L,
    val availableBalanceAfter: Long = 0L,
    val idempotencyKey: String,
    val riskReviewStatus: String = "PASSED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 6. OWNER FINANCIAL LEDGER & WITHDRAWAL
// ==========================================

enum class OwnerLedgerEntryType {
    MARKETPLACE_COMMISSION,
    SELLER_FEE,
    PAYMENT_SERVICE_FEE,
    DELIVERY_SERVICE_FEE,
    AI_PAID_SERVICE,
    OTHER_PLATFORM_FEE,
    REFUND_ADJUSTMENT,
    CHARGEBACK_ADJUSTMENT,
    TAX_ADJUSTMENT,
    GATEWAY_COST,
    OWNER_WITHDRAWAL
}

data class OwnerFinancialLedgerEntry(
    val entryId: String = UUID.randomUUID().toString(),
    val sourceType: String, // "MARKETPLACE_ORDER", "SELLER_FEE", "GATEWAY"
    val sourceId: String,
    val orderId: String? = null,
    val paymentTransactionId: String? = null,
    val countryCode: String,
    val currencyCode: String,
    val amountMinor: Long, // Positive for earnings, negative for payouts/refunds
    val entryType: OwnerLedgerEntryType,
    val status: String = "POSTED",
    val createdAt: Long = System.currentTimeMillis(),
    val immutable: Boolean = true
)

data class OwnerRevenueSummary(
    val currencyCode: String = "SAR",
    val grossPlatformRevenueMinor: Long = 0L,
    val marketplaceCommissionMinor: Long = 0L,
    val sellerFeesMinor: Long = 0L,
    val paymentProcessingCostsMinor: Long = 0L,
    val refundsDeductedMinor: Long = 0L,
    val chargebacksDeductedMinor: Long = 0L,
    val taxesAdjustedMinor: Long = 0L,
    val pendingRevenueMinor: Long = 0L,
    val availableRevenueMinor: Long = 0L,
    val netPlatformRevenueMinor: Long = 0L,
    val totalWithdrawnMinor: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

data class OwnerWithdrawal(
    val withdrawalId: String = UUID.randomUUID().toString(),
    val ownerUid: String,
    val countryCode: String,
    val currencyCode: String,
    val requestedAmountMinor: Long,
    val availableBalanceBefore: Long,
    val availableBalanceAfter: Long,
    val payoutMethod: String = "TREASURY_DIRECT_WIRE",
    val payoutReference: String? = null,
    val status: PayoutStatus = PayoutStatus.REQUESTED,
    val twoFactorVerified: Boolean = true,
    val riskReviewStatus: String = "APPROVED",
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val processingAt: Long? = null,
    val completedAt: Long? = null,
    val failedAt: Long? = null,
    val failureReasonSafe: String? = null,
    val idempotencyKey: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 7. REFUNDS, DISPUTES & CHARGEBACKS
// ==========================================

enum class RefundType {
    FULL,
    PARTIAL,
    SHIPPING,
    ITEM,
    PROMOTIONAL_ADJUSTMENT
}

enum class RefundStatus {
    REQUESTED,
    APPROVED,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class RefundRecord(
    val refundId: String = UUID.randomUUID().toString(),
    val paymentTransactionId: String,
    val orderId: String,
    val customerUid: String,
    val sellerUid: String,
    val amountMinor: Long,
    val currencyCode: String,
    val refundType: RefundType = RefundType.FULL,
    val reason: String,
    val gatewayRefundId: String? = null,
    val status: RefundStatus = RefundStatus.REQUESTED,
    val requestedBy: String,
    val approvedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null
)

enum class DisputeStatus {
    OPENED,
    UNDER_REVIEW,
    EVIDENCE_REQUIRED,
    SUBMITTED,
    WON,
    LOST,
    CLOSED
}

data class PaymentDispute(
    val disputeId: String = UUID.randomUUID().toString(),
    val paymentTransactionId: String,
    val orderId: String,
    val gatewayDisputeId: String? = null,
    val customerUid: String,
    val sellerUid: String,
    val amountMinor: Long,
    val currencyCode: String,
    val reason: String,
    val status: DisputeStatus = DisputeStatus.OPENED,
    val evidenceStatus: String = "PENDING_SELLER_INPUT",
    val evidenceDeadline: Long = System.currentTimeMillis() + (7 * 24 * 3600 * 1000),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

// ==========================================
// 8. WEBHOOKS, RECONCILIATION & AUDIT
// ==========================================

data class PaymentWebhookEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val gateway: String,
    val eventType: String,
    val gatewayEventId: String,
    val payloadHash: String,
    val receivedAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val processingStatus: String = "SUCCESS",
    val retryCount: Int = 0,
    val errorCode: String? = null,
    val relatedTransactionId: String? = null,
    val signatureVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class PaymentReconciliationReport(
    val reportId: String = UUID.randomUUID().toString(),
    val gateway: String,
    val countryCode: String,
    val periodStart: Long,
    val periodEnd: Long,
    val transactionsChecked: Int,
    val matchedCount: Int,
    val mismatchCount: Int,
    val missingCount: Int,
    val duplicateCount: Int,
    val totalGatewayAmountMinor: Long,
    val totalHealthogramAmountMinor: Long,
    val discrepancyAmountMinor: Long,
    val status: String = "RECONCILED",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = System.currentTimeMillis()
)

data class PaymentAuditLog(
    val logId: String = UUID.randomUUID().toString(),
    val actorUid: String,
    val actorRole: String, // "CUSTOMER", "SELLER", "OWNER", "ADMIN", "SYSTEM"
    val action: String,    // "CREATE_PAYMENT", "CAPTURE", "REFUND", "PAYOUT", "EMERGENCY_SWITCH"
    val transactionId: String? = null,
    val orderId: String? = null,
    val sellerUid: String? = null,
    val countryCode: String,
    val previousStatus: String? = null,
    val newStatus: String? = null,
    val amountMinor: Long? = null,
    val currencyCode: String,
    val source: String = "MOBILE_ANDROID",
    val requestId: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
)

data class PaymentRiskCheck(
    val riskCheckId: String = UUID.randomUUID().toString(),
    val paymentTransactionId: String,
    val customerUid: String,
    val sellerUid: String,
    val riskScore: Int = 12, // 0 - 100
    val riskLevel: String = "LOW",
    val country: String,
    val velocityStatus: String = "NORMAL",
    val amountStatus: String = "WITHIN_LIMITS",
    val result: RiskAction = RiskAction.ALLOW,
    val actionTaken: String = "PAYMENT_PERMITTED",
    val createdAt: Long = System.currentTimeMillis()
)

data class GatewayHealth(
    val gatewayId: String,
    val displayName: String,
    val status: String = "HEALTHY", // HEALTHY, DEGRADED, DOWN, MAINTENANCE
    val successRate: Double = 99.8,
    val failureRate: Double = 0.2,
    val averageResponseTimeMs: Long = 340L,
    val webhookDelayMs: Long = 120L,
    val lastSuccessAt: Long = System.currentTimeMillis(),
    val lastFailureAt: Long? = null,
    val maintenanceMode: Boolean = false
)

data class PaymentFeatureFlags(
    val paymentSystemEnabled: Boolean = true,
    val marketplacePaymentsEnabled: Boolean = true,
    val sellerPayoutsEnabled: Boolean = true,
    val ownerWithdrawalsEnabled: Boolean = true,
    val cardPaymentsEnabled: Boolean = true,
    val applePayEnabled: Boolean = true,
    val googlePayEnabled: Boolean = true,
    val localPaymentMethodsEnabled: Boolean = true,
    val refundEnabled: Boolean = true,
    val sellerAutoPayoutEnabled: Boolean = true,
    val internationalPaymentsEnabled: Boolean = false, // Rule: false initially
    val multiCurrencyEnabled: Boolean = true,
    val buyNowPayLaterEnabled: Boolean = false,
    val emergencyPaymentKillSwitch: Boolean = false,
    val emergencyPayoutKillSwitch: Boolean = false
)

fun formatFinancialAmount(minorUnits: Long, currencyCode: String): String {
    val major = minorUnits.toDouble() / 100.0
    val symbol = when (currencyCode.uppercase()) {
        "SAR" -> "SAR"
        "AED" -> "AED"
        "USD" -> "$"
        "GBP" -> "£"
        "EUR" -> "€"
        else -> currencyCode
    }
    return if (symbol.length > 1) {
        String.format(java.util.Locale.US, "%.2f %s", major, symbol)
    } else {
        String.format(java.util.Locale.US, "%s%.2f", symbol, major)
    }
}
