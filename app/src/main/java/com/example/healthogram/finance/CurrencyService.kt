package com.example.healthogram.finance

import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Step 32: CurrencyService
 *
 * Implements integer minor unit arithmetic, ISO currency conversions,
 * multi-currency formatting, and exchange rate auditing.
 * Invariant: Financial ledger values retain original transaction currency and minor units.
 */
interface ExchangeRateProvider {
    fun getRate(fromCurrency: String, toCurrency: String): Double?
}

class StaticExchangeRateProvider : ExchangeRateProvider {
    private val rates = mapOf(
        "OMR_USD" to 2.60,
        "USD_OMR" to 0.385,
        "SAR_USD" to 0.267,
        "USD_SAR" to 3.75,
        "AED_USD" to 0.272,
        "USD_AED" to 3.67,
        "OMR_SAR" to 9.74,
        "SAR_OMR" to 0.103
    )

    override fun getRate(fromCurrency: String, toCurrency: String): Double? {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return 1.0
        return rates["${fromCurrency.uppercase()}_${toCurrency.uppercase()}"]
    }
}

class CurrencyService private constructor(
    private var exchangeRateProvider: ExchangeRateProvider = StaticExchangeRateProvider()
) {

    data class CurrencyDefinition(
        val code: String,
        val minorUnitMultiplier: Int, // 1000 for OMR (Baiza), 100 for USD/SAR/AED, 1 for JPY
        val symbol: String,
        val arabicSymbol: String,
        val displayName: String
    )

    data class ExchangeRateQuote(
        val fromCurrency: String,
        val toCurrency: String,
        val rate: Double,
        val effectiveTimestamp: Long = System.currentTimeMillis(),
        val providerName: String = "Healthogram Treasury Gateway"
    )

    private val supportedCurrencies = ConcurrentHashMap<String, CurrencyDefinition>()
    private val rateHistory = ConcurrentHashMap<String, MutableList<ExchangeRateQuote>>()

    init {
        supportedCurrencies["OMR"] = CurrencyDefinition("OMR", 1000, "OMR", "ر.ع.", "Omani Rial")
        supportedCurrencies["SAR"] = CurrencyDefinition("SAR", 100, "SAR", "ر.س.", "Saudi Riyal")
        supportedCurrencies["AED"] = CurrencyDefinition("AED", 100, "AED", "د.إ.", "UAE Dirham")
        supportedCurrencies["USD"] = CurrencyDefinition("USD", 100, "$", "$", "US Dollar")
        supportedCurrencies["EUR"] = CurrencyDefinition("EUR", 100, "€", "€", "Euro")
    }

    companion object {
        @Volatile
        private var instance: CurrencyService? = null

        fun getInstance(): CurrencyService {
            return instance ?: synchronized(this) {
                instance ?: CurrencyService().also { instance = it }
            }
        }
    }

    fun setExchangeRateProvider(provider: ExchangeRateProvider) {
        this.exchangeRateProvider = provider
    }

    fun getMinorUnitMultiplier(currencyCode: String): Int {
        return supportedCurrencies[currencyCode.uppercase()]?.minorUnitMultiplier ?: 100
    }

    /**
     * Converts major units (e.g. 5.500 OMR) to minor units (5500 Baiza)
     */
    fun toMinorUnits(majorAmount: Double, currencyCode: String): Long {
        val multiplier = getMinorUnitMultiplier(currencyCode)
        return Math.round(majorAmount * multiplier)
    }

    /**
     * Converts minor units (e.g. 5500 Baiza) to major decimal (5.500)
     */
    fun toMajorUnits(minorAmount: Long, currencyCode: String): Double {
        val multiplier = getMinorUnitMultiplier(currencyCode)
        return minorAmount.toDouble() / multiplier
    }

    /**
     * Formats integer minor units to human-readable currency string
     */
    fun formatAmount(
        minorAmount: Long,
        currencyCode: String,
        isArabic: Boolean = false
    ): String {
        val def = supportedCurrencies[currencyCode.uppercase()]
            ?: CurrencyDefinition(currencyCode.uppercase(), 100, currencyCode, currencyCode, currencyCode)

        val major = toMajorUnits(minorAmount, currencyCode)
        val decimalPlaces = if (def.minorUnitMultiplier == 1000) 3 else 2

        val formattedNum = String.format(Locale.US, "%.${decimalPlaces}f", major)
        return if (isArabic) {
            "$formattedNum ${def.arabicSymbol}"
        } else {
            "${def.symbol} $formattedNum"
        }
    }

    /**
     * Converts minor units from one currency to another using historical or live rate
     */
    fun convertMinorUnits(
        minorAmount: Long,
        fromCurrency: String,
        toCurrency: String
    ): Long {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return minorAmount

        val rate = exchangeRateProvider.getRate(fromCurrency, toCurrency)
            ?: throw IllegalArgumentException("No exchange rate available from $fromCurrency to $toCurrency")

        val fromMajor = toMajorUnits(minorAmount, fromCurrency)
        val toMajor = fromMajor * rate
        val toMinor = toMinorUnits(toMajor, toCurrency)

        val quote = ExchangeRateQuote(
            fromCurrency = fromCurrency.uppercase(),
            toCurrency = toCurrency.uppercase(),
            rate = rate
        )
        rateHistory.computeIfAbsent("${fromCurrency}_${toCurrency}") { mutableListOf() }.add(quote)

        return toMinor
    }

    fun getRateHistory(fromCurrency: String, toCurrency: String): List<ExchangeRateQuote> {
        return rateHistory["${fromCurrency}_${toCurrency}"] ?: emptyList()
    }
}
