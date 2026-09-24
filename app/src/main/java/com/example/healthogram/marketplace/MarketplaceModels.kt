package com.example.healthogram.marketplace

import java.util.UUID

/**
 * HEALTHOGRAM — STEP 08: MARKETPLACE CUSTOMER SYSTEM
 * Master Domain Models & Enums
 */

// 1. Marketplace Roles: ONLY Customer and Seller
enum class MarketplaceRoleType(val roleName: String) {
    CUSTOMER("Customer"),
    SELLER("Seller")
}

// 2. Product Status
enum class ProductStatus(val label: String) {
    DRAFT("Draft"),
    PENDING_REVIEW("Pending Review"),
    ACTIVE("Active"),
    PAUSED("Paused"),
    OUT_OF_STOCK("Out of Stock"),
    REJECTED("Rejected"),
    ARCHIVED("Archived")
}

// 3. Product Compliance Status
enum class ProductComplianceStatus(val label: String) {
    NOT_CHECKED("Not Checked"),
    PENDING_REVIEW("Pending Review"),
    APPROVED("Approved"),
    RESTRICTED("Restricted"),
    REJECTED("Rejected")
}

// 4. Order Status (16 Authoritative States)
enum class MarketplaceOrderStatus(val label: String) {
    PENDING_PAYMENT("Pending Payment"),
    PAID("Paid"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    PACKED("Packed"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    COMPLETED("Completed"),
    CANCEL_REQUESTED("Cancel Requested"),
    CANCELLED("Cancelled"),
    RETURN_REQUESTED("Return Requested"),
    RETURNED("Returned"),
    REFUND_PENDING("Refund Pending"),
    REFUNDED("Refunded"),
    FAILED("Failed")
}

// 5. Payment Status
enum class PaymentStatus(val label: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    PAID("Paid"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    PARTIALLY_REFUNDED("Partially Refunded")
}

// 6. Return Status
enum class ReturnStatus(val label: String) {
    REQUESTED("Requested"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    PICKUP_PENDING("Pickup Pending"),
    RECEIVED("Received"),
    APPROVED_FOR_REFUND("Approved for Refund"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

// 7. Refund Status
enum class RefundStatus(val label: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled")
}

// 8. Review Status
enum class ReviewStatus(val label: String) {
    PENDING("Pending"),
    PUBLISHED("Published"),
    HIDDEN("Hidden"),
    REMOVED("Removed")
}

// 9. Support Ticket Category & Status
enum class SupportCategory(val displayName: String) {
    ORDER("Order Inquiry"),
    PAYMENT("Payment Issue"),
    DELIVERY("Delivery & Tracking"),
    RETURN("Return & Exchange"),
    REFUND("Refund Status"),
    PRODUCT("Product Question"),
    SELLER("Seller Feedback"),
    ACCOUNT("Account & Privacy"),
    OTHER("Other Assistance")
}

enum class SupportStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

// 10. Deal Types & Discount Types
enum class DealType(val label: String) {
    PERCENTAGE("Percentage Off"),
    FIXED_AMOUNT("Fixed Amount Off"),
    BUNDLE("Bundle Deal"),
    LIMITED_TIME("Limited Time Offer")
}

enum class DiscountType(val label: String) {
    PERCENTAGE("Percentage"),
    FIXED_AMOUNT("Fixed Amount")
}

// 11. Flash Sale Status
enum class FlashSaleStatus(val label: String) {
    SCHEDULED("Scheduled"),
    ACTIVE("Active"),
    ENDED("Ended"),
    CANCELLED("Cancelled")
}

// 12. Cart Status
enum class CartStatus(val label: String) {
    ACTIVE("Active"),
    CHECKOUT("In Checkout"),
    CONVERTED("Converted to Order"),
    ABANDONED("Abandoned"),
    EXPIRED("Expired")
}

// -------------------------------------------------------------
// FIRESTORE ENTITY DATA CLASSES
// -------------------------------------------------------------

/**
 * 1. Category Collection: marketplace_categories/{categoryId}
 */
data class MarketplaceCategory(
    val categoryId: String,
    val name: String,
    val description: String = "",
    val imageUrl: String = "",
    val icon: String = "medical_services",
    val parentCategoryId: String? = null,
    val countryCode: String = "SA",
    val sortOrder: Int = 0,
    val active: Boolean = true,
    val displayOnHome: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 2. Product Compliance Collection: marketplace_product_compliance/{productId}
 */
data class MarketplaceProductCompliance(
    val productId: String,
    val countryCode: String = "SA",
    val complianceStatus: ProductComplianceStatus = ProductComplianceStatus.APPROVED,
    val restricted: Boolean = false,
    val requiresDocument: Boolean = false,
    val documentType: String? = null,
    val reviewStatus: String = "approved",
    val reviewedBy: String = "compliance_system",
    val reviewedAt: Long = System.currentTimeMillis(),
    val restrictionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 3. Wishlist Collection: marketplace_wishlists/{wishlistId}
 */
data class MarketplaceWishlist(
    val wishlistId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val productId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 4. Cart Collection: marketplace_carts/{cartId}
 */
data class MarketplaceCart(
    val cartId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val currency: String = "SAR",
    val subtotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val deliveryTotal: Double = 0.0,
    val taxTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val itemCount: Int = 0,
    val status: CartStatus = CartStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 5. Cart Items Collection: marketplace_cart_items/{itemId}
 */
data class MarketplaceCartItem(
    val itemId: String = UUID.randomUUID().toString(),
    val cartId: String = "cart_default",
    val customerUid: String = "cust_default",
    val productId: String,
    val sellerUid: String = "seller_default",
    val productTitleSnapshot: String = "Product",
    val productImageSnapshot: String = "",
    val unitPriceSnapshot: Double = 0.0,
    val quantity: Int = 1,
    val variantId: String? = null,
    val lineTotal: Double = unitPriceSnapshot * quantity,
    val currency: String = "SAR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 6. Customer Address Collection: marketplace_addresses/{addressId}
 */
data class MarketplaceAddress(
    val addressId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val label: String = "Home", // Home, Work, Other
    val fullName: String,
    val phone: String,
    val countryCode: String = "SA",
    val countryName: String = "Saudi Arabia",
    val state: String = "Riyadh Province",
    val city: String = "Riyadh",
    val district: String = "Al Olaya",
    val postalCode: String = "12211",
    val addressLine1: String,
    val addressLine2: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryInstructions: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 7. Country Configuration: marketplace_country_config/{countryCode}
 */
data class MarketplaceCountryConfig(
    val countryCode: String = "SA",
    val marketplaceEnabled: Boolean = true,
    val currency: String = "SAR",
    val taxEnabled: Boolean = true,
    val taxMode: String = "VAT_15",
    val paymentMethods: List<String> = listOf("MADA", "APPLE_PAY", "CREDIT_CARD", "STC_PAY"),
    val deliveryEnabled: Boolean = true,
    val returnsEnabled: Boolean = true,
    val internationalBuyingEnabled: Boolean = false, // Strictly OFF by default
    val internationalSellingEnabled: Boolean = false, // Strictly OFF by default
    val minimumOrderValue: Double = 10.0,
    val maximumOrderValue: Double = 50000.0,
    val active: Boolean = true
)

/**
 * 8. Deals Collection: marketplace_deals/{dealId}
 */
data class MarketplaceDeal(
    val dealId: String = UUID.randomUUID().toString(),
    val productId: String,
    val sellerUid: String,
    val dealType: DealType = DealType.PERCENTAGE,
    val discountType: DiscountType = DiscountType.PERCENTAGE,
    val discountValue: Double,
    val startAt: Long = System.currentTimeMillis(),
    val endAt: Long = System.currentTimeMillis() + 86400000L * 7,
    val maxQuantity: Int = 100,
    val customerLimit: Int = 2,
    val countryCode: String = "SA",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 9. Flash Sales Collection: marketplace_flash_sales/{flashSaleId}
 */
data class MarketplaceFlashSale(
    val flashSaleId: String = UUID.randomUUID().toString(),
    val productId: String,
    val sellerUid: String,
    val originalPrice: Double,
    val salePrice: Double,
    val startAt: Long = System.currentTimeMillis() - 3600000L,
    val endAt: Long = System.currentTimeMillis() + 14400000L,
    val quantityLimit: Int = 50,
    val quantitySold: Int = 12,
    val customerLimit: Int = 1,
    val countryCode: String = "SA",
    val status: FlashSaleStatus = FlashSaleStatus.ACTIVE
)

/**
 * 10. Coupons Collection: marketplace_coupons/{couponId}
 */
data class MarketplaceCoupon(
    val couponId: String = UUID.randomUUID().toString(),
    val code: String,
    val discountType: DiscountType = DiscountType.PERCENTAGE,
    val discountValue: Double = 0.0,
    val minimumOrderValue: Double = 50.0,
    val maximumDiscount: Double = 100.0,
    val startAt: Long = System.currentTimeMillis() - 86400000L,
    val endAt: Long = System.currentTimeMillis() + 86400000L * 30,
    val usageLimit: Int = 1000,
    val perCustomerLimit: Int = 1,
    val countryCode: String = "SA",
    val sellerUid: String? = null,
    val applicableProducts: List<String> = emptyList(),
    val applicableCategories: List<String> = emptyList(),
    val status: String = "active"
)

/**
 * 11. Order Collection: marketplace_orders/{orderId}
 */
data class MarketplaceOrderSnapshot(
    val orderId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val orderNumber: String = "HGM-${System.currentTimeMillis().toString().takeLast(8)}",
    val currency: String = "SAR",
    val subtotal: Double,
    val discountTotal: Double = 0.0,
    val deliveryTotal: Double = 15.0,
    val taxTotal: Double = 0.0,
    val serviceFee: Double = 0.0,
    val grandTotal: Double,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val orderStatus: MarketplaceOrderStatus = MarketplaceOrderStatus.PENDING_PAYMENT,
    val deliveryStatus: String = "Preparing Shipment",
    val shippingAddressSnapshot: MarketplaceAddress,
    val billingAddressSnapshot: MarketplaceAddress = shippingAddressSnapshot,
    val countryCode: String = "SA",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null,
    val completedAt: Long? = null,
    val cancelledAt: Long? = null
)

/**
 * 12. Order Items Collection: marketplace_order_items/{itemId}
 */
data class MarketplaceOrderItem(
    val itemId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val productId: String,
    val sellerUid: String,
    val productTitleSnapshot: String,
    val productImageSnapshot: String = "",
    val skuSnapshot: String = "",
    val unitPrice: Double,
    val quantity: Int,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val lineTotal: Double,
    val currency: String = "SAR",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 13. Payments Collection: marketplace_payments/{paymentId}
 */
data class MarketplacePayment(
    val paymentId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val customerUid: String,
    val provider: String = "MadaGateway",
    val providerPaymentId: String = "pay_${UUID.randomUUID().toString().take(12)}",
    val amount: Double,
    val currency: String = "SAR",
    val status: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethodType: String = "MADA",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null
)

/**
 * 14. Returns Collection: marketplace_returns/{returnId}
 */
data class MarketplaceReturn(
    val returnId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val orderItemId: String,
    val customerUid: String,
    val sellerUid: String,
    val reasonCode: String = "DEFECTIVE_OR_UNSUITABLE",
    val description: String,
    val photos: List<String> = emptyList(),
    val status: ReturnStatus = ReturnStatus.REQUESTED,
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val rejectedAt: Long? = null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 15. Refunds Collection: marketplace_refunds/{refundId}
 */
data class MarketplaceRefund(
    val refundId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val paymentId: String,
    val customerUid: String,
    val amount: Double,
    val currency: String = "SAR",
    val reason: String = "Return Item Approved",
    val providerRefundId: String? = null,
    val status: RefundStatus = RefundStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * 16. Reviews Collection: marketplace_reviews/{reviewId}
 */
data class MarketplaceReview(
    val reviewId: String = UUID.randomUUID().toString(),
    val productId: String,
    val orderId: String,
    val orderItemId: String,
    val customerUid: String,
    val sellerUid: String,
    val rating: Int = 5,
    val title: String,
    val reviewText: String,
    val images: List<String> = emptyList(),
    val verifiedPurchase: Boolean = true,
    val status: ReviewStatus = ReviewStatus.PUBLISHED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 17. Recently Viewed Collection: marketplace_recently_viewed/{recordId}
 */
data class MarketplaceRecentlyViewed(
    val recordId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val productId: String,
    val viewedAt: Long = System.currentTimeMillis()
)

/**
 * 18. Support Tickets Collection: marketplace_support_tickets/{ticketId}
 */
data class MarketplaceSupportTicket(
    val ticketId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val orderId: String? = null,
    val category: SupportCategory = SupportCategory.ORDER,
    val subject: String,
    val description: String,
    val attachments: List<String> = emptyList(),
    val status: SupportStatus = SupportStatus.OPEN,
    val priority: String = "NORMAL",
    val assignedTo: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)

/**
 * 19. Marketplace Notifications
 */
data class MarketplaceNotification(
    val notificationId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val title: String,
    val body: String,
    val eventType: String, // order_placed, payment_confirmed, order_shipped, etc.
    val referenceId: String? = null,
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 20. Marketplace Feature Flags
 */
data class MarketplaceFeatureFlags(
    val marketplaceEnabled: Boolean = true,
    val customerMarketplaceEnabled: Boolean = true,
    val sellerMarketplaceEnabled: Boolean = true,
    val internationalMarketplaceEnabled: Boolean = false, // Strictly OFF by default
    val flashSalesEnabled: Boolean = true,
    val couponsEnabled: Boolean = true,
    val reviewsEnabled: Boolean = true,
    val returnsEnabled: Boolean = true,
    val refundsEnabled: Boolean = true,
    val marketplaceSearchEnabled: Boolean = true,
    val recommendationsEnabled: Boolean = true
)
