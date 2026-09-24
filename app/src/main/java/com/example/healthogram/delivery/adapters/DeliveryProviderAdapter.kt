package com.example.healthogram.delivery.adapters

import com.example.healthogram.delivery.CustomerDeliveryAddress
import com.example.healthogram.delivery.DeliveryEstimate
import com.example.healthogram.delivery.DeliveryMode
import com.example.healthogram.delivery.DeliveryServiceType
import com.example.healthogram.delivery.Package
import com.example.healthogram.delivery.Shipment
import com.example.healthogram.delivery.ShipmentStatus
import com.example.healthogram.delivery.ShipmentTrackingEvent
import java.util.UUID

/**
 * Result data models for Delivery Provider Adapter operations.
 */
data class ProviderServiceabilityResult(
    val isServiceable: Boolean,
    val provider: String,
    val estimatedDaysMin: Int,
    val estimatedDaysMax: Int,
    val supportedServices: List<DeliveryServiceType>,
    val restrictionNotes: String? = null
)

data class ProviderRateCalculationResult(
    val provider: String,
    val serviceType: DeliveryServiceType,
    val currencyCode: String,
    val costMinor: Long,
    val estimatedPickupHours: Int,
    val estimatedDeliveryHours: Int
)

data class ProviderShipmentCreationResult(
    val success: Boolean,
    val provider: String,
    val providerShipmentId: String,
    val trackingNumber: String,
    val labelUrl: String,
    val manifestReference: String,
    val estimatedPickupAt: Long,
    val estimatedDeliveryAt: Long,
    val errorMessage: String? = null
)

data class ProviderLabelResult(
    val trackingNumber: String,
    val labelBase64PdfOrPng: String,
    val barcodeNumber: String,
    val format: String = "PDF"
)

data class ProviderManifestResult(
    val manifestId: String,
    val provider: String,
    val shipmentCount: Int,
    val manifestReferenceUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class ProviderWebhookResult(
    val isValid: Boolean,
    val eventType: String,
    val shipmentId: String?,
    val trackingNumber: String?,
    val newStatus: ShipmentStatus?,
    val locationText: String,
    val description: String
)

/**
 * Core interface for delivery provider abstraction.
 * Allows seamless integration of DHL, Aramex, SMSA, FedEx, and internal fleets.
 */
interface DeliveryProviderAdapter {
    val providerId: String
    val providerName: String

    fun checkServiceability(
        originCountry: String,
        destinationCountry: String,
        originPostalCode: String,
        destinationPostalCode: String
    ): ProviderServiceabilityResult

    fun calculateShippingRate(
        originAddress: CustomerDeliveryAddress,
        destinationAddress: CustomerDeliveryAddress,
        weightGrams: Int,
        dimensions: String,
        serviceType: DeliveryServiceType
    ): ProviderRateCalculationResult

    fun createShipment(
        shipment: Shipment,
        packages: List<Package>,
        originAddress: CustomerDeliveryAddress,
        destinationAddress: CustomerDeliveryAddress
    ): ProviderShipmentCreationResult

    fun cancelShipment(providerShipmentId: String, reason: String): Boolean

    fun assignShipment(providerShipmentId: String, driverId: String?): Boolean

    fun getShipment(providerShipmentId: String): Shipment?

    fun getTracking(trackingNumber: String): List<ShipmentTrackingEvent>

    fun generateLabel(providerShipmentId: String): ProviderLabelResult

    fun generateManifest(shipmentIds: List<String>): ProviderManifestResult

    fun schedulePickup(
        providerShipmentId: String,
        pickupAddress: CustomerDeliveryAddress,
        requestedTimestamp: Long
    ): Boolean

    fun reschedulePickup(
        providerShipmentId: String,
        newTimestamp: Long,
        reason: String
    ): Boolean

    fun trackShipment(trackingNumber: String): ShipmentStatus

    fun handleWebhook(
        payload: String,
        signatureHeader: String,
        signingSecret: String
    ): ProviderWebhookResult

    fun createReturnShipment(
        originalShipmentId: String,
        pickupAddress: CustomerDeliveryAddress,
        sellerAddress: CustomerDeliveryAddress,
        reason: String
    ): ProviderShipmentCreationResult

    fun cancelReturnShipment(returnShipmentId: String): Boolean

    fun getDeliveryEstimate(
        originCountry: String,
        destinationCountry: String,
        serviceType: DeliveryServiceType
    ): DeliveryEstimate
}

// ==========================================
// 1. HEALTHOGRAM INTERNAL FLEET ADAPTER
// ==========================================

class InternalDeliveryAdapter : DeliveryProviderAdapter {
    override val providerId: String = "internal_fleet"
    override val providerName: String = "Healthogram Express Fleet"

    override fun checkServiceability(
        originCountry: String,
        destinationCountry: String,
        originPostalCode: String,
        destinationPostalCode: String
    ): ProviderServiceabilityResult {
        // Enforce country-wise restriction: International is disabled
        val isServiceable = originCountry.equals(destinationCountry, ignoreCase = true)
        return ProviderServiceabilityResult(
            isServiceable = isServiceable,
            provider = providerId,
            estimatedDaysMin = 0,
            estimatedDaysMax = 1,
            supportedServices = listOf(
                DeliveryServiceType.SAME_DAY,
                DeliveryServiceType.EXPRESS,
                DeliveryServiceType.SCHEDULED
            ),
            restrictionNotes = if (!isServiceable) "International delivery is not enabled" else null
        )
    }

    override fun calculateShippingRate(
        originAddress: CustomerDeliveryAddress,
        destinationAddress: CustomerDeliveryAddress,
        weightGrams: Int,
        dimensions: String,
        serviceType: DeliveryServiceType
    ): ProviderRateCalculationResult {
        val baseMinor = when (serviceType) {
            DeliveryServiceType.SAME_DAY -> 3500L // SAR 35.00
            DeliveryServiceType.EXPRESS -> 2500L  // SAR 25.00
            DeliveryServiceType.SCHEDULED -> 2000L// SAR 20.00
            else -> 1500L
        }
        val extraWeightMinor = if (weightGrams > 1000) ((weightGrams - 1000) / 500) * 150L else 0L
        return ProviderRateCalculationResult(
            provider = providerId,
            serviceType = serviceType,
            currencyCode = destinationAddress.countryCode.let { if (it == "AE") "AED" else "SAR" },
            costMinor = baseMinor + extraWeightMinor,
            estimatedPickupHours = 2,
            estimatedDeliveryHours = if (serviceType == DeliveryServiceType.SAME_DAY) 6 else 24
        )
    }

    override fun createShipment(
        shipment: Shipment,
        packages: List<Package>,
        originAddress: CustomerDeliveryAddress,
        destinationAddress: CustomerDeliveryAddress
    ): ProviderShipmentCreationResult {
        val trackingNo = if (shipment.trackingNumber.isNotBlank()) shipment.trackingNumber else "HG-${destinationAddress.countryCode}-${System.currentTimeMillis().toString().takeLast(8)}"
        val providerShipmentId = "flt_${UUID.randomUUID().toString().take(8)}"
        return ProviderShipmentCreationResult(
            success = true,
            provider = providerId,
            providerShipmentId = providerShipmentId,
            trackingNumber = trackingNo,
            labelUrl = "https://healthogram.local/labels/$trackingNo.pdf",
            manifestReference = "MNF-HG-${System.currentTimeMillis().toString().takeLast(6)}",
            estimatedPickupAt = System.currentTimeMillis() + 3600000L,
            estimatedDeliveryAt = System.currentTimeMillis() + 86400000L
        )
    }

    override fun cancelShipment(providerShipmentId: String, reason: String): Boolean = true

    override fun assignShipment(providerShipmentId: String, driverId: String?): Boolean = true

    override fun getShipment(providerShipmentId: String): Shipment? = null

    override fun getTracking(trackingNumber: String): List<ShipmentTrackingEvent> {
        val now = System.currentTimeMillis()
        return listOf(
            ShipmentTrackingEvent(
                shipmentId = "sh_${trackingNumber.takeLast(6)}",
                trackingNumber = trackingNumber,
                provider = providerId,
                status = "PICKED_UP",
                statusCode = "PU",
                locationText = "Central Logistics Hub",
                eventTime = now - 3600000L,
                description = "Picked up by Healthogram courier van"
            ),
            ShipmentTrackingEvent(
                shipmentId = "sh_${trackingNumber.takeLast(6)}",
                trackingNumber = trackingNumber,
                provider = providerId,
                status = "IN_TRANSIT",
                statusCode = "IT",
                locationText = "En route to destination district",
                eventTime = now - 1800000L,
                description = "Dispatched with climate-controlled handling"
            )
        )
    }

    override fun generateLabel(providerShipmentId: String): ProviderLabelResult {
        return ProviderLabelResult(
            trackingNumber = "HG-FLT-${providerShipmentId.takeLast(6)}",
            labelBase64PdfOrPng = "JVBERi0xLjQKJcTl8uXrp...",
            barcodeNumber = "490123456789"
        )
    }

    override fun generateManifest(shipmentIds: List<String>): ProviderManifestResult {
        return ProviderManifestResult(
            manifestId = "MNF-${UUID.randomUUID().toString().take(8)}",
            provider = providerId,
            shipmentCount = shipmentIds.size,
            manifestReferenceUrl = "https://healthogram.local/manifests/fleet_${System.currentTimeMillis()}.pdf"
        )
    }

    override fun schedulePickup(
        providerShipmentId: String,
        pickupAddress: CustomerDeliveryAddress,
        requestedTimestamp: Long
    ): Boolean = true

    override fun reschedulePickup(
        providerShipmentId: String,
        newTimestamp: Long,
        reason: String
    ): Boolean = true

    override fun trackShipment(trackingNumber: String): ShipmentStatus = ShipmentStatus.IN_TRANSIT

    override fun handleWebhook(
        payload: String,
        signatureHeader: String,
        signingSecret: String
    ): ProviderWebhookResult {
        val isValid = signatureHeader.isNotBlank()
        val isDelivered = payload.contains("delivered", ignoreCase = true)
        return ProviderWebhookResult(
            isValid = isValid,
            eventType = if (isDelivered) "shipment.delivered" else "shipment.in_transit",
            shipmentId = null,
            trackingNumber = "HG-FLT-AUTO",
            newStatus = if (isDelivered) ShipmentStatus.DELIVERED else ShipmentStatus.IN_TRANSIT,
            locationText = "Riyadh Hub",
            description = "Healthogram fleet automated status update"
        )
    }

    override fun createReturnShipment(
        originalShipmentId: String,
        pickupAddress: CustomerDeliveryAddress,
        sellerAddress: CustomerDeliveryAddress,
        reason: String
    ): ProviderShipmentCreationResult {
        val rtsTracking = "HG-RTS-${System.currentTimeMillis().toString().takeLast(8)}"
        return ProviderShipmentCreationResult(
            success = true,
            provider = providerId,
            providerShipmentId = "rts_${UUID.randomUUID().toString().take(8)}",
            trackingNumber = rtsTracking,
            labelUrl = "https://healthogram.local/labels/rts_$rtsTracking.pdf",
            manifestReference = "MNF-RTS-${System.currentTimeMillis().toString().takeLast(6)}",
            estimatedPickupAt = System.currentTimeMillis() + 86400000L,
            estimatedDeliveryAt = System.currentTimeMillis() + (86400000L * 2)
        )
    }

    override fun cancelReturnShipment(returnShipmentId: String): Boolean = true

    override fun getDeliveryEstimate(
        originCountry: String,
        destinationCountry: String,
        serviceType: DeliveryServiceType
    ): DeliveryEstimate {
        return DeliveryEstimate(
            orderId = "ord_mock",
            sellerUid = "seller_mock",
            countryCode = originCountry,
            serviceType = serviceType,
            estimatedPickupDate = "Today, within 2 hours",
            estimatedDeliveryDate = if (serviceType == DeliveryServiceType.SAME_DAY) "Today by 8 PM" else "Tomorrow by 2 PM",
            confidence = 0.98
        )
    }
}

// ==========================================
// 2. THIRD PARTY PROVIDER ADAPTER (ARAMEX / SMSA / DHL / FEDEX)
// ==========================================

class ThirdPartyDeliveryAdapter(
    override val providerId: String,
    override val providerName: String,
    private val defaultPrefix: String = "3PL"
) : DeliveryProviderAdapter {

    override fun checkServiceability(
        originCountry: String,
        destinationCountry: String,
        originPostalCode: String,
        destinationPostalCode: String
    ): ProviderServiceabilityResult {
        // Enforce country-wise boundary for launch
        val isServiceable = originCountry.equals(destinationCountry, ignoreCase = true)
        return ProviderServiceabilityResult(
            isServiceable = isServiceable,
            provider = providerId,
            estimatedDaysMin = 1,
            estimatedDaysMax = 3,
            supportedServices = listOf(
                DeliveryServiceType.STANDARD,
                DeliveryServiceType.EXPRESS
            ),
            restrictionNotes = if (!isServiceable) "Cross-border shipping is disabled in country delivery settings." else null
        )
    }

    override fun calculateShippingRate(
        originAddress: CustomerDeliveryAddress,
        destinationAddress: CustomerDeliveryAddress,
        weightGrams: Int,
        dimensions: String,
        serviceType: DeliveryServiceType
    ): ProviderRateCalculationResult {
        val baseFee = when (serviceType) {
            DeliveryServiceType.EXPRESS -> 2800L  // SAR 28.00
            else -> 1800L                        // SAR 18.00
        }
        val perKg = if (weightGrams > 1000) ((weightGrams - 1000) / 1000) * 300L else 0L
        return ProviderRateCalculationResult(
            provider = providerId,
            serviceType = serviceType,
            currencyCode = if (destinationAddress.countryCode == "AE") "AED" else "SAR",
            costMinor = baseFee + perKg,
            estimatedPickupHours = 24,
            estimatedDeliveryHours = if (serviceType == DeliveryServiceType.EXPRESS) 48 else 72
        )
    }

    override fun createShipment(
        shipment: Shipment,
        packages: List<Package>,
        originAddress: CustomerDeliveryAddress,
        destinationAddress: CustomerDeliveryAddress
    ): ProviderShipmentCreationResult {
        val trackingNo = "$defaultPrefix-${System.currentTimeMillis().toString().takeLast(9)}"
        val providerShipmentId = "${providerId}_${UUID.randomUUID().toString().take(8)}"
        return ProviderShipmentCreationResult(
            success = true,
            provider = providerId,
            providerShipmentId = providerShipmentId,
            trackingNumber = trackingNo,
            labelUrl = "https://partner-api.$providerId.com/v1/shipments/$trackingNo/label.pdf",
            manifestReference = "MNF-$defaultPrefix-${System.currentTimeMillis().toString().takeLast(6)}",
            estimatedPickupAt = System.currentTimeMillis() + 86400000L,
            estimatedDeliveryAt = System.currentTimeMillis() + (86400000L * 3)
        )
    }

    override fun cancelShipment(providerShipmentId: String, reason: String): Boolean = true

    override fun assignShipment(providerShipmentId: String, driverId: String?): Boolean = true

    override fun getShipment(providerShipmentId: String): Shipment? = null

    override fun getTracking(trackingNumber: String): List<ShipmentTrackingEvent> {
        val now = System.currentTimeMillis()
        return listOf(
            ShipmentTrackingEvent(
                shipmentId = "sh_${trackingNumber.takeLast(6)}",
                trackingNumber = trackingNumber,
                provider = providerId,
                status = "MANIFEST_CREATED",
                statusCode = "MC",
                locationText = "$providerName Sorting Center",
                eventTime = now - 7200000L,
                description = "Electronic shipping data received"
            ),
            ShipmentTrackingEvent(
                shipmentId = "sh_${trackingNumber.takeLast(6)}",
                trackingNumber = trackingNumber,
                provider = providerId,
                status = "IN_TRANSIT",
                statusCode = "IT",
                locationText = "Regional Sorting Facility",
                eventTime = now - 3600000L,
                description = "Package scanned and containerized"
            )
        )
    }

    override fun generateLabel(providerShipmentId: String): ProviderLabelResult {
        return ProviderLabelResult(
            trackingNumber = "$defaultPrefix-${providerShipmentId.takeLast(8)}",
            labelBase64PdfOrPng = "JVBERi0xLjQKJcTl8uXrp32pl...",
            barcodeNumber = "789123456012"
        )
    }

    override fun generateManifest(shipmentIds: List<String>): ProviderManifestResult {
        return ProviderManifestResult(
            manifestId = "MNF-${UUID.randomUUID().toString().take(8)}",
            provider = providerId,
            shipmentCount = shipmentIds.size,
            manifestReferenceUrl = "https://partner-api.$providerId.com/v1/manifests/${System.currentTimeMillis()}.pdf"
        )
    }

    override fun schedulePickup(
        providerShipmentId: String,
        pickupAddress: CustomerDeliveryAddress,
        requestedTimestamp: Long
    ): Boolean = true

    override fun reschedulePickup(
        providerShipmentId: String,
        newTimestamp: Long,
        reason: String
    ): Boolean = true

    override fun trackShipment(trackingNumber: String): ShipmentStatus = ShipmentStatus.IN_TRANSIT

    override fun handleWebhook(
        payload: String,
        signatureHeader: String,
        signingSecret: String
    ): ProviderWebhookResult {
        val isValid = signatureHeader.isNotBlank() && signingSecret.isNotBlank()
        val isDelivered = payload.contains("delivered", ignoreCase = true)
        val isFailed = payload.contains("failed", ignoreCase = true)
        val status = when {
            isDelivered -> ShipmentStatus.DELIVERED
            isFailed -> ShipmentStatus.FAILED
            else -> ShipmentStatus.IN_TRANSIT
        }
        return ProviderWebhookResult(
            isValid = isValid,
            eventType = if (isDelivered) "delivery.success" else "delivery.transit",
            shipmentId = null,
            trackingNumber = "$defaultPrefix-WH-${payload.hashCode().toString().takeLast(6)}",
            newStatus = status,
            locationText = "Local Delivery Terminal",
            description = "$providerName gateway webhook update"
        )
    }

    override fun createReturnShipment(
        originalShipmentId: String,
        pickupAddress: CustomerDeliveryAddress,
        sellerAddress: CustomerDeliveryAddress,
        reason: String
    ): ProviderShipmentCreationResult {
        val rtsTracking = "$defaultPrefix-RET-${System.currentTimeMillis().toString().takeLast(8)}"
        return ProviderShipmentCreationResult(
            success = true,
            provider = providerId,
            providerShipmentId = "ret_${UUID.randomUUID().toString().take(8)}",
            trackingNumber = rtsTracking,
            labelUrl = "https://partner-api.$providerId.com/v1/returns/$rtsTracking.pdf",
            manifestReference = "MNF-RET-${System.currentTimeMillis().toString().takeLast(6)}",
            estimatedPickupAt = System.currentTimeMillis() + 86400000L,
            estimatedDeliveryAt = System.currentTimeMillis() + (86400000L * 3)
        )
    }

    override fun cancelReturnShipment(returnShipmentId: String): Boolean = true

    override fun getDeliveryEstimate(
        originCountry: String,
        destinationCountry: String,
        serviceType: DeliveryServiceType
    ): DeliveryEstimate {
        return DeliveryEstimate(
            orderId = "ord_mock",
            sellerUid = "seller_mock",
            countryCode = originCountry,
            serviceType = serviceType,
            estimatedPickupDate = "Next business day",
            estimatedDeliveryDate = if (serviceType == DeliveryServiceType.EXPRESS) "1 to 2 business days" else "2 to 4 business days",
            confidence = 0.94
        )
    }
}

// ==========================================
// 3. SELLER DIRECT DELIVERY ADAPTER
// ==========================================

class SellerDeliveryAdapter : DeliveryProviderAdapter {
    override val providerId: String = "seller_managed"
    override val providerName: String = "Seller Direct Delivery / Clinic Pickup"

    override fun checkServiceability(
        originCountry: String,
        destinationCountry: String,
        originPostalCode: String,
        destinationPostalCode: String
    ): ProviderServiceabilityResult {
        val isServiceable = originCountry.equals(destinationCountry, ignoreCase = true)
        return ProviderServiceabilityResult(
            isServiceable = isServiceable,
            provider = providerId,
            estimatedDaysMin = 0,
            estimatedDaysMax = 2,
            supportedServices = listOf(
                DeliveryServiceType.STANDARD,
                DeliveryServiceType.PICKUP,
                DeliveryServiceType.SCHEDULED
            )
        )
    }

    override fun calculateShippingRate(
        originAddress: CustomerDeliveryAddress,
        destinationAddress: CustomerDeliveryAddress,
        weightGrams: Int,
        dimensions: String,
        serviceType: DeliveryServiceType
    ): ProviderRateCalculationResult {
        val cost = if (serviceType == DeliveryServiceType.PICKUP) 0L else 1200L // Free for clinic pickup, SAR 12 for seller direct
        return ProviderRateCalculationResult(
            provider = providerId,
            serviceType = serviceType,
            currencyCode = if (destinationAddress.countryCode == "AE") "AED" else "SAR",
            costMinor = cost,
            estimatedPickupHours = 2,
            estimatedDeliveryHours = 24
        )
    }

    override fun createShipment(
        shipment: Shipment,
        packages: List<Package>,
        originAddress: CustomerDeliveryAddress,
        destinationAddress: CustomerDeliveryAddress
    ): ProviderShipmentCreationResult {
        val tracking = "SLR-${System.currentTimeMillis().toString().takeLast(8)}"
        return ProviderShipmentCreationResult(
            success = true,
            provider = providerId,
            providerShipmentId = "slr_${UUID.randomUUID().toString().take(8)}",
            trackingNumber = tracking,
            labelUrl = "https://healthogram.local/labels/$tracking.pdf",
            manifestReference = "SLR-MNF-${System.currentTimeMillis().toString().takeLast(6)}",
            estimatedPickupAt = System.currentTimeMillis() + 3600000L,
            estimatedDeliveryAt = System.currentTimeMillis() + 86400000L
        )
    }

    override fun cancelShipment(providerShipmentId: String, reason: String): Boolean = true
    override fun assignShipment(providerShipmentId: String, driverId: String?): Boolean = true
    override fun getShipment(providerShipmentId: String): Shipment? = null
    override fun getTracking(trackingNumber: String): List<ShipmentTrackingEvent> = emptyList()
    override fun generateLabel(providerShipmentId: String): ProviderLabelResult = ProviderLabelResult(
        trackingNumber = "SLR-LBL-${providerShipmentId.takeLast(6)}",
        labelBase64PdfOrPng = "SLR_LBL_BASE64",
        barcodeNumber = "555123456789"
    )
    override fun generateManifest(shipmentIds: List<String>): ProviderManifestResult = ProviderManifestResult(
        manifestId = "MNF-SLR",
        provider = providerId,
        shipmentCount = shipmentIds.size,
        manifestReferenceUrl = "https://healthogram.local/manifests/seller.pdf"
    )
    override fun schedulePickup(providerShipmentId: String, pickupAddress: CustomerDeliveryAddress, requestedTimestamp: Long): Boolean = true
    override fun reschedulePickup(providerShipmentId: String, newTimestamp: Long, reason: String): Boolean = true
    override fun trackShipment(trackingNumber: String): ShipmentStatus = ShipmentStatus.OUT_FOR_DELIVERY
    override fun handleWebhook(payload: String, signatureHeader: String, signingSecret: String): ProviderWebhookResult = ProviderWebhookResult(
        isValid = true,
        eventType = "seller.status_update",
        shipmentId = null,
        trackingNumber = null,
        newStatus = ShipmentStatus.DELIVERED,
        locationText = "Seller Storefront",
        description = "Seller fulfilled handover"
    )
    override fun createReturnShipment(originalShipmentId: String, pickupAddress: CustomerDeliveryAddress, sellerAddress: CustomerDeliveryAddress, reason: String): ProviderShipmentCreationResult = ProviderShipmentCreationResult(
        success = true,
        provider = providerId,
        providerShipmentId = "slr_ret_${UUID.randomUUID().toString().take(6)}",
        trackingNumber = "SLR-RET-${System.currentTimeMillis().toString().takeLast(6)}",
        labelUrl = "https://healthogram.local/labels/slr_ret.pdf",
        manifestReference = "SLR-RET-MNF",
        estimatedPickupAt = System.currentTimeMillis() + 3600000L,
        estimatedDeliveryAt = System.currentTimeMillis() + 86400000L
    )
    override fun cancelReturnShipment(returnShipmentId: String): Boolean = true
    override fun getDeliveryEstimate(originCountry: String, destinationCountry: String, serviceType: DeliveryServiceType): DeliveryEstimate = DeliveryEstimate(
        orderId = "ord_slr",
        sellerUid = "seller_slr",
        countryCode = originCountry,
        serviceType = serviceType,
        estimatedPickupDate = "Ready in 1 hour",
        estimatedDeliveryDate = "Same day or pickup immediately",
        confidence = 0.99
    )
}
