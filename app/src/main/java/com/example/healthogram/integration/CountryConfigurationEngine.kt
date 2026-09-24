package com.example.healthogram.integration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * HEALTHOGRAM 2.3 — STEP 51: COUNTRY CONFIGURATION ENGINE
 * Centralized governance for country marketplace, payments, delivery, and tax rules.
 * STRICT INVARIANT: international_marketplace_enabled remains false by default.
 */
class CountryConfigurationEngine private constructor() {

    private val countryMarketplaceConfigs = ConcurrentHashMap<String, CountryMarketplaceConfig>()
    private val countryPaymentConfigs = ConcurrentHashMap<String, CountryPaymentConfig23>()
    private val countryDeliveryConfigs = ConcurrentHashMap<String, CountryDeliveryConfig23>()

    private val _marketplaceConfigsFlow = MutableStateFlow<Map<String, CountryMarketplaceConfig>>(emptyMap())
    val marketplaceConfigsFlow: StateFlow<Map<String, CountryMarketplaceConfig>> = _marketplaceConfigsFlow.asStateFlow()

    private val _paymentConfigsFlow = MutableStateFlow<Map<String, CountryPaymentConfig23>>(emptyMap())
    val paymentConfigsFlow: StateFlow<Map<String, CountryPaymentConfig23>> = _paymentConfigsFlow.asStateFlow()

    private val _deliveryConfigsFlow = MutableStateFlow<Map<String, CountryDeliveryConfig23>>(emptyMap())
    val deliveryConfigsFlow: StateFlow<Map<String, CountryDeliveryConfig23>> = _deliveryConfigsFlow.asStateFlow()

    init {
        seedDefaultConfigurations()
    }

    companion object {
        @Volatile
        private var instance: CountryConfigurationEngine? = null

        fun getInstance(): CountryConfigurationEngine {
            return instance ?: synchronized(this) {
                instance ?: CountryConfigurationEngine().also { instance = it }
            }
        }
    }

    private fun seedDefaultConfigurations() {
        // 1. Kingdom of Saudi Arabia (Primary Sovereign Market)
        val saMarketplace = CountryMarketplaceConfig(
            countryCode = "SA",
            countryName = "Saudi Arabia",
            marketplaceEnabled = true,
            internationalMarketplaceEnabled = false,
            allowedProductCategories = listOf("VITAMINS_SUPPLEMENTS", "FITNESS_EQUIPMENT", "WELLNESS_TRACKERS", "PERSONAL_CARE", "FIRST_AID"),
            restrictedProductCategories = listOf("PRESCRIPTION_MEDICATION", "SURGICAL_HARDWARE", "CONTROLLED_SUBSTANCES"),
            sellerRegistrationEnabled = true,
            paymentMethods = listOf("LOCAL_PAYMENT_METHOD", "CARD", "APPLE_PAY"),
            deliveryProviders = listOf("LOCAL_EXPRESS", "ARAMEX", "SMSA"),
            returnPolicyDays = 14,
            taxRatePercent = 15.0,
            currency = "SAR",
            locale = "ar-SA"
        )
        val saPayment = CountryPaymentConfig23(
            countryCode = "SA",
            currency = "SAR",
            primaryProviderId = "local_mada",
            fallbackProviderId = "stripe",
            enabled = true,
            supportedMethods = listOf("LOCAL_PAYMENT_METHOD", "CARD", "APPLE_PAY"),
            minimumAmountMinor = 500L,
            maximumAmountMinor = 5000000L,
            refundEnabled = true,
            sandboxMode = true,
            productionMode = false
        )
        val saDelivery = CountryDeliveryConfig23(
            countryCode = "SA",
            deliveryZones = listOf(
                DeliveryZoneConfig("SA_CENTRAL", "Riyadh & Central Province", listOf("11", "12"), 1.0, 24),
                DeliveryZoneConfig("SA_WESTERN", "Jeddah, Makkah, Madinah", listOf("21", "22", "23"), 1.0, 24),
                DeliveryZoneConfig("SA_EASTERN", "Dammam, Khobar, Dhahran", listOf("31", "32"), 1.0, 24),
                DeliveryZoneConfig("SA_SOUTHERN", "Asir, Jazan, Najran", listOf("61", "62"), 1.25, 48),
                DeliveryZoneConfig("SA_NORTHERN", "Tabuk, Al-Jawf, Hail", listOf("41", "42"), 1.25, 48)
            ),
            primaryProviderId = "local_express",
            fallbackProviderId = "aramex",
            localDeliveryEnabled = true,
            pickupEnabled = true,
            scheduledDeliveryEnabled = true,
            internationalDeliveryEnabled = false
        )

        // 2. United Arab Emirates
        val aeMarketplace = CountryMarketplaceConfig(
            countryCode = "AE",
            countryName = "United Arab Emirates",
            marketplaceEnabled = true,
            internationalMarketplaceEnabled = false,
            allowedProductCategories = listOf("VITAMINS_SUPPLEMENTS", "FITNESS_EQUIPMENT", "WELLNESS_TRACKERS", "PERSONAL_CARE"),
            restrictedProductCategories = listOf("PRESCRIPTION_MEDICATION", "CONTROLLED_SUBSTANCES"),
            sellerRegistrationEnabled = true,
            paymentMethods = listOf("CARD", "APPLE_PAY", "GOOGLE_PAY"),
            deliveryProviders = listOf("ARAMEX", "DHL"),
            returnPolicyDays = 14,
            taxRatePercent = 5.0,
            currency = "AED",
            locale = "ar-AE"
        )
        val aePayment = CountryPaymentConfig23(
            countryCode = "AE",
            currency = "AED",
            primaryProviderId = "stripe",
            fallbackProviderId = null,
            enabled = true,
            minimumAmountMinor = 500L,
            maximumAmountMinor = 5000000L,
            sandboxMode = true,
            productionMode = false
        )
        val aeDelivery = CountryDeliveryConfig23(
            countryCode = "AE",
            deliveryZones = listOf(
                DeliveryZoneConfig("AE_DXB", "Dubai Urban Zone", listOf("DXB"), 1.0, 12),
                DeliveryZoneConfig("AE_AUH", "Abu Dhabi Urban Zone", listOf("AUH"), 1.0, 12)
            ),
            primaryProviderId = "aramex",
            fallbackProviderId = "dhl",
            internationalDeliveryEnabled = false
        )

        // 3. United States (Sandbox Integration)
        val usMarketplace = CountryMarketplaceConfig(
            countryCode = "US",
            countryName = "United States",
            marketplaceEnabled = true,
            internationalMarketplaceEnabled = false,
            allowedProductCategories = listOf("VITAMINS_SUPPLEMENTS", "FITNESS_EQUIPMENT", "WELLNESS_TRACKERS"),
            restrictedProductCategories = listOf("PRESCRIPTION_MEDICATION", "CONTROLLED_SUBSTANCES"),
            sellerRegistrationEnabled = false, // Controlled staging rollout
            paymentMethods = listOf("CARD", "APPLE_PAY", "GOOGLE_PAY"),
            deliveryProviders = listOf("DHL", "FEDEX"),
            returnPolicyDays = 30,
            taxRatePercent = 8.5,
            currency = "USD",
            locale = "en-US"
        )
        val usPayment = CountryPaymentConfig23(
            countryCode = "US",
            currency = "USD",
            primaryProviderId = "stripe",
            minimumAmountMinor = 100L,
            maximumAmountMinor = 2000000L,
            sandboxMode = true,
            productionMode = false
        )
        val usDelivery = CountryDeliveryConfig23(
            countryCode = "US",
            deliveryZones = listOf(
                DeliveryZoneConfig("US_CONUS", "Continental US", emptyList(), 1.0, 48)
            ),
            primaryProviderId = "dhl",
            fallbackProviderId = "fedex",
            internationalDeliveryEnabled = false
        )

        countryMarketplaceConfigs["SA"] = saMarketplace
        countryMarketplaceConfigs["AE"] = aeMarketplace
        countryMarketplaceConfigs["US"] = usMarketplace

        countryPaymentConfigs["SA"] = saPayment
        countryPaymentConfigs["AE"] = aePayment
        countryPaymentConfigs["US"] = usPayment

        countryDeliveryConfigs["SA"] = saDelivery
        countryDeliveryConfigs["AE"] = aeDelivery
        countryDeliveryConfigs["US"] = usDelivery

        refreshFlows()
    }

    private fun refreshFlows() {
        _marketplaceConfigsFlow.value = countryMarketplaceConfigs.toMap()
        _paymentConfigsFlow.value = countryPaymentConfigs.toMap()
        _deliveryConfigsFlow.value = countryDeliveryConfigs.toMap()
    }

    fun getMarketplaceConfig(countryCode: String): CountryMarketplaceConfig {
        return countryMarketplaceConfigs[countryCode.uppercase()] ?: CountryMarketplaceConfig(
            countryCode = countryCode.uppercase(),
            countryName = countryCode.uppercase(),
            marketplaceEnabled = false,
            internationalMarketplaceEnabled = false
        )
    }

    fun getPaymentConfig(countryCode: String): CountryPaymentConfig23 {
        return countryPaymentConfigs[countryCode.uppercase()] ?: CountryPaymentConfig23(
            countryCode = countryCode.uppercase(),
            currency = "USD",
            primaryProviderId = "stripe",
            enabled = false,
            sandboxMode = true,
            productionMode = false
        )
    }

    fun getDeliveryConfig(countryCode: String): CountryDeliveryConfig23 {
        return countryDeliveryConfigs[countryCode.uppercase()] ?: CountryDeliveryConfig23(
            countryCode = countryCode.uppercase(),
            deliveryZones = emptyList(),
            primaryProviderId = "local_express",
            localDeliveryEnabled = false,
            internationalDeliveryEnabled = false
        )
    }

    // Owner Sovereign Toggles
    fun setInternationalMarketplace(countryCode: String, enabled: Boolean, actorUid: String) {
        val current = getMarketplaceConfig(countryCode)
        countryMarketplaceConfigs[countryCode.uppercase()] = current.copy(internationalMarketplaceEnabled = enabled)
        refreshFlows()
    }

    fun setMarketplaceStatus(countryCode: String, enabled: Boolean, actorUid: String) {
        val current = getMarketplaceConfig(countryCode)
        countryMarketplaceConfigs[countryCode.uppercase()] = current.copy(marketplaceEnabled = enabled)
        refreshFlows()
    }

    fun updatePaymentConfig(config: CountryPaymentConfig23) {
        countryPaymentConfigs[config.countryCode.uppercase()] = config
        refreshFlows()
    }
}
