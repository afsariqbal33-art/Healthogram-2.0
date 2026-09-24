package com.example.healthogram.delivery

import java.util.UUID

/**
 * Healthogram Legacy Delivery System Bridge & Compatibility Facade.
 * Bridges legacy calls to the production-ready Step 15 Country-wise Delivery Engine.
 */
enum class DeliveryStatus {
    ORDER_CONFIRMED,
    DISPATCH_PENDING,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED_ATTEMPT,
    RETURNED_TO_SELLER
}

data class LegacyDeliveryPartner(
    val partnerId: String,
    val name: String,
    val supportedCountries: List<String>,
    val trackingApiIntegrationKey: String? = null
)

data class DeliveryShipment(
    val shipmentId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val trackingNumber: String = "HG-${System.currentTimeMillis().toString().takeLast(8)}",
    val partner: LegacyDeliveryPartner,
    val currentStatus: DeliveryStatus = DeliveryStatus.ORDER_CONFIRMED,
    val recipientAddress: String,
    val destinationCountry: String,
    val proofOfDeliveryUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

class DeliveryManager {
    private val repository = DeliveryRepository.getInstance()

    fun getPartnersForCountry(countryCode: String): List<DeliveryPartnerRecord> {
        return repository.partners.value.values.filter {
            it.countryCode.equals(countryCode, ignoreCase = true) || it.serviceRegions.contains(countryCode.uppercase())
        }
    }
}
