package com.example.healthogram.delivery

import java.util.Locale

/**
 * Healthogram FlutterFlow Custom Actions and Custom Functions API Bridge.
 * Exposes server-side delivery engine functions directly to UI controllers,
 * background workers, and client viewmodels.
 */
object DeliveryCustomActions {

    private val repository = DeliveryRepository.getInstance()
    private val engine = DeliveryEngine(repository)

    // ==========================================
    // 1. FLUTTERFLOW CUSTOM ACTIONS
    // ==========================================

    fun calculateShippingRateAction(
        customerAddress: CustomerDeliveryAddress,
        sellerAddress: CustomerDeliveryAddress,
        products: List<SuborderProductItem>,
        deliveryMode: DeliveryMode,
        serviceType: DeliveryServiceType
    ): ShippingQuote {
        return engine.calculateShippingRate(
            customerAddress = customerAddress,
            sellerAddress = sellerAddress,
            products = products,
            deliveryMode = deliveryMode,
            serviceType = serviceType
        )
    }

    fun createShipmentAction(
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
        provider: String? = null
    ): Shipment {
        return engine.createShipment(
            orderId = orderId,
            suborderId = suborderId,
            fulfillmentId = fulfillmentId,
            sellerUid = sellerUid,
            customerUid = customerUid,
            originAddress = originAddress,
            destinationAddress = destinationAddress,
            products = products,
            deliveryMode = deliveryMode,
            serviceType = serviceType,
            chosenProvider = provider
        )
    }

    fun verifyDeliveryOtpAction(
        shipmentId: String,
        otpCode: String,
        recipientName: String
    ): Boolean {
        return engine.verifyDeliveryOtp(
            shipmentId = shipmentId,
            enteredOtp = otpCode,
            recipientName = recipientName
        )
    }

    fun requestReturnAction(
        orderId: String,
        suborderId: String,
        customerUid: String,
        sellerUid: String,
        productIds: List<String>,
        reason: String,
        condition: String,
        evidenceUrl: String
    ): CustomerReturn {
        return engine.requestCustomerReturn(
            orderId = orderId,
            suborderId = suborderId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            productIds = productIds,
            reason = reason,
            condition = condition,
            evidenceReference = evidenceUrl
        )
    }

    fun fileDeliveryDisputeAction(
        orderId: String,
        shipmentId: String,
        customerUid: String,
        sellerUid: String,
        provider: String,
        disputeType: String,
        description: String,
        evidence: String
    ): DeliveryDispute {
        return engine.fileDeliveryDispute(
            orderId = orderId,
            shipmentId = shipmentId,
            customerUid = customerUid,
            sellerUid = sellerUid,
            provider = provider,
            disputeType = disputeType,
            description = description,
            evidence = evidence
        )
    }

    fun rescheduleDeliveryAction(
        shipmentId: String,
        newDate: String,
        reason: String
    ): DeliveryReschedule {
        return engine.rescheduleDelivery(
            shipmentId = shipmentId,
            newDate = newDate,
            reason = reason
        )
    }

    fun submitDeliveryReviewAction(
        shipmentId: String,
        orderId: String,
        customerUid: String,
        provider: String,
        speedRating: Int,
        experienceRating: Int,
        conditionRating: Int,
        comment: String
    ): DeliveryReview {
        val review = DeliveryReview(
            shipmentId = shipmentId,
            orderId = orderId,
            customerUid = customerUid,
            provider = provider,
            speedRating = speedRating,
            experienceRating = experienceRating,
            conditionRating = conditionRating,
            comment = comment
        )
        repository.reviews.let {
            repository.updateCountryConfig(repository.countryConfigs.value["SA"]!!) // trigger state
        }
        return review
    }

    fun toggleEmergencyDeliveryStopAction(enabled: Boolean) {
        val current = repository.featureFlags.value
        repository.updateFeatureFlags(current.copy(emergencyDeliveryStop = enabled))
    }

    fun toggleInternationalDeliveryAction(enabled: Boolean) {
        val current = repository.featureFlags.value
        repository.updateFeatureFlags(current.copy(internationalDeliveryEnabled = enabled))
    }

    // ==========================================
    // 2. FLUTTERFLOW CUSTOM FUNCTIONS
    // ==========================================

    fun formatShippingAmount(amountMinor: Long, currencyCode: String): String {
        val symbol = when (currencyCode.uppercase(Locale.ROOT)) {
            "SAR" -> "SAR"
            "AED" -> "AED"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> currencyCode
        }
        val major = amountMinor / 100
        val minor = kotlin.math.abs(amountMinor % 100).toString().padStart(2, '0')
        return "$symbol $major.$minor"
    }

    fun isDeliveryServiceable(originCountry: String, destCountry: String): Boolean {
        val flags = repository.featureFlags.value
        if (!flags.internationalDeliveryEnabled && !originCountry.equals(destCountry, ignoreCase = true)) {
            return false
        }
        val config = repository.countryConfigs.value[destCountry.uppercase(Locale.ROOT)]
        return config?.deliveryEnabled ?: false
    }

    /**
     * Privacy Filter: Masks sensitive medical product details into safe generic category
     * strings to comply with HIPAA/GDPR health privacy standards.
     */
    fun sanitizePackageLabel(rawItemName: String): String {
        val lower = rawItemName.lowercase(Locale.ROOT)
        return when {
            lower.contains("insulin") || lower.contains("injection") -> "Temperature-Controlled Healthcare Package"
            lower.contains("supplement") || lower.contains("vitamin") -> "Nutritional & Dietary Wellness Supplies"
            lower.contains("tablet") || lower.contains("capsule") -> "Personal Care & Clinical Supplies"
            lower.contains("monitor") || lower.contains("meter") -> "Medical Monitoring Device"
            else -> "Healthcare & Wellness Products"
        }
    }
}
