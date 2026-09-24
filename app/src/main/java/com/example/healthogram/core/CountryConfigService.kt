package com.example.healthogram.core

import java.util.concurrent.ConcurrentHashMap

/**
 * Step 32: CountryConfigService
 *
 * Provides data-driven country configurations for regionalization.
 * Prevents hardcoded single-country logic (e.g. Oman or Saudi Arabia)
 * by driving capabilities, currencies, payment rails, and regulatory rules through schema.
 */
data class CountryConfig(
    val countryCode: String,
    val currency: String,
    val supportedLanguages: List<String> = listOf("en", "ar"),
    val rtlEnabled: Boolean = true,
    val paymentMethods: List<String> = listOf("CARD", "APPLE_PAY", "GOOGLE_PAY"),
    val sellerRequirements: List<String> = listOf("COMMERCIAL_REGISTRATION", "TAX_IDENTIFICATION_NUMBER"),
    val verificationRequirements: List<String> = listOf("GOVERNMENT_ID", "MEDICAL_COUNCIL_LICENSE"),
    val deliveryMethods: List<String> = listOf("STANDARD_COURIER", "LOCAL_PICKUP"),
    val taxConfigurationReference: String = "TAX_CONFIG_${countryCode}",
    val marketplaceEnabled: Boolean = true,
    val internationalMarketplaceEnabled: Boolean = true,
    val healthFeatures: List<String> = listOf("HEALTH_PASSPORT", "FHIR_PORTABILITY", "PRESCRIPTION_DIGITIZATION", "EMERGENCY_CARD"),
    val appointmentFeatures: List<String> = listOf("IN_PERSON", "TELEHEALTH"),
    val translationFeatures: List<String> = listOf("ARABIC_MEDICAL_GLOSSARY", "MULTILINGUAL_LAB_SUMMARY"),
    val supportedIntegrations: List<String> = listOf("LOCAL_HOSPITAL_ADAPTER", "NATIONAL_HEALTH_EXCHANGE"),
    val legalConfigurationReference: String = "TERMS_${countryCode}_V2",
    val active: Boolean = true
)

class CountryConfigService private constructor() {

    private val configs = ConcurrentHashMap<String, CountryConfig>()

    init {
        // Oman Configuration
        configs["OM"] = CountryConfig(
            countryCode = "OM",
            currency = "OMR",
            supportedLanguages = listOf("ar", "en"),
            rtlEnabled = true,
            paymentMethods = listOf("THAWANI", "OMAN_NET", "CARD", "APPLE_PAY"),
            sellerRequirements = listOf("MOCI_CR", "VAT_TIN"),
            verificationRequirements = listOf("OMAN_MEDICAL_SPECIALTY_BOARD", "CIVIL_ID"),
            deliveryMethods = listOf("LOCAL_EXPRESS", "STANDARD_DELIVERY"),
            taxConfigurationReference = "TAX_OM_VAT_5",
            marketplaceEnabled = true,
            internationalMarketplaceEnabled = true
        )

        // Saudi Arabia Configuration
        configs["SA"] = CountryConfig(
            countryCode = "SA",
            currency = "SAR",
            supportedLanguages = listOf("ar", "en"),
            rtlEnabled = true,
            paymentMethods = listOf("MADA", "CARD", "APPLE_PAY", "STC_PAY"),
            sellerRequirements = listOf("MAROOF_CR", "ZATCA_TIN"),
            verificationRequirements = listOf("SCFHS_REGISTRATION", "NATIONAL_ID"),
            deliveryMethods = listOf("SPL_POST", "SPL_EXPRESS"),
            taxConfigurationReference = "TAX_SA_VAT_15",
            marketplaceEnabled = true,
            internationalMarketplaceEnabled = true
        )

        // UAE Configuration
        configs["AE"] = CountryConfig(
            countryCode = "AE",
            currency = "AED",
            supportedLanguages = listOf("ar", "en"),
            rtlEnabled = true,
            paymentMethods = listOf("CARD", "APPLE_PAY", "GOOGLE_PAY"),
            sellerRequirements = listOf("DED_TRADE_LICENSE", "FTA_TRN"),
            verificationRequirements = listOf("MOHAP_LICENSE", "DHA_HAAD_LICENSE"),
            deliveryMethods = listOf("ARAMEX_EXPRESS", "STANDARD_DELIVERY"),
            taxConfigurationReference = "TAX_AE_VAT_5",
            marketplaceEnabled = true,
            internationalMarketplaceEnabled = true
        )

        // United States Configuration (Baseline International)
        configs["US"] = CountryConfig(
            countryCode = "US",
            currency = "USD",
            supportedLanguages = listOf("en", "es"),
            rtlEnabled = false,
            paymentMethods = listOf("CARD", "APPLE_PAY", "GOOGLE_PAY"),
            sellerRequirements = listOf("EIN", "W9_FORM"),
            verificationRequirements = listOf("STATE_MEDICAL_BOARD", "NPI_REGISTRY"),
            deliveryMethods = listOf("USPS", "UPS", "FEDEX"),
            taxConfigurationReference = "TAX_US_SALES",
            marketplaceEnabled = true,
            internationalMarketplaceEnabled = true
        )
    }

    companion object {
        @Volatile
        private var instance: CountryConfigService? = null

        fun getInstance(): CountryConfigService {
            return instance ?: synchronized(this) {
                instance ?: CountryConfigService().also { instance = it }
            }
        }
    }

    fun getCountryConfig(countryCode: String): CountryConfig {
        return configs[countryCode.uppercase()] ?: configs["OM"] ?: CountryConfig(
            countryCode = countryCode.uppercase(),
            currency = "USD"
        )
    }

    fun setCountryConfig(config: CountryConfig) {
        configs[config.countryCode.uppercase()] = config
    }

    fun getAllActiveCountries(): List<CountryConfig> {
        return configs.values.filter { it.active }
    }
}
