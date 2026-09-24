package com.example.healthogram.marketplace

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * HEALTHOGRAM — STEP 08: MARKETPLACE IN-MEMORY REACTIVE REPOSITORY
 * Provides realistic medical marketplace catalog, live state management for Cart,
 * Wishlist, Addresses, Orders, Reviews, and Support Tickets.
 */
class MarketplaceRepository(
    val currentCustomerUid: String = "cust_healthogram_demo"
) {
    // 1. Categories
    val categories = listOf(
        MarketplaceCategory(
            categoryId = "cat_med_equip",
            name = "Medical Equipment",
            description = "Clinically certified monitoring and diagnostic equipment",
            icon = "medical_services",
            sortOrder = 1
        ),
        MarketplaceCategory(
            categoryId = "cat_diag_equip",
            name = "Diagnostic Devices",
            description = "At-home and clinical diagnostic test equipment",
            icon = "biotech",
            parentCategoryId = "cat_med_equip",
            sortOrder = 2
        ),
        MarketplaceCategory(
            categoryId = "cat_monitors",
            name = "Patient Monitoring",
            description = "Smart wearables and vital sign trackers",
            icon = "monitor_heart",
            parentCategoryId = "cat_med_equip",
            sortOrder = 3
        ),
        MarketplaceCategory(
            categoryId = "cat_personal_care",
            name = "Personal Healthcare",
            description = "Daily hygiene and wellness products",
            icon = "spa",
            sortOrder = 4
        ),
        MarketplaceCategory(
            categoryId = "cat_first_aid",
            name = "First Aid & Emergency",
            description = "Trauma kits and emergency dressings",
            icon = "healing",
            sortOrder = 5
        ),
        MarketplaceCategory(
            categoryId = "cat_fitness_rehab",
            name = "Mobility & Rehabilitation",
            description = "Orthopedic supports and physical therapy aids",
            icon = "fitness_center",
            sortOrder = 6
        )
    )

    // 2. Verified Sellers
    val sellers = listOf(
        SellerProfile(
            sellerUid = "seller_omron",
            businessName = "Omron Healthcare Middle East",
            taxNumber = "TAX-SA-3001928374",
            contactEmail = "support@omron-me.com",
            isVerifiedSeller = true,
            sellerLogo = "",
            rating = 4.9f,
            reviewCount = 540,
            deliveryInformation = "Next-day delivery in Riyadh, 2-3 days elsewhere",
            returnInformation = "14 days sealed return for medical monitors"
        ),
        SellerProfile(
            sellerUid = "seller_biosense",
            businessName = "BioSense Diagnostics Labs",
            taxNumber = "TAX-SA-3009847219",
            contactEmail = "info@biosenselabs.sa",
            isVerifiedSeller = true,
            sellerLogo = "",
            rating = 4.8f,
            reviewCount = 290,
            deliveryInformation = "Temperature-controlled medical shipping",
            returnInformation = "Diagnostic kits cannot be returned if unsealed"
        ),
        SellerProfile(
            sellerUid = "seller_ergofit",
            businessName = "ErgoFit Spine & Orthopedics",
            taxNumber = "TAX-SA-3018274610",
            contactEmail = "care@ergofit-sa.com",
            isVerifiedSeller = true,
            sellerLogo = "",
            rating = 4.7f,
            reviewCount = 145,
            deliveryInformation = "Standard delivery within 48 hours",
            returnInformation = "30 days satisfaction guarantee on orthopedic supports"
        )
    )

    // 3. Products
    private val _products = MutableStateFlow(
        listOf(
            MarketplaceProduct(
                productId = "prod_bp_monitor",
                sellerUid = "seller_omron",
                sellerStoreName = "Omron Healthcare Middle East",
                title = "Smart Bluetooth Blood Pressure Monitor",
                description = "Clinically validated upper-arm blood pressure monitor with dual-user memory and immediate Bluetooth sync with Healthogram.",
                category = ProductCategory.HEALTH_MONITORS,
                categoryId = "cat_monitors",
                price = 289.0,
                discountPrice = 249.0,
                currency = "SAR",
                stockQuantity = 45,
                rating = 4.9f,
                reviewCount = 312,
                isFlashSale = true,
                isFeatured = true,
                brand = "Omron",
                sku = "OMR-EVO-89"
            ),
            MarketplaceProduct(
                productId = "prod_pulse_ox",
                sellerUid = "seller_biosense",
                sellerStoreName = "BioSense Diagnostics Labs",
                title = "Continuous Pulse Oximeter & SpO2 Monitor",
                description = "Medical grade pulse oximeter with continuous fingertip telemetry, audible hypoxia alert, and high-contrast OLED display.",
                category = ProductCategory.MEDICAL_EQUIPMENT,
                categoryId = "cat_med_equip",
                price = 149.0,
                discountPrice = 119.0,
                currency = "SAR",
                stockQuantity = 80,
                rating = 4.8f,
                reviewCount = 189,
                isFlashSale = true,
                isFeatured = true,
                brand = "BioSense",
                sku = "BIO-OX-55"
            ),
            MarketplaceProduct(
                productId = "prod_hba1c_kit",
                sellerUid = "seller_biosense",
                sellerStoreName = "BioSense Diagnostics Labs",
                title = "At-Home HbA1c Glycated Hemoglobin Test Kit",
                description = "Prepaid certified capillary blood laboratory diagnostic test kit with physician review and digital results report.",
                category = ProductCategory.MEDICAL_EQUIPMENT,
                categoryId = "cat_diag_equip",
                price = 199.0,
                discountPrice = 169.0,
                currency = "SAR",
                stockQuantity = 35,
                rating = 4.7f,
                reviewCount = 94,
                isFlashSale = false,
                isFeatured = true,
                brand = "ApexLab",
                sku = "APX-HBA1C-01"
            ),
            MarketplaceProduct(
                productId = "prod_lumbar_cushion",
                sellerUid = "seller_ergofit",
                sellerStoreName = "ErgoFit Spine & Orthopedics",
                title = "Orthopedic Memory Foam Lumbar Support",
                description = "Ergonomic posture-correcting memory foam lumbar cushion recommended by certified orthopedic physiotherapists.",
                category = ProductCategory.FITNESS_AND_REHAB,
                categoryId = "cat_fitness_rehab",
                price = 135.0,
                discountPrice = 99.0,
                currency = "SAR",
                stockQuantity = 120,
                rating = 4.6f,
                reviewCount = 78,
                isFlashSale = false,
                isFeatured = false,
                brand = "ErgoFit",
                sku = "ERG-LMB-12"
            ),
            MarketplaceProduct(
                productId = "prod_infra_thermo",
                sellerUid = "seller_omron",
                sellerStoreName = "Omron Healthcare Middle East",
                title = "Non-Contact Infrared Medical Thermometer",
                description = "Ultra-fast 1-second non-contact clinical infrared thermometer with fever alarm color backlight and memory recall.",
                category = ProductCategory.MEDICAL_EQUIPMENT,
                categoryId = "cat_med_equip",
                price = 120.0,
                discountPrice = 89.0,
                currency = "SAR",
                stockQuantity = 60,
                rating = 4.8f,
                reviewCount = 142,
                isFlashSale = true,
                isFeatured = true,
                brand = "Omron",
                sku = "OMR-THM-03"
            ),
            MarketplaceProduct(
                productId = "prod_nebulizer",
                sellerUid = "seller_biosense",
                sellerStoreName = "BioSense Diagnostics Labs",
                title = "Portable Ultrasonic Mesh Nebulizer",
                description = "Pocket-sized silent mesh nebulizer for pediatric and adult respiratory aerosol therapy with rechargeable USB-C battery.",
                category = ProductCategory.MEDICAL_EQUIPMENT,
                categoryId = "cat_med_equip",
                price = 210.0,
                discountPrice = 175.0,
                currency = "SAR",
                stockQuantity = 28,
                rating = 4.9f,
                reviewCount = 88,
                isFlashSale = false,
                isFeatured = true,
                brand = "AeroBreeze",
                sku = "AER-NEB-90"
            ),
            MarketplaceProduct(
                productId = "prod_first_aid_kit",
                sellerUid = "seller_ergofit",
                sellerStoreName = "ErgoFit Spine & Orthopedics",
                title = "Comprehensive Healthcare Trauma First Aid Kit",
                description = "Water-resistant 180-piece emergency medical first aid kit containing sterile trauma dressings, shears, antiseptics, and burn gels.",
                category = ProductCategory.FIRST_AID,
                categoryId = "cat_first_aid",
                price = 160.0,
                discountPrice = 130.0,
                currency = "SAR",
                stockQuantity = 90,
                rating = 4.8f,
                reviewCount = 63,
                isFlashSale = false,
                isFeatured = false,
                brand = "SafeGuard",
                sku = "SFG-FAK-180"
            )
        )
    )
    val products: StateFlow<List<MarketplaceProduct>> = _products.asStateFlow()

    // 4. Wishlist
    private val _wishlist = MutableStateFlow<Set<String>>(setOf("prod_bp_monitor", "prod_nebulizer"))
    val wishlist: StateFlow<Set<String>> = _wishlist.asStateFlow()

    fun toggleWishlist(productId: String) {
        val current = _wishlist.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _wishlist.value = current
    }

    // 5. Cart
    private val _cartItems = MutableStateFlow<List<MarketplaceCartItem>>(
        listOf(
            MarketplaceCartItem(
                itemId = "item_demo_1",
                cartId = "cart_${currentCustomerUid}",
                customerUid = currentCustomerUid,
                productId = "prod_bp_monitor",
                sellerUid = "seller_omron",
                productTitleSnapshot = "Smart Bluetooth Blood Pressure Monitor",
                unitPriceSnapshot = 249.0,
                quantity = 1,
                lineTotal = 249.0,
                currency = "SAR"
            ),
            MarketplaceCartItem(
                itemId = "item_demo_2",
                cartId = "cart_${currentCustomerUid}",
                customerUid = currentCustomerUid,
                productId = "prod_infra_thermo",
                sellerUid = "seller_omron",
                productTitleSnapshot = "Non-Contact Infrared Medical Thermometer",
                unitPriceSnapshot = 89.0,
                quantity = 1,
                lineTotal = 89.0,
                currency = "SAR"
            )
        )
    )
    val cartItems: StateFlow<List<MarketplaceCartItem>> = _cartItems.asStateFlow()

    fun addToCart(product: MarketplaceProduct, quantity: Int = 1) {
        _cartItems.value = MarketplaceCustomActions.addToMarketplaceCart(
            _cartItems.value,
            product,
            currentCustomerUid,
            quantity
        )
    }

    fun updateCartItemQuantity(itemId: String, quantity: Int) {
        _cartItems.value = MarketplaceCustomActions.updateCartQuantity(
            _cartItems.value,
            itemId,
            quantity
        )
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // 6. Addresses
    private val _addresses = MutableStateFlow(
        listOf(
            MarketplaceAddress(
                addressId = "addr_home",
                customerUid = currentCustomerUid,
                label = "Home",
                fullName = "Dr. Tariq Al-Otaibi",
                phone = "+966 50 123 4567",
                countryCode = "SA",
                countryName = "Saudi Arabia",
                city = "Riyadh",
                district = "Al Olaya",
                postalCode = "12211",
                addressLine1 = "King Fahd Road, Tower 4, Apt 18B",
                deliveryInstructions = "Leave at reception if unavailable",
                isDefault = true
            ),
            MarketplaceAddress(
                addressId = "addr_clinic",
                customerUid = currentCustomerUid,
                label = "Work",
                fullName = "Dr. Tariq Al-Otaibi (Clinic)",
                phone = "+966 11 456 7890",
                countryCode = "SA",
                countryName = "Saudi Arabia",
                city = "Riyadh",
                district = "Al Sulaimaniyah",
                postalCode = "12243",
                addressLine1 = "Al Sulaimaniyah Medical Complex, Suite 302",
                deliveryInstructions = "Deliver during business hours 9am - 5pm",
                isDefault = false
            )
        )
    )
    val addresses: StateFlow<List<MarketplaceAddress>> = _addresses.asStateFlow()

    fun addAddress(address: MarketplaceAddress) {
        _addresses.value = _addresses.value + address
    }

    fun setDefaultAddress(addressId: String) {
        _addresses.value = _addresses.value.map {
            it.copy(isDefault = it.addressId == addressId)
        }
    }

    fun deleteAddress(addressId: String) {
        _addresses.value = _addresses.value.filterNot { it.addressId == addressId }
    }

    // 7. Coupons
    val availableCoupons = listOf(
        MarketplaceCoupon(
            couponId = "coup_health10",
            code = "HEALTH10",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10.0,
            minimumOrderValue = 100.0,
            maximumDiscount = 50.0
        ),
        MarketplaceCoupon(
            couponId = "coup_save30",
            code = "MEDSAVE30",
            discountType = DiscountType.FIXED_AMOUNT,
            discountValue = 30.0,
            minimumOrderValue = 200.0
        )
    )

    // 8. Orders
    private val _orders = MutableStateFlow(
        listOf(
            MarketplaceOrderSnapshot(
                orderId = "ord_past_1",
                customerUid = currentCustomerUid,
                orderNumber = "HGM-83920194",
                currency = "SAR",
                subtotal = 338.0,
                discountTotal = 33.8,
                deliveryTotal = 0.0,
                taxTotal = 45.63,
                grandTotal = 349.83,
                paymentStatus = PaymentStatus.PAID,
                orderStatus = MarketplaceOrderStatus.SHIPPED,
                deliveryStatus = "In Transit - Out for Delivery",
                shippingAddressSnapshot = _addresses.value.first(),
                confirmedAt = System.currentTimeMillis() - 86400000L
            ),
            MarketplaceOrderSnapshot(
                orderId = "ord_past_2",
                customerUid = currentCustomerUid,
                orderNumber = "HGM-71829033",
                currency = "SAR",
                subtotal = 199.0,
                discountTotal = 30.0,
                deliveryTotal = 15.0,
                taxTotal = 27.60,
                grandTotal = 211.60,
                paymentStatus = PaymentStatus.PAID,
                orderStatus = MarketplaceOrderStatus.DELIVERED,
                deliveryStatus = "Delivered to Customer",
                shippingAddressSnapshot = _addresses.value.first(),
                confirmedAt = System.currentTimeMillis() - 86400000L * 5,
                completedAt = System.currentTimeMillis() - 86400000L * 3
            )
        )
    )
    val orders: StateFlow<List<MarketplaceOrderSnapshot>> = _orders.asStateFlow()

    fun addOrder(order: MarketplaceOrderSnapshot) {
        _orders.value = listOf(order) + _orders.value
    }

    // 9. Returns
    private val _returns = MutableStateFlow(
        listOf(
            MarketplaceReturn(
                returnId = "ret_demo_1",
                orderId = "ord_past_2",
                orderItemId = "item_demo_past",
                customerUid = currentCustomerUid,
                sellerUid = "seller_biosense",
                reasonCode = "DAMAGED_IN_SHIPPING",
                description = "Box seal was torn during transit.",
                status = ReturnStatus.APPROVED_FOR_REFUND
            )
        )
    )
    val returns: StateFlow<List<MarketplaceReturn>> = _returns.asStateFlow()

    fun addReturn(returnReq: MarketplaceReturn) {
        _returns.value = listOf(returnReq) + _returns.value
    }

    // 10. Reviews
    private val _reviews = MutableStateFlow(
        listOf(
            MarketplaceReview(
                reviewId = "rev_1",
                productId = "prod_bp_monitor",
                orderId = "ord_past_1",
                orderItemId = "item_bp",
                customerUid = currentCustomerUid,
                sellerUid = "seller_omron",
                rating = 5,
                title = "Extremely Accurate & Fast Sync",
                reviewText = "Clinical measurements correspond perfectly with our clinic's desktop sphygmomanometer. Sync with Healthogram app was instant.",
                verifiedPurchase = true,
                status = ReviewStatus.PUBLISHED
            ),
            MarketplaceReview(
                reviewId = "rev_2",
                productId = "prod_pulse_ox",
                orderId = "ord_past_2",
                orderItemId = "item_ox",
                customerUid = "cust_other",
                sellerUid = "seller_biosense",
                rating = 5,
                title = "Excellent for Night SpO2 Monitoring",
                reviewText = "Crisp display and very comfortable finger clip. Reliable SpO2 readings.",
                verifiedPurchase = true,
                status = ReviewStatus.PUBLISHED
            )
        )
    )
    val reviews: StateFlow<List<MarketplaceReview>> = _reviews.asStateFlow()

    fun addReview(review: MarketplaceReview) {
        _reviews.value = listOf(review) + _reviews.value
    }

    // 11. Support Tickets
    private val _supportTickets = MutableStateFlow(
        listOf(
            MarketplaceSupportTicket(
                ticketId = "tkt_101",
                customerUid = currentCustomerUid,
                orderId = "ord_past_1",
                category = SupportCategory.DELIVERY,
                subject = "Estimated Delivery Window Query",
                description = "Would like to confirm if delivery requires physical signature upon arrival.",
                status = SupportStatus.IN_PROGRESS
            )
        )
    )
    val supportTickets: StateFlow<List<MarketplaceSupportTicket>> = _supportTickets.asStateFlow()

    fun addSupportTicket(ticket: MarketplaceSupportTicket) {
        _supportTickets.value = listOf(ticket) + _supportTickets.value
    }

    // 12. Recently Viewed
    private val _recentlyViewed = MutableStateFlow(listOf("prod_bp_monitor", "prod_pulse_ox", "prod_infra_thermo"))
    val recentlyViewed: StateFlow<List<String>> = _recentlyViewed.asStateFlow()

    fun recordView(productId: String) {
        val current = _recentlyViewed.value.filterNot { it == productId }
        _recentlyViewed.value = listOf(productId) + current
    }

    fun clearRecentlyViewed() {
        _recentlyViewed.value = emptyList()
    }

    // 13. Feature Flags
    var featureFlags = MarketplaceFeatureFlags()
}
