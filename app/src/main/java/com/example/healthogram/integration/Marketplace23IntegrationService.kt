package com.example.healthogram.integration

import com.example.healthogram.core.AccountType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * HEALTHOGRAM 2.3 — STEP 51: ADVANCED MARKETPLACE INTEGRATION SERVICE
 * Implements customer/seller workflows, inventory transaction safety,
 * server-side order validation, idempotency, and country category restrictions.
 */
class Marketplace23IntegrationService private constructor() {

    private val countryEngine = CountryConfigurationEngine.getInstance()

    // 1. Sellers Repository
    private val sellers = ConcurrentHashMap<String, SellerProfile23>()
    private val _sellersFlow = MutableStateFlow<Map<String, SellerProfile23>>(emptyMap())
    val sellersFlow: StateFlow<Map<String, SellerProfile23>> = _sellersFlow.asStateFlow()

    // 2. Products Repository
    private val products = ConcurrentHashMap<String, Product23>()
    private val _productsFlow = MutableStateFlow<Map<String, Product23>>(emptyMap())
    val productsFlow: StateFlow<Map<String, Product23>> = _productsFlow.asStateFlow()

    // 3. Orders Repository
    private val orders = ConcurrentHashMap<String, Order23>()
    private val _ordersFlow = MutableStateFlow<Map<String, Order23>>(emptyMap())
    val ordersFlow: StateFlow<Map<String, Order23>> = _ordersFlow.asStateFlow()

    // 4. Idempotency Key Store
    private val idempotencyStore = ConcurrentHashMap<String, String>() // idempotencyKey -> orderId

    // 5. Carts in memory (validated server-side before checkout)
    private val customerCarts = ConcurrentHashMap<String, MutableList<OrderItem23>>()

    init {
        seedInitialMarketplaceData()
    }

    companion object {
        @Volatile
        private var instance: Marketplace23IntegrationService? = null

        fun getInstance(): Marketplace23IntegrationService {
            return instance ?: synchronized(this) {
                instance ?: Marketplace23IntegrationService().also { instance = it }
            }
        }
    }

    private fun seedInitialMarketplaceData() {
        // Initial verified seller
        val seller1 = SellerProfile23(
            sellerId = "seller_wellness_sa",
            ownerUserUid = "usr_seller_001",
            sellerType = SellerAccountType.BUSINESS_SELLER,
            storeName = "Al-Shifa Wellness & Vitality",
            storeDescription = "Authorized distributor of licensed vitamins, ergonomic physical therapy gear, and wearable health monitors.",
            legalName = "Al-Shifa Health Trading Est.",
            countryCode = "SA",
            businessAddress = "King Fahd Road, Al-Olaya, Riyadh 12214",
            taxNumber = "300129382900003",
            commercialRegistrationNumber = "1010482910",
            verificationStatus = SellerVerificationStatus.VERIFIED,
            payoutIban = "SA0380000000608010167519",
            payoutBankName = "Al Rajhi Bank",
            verifiedAt = System.currentTimeMillis() - 86400000L * 30
        )
        sellers[seller1.sellerId] = seller1

        // Initial active products
        val prod1 = Product23(
            productId = "prod_smart_bpm_01",
            sellerId = seller1.sellerId,
            name = "Clinical Bluetooth Blood Pressure Monitor",
            description = "Clinically validated upper-arm digital sphygmomanometer with instant Healthogram Health Passport sync.",
            category = "WELLNESS_TRACKERS",
            sku = "BPM-BT-001",
            images = listOf("https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800"),
            countryCode = "SA",
            priceMinor = 24900L, // 249.00 SAR
            salePriceMinor = 19900L, // 199.00 SAR
            currency = "SAR",
            stockQuantity = 45,
            reservedQuantity = 2,
            status = ProductLifecycleStatus.ACTIVE,
            moderationReason = "Approved by Clinical Product Safety Team",
            moderatorUid = "usr_moderator_001"
        )

        val prod2 = Product23(
            productId = "prod_vit_d3_02",
            sellerId = seller1.sellerId,
            name = "Liposomal Vitamin D3 + K2 (5000 IU Drops)",
            description = "High-bioavailability liquid vitamin D3 with K2 for bone and immune support. Third-party lab tested.",
            category = "VITAMINS_SUPPLEMENTS",
            sku = "VIT-D3K2-50ML",
            images = listOf("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=800"),
            countryCode = "SA",
            priceMinor = 8900L, // 89.00 SAR
            currency = "SAR",
            stockQuantity = 120,
            reservedQuantity = 0,
            status = ProductLifecycleStatus.ACTIVE,
            moderationReason = "Compliant dietary supplement",
            moderatorUid = "usr_moderator_001"
        )

        products[prod1.productId] = prod1
        products[prod2.productId] = prod2

        refreshFlows()
    }

    private fun refreshFlows() {
        _sellersFlow.value = sellers.toMap()
        _productsFlow.value = products.toMap()
        _ordersFlow.value = orders.toMap()
    }

    // -------------------------------------------------------------------------
    // ACCOUNT MODEL ISOLATION & SELLER ONBOARDING (Section 2, 5, 6)
    // -------------------------------------------------------------------------

    /**
     * Invariant: Main healthcare accounts remain: INDIVIDUAL, DOCTOR, CLINIC, HOSPITAL, LABORATORY.
     * Marketplace roles are strictly CUSTOMER or SELLER (INDIVIDUAL_SELLER / BUSINESS_SELLER).
     */
    fun registerSeller(
        userUid: String,
        userAccountType: AccountType,
        sellerType: SellerAccountType,
        storeName: String,
        storeDescription: String,
        legalName: String,
        countryCode: String,
        businessAddress: String,
        taxNumber: String?,
        crNumber: String?,
        payoutIban: String?,
        payoutBank: String?,
        documents: List<SellerVerificationDocument>
    ): Result<SellerProfile23> {
        val countryConfig = countryEngine.getMarketplaceConfig(countryCode)
        if (!countryConfig.marketplaceEnabled || !countryConfig.sellerRegistrationEnabled) {
            return Result.failure(IllegalStateException("Seller registration is currently suspended in country $countryCode"))
        }

        val sellerId = "seller_${UUID.randomUUID().toString().substring(0, 8)}"
        val profile = SellerProfile23(
            sellerId = sellerId,
            ownerUserUid = userUid,
            sellerType = sellerType,
            storeName = storeName,
            storeDescription = storeDescription,
            legalName = legalName,
            countryCode = countryCode,
            businessAddress = businessAddress,
            taxNumber = taxNumber,
            commercialRegistrationNumber = crNumber,
            verificationStatus = SellerVerificationStatus.SUBMITTED,
            documents = documents,
            payoutIban = payoutIban,
            payoutBankName = payoutBank
        )
        sellers[sellerId] = profile
        refreshFlows()
        return Result.success(profile)
    }

    fun reviewSellerOnboarding(
        sellerId: String,
        approved: Boolean,
        moderatorUid: String,
        rejectionReason: String? = null
    ): Boolean {
        val seller = sellers[sellerId] ?: return false
        val updated = if (approved) {
            seller.copy(
                verificationStatus = SellerVerificationStatus.VERIFIED,
                rejectionReason = null,
                verifiedAt = System.currentTimeMillis()
            )
        } else {
            seller.copy(
                verificationStatus = SellerVerificationStatus.REJECTED,
                rejectionReason = rejectionReason ?: "Missing or unverifiable regulatory documents"
            )
        }
        sellers[sellerId] = updated
        refreshFlows()
        return true
    }

    // -------------------------------------------------------------------------
    // PRODUCT LISTING & MODERATION (Section 7, 8)
    // -------------------------------------------------------------------------

    fun createProductListing(
        sellerId: String,
        name: String,
        description: String,
        category: String,
        sku: String,
        images: List<String>,
        countryCode: String,
        priceMinor: Long,
        salePriceMinor: Long?,
        currency: String,
        stockQuantity: Int,
        returnPolicyDays: Int = 14
    ): Result<Product23> {
        val seller = sellers[sellerId] ?: return Result.failure(IllegalArgumentException("Seller not found"))
        if (seller.verificationStatus != SellerVerificationStatus.VERIFIED) {
            return Result.failure(IllegalStateException("Unverified sellers cannot list products"))
        }

        val countryConfig = countryEngine.getMarketplaceConfig(countryCode)
        if (countryConfig.restrictedProductCategories.contains(category)) {
            return Result.failure(IllegalArgumentException("Category $category is legally restricted in country $countryCode"))
        }

        val productId = "prod_${UUID.randomUUID().toString().substring(0, 8)}"
        val product = Product23(
            productId = productId,
            sellerId = sellerId,
            name = name,
            description = description,
            category = category,
            sku = sku,
            images = images,
            countryCode = countryCode,
            priceMinor = priceMinor,
            salePriceMinor = salePriceMinor,
            currency = currency,
            stockQuantity = stockQuantity,
            reservedQuantity = 0,
            status = ProductLifecycleStatus.PENDING_REVIEW, // All new listings require moderation
            returnPolicyDays = returnPolicyDays
        )

        products[productId] = product
        refreshFlows()
        return Result.success(product)
    }

    fun moderateProduct(
        productId: String,
        approved: Boolean,
        moderatorUid: String,
        reason: String
    ): Boolean {
        val product = products[productId] ?: return false
        val updated = if (approved) {
            product.copy(
                status = ProductLifecycleStatus.ACTIVE,
                moderationReason = reason,
                moderatorUid = moderatorUid,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            product.copy(
                status = ProductLifecycleStatus.REJECTED,
                moderationReason = reason,
                moderatorUid = moderatorUid,
                updatedAt = System.currentTimeMillis()
            )
        }
        products[productId] = updated
        refreshFlows()
        return true
    }

    // -------------------------------------------------------------------------
    // TRANSACTIONAL INVENTORY MANAGEMENT (Section 9)
    // Formula: available = stock - reserved. Prevents overselling.
    // -------------------------------------------------------------------------

    @Synchronized
    fun reserveStock(productId: String, quantity: Int): Boolean {
        val product = products[productId] ?: return false
        if (product.status != ProductLifecycleStatus.ACTIVE) return false
        if (product.availableQuantity < quantity) return false

        val updated = product.copy(
            reservedQuantity = product.reservedQuantity + quantity,
            updatedAt = System.currentTimeMillis()
        )
        products[productId] = updated
        refreshFlows()
        return true
    }

    @Synchronized
    fun commitReservedStock(productId: String, quantity: Int): Boolean {
        val product = products[productId] ?: return false
        val newStock = (product.stockQuantity - quantity).coerceAtLeast(0)
        val newReserved = (product.reservedQuantity - quantity).coerceAtLeast(0)
        val newStatus = if (newStock == 0) ProductLifecycleStatus.OUT_OF_STOCK else product.status

        products[productId] = product.copy(
            stockQuantity = newStock,
            reservedQuantity = newReserved,
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        refreshFlows()
        return true
    }

    @Synchronized
    fun releaseReservedStock(productId: String, quantity: Int): Boolean {
        val product = products[productId] ?: return false
        val newReserved = (product.reservedQuantity - quantity).coerceAtLeast(0)
        products[productId] = product.copy(
            reservedQuantity = newReserved,
            updatedAt = System.currentTimeMillis()
        )
        refreshFlows()
        return true
    }

    // -------------------------------------------------------------------------
    // CART & SERVER-SIDE CHECKOUT VALIDATION (Section 10, 11, 12)
    // Never trusts cached cart values.
    // -------------------------------------------------------------------------

    data class CartValidationResult(
        val isValid: Boolean,
        val validatedItems: List<OrderItem23>,
        val subtotalMinor: Long,
        val deliveryFeeMinor: Long,
        val taxMinor: Long,
        val totalMinor: Long,
        val currency: String,
        val validationErrors: List<String>
    )

    fun validateCartAndCalculateTotals(
        customerUid: String,
        items: List<Pair<String, Int>>, // productId -> quantity
        countryCode: String
    ): CartValidationResult {
        val errors = mutableListOf<String>()
        val validatedItems = mutableListOf<OrderItem23>()
        var subtotal = 0L

        val countryConfig = countryEngine.getMarketplaceConfig(countryCode)
        if (!countryConfig.marketplaceEnabled) {
            return CartValidationResult(false, emptyList(), 0, 0, 0, 0, countryConfig.currency, listOf("Marketplace disabled in $countryCode"))
        }

        for ((productId, qty) in items) {
            val product = products[productId]
            if (product == null) {
                errors.add("Product $productId is no longer available")
                continue
            }

            if (product.status != ProductLifecycleStatus.ACTIVE) {
                errors.add("Product ${product.name} is currently inactive or out of stock")
                continue
            }

            if (product.availableQuantity < qty) {
                errors.add("Insufficient stock for ${product.name}. Available: ${product.availableQuantity}")
                continue
            }

            val seller = sellers[product.sellerId]
            if (seller == null || seller.verificationStatus != SellerVerificationStatus.VERIFIED) {
                errors.add("Seller for ${product.name} is not currently active")
                continue
            }

            val effectiveUnitPrice = product.salePriceMinor ?: product.priceMinor
            val lineTotal = effectiveUnitPrice * qty
            subtotal += lineTotal

            validatedItems.add(
                OrderItem23(
                    productId = product.productId,
                    productName = product.name,
                    sellerId = product.sellerId,
                    unitPriceMinor = effectiveUnitPrice,
                    quantity = qty,
                    lineTotalMinor = lineTotal
                )
            )
        }

        val deliveryFee = if (subtotal > 0) 2500L else 0L // 25.00 SAR baseline
        val tax = (subtotal * (countryConfig.taxRatePercent / 100.0)).toLong()
        val total = subtotal + deliveryFee + tax

        return CartValidationResult(
            isValid = errors.isEmpty() && validatedItems.isNotEmpty(),
            validatedItems = validatedItems,
            subtotalMinor = subtotal,
            deliveryFeeMinor = deliveryFee,
            taxMinor = tax,
            totalMinor = total,
            currency = countryConfig.currency,
            validationErrors = errors
        )
    }

    // -------------------------------------------------------------------------
    // ORDER CREATION WITH IDEMPOTENCY (Section 11, 12)
    // -------------------------------------------------------------------------

    @Synchronized
    fun createOrder(
        customerUid: String,
        items: List<Pair<String, Int>>,
        countryCode: String,
        deliveryAddress: String,
        deliveryProviderId: String,
        idempotencyKey: String
    ): Result<Order23> {
        // Idempotency check: prevent duplicate checkout on double tap or network retry
        val existingOrderId = idempotencyStore[idempotencyKey]
        if (existingOrderId != null) {
            val existingOrder = orders[existingOrderId]
            if (existingOrder != null) {
                return Result.success(existingOrder)
            }
        }

        val validation = validateCartAndCalculateTotals(customerUid, items, countryCode)
        if (!validation.isValid) {
            return Result.failure(IllegalStateException("Cart validation failed: ${validation.validationErrors.joinToString(", ")}"))
        }

        // Reserve stock for all items atomically
        for (item in validation.validatedItems) {
            val reserved = reserveStock(item.productId, item.quantity)
            if (!reserved) {
                // Rollback previous reservations in this cart
                for (prev in validation.validatedItems) {
                    if (prev.productId == item.productId) break
                    releaseReservedStock(prev.productId, prev.quantity)
                }
                return Result.failure(IllegalStateException("Could not reserve stock for item ${item.productName}"))
            }
        }

        val platformFee = (validation.subtotalMinor * 0.10).toLong() // 10% platform commission
        val sellerNet = validation.subtotalMinor - platformFee

        val order = Order23(
            customerUid = customerUid,
            sellerId = validation.validatedItems.first().sellerId,
            items = validation.validatedItems,
            subtotalMinor = validation.subtotalMinor,
            deliveryFeeMinor = validation.deliveryFeeMinor,
            taxMinor = validation.taxMinor,
            platformFeeMinor = platformFee,
            sellerNetMinor = sellerNet,
            totalAmountMinor = validation.totalMinor,
            currency = validation.currency,
            countryCode = countryCode,
            deliveryAddress = deliveryAddress,
            deliveryProviderId = deliveryProviderId,
            status = OrderState23.PENDING_PAYMENT,
            idempotencyKey = idempotencyKey
        )

        orders[order.orderId] = order
        idempotencyStore[idempotencyKey] = order.orderId
        refreshFlows()
        return Result.success(order)
    }

    // -------------------------------------------------------------------------
    // ORDER 16-STATE LIFECYCLE TRANSITIONS (Section 11)
    // -------------------------------------------------------------------------

    fun transitionOrderStatus(orderId: String, newStatus: OrderState23, reason: String? = null): Result<Order23> {
        val currentOrder = orders[orderId] ?: return Result.failure(IllegalArgumentException("Order not found"))

        // Controlled server-side state transitions
        val isValidTransition = when (currentOrder.status) {
            OrderState23.PENDING_PAYMENT -> newStatus in listOf(OrderState23.PAID, OrderState23.CANCELLED, OrderState23.FAILED)
            OrderState23.PAID -> newStatus in listOf(OrderState23.CONFIRMED, OrderState23.CANCELLED, OrderState23.REFUND_PENDING)
            OrderState23.CONFIRMED -> newStatus in listOf(OrderState23.PROCESSING, OrderState23.CANCEL_REQUESTED)
            OrderState23.PROCESSING -> newStatus in listOf(OrderState23.READY_FOR_PICKUP, OrderState23.SHIPPED, OrderState23.CANCEL_REQUESTED)
            OrderState23.READY_FOR_PICKUP -> newStatus in listOf(OrderState23.SHIPPED, OrderState23.OUT_FOR_DELIVERY)
            OrderState23.SHIPPED -> newStatus in listOf(OrderState23.OUT_FOR_DELIVERY, OrderState23.DELIVERED)
            OrderState23.OUT_FOR_DELIVERY -> newStatus in listOf(OrderState23.DELIVERED, OrderState23.FAILED)
            OrderState23.DELIVERED -> newStatus in listOf(OrderState23.RETURN_REQUESTED, OrderState23.DISPUTED)
            OrderState23.CANCEL_REQUESTED -> newStatus in listOf(OrderState23.CANCELLED, OrderState23.PROCESSING)
            OrderState23.RETURN_REQUESTED -> newStatus in listOf(OrderState23.RETURNED, OrderState23.DELIVERED)
            OrderState23.RETURNED -> newStatus in listOf(OrderState23.REFUND_PENDING)
            OrderState23.REFUND_PENDING -> newStatus in listOf(OrderState23.REFUNDED, OrderState23.FAILED)
            OrderState23.CANCELLED, OrderState23.REFUNDED, OrderState23.FAILED -> false
            OrderState23.DISPUTED -> newStatus in listOf(OrderState23.REFUNDED, OrderState23.DELIVERED)
        }

        if (!isValidTransition) {
            return Result.failure(IllegalStateException("Invalid order transition from ${currentOrder.status} to $newStatus"))
        }

        // Handle inventory commits or releases on terminal transitions
        if (newStatus == OrderState23.PAID) {
            for (item in currentOrder.items) {
                commitReservedStock(item.productId, item.quantity)
            }
        } else if (newStatus == OrderState23.CANCELLED || newStatus == OrderState23.FAILED) {
            if (currentOrder.status == OrderState23.PENDING_PAYMENT) {
                for (item in currentOrder.items) {
                    releaseReservedStock(item.productId, item.quantity)
                }
            }
        }

        val updatedOrder = currentOrder.copy(
            status = newStatus,
            deliveredAt = if (newStatus == OrderState23.DELIVERED) System.currentTimeMillis() else currentOrder.deliveredAt,
            refundWindowExpiresAt = if (newStatus == OrderState23.DELIVERED) System.currentTimeMillis() + (14L * 86400000L) else currentOrder.refundWindowExpiresAt,
            updatedAt = System.currentTimeMillis()
        )

        orders[orderId] = updatedOrder
        refreshFlows()
        return Result.success(updatedOrder)
    }

    fun getOrder(orderId: String): Order23? = orders[orderId]
    fun getProduct(productId: String): Product23? = products[productId]
    fun getSeller(sellerId: String): SellerProfile23? = sellers[sellerId]
}
