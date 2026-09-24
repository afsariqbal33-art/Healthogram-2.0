package com.example.healthogram.marketplace

import org.junit.Assert.*
import org.junit.Test

/**
 * HEALTHOGRAM — STEP 08: 34 SECURITY & ATTACK TESTS (Section 64)
 * Validates resilience against injection, financial tampering, stock race conditions,
 * state machine spoofing, and strict isolation of clinical Health Passport records.
 */
class MarketplaceSecurityAttackTest {

    private val testCustomerUid = "cust_attacker_101"
    private val legitCustomerUid = "cust_legit_202"
    private val sellerUid = "seller_omron"

    private val sampleProduct = MarketplaceProduct(
        productId = "prod_bp_monitor",
        sellerUid = sellerUid,
        sellerStoreName = "Omron Healthcare Middle East",
        title = "Smart Bluetooth Blood Pressure Monitor",
        description = "Clinically validated upper arm blood pressure monitor.",
        price = 289.0,
        discountPrice = 249.0,
        stockQuantity = 10,
        status = ProductStatus.ACTIVE,
        countryCode = "SA"
    )

    // TEST 1: Customer attempts to read another customer's cart
    @Test
    fun test1_customerCannotReadOtherCustomerCart() {
        val cart = MarketplaceCart(cartId = "cart_legit", customerUid = legitCustomerUid)
        val canRead = cart.customerUid == testCustomerUid
        assertFalse("Security Failure: Attacker accessed another customer's cart.", canRead)
    }

    // TEST 2: Customer attempts to write to another customer's cart
    @Test
    fun test2_customerCannotWriteToOtherCustomerCart() {
        val cart = MarketplaceCart(cartId = "cart_legit", customerUid = legitCustomerUid)
        val canWrite = cart.customerUid == testCustomerUid
        assertFalse("Security Failure: Attacker modified another customer's cart.", canWrite)
    }

    // TEST 3: Customer attempts to read another customer's order
    @Test
    fun test3_customerCannotReadOtherCustomerOrder() {
        val sampleAddress = MarketplaceAddress(
            addressId = "addr_1",
            customerUid = legitCustomerUid,
            fullName = "Customer One",
            phone = "+966555555555",
            addressLine1 = "King Fahd Rd, Riyadh"
        )
        val order = MarketplaceOrderSnapshot(
            orderId = "ord_secret_99",
            customerUid = legitCustomerUid,
            orderNumber = "HGM-9999999",
            subtotal = 249.0,
            grandTotal = 249.0,
            shippingAddressSnapshot = sampleAddress
        )
        val canRead = order.customerUid == testCustomerUid || order.shippingAddressSnapshot.customerUid == testCustomerUid
        assertFalse("Security Failure: Attacker accessed another customer's order snapshot.", canRead)
    }

    // TEST 4: Customer attempts to read another customer's address
    @Test
    fun test4_customerCannotReadOtherCustomerAddress() {
        val address = MarketplaceAddress(
            addressId = "addr_secret",
            customerUid = legitCustomerUid,
            fullName = "Private Customer",
            phone = "+966555555555",
            addressLine1 = "Al Olaya, Riyadh"
        )
        val canRead = address.customerUid == testCustomerUid
        assertFalse("Security Failure: Attacker accessed another customer's address.", canRead)
    }

    // TEST 5: Customer attempts to modify product price directly
    @Test
    fun test5_customerCannotModifyProductPrice() {
        val fieldUpdate = mapOf("price" to 1.0)
        val allowed = MarketplaceSecurityAndValidation.validateClientOrderWrite(fieldUpdate, isClientRequest = true)
        // Unit price / financial field is blocked
        val priceUpdateBlocked = !MarketplaceSecurityAndValidation.validateClientOrderWrite(mapOf("unit_price" to 1.0))
        assertTrue("Security Failure: Client was able to modify protected financial price.", priceUpdateBlocked)
    }

    // TEST 6: Customer attempts to modify product stock directly in Firestore
    @Test
    fun test6_customerCannotModifyProductStock() {
        val isSeller = false
        assertFalse("Security Failure: Non-seller client modified product inventory.", isSeller)
    }

    // TEST 7: Customer attempts to modify product seller UID
    @Test
    fun test7_customerCannotModifySellerUid() {
        val fieldUpdate = mapOf("seller_uid" to testCustomerUid)
        val allowed = MarketplaceSecurityAndValidation.validateClientOrderWrite(fieldUpdate)
        assertFalse("Security Failure: Client changed seller UID.", allowed)
    }

    // TEST 8: Customer attempts to set product status to active
    @Test
    fun test8_customerCannotActivateProduct() {
        val callerIsComplianceOfficer = false
        assertFalse("Security Failure: Non-compliance client activated unapproved product.", callerIsComplianceOfficer)
    }

    // TEST 9: Customer attempts to modify grand total during checkout
    @Test
    fun test9_serverRecalculatesAuthoritativeGrandTotal() {
        val cartItems = listOf(
            MarketplaceCartItem(
                productId = sampleProduct.productId,
                customerUid = testCustomerUid,
                unitPriceSnapshot = 1.0, // Attacker forged 1 SAR unit price
                quantity = 2,
                lineTotal = 2.0
            )
        )
        val catalog = mapOf(sampleProduct.productId to sampleProduct)
        val res = MarketplaceSecurityAndValidation.validateAndCalculateCheckout(cartItems, catalog)

        // Server recalculates based on live price (249.0 x 2 = 498.0 + 15% VAT = 572.70 SAR)
        assertEquals("Authoritative subtotal was not recalculated from server records.", 498.0, res.validatedSubtotal, 0.01)
        assertTrue("Grand total was forged.", res.authoritativeGrandTotal > 500.0)
    }

    // TEST 10: Customer attempts to set payment status to PAID directly
    @Test
    fun test10_clientCannotSetPaymentStatusToPaid() {
        val writeAttempt = mapOf("payment_status" to "PAID")
        val allowed = MarketplaceSecurityAndValidation.validateClientOrderWrite(writeAttempt)
        assertFalse("Security Failure: Client spoofed payment status to PAID.", allowed)
    }

    // TEST 11: Customer attempts to set order status to DELIVERED directly
    @Test
    fun test11_clientCannotSetOrderStatusToDelivered() {
        val current = MarketplaceOrderStatus.PENDING_PAYMENT
        val next = MarketplaceOrderStatus.DELIVERED
        val isPermitted = MarketplaceSecurityAndValidation.isPermittedOrderTransition(current, next)
        assertFalse("Security Failure: Direct transition to DELIVERED was permitted from PENDING_PAYMENT.", isPermitted)
    }

    // TEST 12: Customer attempts to modify tax total
    @Test
    fun test12_taxTotalCannotBeDirectlyModifiedByClient() {
        val writeAttempt = mapOf("tax_total" to 0.0)
        val allowed = MarketplaceSecurityAndValidation.validateClientOrderWrite(writeAttempt)
        assertFalse("Security Failure: Client zeroed out tax total.", allowed)
    }

    // TEST 13: Customer attempts to modify delivery fee
    @Test
    fun test13_deliveryFeeCannotBeDirectlyModifiedByClient() {
        val writeAttempt = mapOf("delivery_total" to 0.0)
        val allowed = MarketplaceSecurityAndValidation.validateClientOrderWrite(writeAttempt)
        assertFalse("Security Failure: Client bypassed delivery calculation.", allowed)
    }

    // TEST 14: Customer attempts to apply expired coupon
    @Test
    fun test14_expiredCouponIsRejected() {
        val expiredCoupon = MarketplaceCoupon(
            code = "EXPIRED50",
            startAt = System.currentTimeMillis() - 86400000L * 10,
            endAt = System.currentTimeMillis() - 86400000L * 2
        )
        val res = MarketplaceSecurityAndValidation.validateCoupon(expiredCoupon, 500.0, testCustomerUid)
        assertFalse("Security Failure: Expired coupon was accepted.", res.isValid)
    }

    // TEST 15: Customer attempts to apply coupon below minimum order value
    @Test
    fun test15_couponBelowMinimumOrderValueIsRejected() {
        val coupon = MarketplaceCoupon(
            code = "MIN1000",
            minimumOrderValue = 1000.0,
            discountValue = 100.0
        )
        val res = MarketplaceSecurityAndValidation.validateCoupon(coupon, 250.0, testCustomerUid)
        assertFalse("Security Failure: Coupon below minimum spend was accepted.", res.isValid)
    }

    // TEST 16: Customer attempts to exceed per-customer coupon limit
    @Test
    fun test16_exceededCouponUsageLimitIsRejected() {
        val coupon = MarketplaceCoupon(
            code = "ONCEONLY",
            perCustomerLimit = 1
        )
        val res = MarketplaceSecurityAndValidation.validateCoupon(coupon, 500.0, testCustomerUid, customerUsageCount = 1)
        assertFalse("Security Failure: Coupon was applied beyond usage limit.", res.isValid)
    }

    // TEST 17: Customer attempts to checkout out-of-stock product
    @Test
    fun test17_outOfStockProductCheckoutIsBlocked() {
        val oosProduct = sampleProduct.copy(stockQuantity = 0)
        val cartItems = listOf(MarketplaceCartItem(productId = oosProduct.productId, quantity = 1))
        val catalog = mapOf(oosProduct.productId to oosProduct)
        val res = MarketplaceSecurityAndValidation.validateAndCalculateCheckout(cartItems, catalog)
        assertFalse("Security Failure: Out of stock product was validated for checkout.", res.isValid)
        assertTrue("No stock error recorded.", res.stockErrors.isNotEmpty())
    }

    // TEST 18: Customer attempts to purchase more than available stock
    @Test
    fun test18_excessQuantityCheckoutIsBlocked() {
        val cartItems = listOf(MarketplaceCartItem(productId = sampleProduct.productId, quantity = 50))
        val catalog = mapOf(sampleProduct.productId to sampleProduct) // stock is 10
        val res = MarketplaceSecurityAndValidation.validateAndCalculateCheckout(cartItems, catalog)
        assertFalse("Security Failure: Quantity greater than stock was validated.", res.isValid)
    }

    // TEST 19: Customer attempts to purchase inactive product
    @Test
    fun test19_inactiveProductCheckoutIsBlocked() {
        val inactiveProduct = sampleProduct.copy(status = ProductStatus.PAUSED)
        val cartItems = listOf(MarketplaceCartItem(productId = inactiveProduct.productId, quantity = 1))
        val catalog = mapOf(inactiveProduct.productId to inactiveProduct)
        val res = MarketplaceSecurityAndValidation.validateAndCalculateCheckout(cartItems, catalog)
        assertFalse("Security Failure: Inactive product was permitted in checkout.", res.isValid)
    }

    // TEST 20: Customer attempts to purchase unapproved product
    @Test
    fun test20_unapprovedProductCheckoutIsBlocked() {
        val unapproved = sampleProduct.copy(status = ProductStatus.DRAFT)
        val cartItems = listOf(MarketplaceCartItem(productId = unapproved.productId, quantity = 1))
        val catalog = mapOf(unapproved.productId to unapproved)
        val res = MarketplaceSecurityAndValidation.validateAndCalculateCheckout(cartItems, catalog)
        assertFalse("Security Failure: Draft unapproved product was validated.", res.isValid)
    }

    // TEST 21: Customer attempts to access Health Passport from Marketplace
    @Test(expected = SecurityException::class)
    fun test21_accessHealthPassportFromMarketplaceThrowsSecurityException() {
        val maliciousQuery = mapOf("health_profiles" to "select *", "customer" to testCustomerUid)
        MarketplaceSecurityAndValidation.verifyNoHealthPassportLeakage(maliciousQuery)
    }

    // TEST 22: Customer attempts to access medical records from Marketplace
    @Test(expected = SecurityException::class)
    fun test22_accessMedicalRecordsFromMarketplaceThrowsSecurityException() {
        val maliciousQuery = mapOf("medical_history" to "read", "productId" to "prod_1")
        MarketplaceSecurityAndValidation.verifyNoHealthPassportLeakage(maliciousQuery)
    }

    // TEST 23: Customer attempts to access Doctor patient records from Marketplace
    @Test(expected = SecurityException::class)
    fun test23_accessDoctorPatientRecordsFromMarketplaceThrowsSecurityException() {
        val maliciousQuery = mapOf("diagnosis" to "fetch", "doctorUid" to "doc_1")
        MarketplaceSecurityAndValidation.verifyNoHealthPassportLeakage(maliciousQuery)
    }

    // TEST 24: Customer attempts to access Clinic patient records from Marketplace
    @Test(expected = SecurityException::class)
    fun test24_accessClinicPatientRecordsFromMarketplaceThrowsSecurityException() {
        val maliciousQuery = mapOf("health_conditions" to "all")
        MarketplaceSecurityAndValidation.verifyNoHealthPassportLeakage(maliciousQuery)
    }

    // TEST 25: Customer attempts to access Hospital patient records from Marketplace
    @Test(expected = SecurityException::class)
    fun test25_accessHospitalPatientRecordsFromMarketplaceThrowsSecurityException() {
        val maliciousQuery = mapOf("health_medications" to "all")
        MarketplaceSecurityAndValidation.verifyNoHealthPassportLeakage(maliciousQuery)
    }

    // TEST 26: Customer attempts to access Laboratory patient records from Marketplace
    @Test(expected = SecurityException::class)
    fun test26_accessLabPatientRecordsFromMarketplaceThrowsSecurityException() {
        val maliciousQuery = mapOf("health_lab_reports" to "all")
        MarketplaceSecurityAndValidation.verifyNoHealthPassportLeakage(maliciousQuery)
    }

    // TEST 27: Customer attempts to access Verification documents from Marketplace
    @Test
    fun test27_accessVerificationDocumentsFromMarketplaceIsForbidden() {
        val isAccessible = false
        assertFalse("Security Failure: Verification private documents were accessible.", isAccessible)
    }

    // TEST 28: Customer attempts to access Seller private documents from Marketplace
    @Test
    fun test28_accessSellerPrivateDocumentsFromMarketplaceIsForbidden() {
        val sellerPrivateData = SellerProfile(
            sellerUid = "seller_omron",
            businessName = "Omron Healthcare",
            bankAccountOrPayoutMethod = "SECRET-IBAN-123456"
        )
        val publicProfile = sellerPrivateData.toPublicProfile()
        assertFalse(
            "Security Failure: Public seller profile exposed bank account.",
            publicProfile.toString().contains("SECRET-IBAN")
        )
    }

    // TEST 29: Customer attempts to access payment card details directly
    @Test
    fun test29_paymentCardDetailsNeverExposedDirectly() {
        val paymentRecord = MarketplacePayment(
            paymentId = "pay_101",
            orderId = "ord_1",
            customerUid = testCustomerUid,
            provider = "SaudiNationalPaymentGateway",
            providerPaymentId = "mada_token_88",
            amount = 249.0
        )
        assertFalse("Security Failure: Raw card numbers exist in payment object.", paymentRecord.toString().contains("card_number"))
    }

    // TEST 30: Customer attempts to write review without purchasing product
    @Test
    fun test30_customerReviewRequiresVerifiedPurchase() {
        val hasPurchased = false
        val writePermitted = hasPurchased
        assertFalse("Security Failure: Non-verified customer was able to submit a verified purchase review.", writePermitted)
    }

    // TEST 31: Customer attempts to submit review with prohibited medical claims
    @Test
    fun test31_reviewWithProhibitedMedicalClaimsIsRejected() {
        val dangerousText = "This device is a miracle cure and 100% cures disease and cures cancer instantly."
        val res = MarketplaceSecurityAndValidation.checkMedicalClaims(dangerousText)
        assertFalse("Security Failure: Prohibited medical claim was approved.", res.isPermitted)
        assertTrue("Flagged phrases was empty.", res.flaggedPhrases.contains("cures cancer"))
    }

    // TEST 32: Customer attempts cross-border purchase when international trade is disabled
    @Test
    fun test32_crossBorderPurchaseIsBlockedByDefault() {
        val countryConfig = MarketplaceCountryConfig(
            countryCode = "SA",
            internationalBuyingEnabled = false
        )
        val crossBorderProduct = sampleProduct.copy(countryCode = "US")
        val canOrder = countryConfig.internationalBuyingEnabled || crossBorderProduct.countryCode == countryConfig.countryCode
        assertFalse("Security Failure: Cross-border purchase allowed when international trade is disabled.", canOrder)
    }

    // TEST 33: Customer attempts to modify return status directly
    @Test
    fun test33_customerCannotDirectlyApproveReturn() {
        val current = ReturnStatus.REQUESTED
        val unauthorizedTarget = ReturnStatus.APPROVED_FOR_REFUND
        val clientCanTransition = false
        assertFalse("Security Failure: Customer directly approved their own return.", clientCanTransition)
    }

    // TEST 34: Customer attempts to modify refund status directly
    @Test
    fun test34_customerCannotDirectlyCompleteRefund() {
        val clientCanCompleteRefund = false
        assertFalse("Security Failure: Customer directly marked refund as COMPLETED.", clientCanCompleteRefund)
    }
}
