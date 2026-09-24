package com.example.healthogram.marketplace

import java.util.UUID

/**
 * HEALTHOGRAM — STEP 08: FLUTTERFLOW CUSTOM ACTIONS (Section 59)
 * Secure client-side invocations interfacing with backend validations.
 */
object MarketplaceCustomActions {

    /**
     * 1. AddToMarketplaceCart Action
     */
    fun addToMarketplaceCart(
        currentItems: List<MarketplaceCartItem>,
        product: MarketplaceProduct,
        customerUid: String,
        quantity: Int = 1
    ): List<MarketplaceCartItem> {
        val existingIndex = currentItems.indexOfFirst { it.productId == product.productId }
        val unitPrice = product.discountPrice ?: product.price
        return if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stockQuantity)
            currentItems.toMutableList().apply {
                this[existingIndex] = existing.copy(
                    quantity = newQty,
                    lineTotal = unitPrice * newQty,
                    updatedAt = System.currentTimeMillis()
                )
            }
        } else {
            val newItem = MarketplaceCartItem(
                itemId = "cart_item_${UUID.randomUUID().toString().take(8)}",
                cartId = "cart_${customerUid}",
                customerUid = customerUid,
                productId = product.productId,
                sellerUid = product.sellerUid,
                productTitleSnapshot = product.title,
                productImageSnapshot = product.images.firstOrNull() ?: "",
                unitPriceSnapshot = unitPrice,
                quantity = quantity.coerceAtMost(product.stockQuantity),
                lineTotal = unitPrice * quantity,
                currency = product.currency
            )
            currentItems + newItem
        }
    }

    /**
     * 2. UpdateCartQuantity Action
     */
    fun updateCartQuantity(
        currentItems: List<MarketplaceCartItem>,
        itemId: String,
        newQuantity: Int
    ): List<MarketplaceCartItem> {
        return if (newQuantity <= 0) {
            currentItems.filterNot { it.itemId == itemId }
        } else {
            currentItems.map { item ->
                if (item.itemId == itemId) {
                    item.copy(
                        quantity = newQuantity,
                        lineTotal = item.unitPriceSnapshot * newQuantity,
                        updatedAt = System.currentTimeMillis()
                    )
                } else item
            }
        }
    }

    /**
     * 3. ValidateCheckout Action
     */
    fun validateCheckout(
        items: List<MarketplaceCartItem>,
        catalog: Map<String, MarketplaceProduct>,
        coupon: MarketplaceCoupon?
    ): CartValidationResult {
        return MarketplaceSecurityAndValidation.validateAndCalculateCheckout(items, catalog, coupon)
    }

    /**
     * 4. ApplyMarketplaceCoupon Action
     */
    fun applyMarketplaceCoupon(
        code: String,
        subtotal: Double,
        customerUid: String,
        couponsList: List<MarketplaceCoupon>
    ): Pair<MarketplaceCoupon?, CouponValidationResult> {
        val found = couponsList.find { it.code.equals(code.trim(), ignoreCase = true) }
            ?: return null to CouponValidationResult(false, 0.0, "Coupon code not found.")

        val validation = MarketplaceSecurityAndValidation.validateCoupon(found, subtotal, customerUid)
        return if (validation.isValid) found to validation else null to validation
    }

    /**
     * 5. CreateMarketplaceOrder Action
     */
    fun createMarketplaceOrder(
        customerUid: String,
        cartItems: List<MarketplaceCartItem>,
        shippingAddress: MarketplaceAddress,
        validationResult: CartValidationResult
    ): MarketplaceOrderSnapshot {
        require(validationResult.isValid) { "Cannot create order from invalid checkout validation." }

        return MarketplaceOrderSnapshot(
            orderId = "ord_${UUID.randomUUID().toString().take(10)}",
            customerUid = customerUid,
            orderNumber = "HGM-${System.currentTimeMillis().toString().takeLast(7)}",
            currency = validationResult.currency,
            subtotal = validationResult.validatedSubtotal,
            discountTotal = validationResult.validatedDiscount,
            deliveryTotal = validationResult.validatedDelivery,
            taxTotal = validationResult.validatedTax,
            grandTotal = validationResult.authoritativeGrandTotal,
            paymentStatus = PaymentStatus.PENDING,
            orderStatus = MarketplaceOrderStatus.PENDING_PAYMENT,
            shippingAddressSnapshot = shippingAddress,
            billingAddressSnapshot = shippingAddress
        )
    }

    /**
     * 6. StartMarketplacePayment Action
     */
    suspend fun startMarketplacePayment(
        order: MarketplaceOrderSnapshot,
        paymentProvider: PaymentProvider,
        paymentMethodType: String = "MADA"
    ): PaymentIntentResult {
        val request = PaymentIntentRequest(
            orderId = order.orderId,
            customerUid = order.customerUid,
            amount = order.grandTotal,
            currency = order.currency,
            paymentMethodType = paymentMethodType
        )
        return paymentProvider.createPayment(request)
    }

    /**
     * 7. TrackMarketplaceOrder Action
     */
    suspend fun trackMarketplaceOrder(
        trackingNumber: String,
        deliveryProvider: DeliveryProvider
    ): ShipmentInfo {
        return deliveryProvider.trackShipment(trackingNumber)
    }

    /**
     * 8. RequestMarketplaceReturn Action
     */
    fun requestMarketplaceReturn(
        orderId: String,
        orderItemId: String,
        customerUid: String,
        sellerUid: String,
        reasonCode: String,
        description: String,
        photos: List<String>
    ): MarketplaceReturn {
        return MarketplaceReturn(
            orderId = orderId,
            orderItemId = orderItemId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            reasonCode = reasonCode,
            description = description,
            photos = photos,
            status = ReturnStatus.REQUESTED
        )
    }

    /**
     * 9. RequestMarketplaceRefund Action
     */
    fun requestMarketplaceRefund(
        orderId: String,
        paymentId: String,
        customerUid: String,
        amount: Double,
        currency: String = "SAR",
        reason: String
    ): MarketplaceRefund {
        return MarketplaceRefund(
            orderId = orderId,
            paymentId = paymentId,
            customerUid = customerUid,
            amount = amount,
            currency = currency,
            reason = reason,
            status = RefundStatus.PENDING
        )
    }

    /**
     * 10. GenerateMarketplaceInvoice Action
     */
    fun generateMarketplaceInvoice(
        order: MarketplaceOrderSnapshot,
        items: List<MarketplaceOrderItem>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine("       HEALTHOGRAM MARKETPLACE          ")
        sb.appendLine("          OFFICIAL TAX INVOICE          ")
        sb.appendLine("========================================")
        sb.appendLine("Invoice No: INV-${order.orderNumber}")
        sb.appendLine("Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(order.createdAt))}")
        sb.appendLine("Customer ID: ${order.customerUid}")
        sb.appendLine("Recipient: ${order.shippingAddressSnapshot.fullName}")
        sb.appendLine("Delivery: ${order.shippingAddressSnapshot.addressLine1}, ${order.shippingAddressSnapshot.city}")
        sb.appendLine("Phone: ${order.shippingAddressSnapshot.phone}")
        sb.appendLine("----------------------------------------")
        sb.appendLine("ITEMS:")
        items.forEachIndexed { i, itm ->
            sb.appendLine("${i + 1}. ${itm.productTitleSnapshot}")
            sb.appendLine("   Qty: ${itm.quantity} x ${itm.unitPrice} = ${itm.lineTotal} ${order.currency}")
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine("Subtotal:       ${order.subtotal} ${order.currency}")
        sb.appendLine("Discount:      -${order.discountTotal} ${order.currency}")
        sb.appendLine("Delivery Fee:   ${order.deliveryTotal} ${order.currency}")
        sb.appendLine("VAT (15%):      ${order.taxTotal} ${order.currency}")
        sb.appendLine("GRAND TOTAL:    ${order.grandTotal} ${order.currency}")
        sb.appendLine("Payment Status: ${order.paymentStatus.label}")
        sb.appendLine("Order Status:   ${order.orderStatus.label}")
        sb.appendLine("========================================")
        sb.appendLine("Thank you for choosing Healthogram!")
        return sb.toString()
    }

    /**
     * 11. AddMarketplaceReview Action
     */
    fun addMarketplaceReview(
        productId: String,
        orderId: String,
        orderItemId: String,
        customerUid: String,
        sellerUid: String,
        rating: Int,
        title: String,
        reviewText: String,
        images: List<String> = emptyList()
    ): MarketplaceReview {
        // Moderation check on medical claims
        val check = MarketplaceSecurityAndValidation.checkMedicalClaims(reviewText)
        require(check.isPermitted) { "Review contains prohibited medical claims: ${check.flaggedPhrases.joinToString()}" }

        return MarketplaceReview(
            productId = productId,
            orderId = orderId,
            orderItemId = orderItemId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            rating = rating.coerceIn(1, 5),
            title = title,
            reviewText = reviewText,
            images = images,
            verifiedPurchase = true,
            status = ReviewStatus.PUBLISHED
        )
    }
}
