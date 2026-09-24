package com.example.healthogram.delivery

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Healthogram Master Repository for Country-wise Delivery, Shipping & Order Fulfillment.
 * Implements in-memory state persistence with pre-configured seed data for Saudi Arabia,
 * UAE, United States, United Kingdom, and Germany.
 */
class DeliveryRepository private constructor() {

    companion object {
        @Volatile
        private var instance: DeliveryRepository? = null

        fun getInstance(): DeliveryRepository {
            return instance ?: synchronized(this) {
                instance ?: DeliveryRepository().also { instance = it }
            }
        }
    }

    // 1. Country Configurations
    private val _countryConfigs = MutableStateFlow<Map<String, CountryDeliveryConfig>>(emptyMap())
    val countryConfigs: StateFlow<Map<String, CountryDeliveryConfig>> = _countryConfigs.asStateFlow()

    // 2. Delivery Zones
    private val _deliveryZones = MutableStateFlow<Map<String, DeliveryZone>>(emptyMap())
    val deliveryZones: StateFlow<Map<String, DeliveryZone>> = _deliveryZones.asStateFlow()

    // 3. Shipping Rate Configs
    private val _shippingRates = MutableStateFlow<List<ShippingRateConfig>>(emptyList())
    val shippingRates: StateFlow<List<ShippingRateConfig>> = _shippingRates.asStateFlow()

    // 4. Shipping Quotes
    private val _shippingQuotes = MutableStateFlow<Map<String, ShippingQuote>>(emptyMap())
    val shippingQuotes: StateFlow<Map<String, ShippingQuote>> = _shippingQuotes.asStateFlow()

    // 5. Order Fulfillments
    private val _fulfillments = MutableStateFlow<Map<String, OrderFulfillment>>(emptyMap())
    val fulfillments: StateFlow<Map<String, OrderFulfillment>> = _fulfillments.asStateFlow()

    // 6. Shipments
    private val _shipments = MutableStateFlow<Map<String, Shipment>>(emptyMap())
    val shipments: StateFlow<Map<String, Shipment>> = _shipments.asStateFlow()

    // 7. Packages
    private val _packages = MutableStateFlow<Map<String, Package>>(emptyMap())
    val packages: StateFlow<Map<String, Package>> = _packages.asStateFlow()

    // 8. Tracking Events
    private val _trackingEvents = MutableStateFlow<Map<String, List<ShipmentTrackingEvent>>>(emptyMap())
    val trackingEvents: StateFlow<Map<String, List<ShipmentTrackingEvent>>> = _trackingEvents.asStateFlow()

    // 9. Delivery Partners & Drivers
    private val _partners = MutableStateFlow<Map<String, DeliveryPartnerRecord>>(emptyMap())
    val partners: StateFlow<Map<String, DeliveryPartnerRecord>> = _partners.asStateFlow()

    private val _drivers = MutableStateFlow<Map<String, DeliveryDriver>>(emptyMap())
    val drivers: StateFlow<Map<String, DeliveryDriver>> = _drivers.asStateFlow()

    private val _assignments = MutableStateFlow<Map<String, DeliveryAssignment>>(emptyMap())
    val assignments: StateFlow<Map<String, DeliveryAssignment>> = _assignments.asStateFlow()

    // 10. Proof of Delivery & OTPs
    private val _proofsOfDelivery = MutableStateFlow<Map<String, ProofOfDelivery>>(emptyMap())
    val proofsOfDelivery: StateFlow<Map<String, ProofOfDelivery>> = _proofsOfDelivery.asStateFlow()

    private val _deliveryOtps = MutableStateFlow<Map<String, DeliveryOtp>>(emptyMap())
    val deliveryOtps: StateFlow<Map<String, DeliveryOtp>> = _deliveryOtps.asStateFlow()

    // 11. Failures, Reschedules, RTS & Customer Returns
    private val _failures = MutableStateFlow<Map<String, DeliveryFailure>>(emptyMap())
    val failures: StateFlow<Map<String, DeliveryFailure>> = _failures.asStateFlow()

    private val _reschedules = MutableStateFlow<Map<String, DeliveryReschedule>>(emptyMap())
    val reschedules: StateFlow<Map<String, DeliveryReschedule>> = _reschedules.asStateFlow()

    private val _returnToSellerRecords = MutableStateFlow<Map<String, ReturnToSeller>>(emptyMap())
    val returnToSellerRecords: StateFlow<Map<String, ReturnToSeller>> = _returnToSellerRecords.asStateFlow()

    private val _customerReturns = MutableStateFlow<Map<String, CustomerReturn>>(emptyMap())
    val customerReturns: StateFlow<Map<String, CustomerReturn>> = _customerReturns.asStateFlow()

    private val _disputes = MutableStateFlow<Map<String, DeliveryDispute>>(emptyMap())
    val disputes: StateFlow<Map<String, DeliveryDispute>> = _disputes.asStateFlow()

    // 12. Financial Ledger & Cash on Delivery
    private val _deliveryLedger = MutableStateFlow<List<DeliveryFinancialLedgerEntry>>(emptyList())
    val deliveryLedger: StateFlow<List<DeliveryFinancialLedgerEntry>> = _deliveryLedger.asStateFlow()

    private val _codRecords = MutableStateFlow<Map<String, CashCollectionRecord>>(emptyMap())
    val codRecords: StateFlow<Map<String, CashCollectionRecord>> = _codRecords.asStateFlow()

    // 13. Delivery Slots
    private val _deliverySlots = MutableStateFlow<List<DeliverySlot>>(emptyList())
    val deliverySlots: StateFlow<List<DeliverySlot>> = _deliverySlots.asStateFlow()

    // 14. Audit Logs, Reconciliation & Reviews
    private val _auditLogs = MutableStateFlow<List<DeliveryAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<DeliveryAuditLog>> = _auditLogs.asStateFlow()

    private val _reconciliationReports = MutableStateFlow<List<DeliveryReconciliationReport>>(emptyList())
    val reconciliationReports: StateFlow<List<DeliveryReconciliationReport>> = _reconciliationReports.asStateFlow()

    private val _reviews = MutableStateFlow<List<DeliveryReview>>(emptyList())
    val reviews: StateFlow<List<DeliveryReview>> = _reviews.asStateFlow()

    // 15. Feature Flags
    private val _featureFlags = MutableStateFlow(DeliveryFeatureFlags())
    val featureFlags: StateFlow<DeliveryFeatureFlags> = _featureFlags.asStateFlow()

    // 16. Restrictions & Customer Addresses
    private val _restrictions = MutableStateFlow<List<ShippingRestriction>>(emptyList())
    val restrictions: StateFlow<List<ShippingRestriction>> = _restrictions.asStateFlow()

    private val _customerAddresses = MutableStateFlow<Map<String, List<CustomerDeliveryAddress>>>(emptyMap())
    val customerAddresses: StateFlow<Map<String, List<CustomerDeliveryAddress>>> = _customerAddresses.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val now = System.currentTimeMillis()

        // Country configs
        val saConfig = CountryDeliveryConfig(
            countryCode = "SA",
            countryName = "Saudi Arabia",
            deliveryEnabled = true,
            defaultCurrency = "SAR",
            defaultTimezone = "Asia/Riyadh",
            supportedDeliveryProviders = listOf("aramex", "smsa", "internal_fleet"),
            defaultDeliveryProvider = "aramex",
            fallbackDeliveryProvider = "internal_fleet",
            internationalDeliveryEnabled = false,
            proofOfDeliveryRequired = true,
            cashOnDeliveryEnabled = true
        )
        val aeConfig = CountryDeliveryConfig(
            countryCode = "AE",
            countryName = "United Arab Emirates",
            deliveryEnabled = true,
            defaultCurrency = "AED",
            defaultTimezone = "Asia/Dubai",
            supportedDeliveryProviders = listOf("aramex", "dhl", "internal_fleet"),
            defaultDeliveryProvider = "aramex",
            fallbackDeliveryProvider = "internal_fleet",
            internationalDeliveryEnabled = false,
            proofOfDeliveryRequired = true,
            cashOnDeliveryEnabled = true
        )
        val usConfig = CountryDeliveryConfig(
            countryCode = "US",
            countryName = "United States",
            deliveryEnabled = true,
            defaultCurrency = "USD",
            defaultTimezone = "America/New_York",
            supportedDeliveryProviders = listOf("fedex", "dhl", "internal_fleet"),
            defaultDeliveryProvider = "fedex",
            fallbackDeliveryProvider = "internal_fleet",
            internationalDeliveryEnabled = false,
            proofOfDeliveryRequired = true,
            cashOnDeliveryEnabled = false
        )
        _countryConfigs.value = mapOf("SA" to saConfig, "AE" to aeConfig, "US" to usConfig)

        // Zones
        val saZoneRiyadh = DeliveryZone(
            zoneId = "zone_sa_riyadh",
            countryCode = "SA",
            zoneName = "Riyadh Metropolitan",
            zoneCode = "RUH-METRO",
            city = "Riyadh",
            region = "Riyadh Province",
            postalCodes = listOf("11564", "12211", "12331", "13321"),
            standardDeliveryDays = 1,
            expressDeliveryAvailable = true,
            sameDayAvailable = true
        )
        val saZoneJeddah = DeliveryZone(
            zoneId = "zone_sa_jeddah",
            countryCode = "SA",
            zoneName = "Jeddah Coastal Hub",
            zoneCode = "JED-HUB",
            city = "Jeddah",
            region = "Makkah Province",
            postalCodes = listOf("21442", "23341"),
            standardDeliveryDays = 2,
            expressDeliveryAvailable = true,
            sameDayAvailable = true
        )
        val saZoneDammam = DeliveryZone(
            zoneId = "zone_sa_dammam",
            countryCode = "SA",
            zoneName = "Eastern Province Dammam",
            zoneCode = "DMM-HUB",
            city = "Dammam",
            region = "Eastern Province",
            postalCodes = listOf("31411"),
            standardDeliveryDays = 2,
            expressDeliveryAvailable = true,
            sameDayAvailable = false
        )
        val aeZoneDubai = DeliveryZone(
            zoneId = "zone_ae_dubai",
            countryCode = "AE",
            zoneName = "Dubai Central",
            zoneCode = "DXB-METRO",
            city = "Dubai",
            region = "Dubai",
            postalCodes = listOf("00000"),
            standardDeliveryDays = 1,
            expressDeliveryAvailable = true,
            sameDayAvailable = true
        )
        _deliveryZones.value = mapOf(
            saZoneRiyadh.zoneId to saZoneRiyadh,
            saZoneJeddah.zoneId to saZoneJeddah,
            saZoneDammam.zoneId to saZoneDammam,
            aeZoneDubai.zoneId to aeZoneDubai
        )

        // Shipping Rate Configs
        _shippingRates.value = listOf(
            ShippingRateConfig(
                countryCode = "SA",
                zoneId = saZoneRiyadh.zoneId,
                deliveryProvider = "internal_fleet",
                deliveryMode = DeliveryMode.PLATFORM_MANAGED,
                serviceType = DeliveryServiceType.SAME_DAY,
                currencyCode = "SAR",
                baseFeeMinor = 3000L, // SAR 30.00
                minimumFeeMinor = 2500L,
                maximumFeeMinor = 6000L,
                estimatedMinDays = 0,
                estimatedMaxDays = 1
            ),
            ShippingRateConfig(
                countryCode = "SA",
                zoneId = saZoneRiyadh.zoneId,
                deliveryProvider = "aramex",
                deliveryMode = DeliveryMode.THIRD_PARTY,
                serviceType = DeliveryServiceType.STANDARD,
                currencyCode = "SAR",
                baseFeeMinor = 1500L, // SAR 15.00
                minimumFeeMinor = 1200L,
                maximumFeeMinor = 4500L,
                estimatedMinDays = 1,
                estimatedMaxDays = 3
            ),
            ShippingRateConfig(
                countryCode = "SA",
                zoneId = saZoneRiyadh.zoneId,
                deliveryProvider = "aramex",
                deliveryMode = DeliveryMode.THIRD_PARTY,
                serviceType = DeliveryServiceType.EXPRESS,
                currencyCode = "SAR",
                baseFeeMinor = 2500L, // SAR 25.00
                minimumFeeMinor = 2000L,
                maximumFeeMinor = 5000L,
                estimatedMinDays = 1,
                estimatedMaxDays = 2
            ),
            ShippingRateConfig(
                countryCode = "SA",
                zoneId = saZoneRiyadh.zoneId,
                deliveryProvider = "seller_managed",
                deliveryMode = DeliveryMode.CUSTOMER_PICKUP,
                serviceType = DeliveryServiceType.PICKUP,
                currencyCode = "SAR",
                baseFeeMinor = 0L, // Free pickup
                minimumFeeMinor = 0L,
                maximumFeeMinor = 0L,
                estimatedMinDays = 0,
                estimatedMaxDays = 1
            ),
            ShippingRateConfig(
                countryCode = "AE",
                zoneId = aeZoneDubai.zoneId,
                deliveryProvider = "aramex",
                deliveryMode = DeliveryMode.THIRD_PARTY,
                serviceType = DeliveryServiceType.STANDARD,
                currencyCode = "AED",
                baseFeeMinor = 2000L,
                minimumFeeMinor = 1500L,
                maximumFeeMinor = 5000L,
                estimatedMinDays = 1,
                estimatedMaxDays = 2
            )
        )

        // Partners & Drivers
        val aramexPartner = DeliveryPartnerRecord(
            partnerId = "aramex",
            partnerName = "Aramex Logistics",
            countryCode = "SA",
            serviceRegions = listOf("Riyadh", "Jeddah", "Dammam", "Dubai"),
            supportedServices = listOf(DeliveryServiceType.STANDARD, DeliveryServiceType.EXPRESS)
        )
        val smsaPartner = DeliveryPartnerRecord(
            partnerId = "smsa",
            partnerName = "SMSA Express",
            countryCode = "SA",
            serviceRegions = listOf("All Saudi Provinces"),
            supportedServices = listOf(DeliveryServiceType.STANDARD, DeliveryServiceType.EXPRESS)
        )
        val internalFleet = DeliveryPartnerRecord(
            partnerId = "internal_fleet",
            partnerName = "Healthogram Express Fleet",
            countryCode = "SA",
            serviceRegions = listOf("Riyadh", "Dubai"),
            supportedServices = listOf(DeliveryServiceType.SAME_DAY, DeliveryServiceType.EXPRESS, DeliveryServiceType.SCHEDULED)
        )
        _partners.value = mapOf(
            aramexPartner.partnerId to aramexPartner,
            smsaPartner.partnerId to smsaPartner,
            internalFleet.partnerId to internalFleet
        )

        val driver1 = DeliveryDriver(
            driverId = "drv_fahad_01",
            partnerId = "internal_fleet",
            displayName = "Fahad Al-Otaibi",
            status = "AVAILABLE",
            serviceZone = "RUH-METRO",
            vehicleType = "REFRIGERATED_VAN",
            currentAssignmentCount = 1
        )
        val driver2 = DeliveryDriver(
            driverId = "drv_tariq_02",
            partnerId = "internal_fleet",
            displayName = "Tariq Mansoor",
            status = "EN_ROUTE",
            serviceZone = "RUH-METRO",
            vehicleType = "MOTORCYCLE",
            currentAssignmentCount = 2
        )
        _drivers.value = mapOf(driver1.driverId to driver1, driver2.driverId to driver2)

        // Restrictions
        _restrictions.value = listOf(
            ShippingRestriction(
                countryCode = "SA",
                productCategory = "CONTROLLED_PHARMACEUTICALS",
                productType = "Narcotic Analgesic",
                prohibited = true,
                restricted = true
            ),
            ShippingRestriction(
                countryCode = "SA",
                productCategory = "COLD_CHAIN_BIOLOGICS",
                productType = "Insulin & Vaccines",
                prohibited = false,
                restricted = true,
                requiresDocumentation = true,
                requiresTemperatureControl = true,
                requiresSpecialCarrier = true
            )
        )

        // Customer addresses
        val defaultCustomerAddress = CustomerDeliveryAddress(
            addressId = "addr_cust_01",
            customerUid = "usr_patient_01",
            countryCode = "SA",
            fullName = "Dr. Sarah Al-Ahmad",
            phoneReference = "+966 50 123 4567",
            addressLine1 = "King Fahd Road, Al Olaya District",
            addressLine2 = "Tower B, Apartment 402",
            city = "Riyadh",
            region = "Riyadh Province",
            postalCode = "12211",
            district = "Al Olaya",
            latitudeApprox = 24.6980,
            longitudeApprox = 46.6850,
            deliveryInstructions = "Please ring doorbell and confirm contactless OTP.",
            isDefault = true,
            validated = true
        )
        _customerAddresses.value = mapOf("usr_patient_01" to listOf(defaultCustomerAddress))

        // Pre-seed sample active order and shipment
        val sampleOrderId = "ord_hgm_88219"
        val sampleFulfillmentId = "ful_001"
        val sampleShipmentId = "sh_991823"
        val sampleTrackingNo = "HG-FLT-88219001"

        val sampleFulfillment = OrderFulfillment(
            fulfillmentId = sampleFulfillmentId,
            orderId = sampleOrderId,
            suborderId = "subord_001",
            sellerUid = "seller_alnoor_pharmacy",
            customerUid = "usr_patient_01",
            fulfillmentType = DeliveryMode.PLATFORM_MANAGED,
            fulfillmentStatus = FulfillmentStatus.OUT_FOR_DELIVERY,
            pickupAddressReference = "Al-Noor Central Pharmacy, Riyadh",
            deliveryAddressReference = "King Fahd Rd, Al Olaya, Riyadh",
            packageCount = 1,
            totalWeightGrams = 450,
            packageDimensions = "20x15x10 cm",
            deliveryProvider = "internal_fleet",
            shipmentId = sampleShipmentId,
            expectedPickupAt = now - 7200000L,
            expectedDeliveryAt = now + 3600000L,
            actualPickupAt = now - 5400000L
        )
        _fulfillments.value = mapOf(sampleFulfillmentId to sampleFulfillment)

        val sampleShipment = Shipment(
            shipmentId = sampleShipmentId,
            orderId = sampleOrderId,
            suborderId = "subord_001",
            fulfillmentId = sampleFulfillmentId,
            sellerUid = "seller_alnoor_pharmacy",
            customerUid = "usr_patient_01",
            countryCode = "SA",
            originZoneId = saZoneRiyadh.zoneId,
            destinationZoneId = saZoneRiyadh.zoneId,
            deliveryProvider = "internal_fleet",
            providerShipmentId = "flt_88219",
            trackingNumber = sampleTrackingNo,
            serviceType = DeliveryServiceType.SAME_DAY,
            deliveryMode = DeliveryMode.PLATFORM_MANAGED,
            packageCount = 1,
            weightGrams = 450,
            dimensions = "20x15x10 cm",
            shippingFeeMinor = 3000L,
            currencyCode = "SAR",
            status = ShipmentStatus.OUT_FOR_DELIVERY,
            estimatedPickupAt = now - 7200000L,
            estimatedDeliveryAt = now + 3600000L,
            actualPickupAt = now - 5400000L
        )
        _shipments.value = mapOf(sampleShipmentId to sampleShipment)

        val samplePkg = Package(
            packageId = "pkg_001",
            shipmentId = sampleShipmentId,
            orderId = sampleOrderId,
            packageNumber = 1,
            weightGrams = 450,
            lengthCm = 20.0,
            widthCm = 15.0,
            heightCm = 10.0,
            barcode = "PKG-88219001",
            trackingNumber = sampleTrackingNo,
            contentsSummary = "Personal Care & Clinical Supplements", // Generic safe label
            fragile = false,
            temperatureSensitive = true,
            specialHandling = "Keep below 25°C",
            status = "SEALED"
        )
        _packages.value = mapOf(samplePkg.packageId to samplePkg)

        val sampleEvents = listOf(
            ShipmentTrackingEvent(
                shipmentId = sampleShipmentId,
                trackingNumber = sampleTrackingNo,
                provider = "internal_fleet",
                status = "ORDER_CONFIRMED",
                statusCode = "CONF",
                locationText = "Healthogram Cloud Gateway",
                eventTime = now - 14400000L,
                description = "Order confirmed and fulfillment route determined."
            ),
            ShipmentTrackingEvent(
                shipmentId = sampleShipmentId,
                trackingNumber = sampleTrackingNo,
                provider = "internal_fleet",
                status = "PACKED",
                statusCode = "PCK",
                locationText = "Al-Noor Pharmacy Hub",
                eventTime = now - 7200000L,
                description = "Seller packed items with thermal insulation seal."
            ),
            ShipmentTrackingEvent(
                shipmentId = sampleShipmentId,
                trackingNumber = sampleTrackingNo,
                provider = "internal_fleet",
                status = "PICKED_UP",
                statusCode = "PU",
                locationText = "Al-Noor Pharmacy Hub",
                eventTime = now - 5400000L,
                description = "Picked up by Healthogram courier driver Fahad Al-Otaibi."
            ),
            ShipmentTrackingEvent(
                shipmentId = sampleShipmentId,
                trackingNumber = sampleTrackingNo,
                provider = "internal_fleet",
                status = "OUT_FOR_DELIVERY",
                statusCode = "OFD",
                locationText = "Al Olaya District Route",
                eventTime = now - 1800000L,
                description = "Rider is en route. Contactless OTP verification required at door."
            )
        )
        _trackingEvents.value = mapOf(sampleShipmentId to sampleEvents)

        // OTP for sample shipment (raw 482915, hashed)
        val sampleOtp = DeliveryOtp(
            shipmentId = sampleShipmentId,
            orderId = sampleOrderId,
            hashedOtp = "482915", // for testing verification
            expiresAt = now + 86400000L
        )
        _deliveryOtps.value = mapOf(sampleShipmentId to sampleOtp)

        // Delivery ledger entries
        val ledgerEntry1 = DeliveryFinancialLedgerEntry(
            shipmentId = sampleShipmentId,
            orderId = sampleOrderId,
            sellerUid = "seller_alnoor_pharmacy",
            provider = "internal_fleet",
            countryCode = "SA",
            currencyCode = "SAR",
            entryType = DeliveryLedgerEntryType.SHIPPING_CHARGE,
            amountMinor = 3000L,
            referenceId = "chg_cust_88219"
        )
        val ledgerEntry2 = DeliveryFinancialLedgerEntry(
            shipmentId = sampleShipmentId,
            orderId = sampleOrderId,
            sellerUid = "seller_alnoor_pharmacy",
            provider = "internal_fleet",
            countryCode = "SA",
            currencyCode = "SAR",
            entryType = DeliveryLedgerEntryType.DELIVERY_PROVIDER_COST,
            amountMinor = 2200L,
            referenceId = "cst_flt_88219"
        )
        val ledgerEntry3 = DeliveryFinancialLedgerEntry(
            shipmentId = sampleShipmentId,
            orderId = sampleOrderId,
            sellerUid = "seller_alnoor_pharmacy",
            provider = "internal_fleet",
            countryCode = "SA",
            currencyCode = "SAR",
            entryType = DeliveryLedgerEntryType.PLATFORM_DELIVERY_REVENUE,
            amountMinor = 800L, // 3000 - 2200 = 800 (SAR 8.00 net margin)
            referenceId = "rev_plf_88219"
        )
        _deliveryLedger.value = listOf(ledgerEntry1, ledgerEntry2, ledgerEntry3)

        // Delivery Slots
        _deliverySlots.value = listOf(
            DeliverySlot(
                countryCode = "SA",
                zoneId = saZoneRiyadh.zoneId,
                provider = "internal_fleet",
                date = "Tomorrow",
                startTime = "09:00",
                endTime = "12:00",
                capacity = 40,
                reservedCapacity = 14,
                availableCapacity = 26
            ),
            DeliverySlot(
                countryCode = "SA",
                zoneId = saZoneRiyadh.zoneId,
                provider = "internal_fleet",
                date = "Tomorrow",
                startTime = "13:00",
                endTime = "17:00",
                capacity = 50,
                reservedCapacity = 22,
                availableCapacity = 28
            ),
            DeliverySlot(
                countryCode = "SA",
                zoneId = saZoneRiyadh.zoneId,
                provider = "internal_fleet",
                date = "Tomorrow",
                startTime = "18:00",
                endTime = "21:00",
                capacity = 30,
                reservedCapacity = 29,
                availableCapacity = 1
            )
        )
    }

    // ==========================================
    // MUTATION METHODS
    // ==========================================

    fun updateCountryConfig(config: CountryDeliveryConfig) {
        _countryConfigs.update { it + (config.countryCode to config) }
    }

    fun addOrUpdateZone(zone: DeliveryZone) {
        _deliveryZones.update { it + (zone.zoneId to zone) }
    }

    fun addOrUpdateRate(rate: ShippingRateConfig) {
        _shippingRates.update { current ->
            val index = current.indexOfFirst { it.rateId == rate.rateId }
            if (index != -1) current.toMutableList().apply { set(index, rate) } else current + rate
        }
    }

    fun saveShippingQuote(quote: ShippingQuote) {
        _shippingQuotes.update { it + (quote.quoteId to quote) }
    }

    fun saveFulfillment(fulfillment: OrderFulfillment) {
        _fulfillments.update { it + (fulfillment.fulfillmentId to fulfillment) }
    }

    fun saveShipment(shipment: Shipment) {
        _shipments.update { it + (shipment.shipmentId to shipment) }
    }

    fun savePackage(pkg: Package) {
        _packages.update { it + (pkg.packageId to pkg) }
    }

    fun appendTrackingEvent(event: ShipmentTrackingEvent) {
        _trackingEvents.update { map ->
            val existing = map[event.shipmentId] ?: emptyList()
            map + (event.shipmentId to (existing + event))
        }
    }

    fun saveProofOfDelivery(pod: ProofOfDelivery) {
        _proofsOfDelivery.update { it + (pod.shipmentId to pod) }
    }

    fun saveDeliveryOtp(otp: DeliveryOtp) {
        _deliveryOtps.update { it + (otp.shipmentId to otp) }
    }

    fun recordFailure(failure: DeliveryFailure) {
        _failures.update { it + (failure.failureId to failure) }
    }

    fun saveReschedule(reschedule: DeliveryReschedule) {
        _reschedules.update { it + (reschedule.rescheduleId to reschedule) }
    }

    fun saveReturnToSeller(rts: ReturnToSeller) {
        _returnToSellerRecords.update { it + (rts.rtsId to rts) }
    }

    fun saveCustomerReturn(ret: CustomerReturn) {
        _customerReturns.update { it + (ret.returnId to ret) }
    }

    fun saveDispute(dispute: DeliveryDispute) {
        _disputes.update { it + (dispute.disputeId to dispute) }
    }

    fun appendLedgerEntry(entry: DeliveryFinancialLedgerEntry) {
        _deliveryLedger.update { it + entry }
    }

    fun saveCashCollection(record: CashCollectionRecord) {
        _codRecords.update { it + (record.recordId to record) }
    }

    fun updateFeatureFlags(flags: DeliveryFeatureFlags) {
        _featureFlags.value = flags
    }

    fun addAuditLog(log: DeliveryAuditLog) {
        _auditLogs.update { it + log }
    }

    fun saveCustomerAddress(customerUid: String, address: CustomerDeliveryAddress) {
        _customerAddresses.update { map ->
            val list = map[customerUid] ?: emptyList()
            val updated = if (address.isDefault) {
                list.map { it.copy(isDefault = false) } + address
            } else {
                list + address
            }
            map + (customerUid to updated)
        }
    }

    fun reserveDeliverySlot(slotId: String): Boolean {
        var reserved = false
        _deliverySlots.update { list ->
            list.map { slot ->
                if (slot.slotId == slotId && slot.availableCapacity > 0) {
                    reserved = true
                    slot.copy(
                        reservedCapacity = slot.reservedCapacity + 1,
                        availableCapacity = slot.availableCapacity - 1
                    )
                } else slot
            }
        }
        return reserved
    }

    fun releaseDeliverySlot(slotId: String) {
        _deliverySlots.update { list ->
            list.map { slot ->
                if (slot.slotId == slotId && slot.reservedCapacity > 0) {
                    slot.copy(
                        reservedCapacity = slot.reservedCapacity - 1,
                        availableCapacity = slot.availableCapacity + 1
                    )
                } else slot
            }
        }
    }
}
