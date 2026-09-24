package com.example.healthogram.integration

import java.util.UUID

/**
 * HEALTHOGRAM 2.3 — STEP 51: ADVANCED INTEGRATION LAYER
 * Centralized Domain Models, Provider Adapters & Country Configuration
 */

// -------------------------------------------------------------------------------------
// 1. PROVIDER CONNECTION & HEALTH STATUS (Section 40, 78)
// -------------------------------------------------------------------------------------
enum class ProviderConnectionStatus(val displayName: String) {
    HEALTHY("Healthy / Online"),
    DEGRADED("Degraded Performance"),
    DISCONNECTED("Disconnected"),
    SANDBOX_ACTIVE("Sandbox / Staging Active"),
    REQUIRES_EXTERNAL_PROVIDER("Requires External Provider Configuration")
}

enum class IntegrationServiceType(val label: String) {
    PAYMENT_GATEWAY("Payment Gateway"),
    DELIVERY_PROVIDER("Logistics & Delivery"),
    FHIR_ENDPOINT("HL7 FHIR Interoperability"),
    HEALTH_CONNECT("Android Health Connect"),
    AI_STUDIO("AI Studio & Multimodal AI"),
    TRANSLATION("Translation & Localization")
}

data class ProviderHealthInfo(
    val providerId: String,
    val providerName: String,
    val serviceType: IntegrationServiceType,
    val countryCode: String,
    val status: ProviderConnectionStatus,
    val isSandbox: Boolean = true,
    val lastHeartbeat: Long = System.currentTimeMillis(),
    val errorRatePercent: Double = 0.0,
    val latencyMs: Long = 45L,
    val requiresExternalConfiguration: Boolean = false,
    val externalProviderNotes: String = ""
)

// -------------------------------------------------------------------------------------
// 2. COUNTRY MARKETPLACE CONFIGURATION (Section 4)
// -------------------------------------------------------------------------------------
data class CountryMarketplaceConfig(
    val countryCode: String,
    val countryName: String,
    val marketplaceEnabled: Boolean = true,
    val internationalMarketplaceEnabled: Boolean = false, // Must remain false by default
    val allowedProductCategories: List<String> = listOf("VITAMINS_SUPPLEMENTS", "FITNESS_EQUIPMENT", "WELLNESS_TRACKERS", "PERSONAL_CARE"),
    val restrictedProductCategories: List<String> = listOf("PRESCRIPTION_MEDICATION", "SURGICAL_HARDWARE", "CONTROLLED_SUBSTANCES"),
    val sellerRegistrationEnabled: Boolean = true,
    val paymentMethods: List<String> = listOf("CARD", "APPLE_PAY", "LOCAL_PAYMENT_METHOD"),
    val deliveryProviders: List<String> = listOf("LOCAL_EXPRESS", "ARAMEX"),
    val returnPolicyDays: Int = 14,
    val taxRatePercent: Double = 15.0,
    val currency: String = "SAR",
    val locale: String = "ar-SA",
    val featureFlags: Map<String, Boolean> = mapOf(
        "coupons_enabled" to true,
        "cash_on_delivery" to false,
        "subscription_products" to false
    )
)

// -------------------------------------------------------------------------------------
// 3. COUNTRY PAYMENT CONFIGURATION (Section 14)
// -------------------------------------------------------------------------------------
data class CountryPaymentConfig23(
    val countryCode: String,
    val currency: String,
    val primaryProviderId: String,
    val fallbackProviderId: String? = null,
    val enabled: Boolean = true,
    val supportedMethods: List<String> = listOf("CARD", "APPLE_PAY", "GOOGLE_PAY", "LOCAL_PAYMENT_METHOD"),
    val minimumAmountMinor: Long = 500L, // 5.00
    val maximumAmountMinor: Long = 5000000L, // 50,000.00
    val refundEnabled: Boolean = true,
    val sandboxMode: Boolean = true, // Default to sandbox/staging
    val productionMode: Boolean = false // Production requires explicit owner authorization
)

// -------------------------------------------------------------------------------------
// 4. COUNTRY DELIVERY CONFIGURATION (Section 24)
// -------------------------------------------------------------------------------------
data class DeliveryZoneConfig(
    val zoneId: String,
    val zoneName: String,
    val postalCodePrefixes: List<String> = emptyList(),
    val rateMultiplier: Double = 1.0,
    val etaHours: Int = 24
)

data class CountryDeliveryConfig23(
    val countryCode: String,
    val deliveryZones: List<DeliveryZoneConfig>,
    val primaryProviderId: String,
    val fallbackProviderId: String? = null,
    val localDeliveryEnabled: Boolean = true,
    val pickupEnabled: Boolean = true,
    val scheduledDeliveryEnabled: Boolean = true,
    val internationalDeliveryEnabled: Boolean = false // Locked false initially
)

// -------------------------------------------------------------------------------------
// 5. SELLER ONBOARDING & VERIFICATION (Section 5, 6)
// -------------------------------------------------------------------------------------
enum class SellerAccountType {
    INDIVIDUAL_SELLER,
    BUSINESS_SELLER
}

enum class SellerVerificationStatus(val label: String) {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    VERIFIED("Verified"),
    REJECTED("Rejected"),
    NEEDS_UPDATE("Needs Update"),
    SUSPENDED("Suspended")
}

data class SellerVerificationDocument(
    val documentId: String = UUID.randomUUID().toString(),
    val documentType: String, // NATIONAL_ID, PASSPORT, COMMERCIAL_REGISTRATION, TAX_CERTIFICATE
    val documentNumber: String,
    val documentUrl: String,
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val verified: Boolean = false
)

data class SellerProfile23(
    val sellerId: String,
    val ownerUserUid: String,
    val sellerType: SellerAccountType,
    val storeName: String,
    val storeDescription: String,
    val legalName: String,
    val countryCode: String,
    val businessAddress: String,
    val taxNumber: String? = null,
    val commercialRegistrationNumber: String? = null,
    val verificationStatus: SellerVerificationStatus = SellerVerificationStatus.DRAFT,
    val rejectionReason: String? = null,
    val documents: List<SellerVerificationDocument> = emptyList(),
    val payoutIban: String? = null,
    val payoutBankName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null
)

// -------------------------------------------------------------------------------------
// 6. PRODUCT MODERATION & INVENTORY (Section 7, 8, 9)
// -------------------------------------------------------------------------------------
enum class ProductLifecycleStatus(val label: String) {
    DRAFT("Draft"),
    PENDING_REVIEW("Pending Review"),
    ACTIVE("Active"),
    PAUSED("Paused"),
    REJECTED("Rejected"),
    OUT_OF_STOCK("Out of Stock"),
    ARCHIVED("Archived")
}

data class Product23(
    val productId: String = UUID.randomUUID().toString(),
    val sellerId: String,
    val name: String,
    val description: String,
    val category: String,
    val sku: String,
    val images: List<String> = emptyList(),
    val videoUrl: String? = null,
    val countryCode: String,
    val priceMinor: Long,
    val salePriceMinor: Long? = null,
    val currency: String = "SAR",
    val stockQuantity: Int,
    val reservedQuantity: Int = 0,
    val lowStockThreshold: Int = 5,
    val status: ProductLifecycleStatus = ProductLifecycleStatus.PENDING_REVIEW,
    val moderationReason: String? = null,
    val moderatorUid: String? = null,
    val returnPolicyDays: Int = 14,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val availableQuantity: Int
        get() = (stockQuantity - reservedQuantity).coerceAtLeast(0)
}

// -------------------------------------------------------------------------------------
// 7. ORDER IDEMPOTENCY & 16-STATE LIFECYCLE (Section 11, 12)
// -------------------------------------------------------------------------------------
enum class OrderState23(val label: String) {
    PENDING_PAYMENT("Pending Payment"),
    PAID("Paid"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    READY_FOR_PICKUP("Ready for Pickup"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCEL_REQUESTED("Cancel Requested"),
    CANCELLED("Cancelled"),
    RETURN_REQUESTED("Return Requested"),
    RETURNED("Returned"),
    REFUND_PENDING("Refund Pending"),
    REFUNDED("Refunded"),
    FAILED("Failed"),
    DISPUTED("Disputed")
}

data class OrderItem23(
    val productId: String,
    val productName: String,
    val sellerId: String,
    val unitPriceMinor: Long,
    val quantity: Int,
    val lineTotalMinor: Long
)

data class Order23(
    val orderId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val sellerId: String,
    val items: List<OrderItem23>,
    val subtotalMinor: Long,
    val deliveryFeeMinor: Long,
    val taxMinor: Long,
    val platformFeeMinor: Long,
    val sellerNetMinor: Long,
    val totalAmountMinor: Long,
    val currency: String = "SAR",
    val countryCode: String = "SA",
    val deliveryAddress: String,
    val deliveryProviderId: String,
    val trackingNumber: String? = null,
    val status: OrderState23 = OrderState23.PENDING_PAYMENT,
    val idempotencyKey: String,
    val paymentTransactionId: String? = null,
    val deliveredAt: Long? = null,
    val refundWindowExpiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------------------------------
// 8. FINANCIAL RECONCILIATION MODELS (Section 22)
// -------------------------------------------------------------------------------------
data class ReconciliationDiscrepancy(
    val discrepancyId: String = UUID.randomUUID().toString(),
    val referenceId: String, // Order ID, Payment ID, or Ledger TX ID
    val type: String, // MISSING_PAYMENT, MISMATCHED_AMOUNT, UNBALANCED_LEDGER, DUPLICATE_WEBHOOK, ORPHAN_PAYOUT
    val expectedAmountMinor: Long,
    val actualAmountMinor: Long,
    val currency: String,
    val severity: String = "HIGH", // CRITICAL, HIGH, MEDIUM, LOW
    val description: String,
    val detectedAt: Long = System.currentTimeMillis(),
    val resolved: Boolean = false
)

data class FinancialReconciliationReport(
    val reportId: String = UUID.randomUUID().toString(),
    val periodStart: Long,
    val periodEnd: Long,
    val totalOrdersChecked: Int,
    val totalPaymentsChecked: Int,
    val totalLedgerEntriesChecked: Int,
    val totalDiscrepancies: Int,
    val discrepancies: List<ReconciliationDiscrepancy>,
    val isBalanced: Boolean,
    val generatedAt: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------------------------------
// 9. OWNER EARNINGS & WITHDRAWAL 2.3 (Section 19, 20)
// -------------------------------------------------------------------------------------
data class OwnerEarnings23Summary(
    val grossMarketplaceRevenueMinor: Long,
    val platformCommissionsMinor: Long,
    val sellerFeesMinor: Long,
    val serviceFeesMinor: Long,
    val paymentProcessingCostsMinor: Long,
    val deliveryRevenueMinor: Long,
    val refundsDeductedMinor: Long,
    val chargebacksMinor: Long,
    val netAvailableMinor: Long,
    val pendingSettlementMinor: Long,
    val totalWithdrawnMinor: Long,
    val currency: String = "SAR",
    val countryBreakdown: Map<String, Long> = emptyMap()
)

data class OwnerWithdrawal23Request(
    val withdrawalId: String = UUID.randomUUID().toString(),
    val ownerUid: String,
    val amountMinor: Long,
    val currency: String,
    val destinationIban: String,
    val destinationBank: String,
    val twoFactorVerified: Boolean = false,
    val status: String = "PENDING", // PENDING, PROCESSING, COMPLETED, REJECTED
    val requestedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val auditLogId: String = UUID.randomUUID().toString()
)

// -------------------------------------------------------------------------------------
// 10. SELLER PAYOUT LIFECYCLE (Section 21)
// -------------------------------------------------------------------------------------
enum class SellerPayoutStatus(val label: String) {
    PENDING("Pending"),
    ELIGIBLE("Eligible"),
    PROCESSING("Processing"),
    PAID("Paid"),
    FAILED("Failed"),
    HELD("Held"),
    REVERSED("Reversed")
}

data class SellerPayout23(
    val payoutId: String = UUID.randomUUID().toString(),
    val sellerId: String,
    val orderId: String,
    val amountMinor: Long,
    val currency: String,
    val destinationIban: String,
    val status: SellerPayoutStatus = SellerPayoutStatus.PENDING,
    val holdReason: String? = null,
    val eligibilityDate: Long, // End of return window
    val initiatedAt: Long? = null,
    val completedAt: Long? = null
)

// -------------------------------------------------------------------------------------
// 11. AI & TRANSLATION METRICS (Section 36, 38)
// -------------------------------------------------------------------------------------
data class AIProviderMetrics(
    val providerId: String,
    val providerName: String,
    val totalRequests: Long = 0,
    val totalTokens: Long = 0,
    val estimatedCostUsd: Double = 0.0,
    val monthlyQuotaTokens: Long = 10_000_000,
    val quotaUsedPercent: Double = 0.0,
    val status: ProviderConnectionStatus = ProviderConnectionStatus.HEALTHY
)

data class TranslationProviderMetrics(
    val providerId: String,
    val providerName: String,
    val languagesSupported: List<String> = emptyList(),
    val totalCharactersTranslated: Long = 0,
    val estimatedCostUsd: Double = 0.0,
    val monthlyQuotaChars: Long = 50_000_000,
    val status: ProviderConnectionStatus = ProviderConnectionStatus.HEALTHY
)

// -------------------------------------------------------------------------------------
// 12. HEALTHCARE INTEGRATION STATUS (Section 26, 31)
// -------------------------------------------------------------------------------------
data class HealthcareIntegrationStatus(
    val integrationType: String, // FHIR_R4, ANDROID_HEALTH_CONNECT
    val endpointOrPackage: String,
    val consentEnforced: Boolean = true,
    val status: ProviderConnectionStatus,
    val lastSyncAt: Long? = null,
    val syncedRecordsCount: Long = 0,
    val notes: String = ""
)
