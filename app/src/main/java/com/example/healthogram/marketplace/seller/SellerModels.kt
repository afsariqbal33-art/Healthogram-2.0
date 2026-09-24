package com.example.healthogram.marketplace.seller

import com.example.healthogram.marketplace.MarketplaceOrderStatus
import java.util.UUID

typealias OrderStatus = MarketplaceOrderStatus

/**
 * HEALTHOGRAM — STEP 09: MARKETPLACE SELLER CENTER DATA MODELS
 *
 * Strict Compliance:
 * 1. Account Structure: ONLY Customer and Seller.
 *    No Pharmacy, Medicine Company, Wholesale/Supplier, Equipment Manufacturer roles.
 * 2. Seller Types: individual_seller, business_seller.
 * 3. Complete Data Isolation from Health Passport.
 * 4. Append-Oriented Ledger and Server-Authoritative Balances.
 */

// ==========================================
// 1. SELLER ENUMS & TYPES
// ==========================================

enum class SellerType(val value: String, val displayName: String) {
    INDIVIDUAL_SELLER("individual_seller", "Individual Seller"),
    BUSINESS_SELLER("business_seller", "Registered Business / Commercial Seller")
}

enum class SellerStatus(val value: String) {
    PENDING("pending"),
    ACTIVE("active"),
    SUSPENDED("suspended"),
    RESTRICTED("restricted"),
    CLOSED("closed")
}

enum class StoreStatus(val value: String) {
    DRAFT("draft"),
    UNDER_REVIEW("under_review"),
    ACTIVE("active"),
    PAUSED("paused"),
    CLOSED("closed")
}

enum class SellerVerificationStatus(val value: String) {
    NOT_STARTED("not_started"),
    DRAFT("draft"),
    SUBMITTED("submitted"),
    UNDER_REVIEW("under_review"),
    ADDITIONAL_INFORMATION_REQUIRED("additional_information_required"),
    VERIFIED("verified"),
    REJECTED("rejected"),
    SUSPENDED("suspended"),
    EXPIRED("expired"),
    REVOKED("revoked")
}

enum class SellerDocumentType(val value: String, val title: String) {
    NATIONAL_ID("national_id", "National ID / Iqama / Passport"),
    COMMERCIAL_REGISTRATION("commercial_registration", "Commercial Registration (CR)"),
    TAX_CERTIFICATE("tax_certificate", "VAT / Tax Registration Certificate"),
    MUNICIPAL_LICENSE("municipal_license", "Municipal Trading License"),
    AUTHORIZED_REP_LETTER("authorized_rep_letter", "Authorized Signatory Letter"),
    HEALTHCARE_SELLER_PERMIT("healthcare_seller_permit", "Health & Wellness Trade Permit"),
    FREELANCE_CERTIFICATE("freelance_certificate", "Freelance Document / Wathiqah")
}

enum class DocumentStatus(val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    EXPIRED("expired")
}

enum class LedgerTransactionType(val value: String) {
    SALE("sale"),
    COMMISSION("commission"),
    PAYMENT_FEE("payment_fee"),
    REFUND("refund"),
    ADJUSTMENT("adjustment"),
    TAX("tax"),
    PAYOUT("payout"),
    CHARGEBACK("chargeback")
}

enum class LedgerStatus(val value: String) {
    PENDING("pending"),
    POSTED("posted"),
    SETTLED("settled"),
    VOIDED("voided")
}

enum class PayoutStatus(val value: String) {
    REQUESTED("requested"),
    UNDER_REVIEW("under_review"),
    APPROVED("approved"),
    PROCESSING("processing"),
    PAID("paid"),
    FAILED("failed"),
    CANCELLED("cancelled")
}

enum class CommissionType(val value: String) {
    PERCENTAGE("percentage"),
    FIXED("fixed"),
    TIERED("tiered")
}

enum class InventoryStatus(val value: String) {
    IN_STOCK("in_stock"),
    LOW_STOCK("low_stock"),
    OUT_OF_STOCK("out_of_stock"),
    PAUSED("paused")
}

enum class InventoryMovementType(val value: String) {
    STOCK_ADDED("stock_added"),
    STOCK_REMOVED("stock_removed"),
    SALE("sale"),
    RETURN("return"),
    ADJUSTMENT("adjustment"),
    RESERVATION("reservation"),
    RESERVATION_RELEASE("reservation_release")
}

enum class ProductComplianceStatus(val value: String) {
    NOT_CHECKED("not_checked"),
    PENDING("pending"),
    APPROVED("approved"),
    RESTRICTED("restricted"),
    REJECTED("rejected"),
    EXPIRED("expired")
}

enum class ProductReviewType(val value: String) {
    CONTENT("content"),
    COMPLIANCE("compliance"),
    CATEGORY("category"),
    RESTRICTED_PRODUCT("restricted_product"),
    PRICING("pricing"),
    DOCUMENT("document"),
    GENERAL("general")
}

enum class SellerOfferType(val value: String) {
    PERCENTAGE_DISCOUNT("percentage_discount"),
    FIXED_DISCOUNT("fixed_discount"),
    BUY_X_GET_Y("buy_x_get_y"),
    BUNDLE("bundle"),
    LIMITED_TIME("limited_time")
}

enum class SellerOfferStatus(val value: String) {
    DRAFT("draft"),
    SCHEDULED("scheduled"),
    ACTIVE("active"),
    ENDED("ended"),
    CANCELLED("cancelled")
}

enum class SellerSupportCategory(val value: String) {
    ACCOUNT("Account"),
    VERIFICATION("Verification"),
    PRODUCT("Product"),
    ORDER("Order"),
    PAYMENT("Payment"),
    PAYOUT("Payout"),
    DELIVERY("Delivery"),
    RETURN("Return"),
    REFUND("Refund"),
    COMPLIANCE("Compliance"),
    TECHNICAL("Technical"),
    OTHER("Other")
}

// ==========================================
// 2. FIRESTORE CORE COLLECTIONS
// ==========================================

/**
 * Collection: marketplace_seller_profiles/{sellerUid}
 */
data class MarketplaceSellerProfile(
    val sellerUid: String,
    val sellerType: SellerType = SellerType.INDIVIDUAL_SELLER,
    val legalName: String = "",
    val storeName: String = "",
    val storeSlug: String = "",
    val logoUrl: String = "",
    val coverImageUrl: String = "",
    val description: String = "",
    val countryCode: String = "SA",
    val currency: String = "SAR",
    val email: String = "",
    val phone: String = "",
    val businessAddress: String = "",
    val verificationStatus: SellerVerificationStatus = SellerVerificationStatus.NOT_STARTED,
    val sellerStatus: SellerStatus = SellerStatus.PENDING,
    val storeStatus: StoreStatus = StoreStatus.DRAFT,
    val ratingAverage: Double = 0.0,
    val reviewCount: Int = 0,
    val totalOrders: Int = 0,
    val totalProducts: Int = 0,
    val internationalSellingEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_seller_verification/{sellerUid}
 * Note: Distinct from Healthcare Verification.
 */
data class MarketplaceSellerVerification(
    val sellerUid: String = "",
    val sellerType: SellerType = SellerType.INDIVIDUAL_SELLER,
    val countryCode: String = "SA",
    val status: SellerVerificationStatus = SellerVerificationStatus.NOT_STARTED,
    val applicationId: String = "",
    val submittedAt: Long? = null,
    val reviewedAt: Long? = null,
    val reviewerUid: String? = null,
    val verifiedAt: Long? = null,
    val expiresAt: Long? = null,
    val rejectionReasonCode: String? = null,
    val additionalInformationRequired: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_seller_documents/{documentId}
 * Private Storage: marketplace_private/{sellerUid}/verification/{documentId}
 */
data class MarketplaceSellerDocument(
    val documentId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val applicationId: String = "",
    val countryCode: String = "SA",
    val documentType: SellerDocumentType = SellerDocumentType.NATIONAL_ID,
    val storagePath: String = "",
    val fileName: String = "",
    val mimeType: String = "application/pdf",
    val fileSize: Long = 0L,
    val documentNumberLast4: String = "",
    val issuedDate: String = "",
    val expiryDate: String = "",
    val issuingAuthority: String = "",
    val status: DocumentStatus = DocumentStatus.PENDING,
    val uploadedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewerUid: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_seller_country_requirements/{requirementId}
 */
data class MarketplaceSellerCountryRequirement(
    val requirementId: String = UUID.randomUUID().toString(),
    val countryCode: String = "SA",
    val sellerType: SellerType = SellerType.INDIVIDUAL_SELLER,
    val documentType: SellerDocumentType = SellerDocumentType.NATIONAL_ID,
    val required: Boolean = true,
    val active: Boolean = true,
    val expiryRequired: Boolean = true,
    val description: String = "",
    val acceptedFileTypes: List<String> = listOf("image/jpeg", "image/png", "application/pdf"),
    val maxFileSize: Long = 10 * 1024 * 1024L, // 10MB
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_seller_applications/{applicationId}
 */
data class MarketplaceSellerApplication(
    val applicationId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val sellerType: SellerType = SellerType.INDIVIDUAL_SELLER,
    val countryCode: String = "SA",
    val status: SellerVerificationStatus = SellerVerificationStatus.SUBMITTED,
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewStartedAt: Long? = null,
    val reviewCompletedAt: Long? = null,
    val reviewerUid: String? = null,
    val decisionReasonCode: String? = null,
    val additionalInformationRequired: String? = null,
    val resubmissionAllowed: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_seller_ledger/{ledgerId}
 * Append-oriented authoritative financial entries.
 */
data class MarketplaceSellerLedgerEntry(
    val ledgerId: String = UUID.randomUUID().toString(),
    val sellerUid: String = "",
    val orderId: String? = null,
    val orderItemId: String? = null,
    val transactionType: LedgerTransactionType = LedgerTransactionType.SALE,
    val grossAmount: Double = 0.0,
    val commission: Double = 0.0,
    val paymentFee: Double = 0.0,
    val refundAmount: Double = 0.0,
    val adjustment: Double = 0.0,
    val taxAmount: Double = 0.0,
    val netAmount: Double = 0.0,
    val currency: String = "SAR",
    val status: LedgerStatus = LedgerStatus.POSTED,
    val createdAt: Long = System.currentTimeMillis(),
    val effectiveAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_seller_balances/{sellerUid}
 * Server-authoritative seller balance summary.
 */
data class MarketplaceSellerBalance(
    val sellerUid: String = "",
    val currency: String = "SAR",
    val grossSales: Double = 0.0,
    val refunds: Double = 0.0,
    val fees: Double = 0.0,
    val taxes: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val availableBalance: Double = 0.0,
    val paidOut: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_product_reviews/{reviewId}
 * Internal review audit notes (not customer reviews).
 */
data class MarketplaceProductReviewAudit(
    val reviewId: String = UUID.randomUUID().toString(),
    val productId: String,
    val sellerUid: String,
    val reviewType: ProductReviewType = ProductReviewType.GENERAL,
    val status: String = "pending",
    val reviewerUid: String? = null,
    val reasonCode: String? = null,
    val reviewNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_product_compliance/{productId}
 */
data class MarketplaceProductCompliance(
    val productId: String,
    val sellerUid: String,
    val countryCode: String = "SA",
    val complianceStatus: ProductComplianceStatus = ProductComplianceStatus.NOT_CHECKED,
    val restricted: Boolean = false,
    val requiresLicense: Boolean = false,
    val requiredDocumentType: String? = null,
    val documentStatus: String? = null,
    val reviewerUid: String? = null,
    val reviewedAt: Long? = null,
    val restrictionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_inventory/{inventoryId}
 */
data class MarketplaceInventory(
    val inventoryId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val productId: String,
    val sku: String,
    val availableQuantity: Int = 0,
    val reservedQuantity: Int = 0,
    val soldQuantity: Int = 0,
    val returnedQuantity: Int = 0,
    val lowStockThreshold: Int = 5,
    val inventoryStatus: InventoryStatus = InventoryStatus.IN_STOCK,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_inventory_movements/{movementId}
 * Append-only stock audit record.
 */
data class MarketplaceInventoryMovement(
    val movementId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val productId: String,
    val type: InventoryMovementType = InventoryMovementType.STOCK_ADDED,
    val quantity: Int = 0,
    val referenceId: String = "",
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_product_prices/{priceId}
 */
data class MarketplaceProductPrice(
    val priceId: String = UUID.randomUUID().toString(),
    val productId: String,
    val sellerUid: String,
    val countryCode: String = "SA",
    val currency: String = "SAR",
    val price: Double = 0.0,
    val compareAtPrice: Double? = null,
    val effectiveFrom: Long = System.currentTimeMillis(),
    val effectiveTo: Long? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_price_history/{historyId}
 */
data class MarketplacePriceHistory(
    val historyId: String = UUID.randomUUID().toString(),
    val productId: String,
    val sellerUid: String,
    val oldPrice: Double = 0.0,
    val newPrice: Double = 0.0,
    val currency: String = "SAR",
    val changedAt: Long = System.currentTimeMillis(),
    val changedByUid: String = "",
    val reason: String = ""
)

/**
 * Collection: marketplace_seller_offers/{offerId}
 */
data class MarketplaceSellerOffer(
    val offerId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val productId: String,
    val offerType: SellerOfferType = SellerOfferType.PERCENTAGE_DISCOUNT,
    val value: Double = 0.0,
    val minimumQuantity: Int = 1,
    val minimumOrderValue: Double = 0.0,
    val startAt: Long = System.currentTimeMillis(),
    val endAt: Long = System.currentTimeMillis() + 86400000L * 7,
    val countryCode: String = "SA",
    val customerLimit: Int = 1,
    val status: SellerOfferStatus = SellerOfferStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_seller_payout_accounts/{payoutAccountId}
 * Masked destination only — Never raw bank account credentials.
 */
data class MarketplaceSellerPayoutAccount(
    val payoutAccountId: String = UUID.randomUUID().toString(),
    val sellerUid: String = "",
    val countryCode: String = "SA",
    val provider: String = "Saudi National Bank / SAR Payouts",
    val accountType: String = "IBAN",
    val maskedDestination: String = "SA•• •••• •••• •••• •••• 4021",
    val verificationStatus: String = "verified",
    val isDefault: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_seller_payout_requests/{requestId}
 */
data class MarketplaceSellerPayoutRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val sellerUid: String = "",
    val amount: Double = 0.0,
    val currency: String = "SAR",
    val payoutAccountId: String = "",
    val status: PayoutStatus = PayoutStatus.REQUESTED,
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val processedAt: Long? = null,
    val failureReasonCode: String? = null,
    val providerPayoutId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: marketplace_commission_rules/{ruleId}
 */
data class MarketplaceCommissionRule(
    val ruleId: String = UUID.randomUUID().toString(),
    val countryCode: String = "SA",
    val categoryId: String = "all",
    val sellerType: SellerType = SellerType.INDIVIDUAL_SELLER,
    val commissionType: CommissionType = CommissionType.PERCENTAGE,
    val commissionValue: Double = 8.0, // 8% default
    val effectiveFrom: Long = System.currentTimeMillis(),
    val effectiveTo: Long? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Collection: seller_support_tickets/{ticketId}
 */
data class SellerSupportTicket(
    val ticketId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val category: SellerSupportCategory = SellerSupportCategory.ACCOUNT,
    val subject: String,
    val description: String,
    val attachments: List<String> = emptyList(),
    val priority: String = "NORMAL",
    val status: String = "open",
    val assignedTo: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)

/**
 * Collection: marketplace_seller_audit_logs/{logId}
 */
data class MarketplaceSellerAuditLog(
    val logId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val action: String, // product_created, price_changed, payout_requested, etc.
    val resourceType: String,
    val resourceId: String,
    val performedByUid: String,
    val timestamp: Long = System.currentTimeMillis(),
    val beforeSummary: String = "",
    val afterSummary: String = "",
    val reason: String = "",
    val ipHash: String = "192.168.0.x",
    val deviceId: String = "android_client"
)

/**
 * Safe Seller-Facing Order View
 * Filters out all private customer health information.
 */
data class SellerOrderView(
    val orderId: String = UUID.randomUUID().toString(),
    val orderNumber: String = "ORD-001",
    val orderDate: Long = System.currentTimeMillis(),
    val orderStatus: String = "CONFIRMED",
    val paymentStatus: String = "PAID",
    val recipientName: String = "Customer",
    val deliveryAddressLine: String = "King Fahd Road",
    val deliveryCity: String = "Riyadh",
    val customerNotes: String = "",
    val items: List<SellerOrderItemView> = emptyList(),
    val subtotal: Double = 0.0,
    val platformCommission: Double = 0.0,
    val sellerNetEarnings: Double = 0.0,
    val currency: String = "SAR"
)

data class SellerOrderItemView(
    val itemId: String,
    val productId: String,
    val productTitle: String,
    val productSku: String,
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double,
    val variantInfo: String = ""
)
