package com.example.healthogram.marketplace

/**
 * HEALTHOGRAM — STEP 08: MARKETPLACE SECURITY, VALIDATION & INTEGRITY CONTROLS
 *
 * Enforces server-side authority, medical claim protection, inventory safety,
 * financial correctness, and strict isolation from Health Passport records.
 */

data class CartValidationResult(
    val isValid: Boolean,
    val validatedSubtotal: Double,
    val validatedTax: Double,
    val validatedDelivery: Double,
    val validatedDiscount: Double,
    val authoritativeGrandTotal: Double,
    val stockErrors: List<String> = emptyList(),
    val complianceErrors: List<String> = emptyList(),
    val currency: String = "SAR"
)

data class CouponValidationResult(
    val isValid: Boolean,
    val discountAmount: Double,
    val message: String
)

data class MedicalClaimCheckResult(
    val isPermitted: Boolean,
    val flaggedPhrases: List<String>,
    val recommendation: String
)

object MarketplaceSecurityAndValidation {

    // 1. Prohibited Medical Claims List (Section 14)
    private val PROHIBITED_MEDICAL_CLAIMS = listOf(
        "cures cancer",
        "guaranteed treatment",
        "100% cures disease",
        "miracle cure",
        "cures diabetes",
        "guaranteed cure",
        "replaces chemotherapy",
        "reverses all aging",
        "eradicates tumor",
        "treats any infection without doctor",
        "instant covid cure",
        "prevents all heart attacks"
    )

    /**
     * Checks text for unsupported or dangerous medical claims.
     */
    fun checkMedicalClaims(text: String): MedicalClaimCheckResult {
        val lower = text.lowercase()
        val flagged = PROHIBITED_MEDICAL_CLAIMS.filter { lower.contains(it) }
        return if (flagged.isNotEmpty()) {
            MedicalClaimCheckResult(
                isPermitted = false,
                flaggedPhrases = flagged,
                recommendation = "Content violates healthcare marketplace policy by making unsupported medical claims."
            )
        } else {
            MedicalClaimCheckResult(
                isPermitted = true,
                flaggedPhrases = emptyList(),
                recommendation = "Approved: No prohibited medical claims detected."
            )
        }
    }

    /**
     * 2. Authoritative Cart & Checkout Calculation (Section 20, 24, 25, 56)
     * Server strictly calculates totals based on current product records.
     */
    fun validateAndCalculateCheckout(
        items: List<MarketplaceCartItem>,
        productCatalog: Map<String, MarketplaceProduct>,
        appliedCoupon: MarketplaceCoupon? = null,
        countryConfig: MarketplaceCountryConfig = MarketplaceCountryConfig()
    ): CartValidationResult {
        val stockErrors = mutableListOf<String>()
        val complianceErrors = mutableListOf<String>()
        var subtotal = 0.0

        for (item in items) {
            val liveProduct = productCatalog[item.productId]
            if (liveProduct == null) {
                stockErrors.add("Product ${item.productTitleSnapshot} is no longer available.")
                continue
            }

            // Verify active status (Section 10)
            if (liveProduct.status != ProductStatus.ACTIVE) {
                complianceErrors.add("Product ${liveProduct.title} is currently inactive.")
            }

            // Verify stock (Section 22)
            if (liveProduct.stockQuantity < item.quantity) {
                stockErrors.add("Insufficient stock for ${liveProduct.title}. Available: ${liveProduct.stockQuantity}, Requested: ${item.quantity}")
            }

            // Authoritative price snapshot re-verification
            val effectiveUnitPrice = liveProduct.discountPrice ?: liveProduct.price
            subtotal += effectiveUnitPrice * item.quantity
        }

        // Coupon calculation
        var discount = 0.0
        if (appliedCoupon != null && subtotal >= appliedCoupon.minimumOrderValue) {
            discount = when (appliedCoupon.discountType) {
                DiscountType.PERCENTAGE -> (subtotal * (appliedCoupon.discountValue / 100.0)).coerceAtMost(appliedCoupon.maximumDiscount)
                DiscountType.FIXED_AMOUNT -> appliedCoupon.discountValue.coerceAtMost(subtotal)
            }
        }

        // Delivery calculation: Standard 15 SAR, free over 200 SAR
        val delivery = if (subtotal >= 200.0 || items.isEmpty()) 0.0 else 15.0

        // Tax calculation: 15% VAT for Saudi Arabia if tax_enabled
        val taxableAmount = (subtotal - discount).coerceAtLeast(0.0)
        val tax = if (countryConfig.taxEnabled) (taxableAmount * 0.15) else 0.0

        val grandTotal = (taxableAmount + delivery + tax)

        val isValid = stockErrors.isEmpty() && complianceErrors.isEmpty() && items.isNotEmpty()

        return CartValidationResult(
            isValid = isValid,
            validatedSubtotal = Math.round(subtotal * 100.0) / 100.0,
            validatedTax = Math.round(tax * 100.0) / 100.0,
            validatedDelivery = delivery,
            validatedDiscount = Math.round(discount * 100.0) / 100.0,
            authoritativeGrandTotal = Math.round(grandTotal * 100.0) / 100.0,
            stockErrors = stockErrors,
            complianceErrors = complianceErrors,
            currency = countryConfig.currency
        )
    }

    /**
     * 3. Coupon Validation (Section 32)
     */
    fun validateCoupon(
        coupon: MarketplaceCoupon,
        subtotal: Double,
        customerUid: String,
        customerUsageCount: Int = 0
    ): CouponValidationResult {
        val now = System.currentTimeMillis()
        if (coupon.status != "active") {
            return CouponValidationResult(false, 0.0, "Coupon is not active.")
        }
        if (now < coupon.startAt || now > coupon.endAt) {
            return CouponValidationResult(false, 0.0, "Coupon has expired or is not yet valid.")
        }
        if (subtotal < coupon.minimumOrderValue) {
            return CouponValidationResult(false, 0.0, "Minimum order of ${coupon.minimumOrderValue} SAR required.")
        }
        if (customerUsageCount >= coupon.perCustomerLimit) {
            return CouponValidationResult(false, 0.0, "You have reached the redemption limit for this coupon.")
        }

        val discount = when (coupon.discountType) {
            DiscountType.PERCENTAGE -> (subtotal * (coupon.discountValue / 100.0)).coerceAtMost(coupon.maximumDiscount)
            DiscountType.FIXED_AMOUNT -> coupon.discountValue.coerceAtMost(subtotal)
        }

        return CouponValidationResult(true, discount, "Coupon applied successfully!")
    }

    /**
     * 4. Order Status State Machine (Section 39)
     * Validates permitted transitions between order states.
     */
    fun isPermittedOrderTransition(
        current: MarketplaceOrderStatus,
        next: MarketplaceOrderStatus
    ): Boolean {
        if (current == next) return true
        return when (current) {
            MarketplaceOrderStatus.PENDING_PAYMENT -> next in listOf(
                MarketplaceOrderStatus.PAID,
                MarketplaceOrderStatus.CANCELLED,
                MarketplaceOrderStatus.FAILED
            )
            MarketplaceOrderStatus.PAID -> next in listOf(
                MarketplaceOrderStatus.CONFIRMED,
                MarketplaceOrderStatus.CANCEL_REQUESTED,
                MarketplaceOrderStatus.CANCELLED,
                MarketplaceOrderStatus.REFUND_PENDING
            )
            MarketplaceOrderStatus.CONFIRMED -> next in listOf(
                MarketplaceOrderStatus.PROCESSING,
                MarketplaceOrderStatus.CANCEL_REQUESTED,
                MarketplaceOrderStatus.CANCELLED
            )
            MarketplaceOrderStatus.PROCESSING -> next in listOf(
                MarketplaceOrderStatus.PACKED,
                MarketplaceOrderStatus.CANCEL_REQUESTED
            )
            MarketplaceOrderStatus.PACKED -> next in listOf(
                MarketplaceOrderStatus.SHIPPED
            )
            MarketplaceOrderStatus.SHIPPED -> next in listOf(
                MarketplaceOrderStatus.OUT_FOR_DELIVERY
            )
            MarketplaceOrderStatus.OUT_FOR_DELIVERY -> next in listOf(
                MarketplaceOrderStatus.DELIVERED,
                MarketplaceOrderStatus.FAILED
            )
            MarketplaceOrderStatus.DELIVERED -> next in listOf(
                MarketplaceOrderStatus.COMPLETED,
                MarketplaceOrderStatus.RETURN_REQUESTED
            )
            MarketplaceOrderStatus.CANCEL_REQUESTED -> next in listOf(
                MarketplaceOrderStatus.CANCELLED,
                MarketplaceOrderStatus.PROCESSING
            )
            MarketplaceOrderStatus.RETURN_REQUESTED -> next in listOf(
                MarketplaceOrderStatus.RETURNED,
                MarketplaceOrderStatus.COMPLETED
            )
            MarketplaceOrderStatus.RETURNED -> next in listOf(
                MarketplaceOrderStatus.REFUND_PENDING,
                MarketplaceOrderStatus.REFUNDED
            )
            MarketplaceOrderStatus.REFUND_PENDING -> next in listOf(
                MarketplaceOrderStatus.REFUNDED,
                MarketplaceOrderStatus.FAILED
            )
            MarketplaceOrderStatus.COMPLETED,
            MarketplaceOrderStatus.CANCELLED,
            MarketplaceOrderStatus.REFUNDED,
            MarketplaceOrderStatus.FAILED -> false // Terminal states
        }
    }

    /**
     * 5. Address Privacy Filter (Section 27)
     * Strips non-fulfillment private address metadata before passing to merchant.
     */
    fun sanitizeAddressForSeller(address: MarketplaceAddress): MarketplaceAddress {
        return address.copy(
            latitude = null,
            longitude = null,
            isDefault = false
        )
    }

    /**
     * 6. CRITICAL PRIVACY BOUNDARY: Health Passport Isolation (Section 50, 53, 69, 72, 76)
     * Throws an explicit SecurityException if any clinical Health Passport collection
     * is queried or passed to marketplace recommendation engines.
     */
    fun verifyNoHealthPassportLeakage(payload: Map<String, Any?>): Boolean {
        val prohibitedHealthFields = listOf(
            "health_profiles",
            "health_conditions",
            "health_medications",
            "health_lab_reports",
            "health_prescriptions",
            "health_documents",
            "health_access_grants",
            "health_access_logs",
            "diagnosis",
            "medications",
            "lab_results",
            "medical_history"
        )
        for (prohibited in prohibitedHealthFields) {
            if (payload.containsKey(prohibited)) {
                throw SecurityException("CRITICAL PRIVACY VIOLATION: Clinical Health Passport data '$prohibited' is strictly prohibited from marketplace systems.")
            }
        }
        return true
    }

    /**
     * 7. Client Write Permission Simulator (Section 54, 70)
     * Ensures clients cannot modify price, stock, seller UID, payment status or verification status.
     */
    fun validateClientOrderWrite(
        incomingChanges: Map<String, Any?>,
        isClientRequest: Boolean = true
    ): Boolean {
        if (!isClientRequest) return true
        val protectedFields = listOf(
            "subtotal",
            "tax_total",
            "delivery_total",
            "grand_total",
            "payment_status",
            "order_status",
            "seller_uid",
            "unit_price",
            "verified_purchase"
        )
        for (field in protectedFields) {
            if (incomingChanges.containsKey(field)) {
                return false // Client direct write of protected financial/status fields is blocked
            }
        }
        return true
    }
}
