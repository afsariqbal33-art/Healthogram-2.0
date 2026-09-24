package com.example.healthogram.finance

import java.util.concurrent.ConcurrentHashMap

data class CountryTaxConfig(
    val countryCode: String,
    val taxName: String, // VAT, GST, Sales Tax
    val taxRateBasisPoints: Int, // 500 = 5.00%, 1500 = 15.00%
    val isZeroRatedForHealthcare: Boolean = true,
    val appliesToDigitalServices: Boolean = true,
    val appliesToMarketplaceGoods: Boolean = true,
    val version: Int = 1
)

data class TaxCalculationResult(
    val countryCode: String,
    val taxableAmountMinorUnits: Long,
    val taxRateBasisPoints: Int,
    val taxAmountMinorUnits: Long,
    val totalAmountMinorUnits: Long,
    val taxName: String,
    val isHealthcareExempt: Boolean
)

/**
 * TaxService 2.1
 * Server-side reproducible tax calculations using integer minor units.
 * Invariant: Never hardcode fluctuating tax rates in mobile code; rates are versioned and audited.
 */
class TaxService private constructor() {

    private val countryConfigs = ConcurrentHashMap<String, CountryTaxConfig>()

    init {
        // Default standard regional rates
        countryConfigs["OM"] = CountryTaxConfig(
            countryCode = "OM",
            taxName = "Oman VAT",
            taxRateBasisPoints = 500, // 5%
            isZeroRatedForHealthcare = true,
            appliesToDigitalServices = true,
            appliesToMarketplaceGoods = true
        )
        countryConfigs["SA"] = CountryTaxConfig(
            countryCode = "SA",
            taxName = "Saudi VAT",
            taxRateBasisPoints = 1500, // 15%
            isZeroRatedForHealthcare = true,
            appliesToDigitalServices = true,
            appliesToMarketplaceGoods = true
        )
        countryConfigs["AE"] = CountryTaxConfig(
            countryCode = "AE",
            taxName = "UAE VAT",
            taxRateBasisPoints = 500, // 5%
            isZeroRatedForHealthcare = true,
            appliesToDigitalServices = true,
            appliesToMarketplaceGoods = true
        )
        countryConfigs["US"] = CountryTaxConfig(
            countryCode = "US",
            taxName = "US Digital Sales Tax",
            taxRateBasisPoints = 0, // State-variable default
            isZeroRatedForHealthcare = true,
            appliesToDigitalServices = false,
            appliesToMarketplaceGoods = false
        )
    }

    companion object {
        @Volatile
        private var instance: TaxService? = null

        fun getInstance(): TaxService {
            return instance ?: synchronized(this) {
                instance ?: TaxService().also { instance = it }
            }
        }
    }

    fun calculateTax(
        countryCode: String,
        amountMinorUnits: Long,
        isHealthcareService: Boolean = false,
        isDigitalService: Boolean = true
    ): TaxCalculationResult {
        val config = countryConfigs[countryCode.uppercase()] ?: CountryTaxConfig(
            countryCode = countryCode,
            taxName = "Standard Tax",
            taxRateBasisPoints = 0,
            isZeroRatedForHealthcare = true,
            appliesToDigitalServices = false,
            appliesToMarketplaceGoods = false
        )

        if (isHealthcareService && config.isZeroRatedForHealthcare) {
            return TaxCalculationResult(
                countryCode = countryCode,
                taxableAmountMinorUnits = amountMinorUnits,
                taxRateBasisPoints = 0,
                taxAmountMinorUnits = 0L,
                totalAmountMinorUnits = amountMinorUnits,
                taxName = config.taxName,
                isHealthcareExempt = true
            )
        }

        val rate = if (isDigitalService && config.appliesToDigitalServices) {
            config.taxRateBasisPoints
        } else if (!isDigitalService && config.appliesToMarketplaceGoods) {
            config.taxRateBasisPoints
        } else {
            0
        }

        // Integer minor unit calculation: (amount * rate) / 10000 with half-up rounding
        val taxMinor = if (rate > 0) {
            (amountMinorUnits * rate + 5000L) / 10000L
        } else {
            0L
        }

        return TaxCalculationResult(
            countryCode = countryCode,
            taxableAmountMinorUnits = amountMinorUnits,
            taxRateBasisPoints = rate,
            taxAmountMinorUnits = taxMinor,
            totalAmountMinorUnits = amountMinorUnits + taxMinor,
            taxName = config.taxName,
            isHealthcareExempt = false
        )
    }

    fun updateConfig(config: CountryTaxConfig) {
        countryConfigs[config.countryCode.uppercase()] = config
    }
}
