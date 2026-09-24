package com.example.healthogram.core.foundation.country

import java.util.concurrent.ConcurrentHashMap

/**
 * Step 49: Healthogram 2.3 Comprehensive Data-Driven Country Configuration Layer.
 *
 * CRITICAL ARCHITECTURAL INVARIANT:
 * `internationalTradeEnabled` is permanently locked to FALSE across all countries.
 * Domestic commerce isolation is strictly enforced.
 */
data class CountryProfile(
    val countryCode: String,
    val countryName: String,
    val currency: String,
    val currencyMinorUnits: Int = 2,
    val defaultLanguage: String = "en",
    val isRtl: Boolean = false,
    val timezone: String,
    val isMarketplaceEnabled: Boolean = true,
    val internationalTradeEnabled: Boolean = false, // Strictly false
    val isPaymentEnabled: Boolean = true,
    val isDeliveryEnabled: Boolean = true,
    val isSellerOnboardingEnabled: Boolean = true,
    val verificationAuthorities: List<String>,
    val paymentGateways: List<String>,
    val deliveryPartners: List<String>,
    val vatRate: Double,
    val requiresPrescriptionForRx: Boolean = true,
    val maxDeliveryWindowHours: Int = 48,
    val temperatureMonitoringRequired: Boolean = true
) {
    init {
        require(!internationalTradeEnabled) {
            "Compliance Violation: Cross-border commerce is strictly disabled. internationalTradeEnabled must be false for $countryCode."
        }
    }
}

class CountryConfigRegistry private constructor() {
    private val countries = ConcurrentHashMap<String, CountryProfile>()

    init {
        // United States
        register(
            CountryProfile(
                countryCode = "US",
                countryName = "United States",
                currency = "USD",
                timezone = "America/New_York",
                verificationAuthorities = listOf("NPI_REGISTRY", "STATE_MEDICAL_BOARD", "DEA_LICENSE"),
                paymentGateways = listOf("STRIPE", "APPLE_PAY", "GOOGLE_PAY"),
                deliveryPartners = listOf("FEDEX", "UPS"),
                vatRate = 0.08
            )
        )

        // Saudi Arabia
        register(
            CountryProfile(
                countryCode = "SA",
                countryName = "Saudi Arabia",
                currency = "SAR",
                defaultLanguage = "ar",
                isRtl = true,
                timezone = "Asia/Riyadh",
                verificationAuthorities = listOf("SCFHS_REGISTRY", "SEHA_ACCREDITATION", "MOH_LICENSE"),
                paymentGateways = listOf("HYPERPAY", "MADA", "APPLE_PAY", "GOOGLE_PAY"),
                deliveryPartners = listOf("ARAMEX", "SMSA"),
                vatRate = 0.15
            )
        )

        // United Arab Emirates
        register(
            CountryProfile(
                countryCode = "AE",
                countryName = "United Arab Emirates",
                currency = "AED",
                defaultLanguage = "ar",
                isRtl = true,
                timezone = "Asia/Dubai",
                verificationAuthorities = listOf("DHA_LICENSE", "DOH_LICENSE", "MOHAP_LICENSE"),
                paymentGateways = listOf("HYPERPAY", "APPLE_PAY", "GOOGLE_PAY"),
                deliveryPartners = listOf("ARAMEX", "FETCHR"),
                vatRate = 0.05
            )
        )

        // Egypt
        register(
            CountryProfile(
                countryCode = "EG",
                countryName = "Egypt",
                currency = "EGP",
                defaultLanguage = "ar",
                isRtl = true,
                timezone = "Africa/Cairo",
                verificationAuthorities = listOf("EGYPTIAN_MEDICAL_SYNDICATE", "EDA_LICENSE"),
                paymentGateways = listOf("FAWRY", "HYPERPAY", "VODAFONE_CASH"),
                deliveryPartners = listOf("BOSTA", "ARAMEX"),
                vatRate = 0.14
            )
        )

        // United Kingdom
        register(
            CountryProfile(
                countryCode = "GB",
                countryName = "United Kingdom",
                currency = "GBP",
                timezone = "Europe/London",
                verificationAuthorities = listOf("GMC_REFERENCE", "CQC_REGISTRATION"),
                paymentGateways = listOf("STRIPE", "APPLE_PAY", "GOOGLE_PAY"),
                deliveryPartners = listOf("ROYAL_MAIL", "DPD"),
                vatRate = 0.20
            )
        )

        // Canada
        register(
            CountryProfile(
                countryCode = "CA",
                countryName = "Canada",
                currency = "CAD",
                timezone = "America/Toronto",
                verificationAuthorities = listOf("COLLEGE_OF_PHYSICIANS", "HEALTH_CANADA"),
                paymentGateways = listOf("STRIPE", "APPLE_PAY", "GOOGLE_PAY", "INTERAC"),
                deliveryPartners = listOf("CANADA_POST", "FEDEX"),
                vatRate = 0.13
            )
        )

        // India
        register(
            CountryProfile(
                countryCode = "IN",
                countryName = "India",
                currency = "INR",
                timezone = "Asia/Kolkata",
                verificationAuthorities = listOf("NMC_REGISTRATION", "STATE_MEDICAL_COUNCIL"),
                paymentGateways = listOf("RAZORPAY", "UPI", "GOOGLE_PAY"),
                deliveryPartners = listOf("DELHIVERY", "BLUEDART"),
                vatRate = 0.18
            )
        )

        // Oman
        register(
            CountryProfile(
                countryCode = "OM",
                countryName = "Oman",
                currency = "OMR",
                currencyMinorUnits = 3,
                defaultLanguage = "ar",
                isRtl = true,
                timezone = "Asia/Muscat",
                verificationAuthorities = listOf("OMAN_MEDICAL_SPECIALTY_BOARD", "MOH_OMAN"),
                paymentGateways = listOf("THAWANI", "OMAN_NET", "APPLE_PAY"),
                deliveryPartners = listOf("LOCAL_EXPRESS", "ARAMEX"),
                vatRate = 0.05
            )
        )
    }

    fun register(profile: CountryProfile) {
        countries[profile.countryCode.uppercase()] = profile
    }

    fun getCountry(code: String?): CountryProfile? {
        if (code == null) return null
        return countries[code.uppercase()]
    }

    fun isCountrySupported(code: String?): Boolean {
        if (code == null) return false
        return countries.containsKey(code.uppercase())
    }

    companion object {
        val instance: CountryConfigRegistry by lazy { CountryConfigRegistry() }
    }
}
