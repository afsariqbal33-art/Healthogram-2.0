package com.example.healthogram.marketplace

import com.example.healthogram.core.MarketplaceRole
import java.util.UUID

enum class ProductCategory(val displayName: String) {
    MEDICAL_EQUIPMENT("Medical Equipment & Devices"),
    WELLNESS_AND_VITAMINS("Wellness & Daily Supplements"),
    PERSONAL_CARE("Personal Care & Hygiene"),
    FITNESS_AND_REHAB("Fitness & Rehabilitation"),
    FIRST_AID("First Aid & Home Health"),
    HEALTH_MONITORS("Smart Health Monitors & Wearables")
}

data class MarketplaceProduct(
    val productId: String = UUID.randomUUID().toString(),
    val sellerUid: String,
    val sellerStoreName: String = "Healthogram Verified Seller",
    val title: String,
    val description: String,
    val shortDescription: String = description.take(120),
    val category: ProductCategory = ProductCategory.MEDICAL_EQUIPMENT,
    val price: Double,
    val discountPrice: Double? = null,
    val currency: String = "SAR",
    val stockQuantity: Int = 100,
    val imageUrls: List<String> = emptyList(),
    val isFlashSale: Boolean = false,
    val rating: Float = 4.8f,
    val reviewCount: Int = 24,
    val countryCode: String = "SA",
    val isRegulatedHealthcareDevice: Boolean = false,
    val specifications: Map<String, String> = emptyMap(),
    // Step 08 Enhanced Fields:
    val sellerType: String = "verified_clinic_or_merchant",
    val sellerNameSnapshot: String = sellerStoreName,
    val slug: String = title.lowercase().replace(" ", "-").replace("[^a-z0-9-]".toRegex(), ""),
    val categoryId: String = category.name.lowercase(),
    val subcategoryId: String? = null,
    val brand: String = "Healthogram Medical",
    val sku: String = "SKU-${UUID.randomUUID().toString().take(8).uppercase()}",
    val compareAtPrice: Double? = discountPrice?.let { price } ?: (price * 1.25),
    val discountPercentage: Int = if (discountPrice != null && price > 0) (((price - discountPrice) / price) * 100).toInt() else 0,
    val lowStockThreshold: Int = 10,
    val unit: String = "piece",
    val images: List<String> = imageUrls,
    val thumbnailUrl: String = imageUrls.firstOrNull() ?: "",
    val status: ProductStatus = ProductStatus.ACTIVE,
    val approvalStatus: String = "approved",
    val productCondition: String = "NEW",
    val shippingAvailable: Boolean = true,
    val deliveryZoneIds: List<String> = listOf("zone_sa_all"),
    val estimatedDeliveryMinDays: Int = 1,
    val estimatedDeliveryMaxDays: Int = 3,
    val ratingAverage: Double = rating.toDouble(),
    val salesCount: Int = 54,
    val wishlistCount: Int = 12,
    val isFeatured: Boolean = true,
    val isDeal: Boolean = discountPrice != null,
    val searchKeywords: List<String> = title.lowercase().split(" "),
    val tags: List<String> = listOf("healthcare", "wellness", category.name.lowercase()),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val stock: Int get() = stockQuantity
}

data class CartItem(
    val product: MarketplaceProduct,
    val quantity: Int = 1
)

data class MarketplaceOrder(
    val orderId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val sellerUid: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val shippingFee: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val currency: String = "USD",
    val deliveryAddress: String,
    val trackingId: String? = null,
    val paymentTransactionId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class SellerProfile(
    val sellerUid: String,
    val businessName: String,
    val taxNumber: String = "TAX-SA-982103",
    val contactEmail: String = "merchant@healthogram.com",
    val bankAccountOrPayoutMethod: String = "IBAN-SA901000000000",
    val totalSalesVolume: Double = 0.0,
    val pendingPayoutBalance: Double = 0.0,
    val isVerifiedSeller: Boolean = false,
    val sellerLogo: String = "",
    val rating: Float = 4.9f,
    val reviewCount: Int = 120,
    val country: String = "Saudi Arabia",
    val deliveryInformation: String = "1-3 days standard delivery across Saudi Arabia",
    val returnInformation: String = "14 days medical standard return policy for sealed products"
) {
    fun toPublicProfile(): PublicSellerProfile = PublicSellerProfile(
        sellerUid = sellerUid,
        sellerName = businessName,
        sellerLogo = sellerLogo,
        isVerifiedSeller = isVerifiedSeller,
        verificationBadge = isVerifiedSeller,
        rating = rating,
        reviewCount = reviewCount,
        country = country,
        deliveryInformation = deliveryInformation,
        returnInformation = returnInformation
    )
}

data class PublicSellerProfile(
    val sellerUid: String,
    val sellerName: String,
    val sellerLogo: String = "",
    val isVerifiedSeller: Boolean = true,
    val verificationBadge: Boolean = true,
    val rating: Float = 4.9f,
    val reviewCount: Int = 120,
    val country: String = "Saudi Arabia",
    val deliveryInformation: String = "1-3 days standard delivery across Saudi Arabia",
    val returnInformation: String = "14 days medical standard return policy for sealed products"
)
