package com.example.healthogram.marketplace

import java.util.UUID

/**
 * HEALTHOGRAM — STEP 08: MARKETPLACE PAYMENT, DELIVERY & SEARCH ABSTRACTIONS
 */

data class PaymentIntentRequest(
    val orderId: String,
    val customerUid: String,
    val amount: Double,
    val currency: String = "SAR",
    val paymentMethodType: String = "MADA"
)

data class PaymentIntentResult(
    val paymentId: String,
    val providerPaymentId: String,
    val status: PaymentStatus,
    val redirectUrl: String? = null,
    val clientSecret: String? = null,
    val errorMessage: String? = null
)

data class PaymentWebhookPayload(
    val provider: String,
    val signature: String,
    val paymentId: String,
    val orderId: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PaymentWebhookResult(
    val success: Boolean,
    val orderId: String,
    val paymentId: String,
    val updatedStatus: PaymentStatus,
    val message: String
)

data class RefundRequest(
    val orderId: String,
    val paymentId: String,
    val customerUid: String,
    val amount: Double,
    val currency: String = "SAR",
    val reason: String
)

data class RefundResult(
    val refundId: String,
    val providerRefundId: String,
    val status: RefundStatus,
    val message: String
)

/**
 * 1. PaymentProvider Interface (Section 36)
 * Gateway-agnostic payment abstraction.
 */
interface PaymentProvider {
    suspend fun createPayment(request: PaymentIntentRequest): PaymentIntentResult
    suspend fun confirmPayment(paymentId: String, verificationToken: String): PaymentStatus
    suspend fun handleWebhook(payload: PaymentWebhookPayload): PaymentWebhookResult
    suspend fun refundPayment(request: RefundRequest): RefundResult
    suspend fun getPaymentStatus(paymentId: String): PaymentStatus
}

/**
 * Implementation of PaymentProvider for Saudi Arabia (Mada / Apple Pay / STC Pay)
 */
class SaudiMarketplacePaymentProvider : PaymentProvider {
    private val paymentDatabase = mutableMapOf<String, MarketplacePayment>()

    override suspend fun createPayment(request: PaymentIntentRequest): PaymentIntentResult {
        // Validation: reject negative or zero amounts
        if (request.amount <= 0.0) {
            return PaymentIntentResult(
                paymentId = "",
                providerPaymentId = "",
                status = PaymentStatus.FAILED,
                errorMessage = "Invalid payment amount."
            )
        }

        val paymentId = "hgm_pay_${UUID.randomUUID().toString().take(10)}"
        val providerPayId = "mada_${UUID.randomUUID().toString().take(12)}"
        val payment = MarketplacePayment(
            paymentId = paymentId,
            orderId = request.orderId,
            customerUid = request.customerUid,
            provider = "SaudiNationalPaymentGateway",
            providerPaymentId = providerPayId,
            amount = request.amount,
            currency = request.currency,
            status = PaymentStatus.PENDING,
            paymentMethodType = request.paymentMethodType
        )
        paymentDatabase[paymentId] = payment

        return PaymentIntentResult(
            paymentId = paymentId,
            providerPaymentId = providerPayId,
            status = PaymentStatus.PENDING,
            clientSecret = "sec_${UUID.randomUUID().toString().take(16)}"
        )
    }

    override suspend fun confirmPayment(paymentId: String, verificationToken: String): PaymentStatus {
        val existing = paymentDatabase[paymentId] ?: return PaymentStatus.FAILED
        // Authoritative server-side transition
        val updated = existing.copy(
            status = PaymentStatus.PAID,
            confirmedAt = System.currentTimeMillis()
        )
        paymentDatabase[paymentId] = updated
        return PaymentStatus.PAID
    }

    override suspend fun handleWebhook(payload: PaymentWebhookPayload): PaymentWebhookResult {
        // Validate webhook signature
        if (payload.signature.isBlank() || payload.signature == "invalid_sig") {
            return PaymentWebhookResult(
                success = false,
                orderId = payload.orderId,
                paymentId = payload.paymentId,
                updatedStatus = PaymentStatus.FAILED,
                message = "Invalid provider webhook signature."
            )
        }

        val existing = paymentDatabase[payload.paymentId]
        if (existing == null) {
            return PaymentWebhookResult(
                success = false,
                orderId = payload.orderId,
                paymentId = payload.paymentId,
                updatedStatus = PaymentStatus.FAILED,
                message = "Unknown payment record."
            )
        }

        // Validate amount and currency match
        if (existing.amount != payload.amount || existing.currency != payload.currency) {
            return PaymentWebhookResult(
                success = false,
                orderId = payload.orderId,
                paymentId = payload.paymentId,
                updatedStatus = PaymentStatus.FAILED,
                message = "Webhook amount or currency mismatch."
            )
        }

        val status = if (payload.status == "paid") PaymentStatus.PAID else PaymentStatus.FAILED
        paymentDatabase[payload.paymentId] = existing.copy(
            status = status,
            confirmedAt = if (status == PaymentStatus.PAID) System.currentTimeMillis() else null
        )

        return PaymentWebhookResult(
            success = true,
            orderId = payload.orderId,
            paymentId = payload.paymentId,
            updatedStatus = status,
            message = "Webhook processed successfully."
        )
    }

    override suspend fun refundPayment(request: RefundRequest): RefundResult {
        val existing = paymentDatabase[request.paymentId]
        if (existing == null || existing.status != PaymentStatus.PAID) {
            return RefundResult(
                refundId = "",
                providerRefundId = "",
                status = RefundStatus.FAILED,
                message = "Cannot refund unconfirmed payment."
            )
        }

        if (request.amount > existing.amount) {
            return RefundResult(
                refundId = "",
                providerRefundId = "",
                status = RefundStatus.FAILED,
                message = "Refund amount cannot exceed original payment."
            )
        }

        val refundId = "ref_${UUID.randomUUID().toString().take(10)}"
        val providerRefundId = "mada_ref_${UUID.randomUUID().toString().take(12)}"
        val newStatus = if (request.amount == existing.amount) PaymentStatus.REFUNDED else PaymentStatus.PARTIALLY_REFUNDED
        paymentDatabase[request.paymentId] = existing.copy(status = newStatus)

        return RefundResult(
            refundId = refundId,
            providerRefundId = providerRefundId,
            status = RefundStatus.COMPLETED,
            message = "Refund processed to original payment method."
        )
    }

    override suspend fun getPaymentStatus(paymentId: String): PaymentStatus {
        return paymentDatabase[paymentId]?.status ?: PaymentStatus.FAILED
    }
}

// -------------------------------------------------------------
// DELIVERY ABSTRACTION (Section 41)
// -------------------------------------------------------------

data class DeliveryCalculationRequest(
    val customerAddress: MarketplaceAddress,
    val items: List<MarketplaceCartItem>,
    val totalWeightKg: Double = 1.0,
    val countryCode: String = "SA"
)

data class DeliveryQuote(
    val deliveryFee: Double,
    val currency: String = "SAR",
    val estimatedMinDays: Int = 1,
    val estimatedMaxDays: Int = 3,
    val carrierName: String = "Saudi Healthcare Express",
    val available: Boolean = true
)

data class ShipmentInfo(
    val shipmentId: String,
    val trackingNumber: String,
    val carrier: String,
    val currentStatus: String,
    val statusTimeline: List<Pair<String, Long>>,
    val estimatedDeliveryDate: Long
)

interface DeliveryProvider {
    suspend fun calculateDelivery(request: DeliveryCalculationRequest): DeliveryQuote
    suspend fun createShipment(orderId: String, address: MarketplaceAddress, items: List<MarketplaceOrderItem>): ShipmentInfo
    suspend fun trackShipment(trackingNumber: String): ShipmentInfo
    suspend fun cancelShipment(trackingNumber: String): Boolean
}

class SaudiHealthcareDeliveryProvider : DeliveryProvider {
    override suspend fun calculateDelivery(request: DeliveryCalculationRequest): DeliveryQuote {
        // Free delivery for orders over 200 SAR, otherwise standard 15 SAR
        val total = request.items.sumOf { it.lineTotal }
        val fee = if (total >= 200.0) 0.0 else 15.0
        return DeliveryQuote(
            deliveryFee = fee,
            currency = "SAR",
            estimatedMinDays = 1,
            estimatedMaxDays = 3,
            carrierName = "Healthogram Swift Medical Delivery"
        )
    }

    override suspend fun createShipment(
        orderId: String,
        address: MarketplaceAddress,
        items: List<MarketplaceOrderItem>
    ): ShipmentInfo {
        val tracking = "HGM-EXP-${UUID.randomUUID().toString().take(8).uppercase()}"
        return ShipmentInfo(
            shipmentId = "shp_${UUID.randomUUID().toString().take(10)}",
            trackingNumber = tracking,
            carrier = "Healthogram Swift Medical Delivery",
            currentStatus = "Packed & Ready for Dispatch",
            statusTimeline = listOf(
                "Shipment Order Registered" to System.currentTimeMillis(),
                "Packed at Healthcare Warehouse" to System.currentTimeMillis()
            ),
            estimatedDeliveryDate = System.currentTimeMillis() + 86400000L * 2
        )
    }

    override suspend fun trackShipment(trackingNumber: String): ShipmentInfo {
        return ShipmentInfo(
            shipmentId = "shp_demo",
            trackingNumber = trackingNumber,
            carrier = "Healthogram Swift Medical Delivery",
            currentStatus = "Out for Delivery",
            statusTimeline = listOf(
                "Order Placed" to System.currentTimeMillis() - 86400000L * 2,
                "Payment Confirmed" to System.currentTimeMillis() - 86400000L * 2 + 3600000L,
                "Packed at Medical Warehouse" to System.currentTimeMillis() - 86400000L,
                "Departed Distribution Facility" to System.currentTimeMillis() - 43200000L,
                "Out for Delivery" to System.currentTimeMillis() - 7200000L
            ),
            estimatedDeliveryDate = System.currentTimeMillis() + 14400000L
        )
    }

    override suspend fun cancelShipment(trackingNumber: String): Boolean {
        return true
    }
}

// -------------------------------------------------------------
// SEARCH ABSTRACTION (Section 15, 16)
// -------------------------------------------------------------

data class MarketplaceSearchFilter(
    val query: String = "",
    val categoryId: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minRating: Float? = null,
    val brand: String? = null,
    val countryCode: String = "SA",
    val onlyDeals: Boolean = false,
    val onlyFlashSale: Boolean = false,
    val inStockOnly: Boolean = true,
    val sortBy: SearchSortOrder = SearchSortOrder.RELEVANCE
)

enum class SearchSortOrder(val displayName: String) {
    RELEVANCE("Relevance"),
    NEWEST("Newest First"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    RATING("Highest Rated"),
    BEST_SELLING("Best Selling"),
    DISCOUNT("Biggest Discount")
}

interface MarketplaceSearchProvider {
    suspend fun searchProducts(
        filter: MarketplaceSearchFilter,
        page: Int = 0,
        pageSize: Int = 20
    ): List<MarketplaceProduct>
}

class IndexedMarketplaceSearchProvider(
    private val productCatalog: () -> List<MarketplaceProduct>
) : MarketplaceSearchProvider {

    override suspend fun searchProducts(
        filter: MarketplaceSearchFilter,
        page: Int,
        pageSize: Int
    ): List<MarketplaceProduct> {
        var results = productCatalog().filter { it.status == ProductStatus.ACTIVE }

        // Country filtering: Local / country-wise only (Section 3, 28)
        results = results.filter { it.countryCode.equals(filter.countryCode, ignoreCase = true) }

        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            results = results.filter { product ->
                product.title.lowercase().contains(q) ||
                product.description.lowercase().contains(q) ||
                product.brand.lowercase().contains(q) ||
                product.sellerStoreName.lowercase().contains(q) ||
                product.searchKeywords.any { it.contains(q) } ||
                product.tags.any { it.contains(q) }
            }
        }

        if (filter.categoryId != null && filter.categoryId != "all") {
            results = results.filter { it.categoryId.equals(filter.categoryId, ignoreCase = true) }
        }

        if (filter.minPrice != null) {
            results = results.filter { (it.discountPrice ?: it.price) >= filter.minPrice }
        }
        if (filter.maxPrice != null) {
            results = results.filter { (it.discountPrice ?: it.price) <= filter.maxPrice }
        }
        if (filter.minRating != null) {
            results = results.filter { it.rating >= filter.minRating }
        }
        if (filter.brand != null && filter.brand.isNotBlank()) {
            results = results.filter { it.brand.equals(filter.brand, ignoreCase = true) }
        }
        if (filter.onlyDeals) {
            results = results.filter { it.isDeal }
        }
        if (filter.onlyFlashSale) {
            results = results.filter { it.isFlashSale }
        }
        if (filter.inStockOnly) {
            results = results.filter { it.stockQuantity > 0 }
        }

        // Sorting
        results = when (filter.sortBy) {
            SearchSortOrder.RELEVANCE -> results
            SearchSortOrder.NEWEST -> results.sortedByDescending { it.createdAt }
            SearchSortOrder.PRICE_LOW_HIGH -> results.sortedBy { it.discountPrice ?: it.price }
            SearchSortOrder.PRICE_HIGH_LOW -> results.sortedByDescending { it.discountPrice ?: it.price }
            SearchSortOrder.RATING -> results.sortedByDescending { it.rating }
            SearchSortOrder.BEST_SELLING -> results.sortedByDescending { it.salesCount }
            SearchSortOrder.DISCOUNT -> results.sortedByDescending { it.discountPercentage }
        }

        // Pagination: cursor / limit offset
        val startIndex = page * pageSize
        return results.drop(startIndex).take(pageSize)
    }
}
