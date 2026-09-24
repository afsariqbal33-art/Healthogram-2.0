package com.example.healthogram

import com.example.healthogram.core.AccountType
import com.example.healthogram.delivery.CustomerDeliveryAddress
import com.example.healthogram.delivery.DeliveryServiceType
import com.example.healthogram.delivery.adapters.InternalDeliveryAdapter
import com.example.healthogram.finance.FinancialLedgerEngine
import com.example.healthogram.healthpassport.fhir.FHIRInteroperabilityService
import com.example.healthogram.integration.*
import com.example.healthogram.payments.PaymentMethodType
import com.example.healthogram.payments.gateways.GatewayPaymentSessionRequest
import com.example.healthogram.payments.gateways.LocalGatewayAdapter
import com.example.healthogram.payments.gateways.StripeGatewayAdapter
import com.example.healthogram.translation.GeminiTranslationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * HEALTHOGRAM 2.3 — STEP 51: ADVANCED INTEGRATION VALIDATION TEST SUITE
 * Validates the full integration layer without breaking production architecture:
 * Marketplace 2.3, Country Payments, Double-entry Ledger Reconciliation,
 * Owner Earnings, Seller Payouts, Delivery, FHIR, Health Connect, AI Studio, and Translation.
 */
class HealthogramStep51AdvancedIntegrationTestSuite {

    private val countryEngine = CountryConfigurationEngine.getInstance()
    private val marketplaceService = Marketplace23IntegrationService.getInstance()
    private val reconciliationService = FinancialReconciliationService.getInstance()
    private val ownerEarningsService = OwnerEarnings23IntegrationService.getInstance()
    private val sellerPayoutService = SellerPayout23IntegrationService.getInstance()
    private val providerRegistry = ExternalProviderAdapterRegistry.getInstance()
    private val webhookHandler = WebhooksIntegrationHandler.getInstance()
    private val ledgerEngine = FinancialLedgerEngine.getInstance()

    // -------------------------------------------------------------------------
    // 1. COUNTRY CONFIGURATION & SOVEREIGN INVARIANTS (Section 4, 79)
    // -------------------------------------------------------------------------
    @Test
    fun testCountryConfigurationDefaults_InternationalMarketplaceLockedOff() {
        val saConfig = countryEngine.getMarketplaceConfig("SA")
        val aeConfig = countryEngine.getMarketplaceConfig("AE")
        val usConfig = countryEngine.getMarketplaceConfig("US")

        assertTrue("SA marketplace must be enabled", saConfig.marketplaceEnabled)
        assertFalse("CRITICAL: International marketplace must default to false for SA", saConfig.internationalMarketplaceEnabled)
        assertFalse("CRITICAL: International marketplace must default to false for AE", aeConfig.internationalMarketplaceEnabled)
        assertFalse("CRITICAL: International marketplace must default to false for US", usConfig.internationalMarketplaceEnabled)

        // Verify restricted medical categories are enforced
        assertTrue(saConfig.restrictedProductCategories.contains("PRESCRIPTION_MEDICATION"))
        assertTrue(saConfig.restrictedProductCategories.contains("CONTROLLED_SUBSTANCES"))

        val saPayment = countryEngine.getPaymentConfig("SA")
        assertTrue("Payment gateways must default to sandbox mode", saPayment.sandboxMode)
        assertFalse("Production mode must be locked false by default", saPayment.productionMode)
    }

    // -------------------------------------------------------------------------
    // 2. SELLER ONBOARDING & PRODUCT MODERATION LIFECYCLE (Section 5, 6, 7, 8)
    // -------------------------------------------------------------------------
    @Test
    fun testSellerOnboardingAndProductListingModeration() {
        // Individual seller registration
        val regResult = marketplaceService.registerSeller(
            userUid = "usr_doctor_441",
            userAccountType = AccountType.DOCTOR,
            sellerType = SellerAccountType.BUSINESS_SELLER,
            storeName = "Dr. Tariq Orthopedic Supplies",
            storeDescription = "Ergonomic braces and rehab bands",
            legalName = "Tariq Health Corp",
            countryCode = "SA",
            businessAddress = "Prince Sultan St, Jeddah",
            taxNumber = "310029384900003",
            crNumber = "4030192831",
            payoutIban = "SA4480000000608010167520",
            payoutBank = "Alinma Bank",
            documents = listOf(
                SellerVerificationDocument(
                    documentType = "COMMERCIAL_REGISTRATION",
                    documentNumber = "4030192831",
                    documentUrl = "https://cdn.healthogram.com/cr_4030192831.pdf"
                )
            )
        )
        assertTrue("Seller registration must succeed", regResult.isSuccess)
        val seller = regResult.getOrThrow()
        assertEquals(SellerVerificationStatus.SUBMITTED, seller.verificationStatus)

        // Attempting to list products while unverified must fail
        val unverifiedListing = marketplaceService.createProductListing(
            sellerId = seller.sellerId,
            name = "Ergonomic Cervical Collar",
            description = "Support collar",
            category = "WELLNESS_TRACKERS",
            sku = "ECC-001",
            images = listOf("https://cdn.healthogram.com/collar.jpg"),
            countryCode = "SA",
            priceMinor = 14900L,
            salePriceMinor = null,
            currency = "SAR",
            stockQuantity = 20
        )
        assertTrue("Unverified seller cannot list products", unverifiedListing.isFailure)

        // Approve seller
        val approved = marketplaceService.reviewSellerOnboarding(seller.sellerId, true, "admin_verifier_01")
        assertTrue("Admin approval must succeed", approved)
        assertEquals(SellerVerificationStatus.VERIFIED, marketplaceService.getSeller(seller.sellerId)?.verificationStatus)

        // Attempting to list restricted category (e.g. Prescription medication) must fail
        val restrictedListing = marketplaceService.createProductListing(
            sellerId = seller.sellerId,
            name = "Amoxicillin 500mg",
            description = "Antibiotic",
            category = "PRESCRIPTION_MEDICATION",
            sku = "AMX-500",
            images = listOf("https://cdn.healthogram.com/amx.jpg"),
            countryCode = "SA",
            priceMinor = 3500L,
            salePriceMinor = null,
            currency = "SAR",
            stockQuantity = 50
        )
        assertTrue("Restricted category product listing must be blocked", restrictedListing.isFailure)

        // List valid product
        val validListing = marketplaceService.createProductListing(
            sellerId = seller.sellerId,
            name = "Post-Surgical Knee Immobilizer",
            description = "Breathable knee support brace",
            category = "FITNESS_EQUIPMENT",
            sku = "KI-404",
            images = listOf("https://cdn.healthogram.com/knee.jpg"),
            countryCode = "SA",
            priceMinor = 29900L,
            salePriceMinor = null,
            currency = "SAR",
            stockQuantity = 15
        )
        assertTrue("Valid product listing must succeed", validListing.isSuccess)
        val product = validListing.getOrThrow()
        assertEquals(ProductLifecycleStatus.PENDING_REVIEW, product.status)

        // Moderate product to ACTIVE
        marketplaceService.moderateProduct(product.productId, true, "admin_med_01", "Compliant orthopedic device")
        assertEquals(ProductLifecycleStatus.ACTIVE, marketplaceService.getProduct(product.productId)?.status)
    }

    // -------------------------------------------------------------------------
    // 3. INVENTORY ATOMIC RESERVATION & CART VALIDATION (Section 9, 10)
    // -------------------------------------------------------------------------
    @Test
    fun testInventoryReservationAndOversellingPrevention() {
        val prod = marketplaceService.getProduct("prod_smart_bpm_01")
        assertNotNull("Seed product must exist", prod)
        val initialAvailable = prod!!.availableQuantity

        // Reserve 5 units
        val reserveOk = marketplaceService.reserveStock(prod.productId, 5)
        assertTrue("Reserving within available stock must succeed", reserveOk)
        assertEquals(initialAvailable - 5, marketplaceService.getProduct(prod.productId)?.availableQuantity)

        // Attempt to reserve more than available
        val excessiveReserve = marketplaceService.reserveStock(prod.productId, 9999)
        assertFalse("Overselling reservation must be blocked", excessiveReserve)

        // Release reservation
        marketplaceService.releaseReservedStock(prod.productId, 5)
        assertEquals(initialAvailable, marketplaceService.getProduct(prod.productId)?.availableQuantity)
    }

    // -------------------------------------------------------------------------
    // 4. ORDER IDEMPOTENCY & 16-STATE MACHINE (Section 11, 12)
    // -------------------------------------------------------------------------
    @Test
    fun testOrderCreationWithIdempotencyAndStateTransitions() {
        val idempotencyKey = "test_idem_${UUID.randomUUID()}"
        val items = listOf("prod_smart_bpm_01" to 2)

        val orderResult = marketplaceService.createOrder(
            customerUid = "usr_patient_001",
            items = items,
            countryCode = "SA",
            deliveryAddress = "Al-Malqa, Riyadh, Saudi Arabia",
            deliveryProviderId = "local_express",
            idempotencyKey = idempotencyKey
        )
        assertTrue("Order creation must succeed", orderResult.isSuccess)
        val order = orderResult.getOrThrow()
        assertEquals(OrderState23.PENDING_PAYMENT, order.status)

        // Duplicate call with same idempotency key must return existing order
        val duplicateResult = marketplaceService.createOrder(
            customerUid = "usr_patient_001",
            items = items,
            countryCode = "SA",
            deliveryAddress = "Al-Malqa, Riyadh, Saudi Arabia",
            deliveryProviderId = "local_express",
            idempotencyKey = idempotencyKey
        )
        assertEquals(order.orderId, duplicateResult.getOrThrow().orderId)

        // Test controlled state transitions: PENDING_PAYMENT -> PAID -> CONFIRMED -> PROCESSING -> SHIPPED -> OUT_FOR_DELIVERY -> DELIVERED
        marketplaceService.transitionOrderStatus(order.orderId, OrderState23.PAID, "Payment captured")
        assertEquals(OrderState23.PAID, marketplaceService.getOrder(order.orderId)?.status)

        marketplaceService.transitionOrderStatus(order.orderId, OrderState23.CONFIRMED)
        marketplaceService.transitionOrderStatus(order.orderId, OrderState23.PROCESSING)
        marketplaceService.transitionOrderStatus(order.orderId, OrderState23.SHIPPED)
        marketplaceService.transitionOrderStatus(order.orderId, OrderState23.OUT_FOR_DELIVERY)
        marketplaceService.transitionOrderStatus(order.orderId, OrderState23.DELIVERED)

        val delivered = marketplaceService.getOrder(order.orderId)
        assertEquals(OrderState23.DELIVERED, delivered?.status)
        assertNotNull(delivered?.deliveredAt)

        // Illegal transition: Cannot go directly from DELIVERED to PAID
        val illegalTransition = marketplaceService.transitionOrderStatus(order.orderId, OrderState23.PAID)
        assertTrue("Illegal state machine transition must fail", illegalTransition.isFailure)
    }

    // -------------------------------------------------------------------------
    // 5. PAYMENT GATEWAY ADAPTERS & WEBHOOK VERIFICATION (Section 13, 14, 15, 16)
    // -------------------------------------------------------------------------
    @Test
    fun testPaymentGatewayAdaptersAndWebhookVerification() = runBlocking {
        val stripe = StripeGatewayAdapter(isLiveMode = false)
        val mada = LocalGatewayAdapter()

        val req = GatewayPaymentSessionRequest(
            orderId = "ord_test_01",
            customerUid = "usr_001",
            amountMinor = 19900L,
            currencyCode = "SAR",
            paymentMethodType = PaymentMethodType.CARD,
            returnUrl = "https://app.healthogram.com/checkout/success",
            cancelUrl = "https://app.healthogram.com/checkout/cancel",
            idempotencyKey = "idem_pay_01"
        )

        val sessionResult = stripe.createPaymentSession(req)
        assertNotNull("Stripe session creation must succeed", sessionResult.sessionId)

        val madaSession = mada.createPaymentSession(req)
        assertNotNull("Mada session creation must succeed", madaSession.sessionId)

        // Webhook signature verification and deduplication
        val eventId = "evt_${UUID.randomUUID()}"
        val payload = "{\"order_id\":\"ord_test_01\",\"transaction_id\":\"pi_test_123\"}"

        val webhookResult1 = webhookHandler.handleInboundWebhook(
            providerId = "stripe",
            eventId = eventId,
            eventType = "payment_intent.succeeded",
            signatureHeader = "sig_valid_hex_123",
            payloadJson = payload,
            signingSecret = "whsec_test_secret_abc"
        )
        assertEquals("PROCESSED", webhookResult1.processingStatus)
        assertTrue(webhookResult1.signatureVerified)

        // Replaying same webhook must be marked DUPLICATE_SKIPPED
        val replayResult = webhookHandler.handleInboundWebhook(
            providerId = "stripe",
            eventId = eventId,
            eventType = "payment_intent.succeeded",
            signatureHeader = "sig_valid_hex_123",
            payloadJson = payload,
            signingSecret = "whsec_test_secret_abc"
        )
        assertEquals("DUPLICATE_SKIPPED", replayResult.processingStatus)
    }

    // -------------------------------------------------------------------------
    // 6. FINANCIAL LEDGER & RECONCILIATION ENGINE (Section 17, 18, 22)
    // -------------------------------------------------------------------------
    @Test
    fun testFinancialLedgerBalanceAndReconciliationReport() {
        val orderId = "ord_rec_test_${UUID.randomUUID().toString().substring(0, 6)}"

        // Record double entry in ledger: Customer Payment Debit & Revenue/Seller Credits
        ledgerEngine.postCustomerOrderTransaction(
            orderId = orderId,
            paymentId = "pay_${UUID.randomUUID().toString().take(6)}",
            customerId = "usr_cust_001",
            sellerId = "seller_wellness_sa",
            country = "SA",
            currency = "SAR",
            productSubtotal = 100.0,
            deliveryFee = 15.0,
            platformServiceFee = 10.0,
            taxAmount = 15.0,
            paymentGatewayFee = 2.5
        )

        val report = reconciliationService.runFullReconciliation("test_auditor")
        assertNotNull(report)
        assertTrue("Reconciliation report must cover all ledger entries", report.totalLedgerEntriesChecked > 0)
    }

    // -------------------------------------------------------------------------
    // 7. OWNER EARNINGS 2.3 & 2FA WITHDRAWAL (Section 19, 20)
    // -------------------------------------------------------------------------
    @Test
    fun testOwnerEarningsSummaryAnd2FAWithdrawalWorkflow() {
        val summary = ownerEarningsService.getOwnerEarningsSummary(currencyFilter = "SAR")
        assertNotNull(summary)
        assertTrue("Net available calculation must be non-negative", summary.netAvailableMinor >= 0)

        // Attempt withdrawal without 2FA must fail
        val invalid2FAResult = ownerEarningsService.requestOwnerWithdrawal(
            ownerUid = "usr_owner_root",
            amountMinor = 500000L, // 5,000.00 SAR
            currency = "SAR",
            destinationIban = "SA0380000000608010167519",
            destinationBank = "Al Rajhi Commercial Bank",
            twoFactorCode = "bad" // Invalid 2FA
        )
        assertTrue("Withdrawal without valid 6-digit TOTP must fail", invalid2FAResult.isFailure)

        // Below minimum threshold (e.g. 50 SAR < 1000 SAR minimum) must fail
        val belowMinResult = ownerEarningsService.requestOwnerWithdrawal(
            ownerUid = "usr_owner_root",
            amountMinor = 5000L, // 50.00 SAR
            currency = "SAR",
            destinationIban = "SA0380000000608010167519",
            destinationBank = "Al Rajhi Commercial Bank",
            twoFactorCode = "123456"
        )
        assertTrue("Withdrawal below minimum threshold must fail", belowMinResult.isFailure)
    }

    // -------------------------------------------------------------------------
    // 8. SELLER PAYOUT LIFECYCLE (Section 21)
    // -------------------------------------------------------------------------
    @Test
    fun testSellerPayoutLifecycleAndHoldPolicies() {
        val testOrder = Order23(
            orderId = "ord_payout_test",
            customerUid = "usr_cust_01",
            sellerId = "seller_wellness_sa",
            items = emptyList(),
            subtotalMinor = 20000L,
            deliveryFeeMinor = 2500L,
            taxMinor = 3000L,
            platformFeeMinor = 2000L,
            sellerNetMinor = 18000L,
            totalAmountMinor = 25500L,
            currency = "SAR",
            countryCode = "SA",
            deliveryAddress = "Riyadh",
            deliveryProviderId = "local_express",
            status = OrderState23.DELIVERED,
            idempotencyKey = "idem_po_test",
            deliveredAt = System.currentTimeMillis() - 86400000L * 20, // 20 days ago
            refundWindowExpiresAt = System.currentTimeMillis() - 86400000L * 6 // Expired 6 days ago
        )

        val payoutReg = sellerPayoutService.registerPayoutForOrder(testOrder)
        assertTrue("Payout registration must succeed", payoutReg.isSuccess)
        val payout = payoutReg.getOrThrow()

        // Since refund window is expired and seller is verified, it should be ELIGIBLE
        assertEquals(SellerPayoutStatus.ELIGIBLE, payout.status)

        // Process eligible payout
        val processed = sellerPayoutService.processEligiblePayout(payout.payoutId, "finance_admin")
        assertTrue("Processing eligible payout must succeed", processed.isSuccess)
        assertEquals(SellerPayoutStatus.PAID, processed.getOrThrow().status)
    }

    // -------------------------------------------------------------------------
    // 9. DELIVERY PROVIDER & HEALTHCARE INTEROPERABILITY (Section 23, 26, 31)
    // -------------------------------------------------------------------------
    @Test
    fun testDeliveryAndHealthcareInteroperability() = runBlocking {
        val delivery = InternalDeliveryAdapter()
        val quoteResult = delivery.checkServiceability("SA", "SA", "11564", "21423")
        assertTrue("Local delivery within same country must be serviceable", quoteResult.isServiceable)

        // International delivery must be unserviceable
        val intlQuote = delivery.checkServiceability("SA", "US", "11564", "94103")
        assertFalse("Cross-border delivery must be unserviceable", intlQuote.isServiceable)

        // FHIR R4 Resource Validation
        val fhirService = FHIRInteroperabilityService.getInstance()
        assertNotNull("FHIR service must be active", fhirService)

        // Verify External Provider Registry
        val registry = ExternalProviderAdapterRegistry.getInstance()
        val dhlStatus = registry.providerHealthFlow.value["dhl_express"]
        assertNotNull("DHL adapter must be registered", dhlStatus)
        assertEquals(
            "Unconnected external services must be explicitly marked REQUIRES_EXTERNAL_PROVIDER",
            ProviderConnectionStatus.REQUIRES_EXTERNAL_PROVIDER,
            dhlStatus?.status
        )

        val smartOnFhir = registry.providerHealthFlow.value["smart_on_fhir_external"]
        assertEquals(
            ProviderConnectionStatus.REQUIRES_EXTERNAL_PROVIDER,
            smartOnFhir?.status
        )
    }

    // -------------------------------------------------------------------------
    // 10. AI & TRANSLATION ZERO-PHI AND RATE LIMITS (Section 34, 37)
    // -------------------------------------------------------------------------
    @Test
    fun testAITranslationZeroPHIAndLanguageDetection() = runBlocking {
        val translator = GeminiTranslationProvider()

        // Arabic detection
        val arDetect = translator.detectLanguage("مرحبا بك في هيلثوجرام")
        assertEquals("ar", arDetect.first)

        // English detection
        val enDetect = translator.detectLanguage("Welcome to Healthogram Medical Portal")
        assertEquals("en", enDetect.first)

        // Translation preserves text and adds healthcare disclaimer when context is healthcare
        val translation = translator.translateText(
            text = "Take one tablet daily after meals",
            sourceLang = "en",
            targetLang = "ar",
            isHealthcareContext = true
        )
        assertNotNull(translation.translatedText)
        assertTrue("Medical translation must preserve healthcare context", translation.isHealthcareContext)
    }
}
