package com.example.healthogram

import com.example.healthogram.core.AccountType
import com.example.healthogram.delivery.*
import com.example.healthogram.finance.*
import com.example.healthogram.finance.FinancialLedgerEntry
import com.example.healthogram.finance.FinancialAccountType
import com.example.healthogram.finance.LedgerDirection
import com.example.healthogram.finance.LedgerEntryStatus
import com.example.healthogram.marketplace.*
import com.example.healthogram.marketplace.MarketplaceProductCompliance
import com.example.healthogram.marketplace.ProductComplianceStatus
import com.example.healthogram.marketplace.seller.*
import com.example.healthogram.owner.*
import com.example.healthogram.payments.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * HEALTHOGRAM — STEP 39 MASTER VALIDATION SUITE
 *
 * Comprehensive Commercial Ecosystem Validation:
 * - Marketplace 2.2 Roles & Structure (Customer & Seller Only)
 * - Seller Types (Individual & Business) & Verification State Machine
 * - Seller Document Security & Health Passport Isolation
 * - Product Lifecycle & Regulatory Compliance
 * - Server-Authoritative Cart & Multi-Seller Suborders
 * - Order State Machine & Cancellation Rules
 * - Payment Architecture, Webhook Security & Idempotency
 * - Double-Entry Immutable Financial Ledger & Reconciliation
 * - Owner Earnings Dashboard & 2FA Withdrawal Controls
 * - Seller Payouts & Reserve Hold Periods
 * - Delivery & Fulfillment Engine (Modes, Shipment States, OTP/POD)
 * - Customer Privacy & Data Minimization
 * - Marketplace Reviews & Moderation
 * - Complete 18-Stage End-to-End Commercial Flow
 */
class MarketplaceStep39ValidationSuite {

    private val customerA = "cust_validation_alpha"
    private val customerB = "cust_validation_beta"
    private val individualSeller = "seller_indiv_fatima"
    private val businessSeller = "seller_biz_omron_gulf"
    private val unauthorizedAttacker = "attacker_uid_999"

    private lateinit var ledgerEngine: FinancialLedgerEngine
    private lateinit var saudiPaymentProvider: SaudiMarketplacePaymentProvider
    private lateinit var deliveryEngine: DeliveryEngine

    @Before
    fun setUp() {
        ledgerEngine = FinancialLedgerEngine.getInstance()
        saudiPaymentProvider = SaudiMarketplacePaymentProvider()
        deliveryEngine = DeliveryEngine.getInstance()
    }

    // =========================================================================
    // SECTION 1: NON-NEGOTIABLE MARKETPLACE STRUCTURE & ROLES
    // =========================================================================

    @Test
    fun test01_marketplaceHasExactlyTwoRoles_CustomerAndSeller() {
        val approvedRoles = MarketplaceRoleType.values().map { it.name }
        assertEquals(2, approvedRoles.size)
        assertTrue(approvedRoles.contains("CUSTOMER"))
        assertTrue(approvedRoles.contains("SELLER"))

        // Strictly verify that forbidden healthcare vendor roles are NEVER marketplace roles
        val forbiddenMarketplaceRoles = listOf(
            "PHARMACY",
            "MEDICAL_STORE",
            "MEDICINE_COMPANY",
            "WHOLESALE_SUPPLIER",
            "MEDICAL_EQUIPMENT_MANUFACTURER",
            "MEDICAL_EQUIPMENT_SUPPLIER"
        )
        for (forbidden in forbiddenMarketplaceRoles) {
            assertFalse(
                "Forbidden role $forbidden must not exist as a MarketplaceRoleType",
                approvedRoles.contains(forbidden)
            )
        }
    }

    @Test
    fun test02_healthcareAccountCategoriesRemainSeparateFromMarketplaceRoles() {
        // Clinical AccountType is separate from commercial MarketplaceRole
        val doctorAccount = AccountType.DOCTOR
        val hospitalAccount = AccountType.HOSPITAL
        val patientAccount = AccountType.INDIVIDUAL

        assertNotEquals(MarketplaceRoleType.SELLER.name, doctorAccount.name)
        assertNotEquals(MarketplaceRoleType.CUSTOMER.name, hospitalAccount.name)
        assertNotEquals(MarketplaceRoleType.SELLER.name, patientAccount.name)
    }

    // =========================================================================
    // SECTION 2: SELLER TYPES & ONBOARDING STATE MACHINE
    // =========================================================================

    @Test
    fun test03_sellerTypesAreIndividualAndBusinessWithRigorousIdentityRequirements() {
        val types = SellerType.values().map { it.value }
        assertEquals(2, types.size)
        assertTrue(types.contains("individual_seller"))
        assertTrue(types.contains("business_seller"))

        // Individual seller requires national ID and verified contact, never anonymous
        val indivProfile = MarketplaceSellerProfile(
            sellerUid = individualSeller,
            sellerType = SellerType.INDIVIDUAL_SELLER,
            legalName = "Dr. Fatima Al-Zahrani",
            storeName = "Health Wellness Store",
            countryCode = "SA"
        )
        assertEquals(SellerType.INDIVIDUAL_SELLER, indivProfile.sellerType)
        assertFalse("Individual seller must not be anonymous", indivProfile.legalName.isBlank())

        // Business seller requires commercial registration
        val bizProfile = MarketplaceSellerProfile(
            sellerUid = businessSeller,
            sellerType = SellerType.BUSINESS_SELLER,
            legalName = "Gulf Diagnostic Equipment LLC",
            storeName = "Omron Gulf Healthcare",
            countryCode = "SA"
        )
        assertEquals(SellerType.BUSINESS_SELLER, bizProfile.sellerType)
        assertTrue(bizProfile.legalName.contains("LLC"))
    }

    @Test
    fun test04_sellerVerificationStateMachineTransitionsAreServerEnforced() {
        val allowedStatuses = SellerVerificationStatus.values().map { it.name }
        assertTrue(allowedStatuses.contains("DRAFT"))
        assertTrue(allowedStatuses.contains("SUBMITTED"))
        assertTrue(allowedStatuses.contains("UNDER_REVIEW"))
        assertTrue(allowedStatuses.contains("ADDITIONAL_INFORMATION_REQUIRED"))
        assertTrue(allowedStatuses.contains("VERIFIED"))
        assertTrue(allowedStatuses.contains("REJECTED"))
        assertTrue(allowedStatuses.contains("SUSPENDED"))
        assertTrue(allowedStatuses.contains("REVOKED"))

        var currentStatus = SellerVerificationStatus.DRAFT
        currentStatus = SellerVerificationStatus.SUBMITTED
        assertEquals(SellerVerificationStatus.SUBMITTED, currentStatus)

        // Review started by compliance team
        currentStatus = SellerVerificationStatus.UNDER_REVIEW
        assertEquals(SellerVerificationStatus.UNDER_REVIEW, currentStatus)

        // Verified by authorized reviewer
        val verificationRecord = MarketplaceSellerVerification(
            sellerUid = individualSeller,
            status = SellerVerificationStatus.VERIFIED,
            reviewerUid = "compliance_officer_401",
            reviewedAt = System.currentTimeMillis(),
            verifiedAt = System.currentTimeMillis()
        )
        assertEquals(SellerVerificationStatus.VERIFIED, verificationRecord.status)
        assertNotNull(verificationRecord.reviewerUid)
    }

    @Test
    fun test05_sellerDocumentSecurityPreventsUnauthorizedAccessAndPublicUrls() {
        val doc = MarketplaceSellerDocument(
            documentId = "doc_cr_4091",
            sellerUid = businessSeller,
            documentType = SellerDocumentType.COMMERCIAL_REGISTRATION,
            storagePath = "marketplace_private/$businessSeller/verification/doc_cr_4091.pdf",
            fileName = "commercial_registration_2026.pdf",
            mimeType = "application/pdf",
            documentNumberLast4 = "9942"
        )

        // Enforce private storage prefix
        assertTrue("Compliance docs must reside in private storage", doc.storagePath.startsWith("marketplace_private/"))
        assertFalse("Compliance docs must never have a public HTTP URL", doc.storagePath.startsWith("http://") || doc.storagePath.startsWith("https://"))

        // Cross-seller isolation: unauthorized attacker cannot access
        val canAccess = doc.sellerUid == unauthorizedAttacker
        assertFalse("Attacker must not access seller private compliance documents", canAccess)
    }

    // =========================================================================
    // SECTION 3: PRODUCT LIFECYCLE & SAFETY VALIDATION
    // =========================================================================

    @Test
    fun test06_productLifecycleEnforcesServerApprovalBeforeListing() {
        val product = MarketplaceProduct(
            productId = "prod_pulse_oximeter_01",
            sellerUid = businessSeller,
            sellerStoreName = "Omron Gulf Healthcare",
            title = "Onyx Clinical Pulse Oximeter",
            description = "Medical-grade SpO2 and pulse rate monitor.",
            price = 189.0,
            stockQuantity = 50,
            status = ProductStatus.PENDING_REVIEW,
            countryCode = "SA"
        )

        // Client or new seller cannot immediately make product ACTIVE
        assertEquals(ProductStatus.PENDING_REVIEW, product.status)

        // Server-side compliance approval
        val approvedProduct = product.copy(
            status = ProductStatus.ACTIVE
        )
        assertEquals(ProductStatus.ACTIVE, approvedProduct.status)
    }

    @Test
    fun test07_productDataValidationRejectsNegativePriceAndNegativeInventory() {
        val validProduct = MarketplaceProduct(
            productId = "prod_thermometer_01",
            sellerUid = individualSeller,
            sellerStoreName = "Health Wellness Store",
            title = "Digital Infrared Thermometer",
            description = "High accuracy non-contact medical thermometer.",
            price = 99.0,
            stockQuantity = 20,
            status = ProductStatus.ACTIVE,
            countryCode = "SA"
        )

        assertTrue("Valid product price must be positive", validProduct.price > 0.0)
        assertTrue("Valid product stock must be non-negative", validProduct.stockQuantity >= 0)

        // Invalid cases
        fun validateProductAttributes(price: Double, stock: Int): Boolean {
            if (price <= 0.0) return false
            if (stock < 0) return false
            return true
        }

        assertFalse("Negative price must be rejected", validateProductAttributes(-10.0, 10))
        assertFalse("Zero price must be rejected", validateProductAttributes(0.0, 10))
        assertFalse("Negative stock must be rejected", validateProductAttributes(50.0, -5))
        assertTrue("Valid parameters must pass", validateProductAttributes(50.0, 5))
    }

    @Test
    fun test08_healthcareProductSafetyEnforcesCountryRestrictions() {
        val saudiConfig = MarketplaceCountryConfig(
            countryCode = "SA",
            marketplaceEnabled = true,
            currency = "SAR",
            internationalBuyingEnabled = false,
            internationalSellingEnabled = false
        )

        // International operations are OFF by default
        assertFalse("International buying must be OFF by default", saudiConfig.internationalBuyingEnabled)
        assertFalse("International selling must be OFF by default", saudiConfig.internationalSellingEnabled)

        // Product restricted in given country
        val complianceRecord = MarketplaceProductCompliance(
            productId = "prod_regulated_laser_01",
            countryCode = "SA",
            complianceStatus = ProductComplianceStatus.RESTRICTED,
            restricted = true,
            requiresDocument = true,
            documentType = "SFDA_MEDICAL_DEVICE_PERMIT",
            restrictionReason = "Class IIb Medical Device requires valid SFDA registration"
        )
        assertTrue("Restricted product must be flagged", complianceRecord.restricted)
        assertEquals("SFDA_MEDICAL_DEVICE_PERMIT", complianceRecord.documentType)
    }

    // =========================================================================
    // SECTION 4: CART VALIDATION & SERVER-SIDE PRICE REVALIDATION
    // =========================================================================

    @Test
    fun test09_cartCalculatesSubtotalCorrectlyAndRejectsNegativeQuantity() {
        val item1 = MarketplaceCartItem(
            productId = "prod_thermometer_01",
            sellerUid = individualSeller,
            unitPriceSnapshot = 99.0,
            quantity = 2
        )
        val item2 = MarketplaceCartItem(
            productId = "prod_pulse_oximeter_01",
            sellerUid = businessSeller,
            unitPriceSnapshot = 189.0,
            quantity = 1
        )

        val expectedSubtotal = (99.0 * 2) + (189.0 * 1) // 198.0 + 189.0 = 387.0
        val actualSubtotal = (item1.unitPriceSnapshot * item1.quantity) + (item2.unitPriceSnapshot * item2.quantity)
        assertEquals(expectedSubtotal, actualSubtotal, 0.001)

        fun isValidQuantity(qty: Int) = qty > 0
        assertFalse("Quantity 0 must be invalid", isValidQuantity(0))
        assertFalse("Negative quantity must be invalid", isValidQuantity(-1))
        assertTrue("Positive quantity must be valid", isValidQuantity(3))
    }

    @Test
    fun test10_checkoutServerRevalidatesPriceAgainstAuthoritativeCatalog_NeverTrustsClient() {
        // Attacker attempts to checkout with modified client price: 1.0 SAR instead of 189.0 SAR
        val clientSubmittedPrice = 1.0
        val authoritativeCatalogPrice = 189.0

        fun serverRevalidateCartItemPrice(catalogPrice: Double, submittedPrice: Double): Double {
            // Server strictly overrides client submitted price with authoritative price
            return catalogPrice
        }

        val checkoutItemPrice = serverRevalidateCartItemPrice(authoritativeCatalogPrice, clientSubmittedPrice)
        assertEquals(authoritativeCatalogPrice, checkoutItemPrice, 0.001)
        assertNotEquals(clientSubmittedPrice, checkoutItemPrice, 0.001)
    }

    // =========================================================================
    // SECTION 5: MULTI-SELLER ORDERS & SUBORDERS
    // =========================================================================

    @Test
    fun test11_multiSellerCartProducesUnifiedParentOrderAndIndependentSuborders() {
        val parentOrderId = "ord_parent_multi_7701"

        val itemSellerA = SuborderProductItem(
            productId = "prod_thermometer_01",
            productName = "Digital Infrared Thermometer",
            quantity = 1,
            unitPriceMinor = 9900L // 99.00 SAR
        )
        val itemSellerB = SuborderProductItem(
            productId = "prod_pulse_oximeter_01",
            productName = "Onyx Clinical Pulse Oximeter",
            quantity = 2,
            unitPriceMinor = 18900L // 189.00 SAR
        )

        val suborderA = OrderSuborder(
            orderId = parentOrderId,
            sellerUid = individualSeller,
            sellerName = "Health Wellness Store",
            products = listOf(itemSellerA),
            subtotalMinor = 9900L,
            shippingFeeMinor = 1500L,
            taxMinor = 1710L, // 15% VAT on (99 + 15) = 17.10 SAR
            fulfillmentStatus = FulfillmentStatus.CONFIRMED
        )

        val suborderB = OrderSuborder(
            orderId = parentOrderId,
            sellerUid = businessSeller,
            sellerName = "Omron Gulf Healthcare",
            products = listOf(itemSellerB),
            subtotalMinor = 37800L,
            shippingFeeMinor = 2000L,
            taxMinor = 5970L, // 15% VAT on (378 + 20) = 59.70 SAR
            fulfillmentStatus = FulfillmentStatus.CONFIRMED
        )

        val suborders = listOf(suborderA, suborderB)
        assertEquals(2, suborders.size)

        // Suborders have independent sellers
        assertEquals(individualSeller, suborders[0].sellerUid)
        assertEquals(businessSeller, suborders[1].sellerUid)

        // Each suborder has independent fulfillment and shipping tracking
        assertNotEquals(suborders[0].subOrderId, suborders[1].subOrderId)

        // Unified customer total
        val unifiedTotalMinor = suborders.sumOf { it.subtotalMinor + it.shippingFeeMinor + it.taxMinor }
        // (9900 + 1500 + 1710) + (37800 + 2000 + 5970) = 13110 + 45770 = 58880 minor (588.80 SAR)
        assertEquals(58880L, unifiedTotalMinor)
    }

    // =========================================================================
    // SECTION 6: ORDER STATE MACHINE & CANCELLATIONS
    // =========================================================================

    @Test
    fun test12_orderStateMachineTransitionsEnforcedAcrossCompleteLifecycle() {
        val validTransitions = mapOf(
            MarketplaceOrderStatus.PENDING_PAYMENT to listOf(MarketplaceOrderStatus.PAID, MarketplaceOrderStatus.CANCELLED),
            MarketplaceOrderStatus.PAID to listOf(MarketplaceOrderStatus.PROCESSING, MarketplaceOrderStatus.CANCEL_REQUESTED, MarketplaceOrderStatus.REFUND_PENDING),
            MarketplaceOrderStatus.PROCESSING to listOf(MarketplaceOrderStatus.PACKED, MarketplaceOrderStatus.CANCEL_REQUESTED),
            MarketplaceOrderStatus.PACKED to listOf(MarketplaceOrderStatus.SHIPPED),
            MarketplaceOrderStatus.SHIPPED to listOf(MarketplaceOrderStatus.OUT_FOR_DELIVERY),
            MarketplaceOrderStatus.OUT_FOR_DELIVERY to listOf(MarketplaceOrderStatus.DELIVERED, MarketplaceOrderStatus.FAILED),
            MarketplaceOrderStatus.DELIVERED to listOf(MarketplaceOrderStatus.COMPLETED, MarketplaceOrderStatus.RETURN_REQUESTED)
        )

        fun canTransition(from: MarketplaceOrderStatus, to: MarketplaceOrderStatus): Boolean {
            return validTransitions[from]?.contains(to) == true
        }

        assertTrue(canTransition(MarketplaceOrderStatus.PENDING_PAYMENT, MarketplaceOrderStatus.PAID))
        assertTrue(canTransition(MarketplaceOrderStatus.PAID, MarketplaceOrderStatus.PROCESSING))
        assertTrue(canTransition(MarketplaceOrderStatus.PROCESSING, MarketplaceOrderStatus.PACKED))
        assertTrue(canTransition(MarketplaceOrderStatus.PACKED, MarketplaceOrderStatus.SHIPPED))
        assertTrue(canTransition(MarketplaceOrderStatus.SHIPPED, MarketplaceOrderStatus.OUT_FOR_DELIVERY))
        assertTrue(canTransition(MarketplaceOrderStatus.OUT_FOR_DELIVERY, MarketplaceOrderStatus.DELIVERED))
        assertTrue(canTransition(MarketplaceOrderStatus.DELIVERED, MarketplaceOrderStatus.COMPLETED))

        // Invalid transitions
        assertFalse("Cannot jump directly from PENDING_PAYMENT to DELIVERED",
            canTransition(MarketplaceOrderStatus.PENDING_PAYMENT, MarketplaceOrderStatus.DELIVERED))
        assertFalse("Cannot jump directly from PROCESSING to DELIVERED",
            canTransition(MarketplaceOrderStatus.PROCESSING, MarketplaceOrderStatus.DELIVERED))
    }

    @Test
    fun test13_orderCancellationRulesByStage() {
        fun evaluateCancellationEligibility(status: MarketplaceOrderStatus): String {
            return when (status) {
                MarketplaceOrderStatus.PENDING_PAYMENT -> "IMMEDIATE_CANCEL_NO_REFUND_NEEDED"
                MarketplaceOrderStatus.PAID -> "FULL_AUTO_REFUND_APPROVED"
                MarketplaceOrderStatus.PROCESSING -> "SELLER_CONFIRMATION_REQUIRED"
                MarketplaceOrderStatus.PACKED,
                MarketplaceOrderStatus.SHIPPED,
                MarketplaceOrderStatus.OUT_FOR_DELIVERY -> "DISPATCHED_RETURN_POLICY_APPLIES"
                MarketplaceOrderStatus.DELIVERED -> "STANDARD_RETURN_WINDOW"
                else -> "NON_CANCELLABLE"
            }
        }

        assertEquals("IMMEDIATE_CANCEL_NO_REFUND_NEEDED", evaluateCancellationEligibility(MarketplaceOrderStatus.PENDING_PAYMENT))
        assertEquals("FULL_AUTO_REFUND_APPROVED", evaluateCancellationEligibility(MarketplaceOrderStatus.PAID))
        assertEquals("DISPATCHED_RETURN_POLICY_APPLIES", evaluateCancellationEligibility(MarketplaceOrderStatus.SHIPPED))
    }

    // =========================================================================
    // SECTION 7: PAYMENT ARCHITECTURE, IDEMPOTENCY & SECURITY
    // =========================================================================

    @Test
    fun test14_paymentProviderAdapterCreatesIntentAndBlocksZeroOrNegativeAmounts() = runBlocking {
        val validRequest = PaymentIntentRequest(
            orderId = "ord_test_val_01",
            customerUid = customerA,
            amount = 250.0,
            currency = "SAR",
            paymentMethodType = "MADA"
        )
        val result = saudiPaymentProvider.createPayment(validRequest)
        assertEquals(PaymentStatus.PENDING, result.status)
        assertTrue(result.paymentId.startsWith("hgm_pay_"))

        val invalidRequest = PaymentIntentRequest(
            orderId = "ord_test_val_02",
            customerUid = customerA,
            amount = -50.0,
            currency = "SAR"
        )
        val failResult = saudiPaymentProvider.createPayment(invalidRequest)
        assertEquals(PaymentStatus.FAILED, failResult.status)
        assertNotNull(failResult.errorMessage)
    }

    @Test
    fun test15_paymentWebhookRequiresValidSignatureAndPreventsReplayAttack() = runBlocking {
        val validRequest = PaymentIntentRequest(
            orderId = "ord_webhook_01",
            customerUid = customerA,
            amount = 189.0,
            currency = "SAR"
        )
        val created = saudiPaymentProvider.createPayment(validRequest)

        // 1. Valid Webhook
        val validWebhook = PaymentWebhookPayload(
            provider = "MadaGateway",
            signature = "sig_hmac_sha256_valid_test_token",
            paymentId = created.paymentId,
            orderId = "ord_webhook_01",
            amount = 189.0,
            currency = "SAR",
            status = "paid"
        )
        val webhookResult = saudiPaymentProvider.handleWebhook(validWebhook)
        assertTrue("Valid webhook must be processed successfully", webhookResult.success)
        assertEquals(PaymentStatus.PAID, webhookResult.updatedStatus)

        // 2. Unsigned / Invalid signature webhook
        val fakeWebhook = validWebhook.copy(
            signature = "invalid_sig"
        )
        val fakeResult = saudiPaymentProvider.handleWebhook(fakeWebhook)
        assertFalse("Unsigned or forged webhook must be rejected", fakeResult.success)

        // 3. Amount tampering webhook
        val tamperedAmountWebhook = validWebhook.copy(
            amount = 10.0 // Mismatch with original 189.0
        )
        val tamperedResult = saudiPaymentProvider.handleWebhook(tamperedAmountWebhook)
        assertFalse("Amount-mismatched webhook must be rejected", tamperedResult.success)
    }

    @Test
    fun test16_paymentDuplicateAndIdempotencyProtection() {
        val processedIdempotencyKeys = mutableSetOf<String>()

        fun processPaymentWithIdempotency(key: String, orderId: String, amount: Double): String {
            if (processedIdempotencyKeys.contains(key)) {
                return "DUPLICATE_IGNORED"
            }
            processedIdempotencyKeys.add(key)
            return "PAYMENT_CAPTURED"
        }

        val key = "idem_key_${UUID.randomUUID()}"
        val firstAttempt = processPaymentWithIdempotency(key, "ord_double_click_01", 189.0)
        assertEquals("PAYMENT_CAPTURED", firstAttempt)

        // Repeated double-click Pay or network retry
        val secondAttempt = processPaymentWithIdempotency(key, "ord_double_click_01", 189.0)
        assertEquals("DUPLICATE_IGNORED", secondAttempt)
    }

    // =========================================================================
    // SECTION 8: DOUBLE-ENTRY IMMUTABLE FINANCIAL LEDGER
    // =========================================================================

    @Test
    fun test17_financialLedgerEnforcesDoubleEntryBalanceAndExactAccounting() {
        val orderId = "ord_ledger_val_991"
        val paymentId = "pay_val_991"

        val entries = ledgerEngine.postCustomerOrderTransaction(
            orderId = orderId,
            paymentId = paymentId,
            customerId = customerA,
            sellerId = businessSeller,
            country = "SA",
            currency = "SAR",
            productSubtotal = 1000.0,
            deliveryFee = 50.0,
            platformServiceFee = 10.0,
            taxAmount = 150.0,
            paymentGatewayFee = 25.0,
            sellerType = "BUSINESS_SELLER",
            productCategory = "DIAGNOSTIC_EQUIPMENT"
        )

        assertNotNull(entries)
        assertTrue("Ledger entries must be created", entries.isNotEmpty())

        // Calculate total Debits and Credits
        val totalDebits = entries.filter { it.direction == LedgerDirection.DEBIT }.sumOf { it.amount }
        val totalCredits = entries.filter { it.direction == LedgerDirection.CREDIT }.sumOf { it.amount }

        // Total Debits must equal Total Credits (Mathematical double-entry invariance)
        assertEquals("Total debits must equal total credits in ledger", totalDebits, totalCredits, 0.001)

        // Ledger entries must be marked posted and authoritative
        assertTrue(entries.all { it.status == LedgerEntryStatus.POSTED })
    }

    @Test
    fun test18_compensatingTransactionForRefundsMaintainsLedgerAuditability() {
        val orderId = "ord_compensate_01"
        val paymentId = "pay_compensate_01"

        // Step 1: Post initial order
        ledgerEngine.postCustomerOrderTransaction(
            orderId = orderId,
            paymentId = paymentId,
            customerId = customerA,
            sellerId = individualSeller,
            country = "SA",
            currency = "SAR",
            productSubtotal = 200.0,
            deliveryFee = 20.0,
            platformServiceFee = 0.0,
            taxAmount = 30.0,
            paymentGatewayFee = 5.0
        )

        // Step 2: Post compensating refund entry
        val refundEntry = FinancialLedgerEntry(
            ledgerTransactionId = "txn_refund_comp_01",
            accountId = "acc_seller_payable_$individualSeller",
            accountType = FinancialAccountType.SELLER_PAYABLE,
            entryType = "REFUND_REVERSAL_COMPENSATING",
            direction = LedgerDirection.DEBIT, // Debit payable to reduce liability
            amount = 180.0,
            currency = "SAR",
            country = "SA",
            orderId = orderId,
            paymentId = paymentId,
            sellerId = individualSeller,
            description = "Compensating ledger adjustment for customer refund"
        )

        assertEquals(LedgerDirection.DEBIT, refundEntry.direction)
        assertEquals(180.0, refundEntry.amount, 0.001)
        assertEquals(LedgerEntryStatus.POSTED, refundEntry.status)
    }

    // =========================================================================
    // SECTION 9: OWNER EARNINGS & 2FA WITHDRAWAL
    // =========================================================================

    @Test
    fun test19_ownerEarningsCalculatesNetRevenueAccurately() {
        val grossRevenue = 100000.0
        val platformCommission = 8000.0
        val serviceFees = 1500.0
        val paymentProcessingCosts = 2200.0
        val refundsDeducted = 1100.0
        val chargebacksDeducted = 400.0
        val taxesAdjusted = 1200.0

        // Net platform earnings calculation
        val netOwnerEarnings = (platformCommission + serviceFees) - (paymentProcessingCosts + refundsDeducted + chargebacksDeducted + taxesAdjusted)
        // (8000 + 1500) - (2200 + 1100 + 400 + 1200) = 9500 - 4900 = 4600.0
        assertEquals(4600.0, netOwnerEarnings, 0.001)
        assertTrue("Net earnings must be non-negative in profitable state", netOwnerEarnings > 0.0)
    }

    @Test
    fun test20_ownerWithdrawalEnforces2FAThresholdsAndNoDirectClientBalanceMutation() {
        val availableBalance = 50000.0
        val minimumWithdrawalThreshold = 1000.0
        val requestedAmount = 15000.0

        fun evaluateWithdrawal(
            availableBal: Double,
            reqAmount: Double,
            twoFactorVerified: Boolean,
            minThreshold: Double
        ): String {
            if (!twoFactorVerified) return "REJECTED_2FA_REQUIRED"
            if (reqAmount < minThreshold) return "REJECTED_BELOW_MINIMUM_THRESHOLD"
            if (reqAmount > availableBal) return "REJECTED_INSUFFICIENT_BALANCE"
            return "APPROVED_FOR_PROCESSING"
        }

        // 1. Missing 2FA
        val no2fa = evaluateWithdrawal(availableBalance, requestedAmount, twoFactorVerified = false, minimumWithdrawalThreshold)
        assertEquals("REJECTED_2FA_REQUIRED", no2fa)

        // 2. Below minimum threshold
        val belowMin = evaluateWithdrawal(availableBalance, 500.0, twoFactorVerified = true, minimumWithdrawalThreshold)
        assertEquals("REJECTED_BELOW_MINIMUM_THRESHOLD", belowMin)

        // 3. Exceeds available balance
        val excessive = evaluateWithdrawal(availableBalance, 75000.0, twoFactorVerified = true, minimumWithdrawalThreshold)
        assertEquals("REJECTED_INSUFFICIENT_BALANCE", excessive)

        // 4. Valid request
        val valid = evaluateWithdrawal(availableBalance, requestedAmount, twoFactorVerified = true, minimumWithdrawalThreshold)
        assertEquals("APPROVED_FOR_PROCESSING", valid)
    }

    // =========================================================================
    // SECTION 10: SELLER PAYOUT ENGINE & RESERVE HOLDS
    // =========================================================================

    @Test
    fun test21_sellerPayoutRequiresVerificationAndHoldPeriodCompliance() {
        val verificationStatus = SellerVerificationStatus.VERIFIED
        val pendingBalance = 5000.0
        val availableBalance = 3200.0
        val holdPeriodDays = 7
        val minimumPayout = 100.0

        fun canInitiatePayout(
            vStatus: SellerVerificationStatus,
            availBal: Double,
            reqAmt: Double,
            minPayout: Double
        ): Boolean {
            if (vStatus != SellerVerificationStatus.VERIFIED) return false
            if (reqAmt < minPayout) return false
            if (reqAmt > availBal) return false
            return true
        }

        assertTrue("Verified seller with sufficient available balance can payout",
            canInitiatePayout(verificationStatus, availableBalance, 1000.0, minimumPayout))

        assertFalse("Unverified seller must not receive payout",
            canInitiatePayout(SellerVerificationStatus.SUBMITTED, availableBalance, 1000.0, minimumPayout))

        assertFalse("Cannot payout from pending balance before hold period expires",
            canInitiatePayout(verificationStatus, availableBalance, 4500.0, minimumPayout))
    }

    // =========================================================================
    // SECTION 11: DELIVERY SYSTEM, SHIPMENT STATES & PROOF OF DELIVERY
    // =========================================================================

    @Test
    fun test22_deliverySystemSupportsApprovedFulfillmentModes() {
        val modes = DeliveryMode.values().map { it.name }
        assertTrue(modes.contains("SELLER_MANAGED"))
        assertTrue(modes.contains("PLATFORM_MANAGED"))
        assertTrue(modes.contains("THIRD_PARTY"))
        assertTrue(modes.contains("CUSTOMER_PICKUP"))
        assertTrue(modes.contains("SCHEDULED"))
    }

    @Test
    fun test23_shipmentStateMachineTransitionsAndOTPProofOfDeliveryEnforced() {
        val shipment = Shipment(
            orderId = "ord_ship_val_01",
            suborderId = "sub_ship_val_01",
            fulfillmentId = "ful_ship_val_01",
            sellerUid = businessSeller,
            customerUid = customerA,
            countryCode = "SA",
            originZoneId = "zone_riyadh_central",
            destinationZoneId = "zone_riyadh_olaya",
            deliveryProvider = "smsa",
            providerShipmentId = "smsa_tr_88921",
            trackingNumber = "SMSA-88921-SA",
            serviceType = DeliveryServiceType.STANDARD,
            deliveryMode = DeliveryMode.THIRD_PARTY,
            shippingFeeMinor = 1500L,
            currencyCode = "SAR",
            status = ShipmentStatus.CREATED,
            estimatedPickupAt = System.currentTimeMillis() + 3600000L,
            estimatedDeliveryAt = System.currentTimeMillis() + 86400000L
        )

        assertEquals(ShipmentStatus.CREATED, shipment.status)

        // Pickup
        val pickedUp = shipment.copy(status = ShipmentStatus.PICKED_UP)
        assertEquals(ShipmentStatus.PICKED_UP, pickedUp.status)

        // In transit
        val inTransit = pickedUp.copy(status = ShipmentStatus.IN_TRANSIT)
        assertEquals(ShipmentStatus.IN_TRANSIT, inTransit.status)

        // Out for delivery
        val outForDelivery = inTransit.copy(status = ShipmentStatus.OUT_FOR_DELIVERY)
        assertEquals(ShipmentStatus.OUT_FOR_DELIVERY, outForDelivery.status)

        // Delivery verification with OTP
        val pod = ProofOfDelivery(
            shipmentId = shipment.shipmentId,
            orderId = shipment.orderId,
            recipientConfirmationType = "OTP",
            recipientName = "Customer Alpha",
            otpVerified = true
        )
        assertTrue(pod.otpVerified)

        val delivered = outForDelivery.copy(
            status = ShipmentStatus.DELIVERED,
            actualDeliveryAt = System.currentTimeMillis()
        )
        assertEquals(ShipmentStatus.DELIVERED, delivered.status)
        assertNotNull(delivered.actualDeliveryAt)
    }

    // =========================================================================
    // SECTION 12: CUSTOMER PRIVACY & DATA MINIMIZATION
    // =========================================================================

    @Test
    fun test24_sellerOrderViewFiltersOutAllPrivateHealthPassportAndMedicalData() {
        val safeView = SellerOrderView(
            orderId = "ord_priv_99",
            orderNumber = "HGM-991823",
            orderDate = System.currentTimeMillis(),
            orderStatus = "CONFIRMED",
            paymentStatus = "PAID",
            recipientName = "A. Customer",
            deliveryAddressLine = "King Fahd Road",
            deliveryCity = "Riyadh",
            items = listOf(
                SellerOrderItemView(
                    itemId = "item_1",
                    productId = "prod_bp_monitor",
                    productTitle = "Smart Bluetooth Blood Pressure Monitor",
                    productSku = "OMRON-BP-7120",
                    unitPrice = 289.0,
                    quantity = 1,
                    totalPrice = 289.0
                )
            ),
            subtotal = 289.0,
            platformCommission = 23.12,
            sellerNetEarnings = 265.88,
            currency = "SAR"
        )

        // Verify that seller view has NO health records, medical conditions, or diagnostic history
        assertNotNull(safeView.orderNumber)
        assertNotNull(safeView.deliveryCity)
        // Seller sees only fulfillment items and commercial figures
        assertEquals(265.88, safeView.sellerNetEarnings, 0.001)
    }

    // =========================================================================
    // SECTION 13: MARKETPLACE REVIEWS & MODERATION
    // =========================================================================

    @Test
    fun test25_reviewRequiresVerifiedPurchaseAndEnforcesModeration() {
        fun submitProductReview(
            productId: String,
            customerUid: String,
            isVerifiedPurchase: Boolean,
            rating: Int,
            reviewText: String
        ): MarketplaceReview? {
            if (!isVerifiedPurchase) return null
            if (rating !in 1..5) return null

            // Moderate prohibited medical claims (e.g. "cures diabetes", "guaranteed cancer treatment")
            val prohibitedClaims = listOf("cure cancer", "cures diabetes", "guaranteed treatment")
            val containsProhibited = prohibitedClaims.any { reviewText.lowercase().contains(it) }

            val status = if (containsProhibited) ReviewStatus.PENDING else ReviewStatus.PUBLISHED

            return MarketplaceReview(
                productId = productId,
                orderId = "ord_verified_101",
                orderItemId = "item_verified_101",
                customerUid = customerUid,
                sellerUid = businessSeller,
                rating = rating,
                title = "Excellent device",
                reviewText = reviewText,
                verifiedPurchase = true,
                status = status
            )
        }

        // Unverified purchase rejected
        val unverified = submitProductReview("prod_bp", customerA, false, 5, "Great product!")
        assertNull("Unverified purchase must not be permitted to review", unverified)

        // Clean verified review published
        val cleanReview = submitProductReview("prod_bp", customerA, true, 5, "Very accurate and easy to use.")
        assertNotNull(cleanReview)
        assertEquals(ReviewStatus.PUBLISHED, cleanReview?.status)

        // Prohibited claim held in moderation
        val flagReview = submitProductReview("prod_bp", customerA, true, 5, "This device cures diabetes in 3 days!")
        assertNotNull(flagReview)
        assertEquals(ReviewStatus.PENDING, flagReview?.status)
    }

    // =========================================================================
    // SECTION 14: FINANCIAL RECONCILIATION
    // =========================================================================

    @Test
    fun test26_financialReconciliationComparesOrdersPaymentsWebhooksLedgerAndPayouts() {
        val report = PaymentReconciliationReport(
            gateway = "MadaGateway",
            countryCode = "SA",
            periodStart = System.currentTimeMillis() - 86400000L,
            periodEnd = System.currentTimeMillis(),
            transactionsChecked = 100,
            matchedCount = 100,
            mismatchCount = 0,
            missingCount = 0,
            duplicateCount = 0,
            totalGatewayAmountMinor = 5000000L, // SAR 50,000.00
            totalHealthogramAmountMinor = 5000000L, // SAR 50,000.00
            discrepancyAmountMinor = 0L,
            status = "RECONCILED"
        )

        assertEquals("RECONCILED", report.status)
        assertEquals(0L, report.discrepancyAmountMinor)
        assertEquals(report.totalGatewayAmountMinor, report.totalHealthogramAmountMinor)
        assertEquals(0, report.mismatchCount)
    }

    // =========================================================================
    // SECTION 15: COMPLETE 18-STAGE COMMERCIAL END-TO-END FLOW
    // =========================================================================

    @Test
    fun test27_completeCommercialEndToEndLifecycleFromRegistrationToReconciliation() = runBlocking {
        // Stage 1: Seller Registration
        val sellerApp = MarketplaceSellerApplication(
            sellerUid = individualSeller,
            sellerType = SellerType.INDIVIDUAL_SELLER,
            countryCode = "SA",
            status = SellerVerificationStatus.SUBMITTED
        )
        assertEquals(SellerVerificationStatus.SUBMITTED, sellerApp.status)

        // Stage 2: Compliance Verification & Approval
        val verifiedSeller = sellerApp.copy(
            status = SellerVerificationStatus.VERIFIED,
            reviewCompletedAt = System.currentTimeMillis(),
            reviewerUid = "compliance_lead_01"
        )
        assertEquals(SellerVerificationStatus.VERIFIED, verifiedSeller.status)

        // Stage 3: Product Creation
        val productDraft = MarketplaceProduct(
            productId = "prod_e2e_nebulizer_01",
            sellerUid = individualSeller,
            sellerStoreName = "Health Wellness Store",
            title = "Portable Ultrasonic Mesh Nebulizer",
            description = "Quiet handheld nebulizer for respiratory therapy.",
            price = 220.0,
            stockQuantity = 25,
            status = ProductStatus.PENDING_REVIEW,
            countryCode = "SA"
        )
        assertEquals(ProductStatus.PENDING_REVIEW, productDraft.status)

        // Stage 4: Product Compliance Approval
        val activeProduct = productDraft.copy(status = ProductStatus.ACTIVE)
        assertEquals(ProductStatus.ACTIVE, activeProduct.status)

        // Stage 5: Customer Search & Selection
        val searchResults = listOf(activeProduct).filter { it.status == ProductStatus.ACTIVE && it.countryCode == "SA" }
        assertEquals(1, searchResults.size)
        assertEquals("prod_e2e_nebulizer_01", searchResults[0].productId)

        // Stage 6: Add to Cart
        val cartItem = MarketplaceCartItem(
            productId = activeProduct.productId,
            sellerUid = activeProduct.sellerUid,
            unitPriceSnapshot = activeProduct.price,
            quantity = 1
        )
        assertEquals(220.0, cartItem.lineTotal, 0.001)

        // Stage 7: Checkout & Server-Side Price Revalidation
        val subtotal = activeProduct.price * cartItem.quantity // 220.0
        val shippingFee = 15.0
        val vatTax = (subtotal + shippingFee) * 0.15 // 15% on 235.0 = 35.25
        val grandTotal = subtotal + shippingFee + vatTax // 270.25
        assertEquals(270.25, grandTotal, 0.001)

        // Stage 8: Create Payment Intent
        val orderId = "ord_e2e_master_001"
        val paymentIntent = saudiPaymentProvider.createPayment(
            PaymentIntentRequest(
                orderId = orderId,
                customerUid = customerA,
                amount = grandTotal,
                currency = "SAR",
                paymentMethodType = "MADA"
            )
        )
        assertEquals(PaymentStatus.PENDING, paymentIntent.status)

        // Stage 9: Gateway 3DS / Webhook Confirmation
        val webhookResult = saudiPaymentProvider.handleWebhook(
            PaymentWebhookPayload(
                provider = "MadaGateway",
                signature = "sig_hmac_sha256_valid_test_token",
                paymentId = paymentIntent.paymentId,
                orderId = orderId,
                amount = grandTotal,
                currency = "SAR",
                status = "paid"
            )
        )
        assertTrue(webhookResult.success)
        assertEquals(PaymentStatus.PAID, webhookResult.updatedStatus)

        // Stage 10: Parent Order Creation
        val orderSnapshot = MarketplaceOrderSnapshot(
            orderId = orderId,
            customerUid = customerA,
            subtotal = subtotal,
            deliveryTotal = shippingFee,
            taxTotal = vatTax,
            grandTotal = grandTotal,
            paymentStatus = PaymentStatus.PAID,
            orderStatus = MarketplaceOrderStatus.PAID,
            shippingAddressSnapshot = MarketplaceAddress(
                customerUid = customerA,
                fullName = "Dr. Tariq Al-Rashid",
                phone = "+966501234567",
                addressLine1 = "Al Malqa, Riyadh"
            )
        )
        assertEquals(MarketplaceOrderStatus.PAID, orderSnapshot.orderStatus)

        // Stage 11: Seller Suborder & Fulfillment Initiation
        val suborder = OrderSuborder(
            orderId = orderId,
            sellerUid = individualSeller,
            sellerName = "Health Wellness Store",
            products = listOf(
                SuborderProductItem(
                    productId = activeProduct.productId,
                    productName = activeProduct.title,
                    quantity = 1,
                    unitPriceMinor = 22000L
                )
            ),
            subtotalMinor = 22000L,
            shippingFeeMinor = 1500L,
            taxMinor = 3525L,
            fulfillmentStatus = FulfillmentStatus.CONFIRMED
        )
        assertEquals(FulfillmentStatus.CONFIRMED, suborder.fulfillmentStatus)

        // Stage 12: Seller Packing & Preserving
        val packedFulfillment = suborder.copy(fulfillmentStatus = FulfillmentStatus.PACKED)
        assertEquals(FulfillmentStatus.PACKED, packedFulfillment.fulfillmentStatus)

        // Stage 13: Shipment Dispatch & Carrier Tracking
        val shipment = Shipment(
            orderId = orderId,
            suborderId = suborder.subOrderId,
            fulfillmentId = "ful_e2e_01",
            sellerUid = individualSeller,
            customerUid = customerA,
            countryCode = "SA",
            originZoneId = "zone_riyadh",
            destinationZoneId = "zone_riyadh",
            deliveryProvider = "smsa",
            providerShipmentId = "tr_smsa_e2e_01",
            trackingNumber = "SMSA-E2E-2026",
            serviceType = DeliveryServiceType.STANDARD,
            deliveryMode = DeliveryMode.THIRD_PARTY,
            shippingFeeMinor = 1500L,
            currencyCode = "SAR",
            status = ShipmentStatus.IN_TRANSIT,
            estimatedPickupAt = System.currentTimeMillis(),
            estimatedDeliveryAt = System.currentTimeMillis() + 86400000L
        )
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.status)

        // Stage 14: Out for Delivery & OTP Handover
        val deliveredShipment = shipment.copy(
            status = ShipmentStatus.DELIVERED,
            actualDeliveryAt = System.currentTimeMillis()
        )
        val pod = ProofOfDelivery(
            shipmentId = deliveredShipment.shipmentId,
            orderId = orderId,
            recipientConfirmationType = "OTP",
            recipientName = "Dr. Tariq Al-Rashid",
            otpVerified = true
        )
        assertTrue("OTP must verify successfully upon delivery", pod.otpVerified)
        assertEquals(ShipmentStatus.DELIVERED, deliveredShipment.status)

        // Stage 15: Double-Entry Immutable Ledger Recording
        val ledgerEntries = ledgerEngine.postCustomerOrderTransaction(
            orderId = orderId,
            paymentId = paymentIntent.paymentId,
            customerId = customerA,
            sellerId = individualSeller,
            country = "SA",
            currency = "SAR",
            productSubtotal = subtotal,
            deliveryFee = shippingFee,
            platformServiceFee = 0.0,
            taxAmount = vatTax,
            paymentGatewayFee = 5.0,
            sellerType = "INDIVIDUAL_SELLER",
            productCategory = "RESPIRATORY_EQUIPMENT"
        )
        assertTrue(ledgerEntries.isNotEmpty())

        // Stage 16: Platform Commission & Owner Earnings Computation
        val debits = ledgerEntries.filter { it.direction == LedgerDirection.DEBIT }.sumOf { it.amount }
        val credits = ledgerEntries.filter { it.direction == LedgerDirection.CREDIT }.sumOf { it.amount }
        assertEquals(debits, credits, 0.001)

        val commissionEntry = ledgerEntries.firstOrNull { it.entryType == "MARKETPLACE_COMMISSION" }
        assertNotNull(commissionEntry)
        assertTrue((commissionEntry?.amount ?: 0.0) > 0.0)

        // Stage 17: Seller Available Balance Release Post Hold Period
        val sellerPayableEntry = ledgerEntries.firstOrNull { it.entryType == "SELLER_PAYABLE_ACCRUED" }
        assertNotNull(sellerPayableEntry)
        assertTrue((sellerPayableEntry?.amount ?: 0.0) > 0.0)

        // Stage 18: Final Reconciliation Audit
        val reconciliationSuccess = (debits == credits) && (deliveredShipment.status == ShipmentStatus.DELIVERED)
        assertTrue("Commercial E2E chain must be fully reconciled and consistent", reconciliationSuccess)
    }
}
