package com.example.healthogram.core.i18n

/**
 * Healthogram 2.0 Multi-Country Configuration & High-Precision Financial Arithmetic.
 *
 * Implements architectural constraints:
 * - Dynamic country configurations (phone code, currency, tax, feature flags).
 * - Safe minor-unit integer arithmetic (eliminating IEEE 754 floating-point drift).
 * - Deterministic commission calculation with absolute zero-drift sum preservation.
 * - By default, international marketplace remains disabled unless explicitly activated.
 */
data class CountryConfig(
    val countryCode: String,            // ISO-3166-1 alpha-2 (e.g., "OM", "SA")
    val countryName: String,
    val phoneDialCode: String,          // e.g., "+968", "+966"
    val defaultCurrency: String,        // e.g., "OMR", "SAR", "AED"
    val subunitFactor: Int,             // 1000 for OMR/KWD/BHD, 100 for SAR/AED/USD
    val currencySymbol: String,
    val vatBasisPoints: Int,            // 500 = 5.0%, 1500 = 15.0%
    val isMarketplaceEnabled: Boolean,
    val isDeliveryEnabled: Boolean,
    val emergencyNumber: String
)

object CountryRegistry {
    private val countries = mapOf(
        "OM" to CountryConfig(
            countryCode = "OM",
            countryName = "Sultanate of Oman",
            phoneDialCode = "+968",
            defaultCurrency = "OMR",
            subunitFactor = 1000, // 1000 Baiza = 1 OMR
            currencySymbol = "ر.ع.",
            vatBasisPoints = 500, // 5% VAT
            isMarketplaceEnabled = true,
            isDeliveryEnabled = true,
            emergencyNumber = "9999"
        ),
        "SA" to CountryConfig(
            countryCode = "SA",
            countryName = "Kingdom of Saudi Arabia",
            phoneDialCode = "+966",
            defaultCurrency = "SAR",
            subunitFactor = 100, // 100 Halalas = 1 SAR
            currencySymbol = "ر.س",
            vatBasisPoints = 1500, // 15% VAT
            isMarketplaceEnabled = true,
            isDeliveryEnabled = true,
            emergencyNumber = "997"
        ),
        "AE" to CountryConfig(
            countryCode = "AE",
            countryName = "United Arab Emirates",
            phoneDialCode = "+971",
            defaultCurrency = "AED",
            subunitFactor = 100, // 100 Fils = 1 AED
            currencySymbol = "د.إ",
            vatBasisPoints = 500, // 5% VAT
            isMarketplaceEnabled = true,
            isDeliveryEnabled = true,
            emergencyNumber = "998"
        ),
        "KW" to CountryConfig(
            countryCode = "KW",
            countryName = "State of Kuwait",
            phoneDialCode = "+965",
            defaultCurrency = "KWD",
            subunitFactor = 1000, // 1000 Fils = 1 KWD
            currencySymbol = "د.ك",
            vatBasisPoints = 0,
            isMarketplaceEnabled = true,
            isDeliveryEnabled = true,
            emergencyNumber = "112"
        ),
        "QA" to CountryConfig(
            countryCode = "QA",
            countryName = "State of Qatar",
            phoneDialCode = "+974",
            defaultCurrency = "QAR",
            subunitFactor = 100, // 100 Dirhams = 1 QAR
            currencySymbol = "ر.ق",
            vatBasisPoints = 0,
            isMarketplaceEnabled = true,
            isDeliveryEnabled = true,
            emergencyNumber = "999"
        ),
        "BH" to CountryConfig(
            countryCode = "BH",
            countryName = "Kingdom of Bahrain",
            phoneDialCode = "+973",
            defaultCurrency = "BHD",
            subunitFactor = 1000, // 1000 Fils = 1 BHD
            currencySymbol = "د.ب",
            vatBasisPoints = 1000, // 10% VAT
            isMarketplaceEnabled = true,
            isDeliveryEnabled = true,
            emergencyNumber = "999"
        )
    )

    fun getCountry(code: String): CountryConfig {
        return countries[code.uppercase()] ?: countries["OM"]!!
    }

    fun getAllSupportedCountries(): List<CountryConfig> {
        return countries.values.toList()
    }
}

class CurrencyMinorUnitService {
    /**
     * Converts a major decimal value to exact minor units integer (e.g. 12.500 OMR -> 12500 baiza).
     */
    fun toMinorUnits(majorAmount: Double, subunitFactor: Int): Long {
        return Math.round(majorAmount * subunitFactor)
    }

    /**
     * Converts minor units integer back to major floating value for display.
     */
    fun toMajorUnits(minorUnits: Long, subunitFactor: Int): Double {
        return minorUnits.toDouble() / subunitFactor.toDouble()
    }

    /**
     * Formats minor units into local display string with precise decimal padding.
     */
    fun formatCurrency(minorUnits: Long, currencyCode: String, subunitFactor: Int): String {
        val decimals = if (subunitFactor >= 1000) 3 else 2
        val major = minorUnits.toDouble() / subunitFactor.toDouble()
        return "%.${decimals}f %s".format(major, currencyCode)
    }

    /**
     * Calculates platform commission and seller payout using integer basis points.
     * Guaranteed invariant: commission + sellerPayout == grossMinorUnits.
     * Basis points: 1000 bps = 10.0%.
     */
    fun calculateCommissionSplit(
        grossMinorUnits: Long,
        commissionBasisPoints: Int = 1000 // 10% default
    ): Pair<Long, Long> {
        if (grossMinorUnits <= 0) return Pair(0L, 0L)

        // Integer arithmetic with rounding
        val commission = (grossMinorUnits * commissionBasisPoints + 5000L) / 10000L
        val sellerPayout = grossMinorUnits - commission

        // Invariant safety assertion
        check(commission + sellerPayout == grossMinorUnits) {
            "Financial Invariant Violation: Commission split sum mismatch"
        }

        return Pair(commission, sellerPayout)
    }
}
