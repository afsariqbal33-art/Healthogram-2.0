package com.example.healthogram.delivery

import com.example.healthogram.delivery.adapters.DeliveryProviderAdapter
import com.example.healthogram.delivery.adapters.InternalDeliveryAdapter
import com.example.healthogram.delivery.adapters.SellerDeliveryAdapter
import com.example.healthogram.delivery.adapters.ThirdPartyDeliveryAdapter
import java.security.MessageDigest
import java.util.UUID

/**
 * Healthogram Delivery Engine.
 * Authoritative Server-side orchestration for:
 * - Country & Zone Resolution
 * - Shipping Rate Calculation & Quotes
 * - Restriction & Health-safety checks
 * - Shipment & Package Creation
 * - Carrier Abstraction & Tracking
 * - Proof of Delivery & OTP Verification
 * - Returns, RTS, Disputes & Ledgers
 */
class DeliveryEngine(
    private val repository: DeliveryRepository = DeliveryRepository.getInstance()
) {

    // Registered provider adapters
    private val adapters: Map<String, DeliveryProviderAdapter> = mapOf(
        "internal_fleet" to InternalDeliveryAdapter(),
        "aramex" to ThirdPartyDeliveryAdapter("aramex", "Aramex Logistics", "ARX"),
        "smsa" to ThirdPartyDeliveryAdapter("smsa", "SMSA Express", "SMSA"),
        "dhl" to ThirdPartyDeliveryAdapter("dhl", "DHL Express", "DHL"),
        "fedex" to ThirdPartyDeliveryAdapter("fedex", "FedEx Express", "FDX"),
        "seller_managed" to SellerDeliveryAdapter()
    )

    fun getAdapter(providerId: String): DeliveryProviderAdapter {
        return adapters[providerId] ?: adapters["internal_fleet"] ?: InternalDeliveryAdapter()
    }

    // ==========================================
    // 1. COUNTRY & ZONE RESOLUTION
    // ==========================================

    fun resolveCountry(countryCode: String): CountryDeliveryConfig {
        val configs = repository.countryConfigs.value
        val config = configs[countryCode.uppercase()]
            ?: configs["SA"]
            ?: throw IllegalStateException("Country delivery configuration missing for $countryCode")

        if (!config.deliveryEnabled) {
            throw IllegalStateException("Delivery is currently disabled for country ${config.countryName}")
        }
        return config
    }

    fun resolveZone(countryCode: String, city: String, postalCode: String): DeliveryZone? {
        val zones = repository.deliveryZones.value.values.filter {
            it.countryCode.equals(countryCode, ignoreCase = true) && it.active
        }
        // 1st: match postal code
        val postalMatch = zones.firstOrNull { it.postalCodes.contains(postalCode.trim()) }
        if (postalMatch != null) return postalMatch

        // 2nd: match city
        val cityMatch = zones.firstOrNull { it.city.equals(city.trim(), ignoreCase = true) }
        if (cityMatch != null) return cityMatch

        // 3rd: fallback to first active zone in country
        return zones.firstOrNull()
    }

    // ==========================================
    // 2. ADDRESS VALIDATION
    // ==========================================

    fun validateDeliveryAddress(address: CustomerDeliveryAddress): AddressValidationResult {
        val errors = mutableListOf<String>()
        if (address.fullName.isBlank()) errors.add("Recipient name is required")
        if (address.phoneReference.isBlank()) errors.add("Contact phone is required for delivery coordination")
        if (address.addressLine1.isBlank()) errors.add("Street address line 1 is required")
        if (address.city.isBlank()) errors.add("City is required")

        val countryConfig = repository.countryConfigs.value[address.countryCode.uppercase()]
        if (countryConfig == null || !countryConfig.deliveryEnabled) {
            errors.add("Delivery is not enabled for destination country ${address.countryCode}")
        }

        val zone = resolveZone(address.countryCode, address.city, address.postalCode)
        if (zone == null) {
            errors.add("Address is outside serviceable delivery zones")
        }

        val isValid = errors.isEmpty()
        return AddressValidationResult(
            isValid = isValid,
            countryMatch = countryConfig != null,
            zoneId = zone?.zoneId,
            isServiceable = isValid && zone != null,
            errors = errors,
            standardizedAddress = if (isValid) address.copy(validated = true) else null
        )
    }

    // ==========================================
    // 3. PRODUCT RESTRICTION CHECKS
    // ==========================================

    fun checkProductRestrictions(
        countryCode: String,
        products: List<SuborderProductItem>
    ): List<String> {
        val restrictions = repository.restrictions.value.filter {
            it.countryCode.equals(countryCode, ignoreCase = true) && it.active
        }
        val warnings = mutableListOf<String>()

        for (product in products) {
            val matching = restrictions.firstOrNull {
                it.productCategory.equals(product.productName, ignoreCase = true) ||
                        (it.prohibited && product.productName.contains("Narcotic", ignoreCase = true))
            }
            if (matching != null && matching.prohibited) {
                warnings.add("Item '${product.productName}' is legally prohibited from shipping in $countryCode.")
            }
        }
        return warnings
    }

    // ==========================================
    // 4. SHIPPING RATE ENGINE & QUOTES
    // ==========================================

    fun calculateShippingRate(
        customerAddress: CustomerDeliveryAddress,
        sellerAddress: CustomerDeliveryAddress,
        products: List<SuborderProductItem>,
        deliveryMode: DeliveryMode,
        serviceType: DeliveryServiceType,
        sellerUid: String = "seller_default"
    ): ShippingQuote {
        val flags = repository.featureFlags.value
        if (flags.emergencyDeliveryStop) {
            throw IllegalStateException("Delivery system is temporarily paused under emergency control.")
        }

        // Enforce Local/Country-wise constraint (Section 5)
        if (!customerAddress.countryCode.equals(sellerAddress.countryCode, ignoreCase = true)) {
            if (!flags.internationalDeliveryEnabled) {
                throw IllegalStateException("Cross-border delivery is not enabled. Marketplace is currently country-wise local only.")
            }
        }

        val country = resolveCountry(customerAddress.countryCode)
        val zone = resolveZone(customerAddress.countryCode, customerAddress.city, customerAddress.postalCode)
            ?: throw IllegalStateException("Destination address is not within an active delivery zone.")

        // Check product restrictions
        val restrictionErrors = checkProductRestrictions(customerAddress.countryCode, products)
        if (restrictionErrors.isNotEmpty()) {
            throw IllegalArgumentException(restrictionErrors.joinToString("; "))
        }

        // Total weight and subtotal
        val totalWeight = products.sumOf { it.weightGrams * it.quantity }
        val subtotalMinor = products.sumOf { it.unitPriceMinor * it.quantity }

        // Find matching rate config
        val rates = repository.shippingRates.value
        val matchedRate = rates.firstOrNull {
            it.countryCode.equals(country.countryCode, ignoreCase = true) &&
                    (it.zoneId == zone.zoneId || it.zoneId.isEmpty()) &&
                    it.serviceType == serviceType &&
                    it.deliveryMode == deliveryMode &&
                    it.active
        }

        val baseFeeMinor = matchedRate?.baseFeeMinor ?: when (serviceType) {
            DeliveryServiceType.SAME_DAY -> 3500L
            DeliveryServiceType.EXPRESS -> 2500L
            DeliveryServiceType.PICKUP -> 0L
            else -> 1500L
        }

        // Free shipping rule
        val finalFeeMinor = if (matchedRate != null && subtotalMinor >= matchedRate.freeShippingThresholdMinor && matchedRate.freeShippingThresholdMinor > 0) {
            0L
        } else {
            val perKgRate = matchedRate?.perKgFeeMinor ?: 200L
            val extraKg = if (totalWeight > 1000) (totalWeight - 1000) / 1000 else 0
            baseFeeMinor + (extraKg * perKgRate)
        }

        // Tax (e.g. 15% VAT in SA)
        val taxMinor = if (country.countryCode == "SA") (finalFeeMinor * 15) / 100 else 0L
        val totalShippingMinor = finalFeeMinor + taxMinor

        val now = System.currentTimeMillis()
        val estimatedPickup = now + 3600000L
        val estimatedDelivery = when (serviceType) {
            DeliveryServiceType.SAME_DAY -> now + (6 * 3600000L)
            DeliveryServiceType.EXPRESS -> now + (24 * 3600000L)
            DeliveryServiceType.PICKUP -> now + (2 * 3600000L)
            else -> now + (48 * 3600000L)
        }

        val resolvedProvider = when (deliveryMode) {
            DeliveryMode.PLATFORM_MANAGED -> "internal_fleet"
            DeliveryMode.CUSTOMER_PICKUP, DeliveryMode.SELLER_MANAGED -> "seller_managed"
            else -> country.defaultDeliveryProvider
        }

        val quote = ShippingQuote(
            customerUid = customerAddress.customerUid,
            orderId = "ord_quote_${UUID.randomUUID().toString().take(6)}",
            sellerUid = sellerUid,
            countryCode = country.countryCode,
            currencyCode = country.defaultCurrency,
            deliveryMode = deliveryMode,
            serviceType = serviceType,
            provider = resolvedProvider,
            shippingFeeMinor = finalFeeMinor,
            taxMinor = taxMinor,
            totalShippingMinor = totalShippingMinor,
            estimatedPickupAt = estimatedPickup,
            estimatedDeliveryAt = estimatedDelivery
        )

        repository.saveShippingQuote(quote)
        return quote
    }

    // ==========================================
    // 5. SHIPMENT CREATION
    // ==========================================

    fun createShipment(
        orderId: String,
        suborderId: String,
        fulfillmentId: String,
        sellerUid: String,
        customerUid: String,
        originAddress: CustomerDeliveryAddress,
        destinationAddress: CustomerDeliveryAddress,
        products: List<SuborderProductItem>,
        deliveryMode: DeliveryMode,
        serviceType: DeliveryServiceType,
        chosenProvider: String? = null
    ): Shipment {
        val flags = repository.featureFlags.value
        if (flags.emergencyShipmentCreationStop) {
            throw IllegalStateException("Shipment creation is temporarily halted by platform emergency controls.")
        }

        val country = resolveCountry(destinationAddress.countryCode)
        val providerId = chosenProvider ?: when (deliveryMode) {
            DeliveryMode.PLATFORM_MANAGED -> "internal_fleet"
            DeliveryMode.CUSTOMER_PICKUP, DeliveryMode.SELLER_MANAGED -> "seller_managed"
            else -> country.defaultDeliveryProvider
        }
        val adapter = getAdapter(providerId)

        val totalWeight = products.sumOf { it.weightGrams * it.quantity }
        val originZone = resolveZone(originAddress.countryCode, originAddress.city, originAddress.postalCode)?.zoneId ?: "zone_origin"
        val destZone = resolveZone(destinationAddress.countryCode, destinationAddress.city, destinationAddress.postalCode)?.zoneId ?: "zone_dest"

        val shipmentId = "sh_${UUID.randomUUID().toString().take(8)}"
        val trackingNo = "HG-${destinationAddress.countryCode}-${System.currentTimeMillis().toString().takeLast(8)}"

        val now = System.currentTimeMillis()
        val estimatedPickup = now + 3600000L
        val estimatedDelivery = when (serviceType) {
            DeliveryServiceType.SAME_DAY -> now + (6 * 3600000L)
            DeliveryServiceType.EXPRESS -> now + (24 * 3600000L)
            else -> now + (48 * 3600000L)
        }

        val shipment = Shipment(
            shipmentId = shipmentId,
            orderId = orderId,
            suborderId = suborderId,
            fulfillmentId = fulfillmentId,
            sellerUid = sellerUid,
            customerUid = customerUid,
            countryCode = destinationAddress.countryCode,
            originZoneId = originZone,
            destinationZoneId = destZone,
            deliveryProvider = providerId,
            providerShipmentId = "pv_${UUID.randomUUID().toString().take(8)}",
            trackingNumber = trackingNo,
            serviceType = serviceType,
            deliveryMode = deliveryMode,
            packageCount = 1,
            weightGrams = totalWeight,
            dimensions = "20x15x10 cm",
            shippingFeeMinor = 2500L,
            currencyCode = country.defaultCurrency,
            status = ShipmentStatus.CREATED,
            estimatedPickupAt = estimatedPickup,
            estimatedDeliveryAt = estimatedDelivery
        )

        val pkg = Package(
            shipmentId = shipmentId,
            orderId = orderId,
            packageNumber = 1,
            weightGrams = totalWeight,
            barcode = "PKG-${trackingNo.takeLast(8)}",
            trackingNumber = trackingNo,
            contentsSummary = "Healthcare and Wellness Essentials", // Privacy protected
            temperatureSensitive = products.any { it.temperatureSensitive },
            fragile = products.any { it.fragile }
        )

        // Delegate to adapter
        val creationResult = adapter.createShipment(shipment, listOf(pkg), originAddress, destinationAddress)

        val finalizedShipment = shipment.copy(
            trackingNumber = creationResult.trackingNumber,
            providerShipmentId = creationResult.providerShipmentId,
            estimatedPickupAt = creationResult.estimatedPickupAt,
            estimatedDeliveryAt = creationResult.estimatedDeliveryAt
        )

        // Persist
        repository.saveShipment(finalizedShipment)
        repository.savePackage(pkg.copy(trackingNumber = creationResult.trackingNumber))

        // Initial tracking event
        val initialEvent = ShipmentTrackingEvent(
            shipmentId = shipmentId,
            trackingNumber = finalizedShipment.trackingNumber,
            provider = providerId,
            status = "SHIPMENT_CREATED",
            statusCode = "CRT",
            locationText = "${destinationAddress.city} Logistics Origin",
            eventTime = now,
            description = "Shipment created with carrier ${adapter.providerName}."
        )
        repository.appendTrackingEvent(initialEvent)

        // Generate and store OTP (for contactless delivery)
        val rawOtp = ((Math.random() * 900000).toInt() + 100000).toString()
        val hashedOtp = sha256(rawOtp)
        val deliveryOtp = DeliveryOtp(
            shipmentId = shipmentId,
            orderId = orderId,
            hashedOtp = rawOtp // stored plain in memory for simulation verification
        )
        repository.saveDeliveryOtp(deliveryOtp)

        // Update fulfillment state
        val existingFulfillment = repository.fulfillments.value[fulfillmentId]
        if (existingFulfillment != null) {
            repository.saveFulfillment(
                existingFulfillment.copy(
                    fulfillmentStatus = FulfillmentStatus.READY_FOR_PICKUP,
                    shipmentId = shipmentId,
                    expectedPickupAt = finalizedShipment.estimatedPickupAt,
                    expectedDeliveryAt = finalizedShipment.estimatedDeliveryAt
                )
            )
        }

        // Ledger entries (Customer shipping charge vs. Provider cost)
        val customerCharge = 2500L
        val providerCost = 1800L
        val platformMargin = customerCharge - providerCost

        repository.appendLedgerEntry(
            DeliveryFinancialLedgerEntry(
                shipmentId = shipmentId,
                orderId = orderId,
                sellerUid = sellerUid,
                provider = providerId,
                countryCode = destinationAddress.countryCode,
                currencyCode = country.defaultCurrency,
                entryType = DeliveryLedgerEntryType.SHIPPING_CHARGE,
                amountMinor = customerCharge,
                referenceId = "chg_$shipmentId"
            )
        )
        repository.appendLedgerEntry(
            DeliveryFinancialLedgerEntry(
                shipmentId = shipmentId,
                orderId = orderId,
                sellerUid = sellerUid,
                provider = providerId,
                countryCode = destinationAddress.countryCode,
                currencyCode = country.defaultCurrency,
                entryType = DeliveryLedgerEntryType.DELIVERY_PROVIDER_COST,
                amountMinor = providerCost,
                referenceId = "cst_$shipmentId"
            )
        )
        repository.appendLedgerEntry(
            DeliveryFinancialLedgerEntry(
                shipmentId = shipmentId,
                orderId = orderId,
                sellerUid = sellerUid,
                provider = providerId,
                countryCode = destinationAddress.countryCode,
                currencyCode = country.defaultCurrency,
                entryType = DeliveryLedgerEntryType.PLATFORM_DELIVERY_REVENUE,
                amountMinor = platformMargin,
                referenceId = "mar_$shipmentId"
            )
        )

        // Audit log
        repository.addAuditLog(
            DeliveryAuditLog(
                actorUid = sellerUid,
                actorRole = "SELLER",
                action = "CREATE_SHIPMENT",
                orderId = orderId,
                shipmentId = shipmentId,
                previousStatus = "CONFIRMED",
                newStatus = "CREATED",
                provider = providerId
            )
        )

        return finalizedShipment
    }

    // ==========================================
    // 6. PROOF OF DELIVERY & OTP VERIFICATION
    // ==========================================

    fun verifyDeliveryOtp(
        shipmentId: String,
        enteredOtp: String,
        recipientName: String,
        deliveryMethod: String = "CONTACTLESS_OTP"
    ): Boolean {
        val storedOtp = repository.deliveryOtps.value[shipmentId]
            ?: throw IllegalStateException("No active delivery OTP found for shipment $shipmentId")

        if (storedOtp.verified) {
            return true
        }

        // Compare entered code
        val matches = storedOtp.hashedOtp == enteredOtp.trim()
        if (!matches) {
            repository.saveDeliveryOtp(storedOtp.copy(attempts = storedOtp.attempts + 1))
            return false
        }

        val now = System.currentTimeMillis()
        repository.saveDeliveryOtp(
            storedOtp.copy(
                verified = true,
                verifiedAt = now
            )
        )

        // Complete delivery
        val shipment = repository.shipments.value[shipmentId]
        if (shipment != null) {
            val deliveredShipment = shipment.copy(
                status = ShipmentStatus.DELIVERED,
                actualDeliveryAt = now,
                updatedAt = now
            )
            repository.saveShipment(deliveredShipment)

            // Update fulfillment
            val fulfillment = repository.fulfillments.value[shipment.fulfillmentId]
            if (fulfillment != null) {
                repository.saveFulfillment(
                    fulfillment.copy(
                        fulfillmentStatus = FulfillmentStatus.DELIVERED,
                        actualDeliveryAt = now,
                        updatedAt = now
                    )
                )
            }

            // Record Proof of Delivery
            val pod = ProofOfDelivery(
                shipmentId = shipmentId,
                orderId = shipment.orderId,
                deliveryMethod = deliveryMethod,
                recipientConfirmationType = "OTP",
                recipientName = recipientName,
                confirmationTimestamp = now,
                otpVerified = true,
                notes = "Contactless OTP handoff verified successfully."
            )
            repository.saveProofOfDelivery(pod)

            // Append tracking event
            repository.appendTrackingEvent(
                ShipmentTrackingEvent(
                    shipmentId = shipmentId,
                    trackingNumber = shipment.trackingNumber,
                    provider = shipment.deliveryProvider,
                    status = "DELIVERED",
                    statusCode = "DLV",
                    locationText = "Customer Doorstep",
                    eventTime = now,
                    description = "Delivered to $recipientName with OTP verification."
                )
            )

            // Audit
            repository.addAuditLog(
                DeliveryAuditLog(
                    actorUid = "carrier_driver",
                    actorRole = "DRIVER",
                    action = "CONFIRM_DELIVERY_OTP",
                    orderId = shipment.orderId,
                    shipmentId = shipmentId,
                    previousStatus = shipment.status.name,
                    newStatus = "DELIVERED",
                    provider = shipment.deliveryProvider
                )
            )
        }
        return true
    }

    // ==========================================
    // 7. DELIVERY FAILURE & RESCHEDULE
    // ==========================================

    fun recordDeliveryFailure(
        shipmentId: String,
        reasonCode: String,
        description: String,
        retryAllowed: Boolean = true
    ): DeliveryFailure {
        val shipment = repository.shipments.value[shipmentId]
            ?: throw IllegalStateException("Shipment $shipmentId not found")

        val now = System.currentTimeMillis()
        val failure = DeliveryFailure(
            shipmentId = shipmentId,
            orderId = shipment.orderId,
            reasonCode = reasonCode,
            description = description,
            attemptedAt = now,
            retryAllowed = retryAllowed,
            nextAttemptAt = if (retryAllowed) now + 86400000L else null
        )
        repository.recordFailure(failure)

        repository.saveShipment(shipment.copy(status = ShipmentStatus.FAILED, updatedAt = now))

        repository.appendTrackingEvent(
            ShipmentTrackingEvent(
                shipmentId = shipmentId,
                trackingNumber = shipment.trackingNumber,
                provider = shipment.deliveryProvider,
                status = "DELIVERY_ATTEMPTED_FAILED",
                statusCode = "FAIL",
                locationText = "Delivery Address",
                eventTime = now,
                description = "Delivery attempt unsuccessful: $description ($reasonCode)"
            )
        )

        return failure
    }

    fun rescheduleDelivery(
        shipmentId: String,
        newDate: String,
        reason: String,
        requestedBy: String = "CUSTOMER"
    ): DeliveryReschedule {
        val shipment = repository.shipments.value[shipmentId]
            ?: throw IllegalStateException("Shipment $shipmentId not found")

        val reschedule = DeliveryReschedule(
            shipmentId = shipmentId,
            oldDate = "Original ETA",
            newDate = newDate,
            reason = reason,
            requestedBy = requestedBy,
            approved = true
        )
        repository.saveReschedule(reschedule)

        repository.appendTrackingEvent(
            ShipmentTrackingEvent(
                shipmentId = shipmentId,
                trackingNumber = shipment.trackingNumber,
                provider = shipment.deliveryProvider,
                status = "RESCHEDULED",
                statusCode = "RESCHED",
                locationText = "Logistics Routing Hub",
                eventTime = System.currentTimeMillis(),
                description = "Delivery window rescheduled to $newDate. Reason: $reason"
            )
        )
        return reschedule
    }

    // ==========================================
    // 8. RETURN-TO-SELLER (RTS) & CUSTOMER RETURNS
    // ==========================================

    fun initiateReturnToSeller(
        shipmentId: String,
        reason: String
    ): ReturnToSeller {
        val shipment = repository.shipments.value[shipmentId]
            ?: throw IllegalStateException("Shipment $shipmentId not found")

        val now = System.currentTimeMillis()
        val rts = ReturnToSeller(
            shipmentId = shipmentId,
            orderId = shipment.orderId,
            sellerUid = shipment.sellerUid,
            reason = reason,
            initiatedAt = now,
            status = "IN_TRANSIT"
        )
        repository.saveReturnToSeller(rts)

        repository.saveShipment(shipment.copy(status = ShipmentStatus.RETURNED_TO_SELLER, updatedAt = now))

        repository.appendTrackingEvent(
            ShipmentTrackingEvent(
                shipmentId = shipmentId,
                trackingNumber = shipment.trackingNumber,
                provider = shipment.deliveryProvider,
                status = "RETURN_TO_SELLER",
                statusCode = "RTS",
                locationText = "Regional Hub",
                eventTime = now,
                description = "Package en route back to seller. Reason: $reason"
            )
        )
        return rts
    }

    fun requestCustomerReturn(
        orderId: String,
        suborderId: String,
        customerUid: String,
        sellerUid: String,
        productIds: List<String>,
        reason: String,
        condition: String,
        evidenceReference: String
    ): CustomerReturn {
        val ret = CustomerReturn(
            orderId = orderId,
            suborderId = suborderId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            productIds = productIds,
            reason = reason,
            condition = condition,
            evidenceReference = evidenceReference,
            status = CustomerReturnStatus.REQUESTED
        )
        repository.saveCustomerReturn(ret)

        repository.addAuditLog(
            DeliveryAuditLog(
                actorUid = customerUid,
                actorRole = "CUSTOMER",
                action = "REQUEST_RETURN",
                orderId = orderId,
                shipmentId = "ret_${ret.returnId.take(6)}",
                previousStatus = "DELIVERED",
                newStatus = "RETURN_REQUESTED",
                provider = "internal_fleet"
            )
        )
        return ret
    }

    fun approveCustomerReturn(returnId: String): CustomerReturn {
        val current = repository.customerReturns.value[returnId]
            ?: throw IllegalStateException("Return $returnId not found")

        val approved = current.copy(
            status = CustomerReturnStatus.APPROVED,
            approvedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.saveCustomerReturn(approved)
        return approved
    }

    // ==========================================
    // 9. DELIVERY DISPUTES
    // ==========================================

    fun fileDeliveryDispute(
        orderId: String,
        shipmentId: String,
        customerUid: String,
        sellerUid: String,
        provider: String,
        disputeType: String,
        description: String,
        evidence: String
    ): DeliveryDispute {
        val dispute = DeliveryDispute(
            orderId = orderId,
            shipmentId = shipmentId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            provider = provider,
            disputeType = disputeType,
            description = description,
            evidence = evidence,
            status = "OPEN"
        )
        repository.saveDispute(dispute)
        return dispute
    }

    // ==========================================
    // 10. DELAY DETECTION & RECONCILIATION
    // ==========================================

    fun detectDelayedShipments(): List<Shipment> {
        val now = System.currentTimeMillis()
        val activeShipments = repository.shipments.value.values.filter {
            it.status != ShipmentStatus.DELIVERED &&
                    it.status != ShipmentStatus.CANCELLED &&
                    it.status != ShipmentStatus.RETURNED_TO_SELLER
        }

        val delayedList = mutableListOf<Shipment>()
        for (sh in activeShipments) {
            if (now > sh.estimatedDeliveryAt && !sh.isDelayed) {
                val marked = sh.copy(
                    isDelayed = true,
                    delayReason = "ETA exceeded carrier delivery window",
                    updatedAt = now
                )
                repository.saveShipment(marked)
                delayedList.add(marked)

                repository.appendTrackingEvent(
                    ShipmentTrackingEvent(
                        shipmentId = sh.shipmentId,
                        trackingNumber = sh.trackingNumber,
                        provider = sh.deliveryProvider,
                        status = "DELIVERY_DELAYED",
                        statusCode = "DLY",
                        locationText = "Carrier Network",
                        eventTime = now,
                        description = "Shipment in transit is experiencing logistics delay."
                    )
                )
            }
        }
        return delayedList
    }

    fun reconcileProvider(providerId: String): DeliveryReconciliationReport {
        val shipments = repository.shipments.value.values.filter { it.deliveryProvider == providerId }
        val matched = shipments.count { it.status == ShipmentStatus.DELIVERED }
        val discrepancies = shipments.filter { it.isDelayed || it.status == ShipmentStatus.FAILED }

        val report = DeliveryReconciliationReport(
            provider = providerId,
            periodStart = System.currentTimeMillis() - (7 * 86400000L),
            periodEnd = System.currentTimeMillis(),
            totalShipmentsChecked = shipments.size,
            matchedCount = matched,
            discrepancyCount = discrepancies.size,
            discrepancyDetails = discrepancies.map { "Shipment ${it.trackingNumber}: status=${it.status}, delayed=${it.isDelayed}" }
        )
        return report
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Step 37 / REQ-017: Customer Multi-Address Delivery Book Management
     */
    fun getCustomerAddressBook(customerUid: String): List<CustomerDeliveryAddress> {
        return repository.customerAddresses.value[customerUid] ?: emptyList()
    }

    fun saveCustomerAddress(customerUid: String, address: CustomerDeliveryAddress): CustomerDeliveryAddress {
        val validated = validateDeliveryAddress(address)
        val finalAddress = if (validated.isValid) address.copy(validated = true) else address
        repository.saveCustomerAddress(customerUid, finalAddress)
        return finalAddress
    }

    companion object {
        @Volatile
        private var instance: DeliveryEngine? = null

        fun getInstance(repository: DeliveryRepository = DeliveryRepository.getInstance()): DeliveryEngine {
            return instance ?: synchronized(this) {
                instance ?: DeliveryEngine(repository).also { instance = it }
            }
        }
    }
}

