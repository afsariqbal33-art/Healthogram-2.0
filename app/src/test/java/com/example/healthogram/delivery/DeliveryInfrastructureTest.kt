package com.example.healthogram.delivery

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Step 15: Country-wise Delivery, Shipping & Fulfillment System Automated Verification Suite.
 * Validates:
 * 1. Multi-country configuration & sovereign boundary enforcement
 * 2. Delivery zone & postal code resolution
 * 3. Strict address validation
 * 4. Server-authoritative shipping rate calculation with integer minor units
 * 5. Free shipping threshold application
 * 6. International delivery boundary locking (Cross-border disabled)
 * 7. Medical product restriction enforcement
 * 8. End-to-end shipment & package creation with privacy filtering
 * 9. Proof of delivery & contactless OTP handoff
 * 10. Delivery failure handling & Return-to-Seller (RTS)
 * 11. Emergency delivery stop kill switch
 * 12. Double-entry financial delivery ledger reconciliation
 */
class DeliveryInfrastructureTest {

    private lateinit var repository: DeliveryRepository
    private lateinit var engine: DeliveryEngine

    private val validCustomerAddressSA = CustomerDeliveryAddress(
        addressId = "addr_test_cust_sa",
        customerUid = "usr_patient_test",
        countryCode = "SA",
        fullName = "Dr. Sarah Al-Ahmad",
        phoneReference = "+966 50 123 4567",
        addressLine1 = "King Fahd Road, Al Olaya",
        city = "Riyadh",
        region = "Riyadh Province",
        postalCode = "12211",
        isDefault = true,
        validated = true
    )

    private val validSellerAddressSA = CustomerDeliveryAddress(
        addressId = "addr_test_seller_sa",
        customerUid = "seller_test_pharmacy",
        countryCode = "SA",
        fullName = "Al-Noor Central Pharmacy",
        phoneReference = "+966 11 222 3333",
        addressLine1 = "King Fahd Medical District",
        city = "Riyadh",
        region = "Riyadh Province",
        postalCode = "11564"
    )

    private val validCustomerAddressAE = CustomerDeliveryAddress(
        addressId = "addr_test_cust_ae",
        customerUid = "usr_patient_ae",
        countryCode = "AE",
        fullName = "Omar Al-Mansoor",
        phoneReference = "+971 50 987 6543",
        addressLine1 = "Sheikh Zayed Road",
        city = "Dubai",
        region = "Dubai",
        postalCode = "00000"
    )

    private val standardCartProducts = listOf(
        SuborderProductItem(
            productId = "prod_bio_01",
            productName = "Cold-Chain Bio-Complex Supplements",
            quantity = 2,
            unitPriceMinor = 4500L,
            weightGrams = 250,
            temperatureSensitive = true
        )
    )

    @Before
    fun setup() {
        repository = DeliveryRepository.getInstance()
        engine = DeliveryEngine(repository)

        // Reset feature flags
        repository.updateFeatureFlags(
            DeliveryFeatureFlags(
                emergencyDeliveryStop = false,
                emergencyShipmentCreationStop = false,
                emergencyPayoutHold = false,
                internationalDeliveryEnabled = false // strictly false
            )
        )
    }

    @Test
    fun testCountryDeliveryConfigurationsLoaded() {
        val configs = repository.countryConfigs.value
        assertTrue("Saudi Arabia must be configured", configs.containsKey("SA"))
        assertTrue("UAE must be configured", configs.containsKey("AE"))
        assertTrue("USA must be configured", configs.containsKey("US"))

        val saConfig = configs["SA"]!!
        assertEquals("Saudi Arabia", saConfig.countryName)
        assertEquals("SAR", saConfig.defaultCurrency)
        assertTrue("Delivery must be enabled in SA", saConfig.deliveryEnabled)
        assertTrue("POD is required in SA", saConfig.proofOfDeliveryRequired)
        assertFalse("International delivery MUST be false initially", saConfig.internationalDeliveryEnabled)
        assertEquals("aramex", saConfig.defaultDeliveryProvider)
    }

    @Test
    fun testDeliveryZoneResolution() {
        val riyadhZone = engine.resolveZone("SA", "Riyadh", "12211")
        assertNotNull("Riyadh zone should be resolved", riyadhZone)
        assertEquals("RUH-METRO", riyadhZone?.zoneCode)
        assertTrue("Same-day available in Riyadh", riyadhZone?.sameDayAvailable ?: false)

        val dubaiZone = engine.resolveZone("AE", "Dubai", "00000")
        assertNotNull("Dubai zone should be resolved", dubaiZone)
        assertEquals("DXB-METRO", dubaiZone?.zoneCode)
    }

    @Test
    fun testAddressValidation_ValidAndInvalid() {
        val validResult = engine.validateDeliveryAddress(validCustomerAddressSA)
        assertTrue("Valid address should pass", validResult.isValid)
        assertTrue("Should be marked serviceable", validResult.isServiceable)
        assertEquals(0, validResult.errors.size)

        val invalidAddress = validCustomerAddressSA.copy(
            addressLine1 = "",
            phoneReference = "",
            city = ""
        )
        val invalidResult = engine.validateDeliveryAddress(invalidAddress)
        assertFalse("Incomplete address must fail validation", invalidResult.isValid)
        assertTrue("Should list validation errors", invalidResult.errors.size >= 3)
    }

    @Test
    fun testServerSideShippingRateCalculation() {
        val quote = engine.calculateShippingRate(
            customerAddress = validCustomerAddressSA,
            sellerAddress = validSellerAddressSA,
            products = standardCartProducts,
            deliveryMode = DeliveryMode.PLATFORM_MANAGED,
            serviceType = DeliveryServiceType.SAME_DAY
        )

        assertNotNull("Shipping quote must be generated", quote)
        assertEquals("SAR", quote.currencyCode)
        assertTrue("Shipping fee must be greater than zero", quote.shippingFeeMinor > 0)
        assertTrue("Tax must be computed", quote.taxMinor > 0)
        assertEquals(quote.shippingFeeMinor + quote.taxMinor, quote.totalShippingMinor)
        assertEquals(ShippingQuoteStatus.ACTIVE, quote.status)
        assertFalse("Quote should not be expired immediately", quote.isExpired())
    }

    @Test
    fun testFreeShippingThreshold() {
        val highValueProducts = listOf(
            SuborderProductItem(
                productId = "prod_expensive_equip",
                productName = "Clinical Diagnostics Analyzer",
                quantity = 1,
                unitPriceMinor = 25000L, // SAR 250.00 (Exceeds SAR 200 threshold)
                weightGrams = 500
            )
        )

        val quote = engine.calculateShippingRate(
            customerAddress = validCustomerAddressSA,
            sellerAddress = validSellerAddressSA,
            products = highValueProducts,
            deliveryMode = DeliveryMode.THIRD_PARTY,
            serviceType = DeliveryServiceType.STANDARD
        )

        assertEquals("Shipping fee must be 0 when exceeding free threshold", 0L, quote.shippingFeeMinor)
        assertEquals("Total shipping must be 0", 0L, quote.totalShippingMinor)
    }

    @Test(expected = IllegalStateException::class)
    fun testInternationalDeliveryBlockedByDefault() {
        // Customer in UAE, Seller in Saudi Arabia (Cross-border)
        engine.calculateShippingRate(
            customerAddress = validCustomerAddressAE,
            sellerAddress = validSellerAddressSA,
            products = standardCartProducts,
            deliveryMode = DeliveryMode.THIRD_PARTY,
            serviceType = DeliveryServiceType.STANDARD
        )
    }

    @Test
    fun testProductShippingRestrictions() {
        val restrictedProducts = listOf(
            SuborderProductItem(
                productId = "prod_narcotic",
                productName = "Controlled Narcotic Analgesic Tablet",
                quantity = 1,
                unitPriceMinor = 5000L
            )
        )

        val warnings = engine.checkProductRestrictions("SA", restrictedProducts)
        assertTrue("Prohibited product must be detected", warnings.isNotEmpty())
        assertTrue("Should contain prohibited warning", warnings.first().contains("prohibited", ignoreCase = true))
    }

    @Test
    fun testShipmentAndPackageCreation() {
        val shipment = engine.createShipment(
            orderId = "ord_test_901",
            suborderId = "subord_test_901",
            fulfillmentId = "ful_test_901",
            sellerUid = "seller_test_pharmacy",
            customerUid = "usr_patient_test",
            originAddress = validSellerAddressSA,
            destinationAddress = validCustomerAddressSA,
            products = standardCartProducts,
            deliveryMode = DeliveryMode.PLATFORM_MANAGED,
            serviceType = DeliveryServiceType.SAME_DAY
        )

        assertNotNull("Shipment must be created", shipment)
        assertTrue("Tracking number must start with HG-SA", shipment.trackingNumber.startsWith("HG-SA"))
        assertEquals("internal_fleet", shipment.deliveryProvider)

        val pkg = repository.packages.value.values.firstOrNull { it.shipmentId == shipment.shipmentId }
        assertNotNull("Package record must exist", pkg)
        assertEquals("Healthcare and Wellness Essentials", pkg?.contentsSummary)
        assertTrue("Must be flagged temperature sensitive", pkg?.temperatureSensitive ?: false)

        val trackingEvents = repository.trackingEvents.value[shipment.shipmentId]
        assertNotNull("Initial tracking event must exist", trackingEvents)
        assertEquals("SHIPMENT_CREATED", trackingEvents?.first()?.status)
    }

    @Test
    fun testContactlessOtpProofOfDelivery() {
        val shipment = engine.createShipment(
            orderId = "ord_test_902",
            suborderId = "subord_test_902",
            fulfillmentId = "ful_test_902",
            sellerUid = "seller_test_pharmacy",
            customerUid = "usr_patient_test",
            originAddress = validSellerAddressSA,
            destinationAddress = validCustomerAddressSA,
            products = standardCartProducts,
            deliveryMode = DeliveryMode.PLATFORM_MANAGED,
            serviceType = DeliveryServiceType.SAME_DAY
        )

        val otp = repository.deliveryOtps.value[shipment.shipmentId]
        assertNotNull("Delivery OTP must be generated", otp)

        // Verify invalid code
        val failedVerification = engine.verifyDeliveryOtp(shipment.shipmentId, "000000", "Dr. Sarah Al-Ahmad")
        assertFalse("Wrong OTP must fail", failedVerification)

        // Verify correct code
        val successVerification = engine.verifyDeliveryOtp(shipment.shipmentId, otp!!.hashedOtp, "Dr. Sarah Al-Ahmad")
        assertTrue("Correct OTP must succeed", successVerification)

        val updatedShipment = repository.shipments.value[shipment.shipmentId]
        assertEquals(ShipmentStatus.DELIVERED, updatedShipment?.status)

        val pod = repository.proofsOfDelivery.value[shipment.shipmentId]
        assertNotNull("Proof of Delivery record must be persisted", pod)
        assertEquals("OTP", pod?.recipientConfirmationType)
        assertTrue("OTP verified flag must be true", pod?.otpVerified ?: false)
    }

    @Test
    fun testDeliveryFailureAndReturnToSeller() {
        val shipment = engine.createShipment(
            orderId = "ord_test_903",
            suborderId = "subord_test_903",
            fulfillmentId = "ful_test_903",
            sellerUid = "seller_test_pharmacy",
            customerUid = "usr_patient_test",
            originAddress = validSellerAddressSA,
            destinationAddress = validCustomerAddressSA,
            products = standardCartProducts,
            deliveryMode = DeliveryMode.PLATFORM_MANAGED,
            serviceType = DeliveryServiceType.SAME_DAY
        )

        val failure = engine.recordDeliveryFailure(
            shipmentId = shipment.shipmentId,
            reasonCode = "customer_unavailable",
            description = "Recipient phone unanswered at door."
        )
        assertNotNull(failure)
        val failedShipment = repository.shipments.value[shipment.shipmentId]
        assertEquals(ShipmentStatus.FAILED, failedShipment?.status)

        val rts = engine.initiateReturnToSeller(shipment.shipmentId, "Exceeded delivery retry attempts")
        assertNotNull(rts)
        val rtsShipment = repository.shipments.value[shipment.shipmentId]
        assertEquals(ShipmentStatus.RETURNED_TO_SELLER, rtsShipment?.status)
    }

    @Test(expected = IllegalStateException::class)
    fun testEmergencyDeliveryStopKillSwitch() {
        repository.updateFeatureFlags(repository.featureFlags.value.copy(emergencyDeliveryStop = true))

        engine.calculateShippingRate(
            customerAddress = validCustomerAddressSA,
            sellerAddress = validSellerAddressSA,
            products = standardCartProducts,
            deliveryMode = DeliveryMode.PLATFORM_MANAGED,
            serviceType = DeliveryServiceType.SAME_DAY
        )
    }

    @Test
    fun testDeliveryLedgerEntries() {
        val initialLedgerCount = repository.deliveryLedger.value.size

        val shipment = engine.createShipment(
            orderId = "ord_test_904",
            suborderId = "subord_test_904",
            fulfillmentId = "ful_test_904",
            sellerUid = "seller_test_pharmacy",
            customerUid = "usr_patient_test",
            originAddress = validSellerAddressSA,
            destinationAddress = validCustomerAddressSA,
            products = standardCartProducts,
            deliveryMode = DeliveryMode.PLATFORM_MANAGED,
            serviceType = DeliveryServiceType.SAME_DAY
        )

        val newLedger = repository.deliveryLedger.value
        assertTrue("Ledger entries must be added for shipment", newLedger.size >= initialLedgerCount + 3)

        val shippingCharge = newLedger.firstOrNull { it.shipmentId == shipment.shipmentId && it.entryType == DeliveryLedgerEntryType.SHIPPING_CHARGE }
        val providerCost = newLedger.firstOrNull { it.shipmentId == shipment.shipmentId && it.entryType == DeliveryLedgerEntryType.DELIVERY_PROVIDER_COST }
        val platformRevenue = newLedger.firstOrNull { it.shipmentId == shipment.shipmentId && it.entryType == DeliveryLedgerEntryType.PLATFORM_DELIVERY_REVENUE }

        assertNotNull("Customer shipping charge must be posted", shippingCharge)
        assertNotNull("Provider cost must be posted", providerCost)
        assertNotNull("Platform delivery revenue margin must be posted", platformRevenue)

        assertEquals(
            "Shipping Charge minus Provider Cost must equal Platform Revenue",
            shippingCharge!!.amountMinor - providerCost!!.amountMinor,
            platformRevenue!!.amountMinor
        )
    }
}
