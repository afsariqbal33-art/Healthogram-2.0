package com.example.healthogram.payments

import java.util.UUID

/**
 * Supported payment providers across regions.
 */
enum class PaymentProviderType {
    STRIPE,
    PAYPAL,
    APPLE_PAY,
    GOOGLE_PAY,
    MADA_SAUDI,
    BENEFIT_BAHRAIN,
    RAZORPAY_INDIA,
    FAWRY_EGYPT,
    DIRECT_BANK_TRANSFER
}

enum class TransactionStatus {
    PENDING,
    PROCESSING,
    AUTHORIZED,
    SETTLED,
    FAILED,
    REFUNDED,
    CHARGEBACK,
    CANCELLED
}

data class PaymentMethodOption(
    val methodId: String,
    val name: String,
    val providerType: PaymentProviderType,
    val supportsRecurring: Boolean = false
)

data class CountryPaymentConfiguration(
    val countryCode: String,
    val defaultCurrency: String,
    val supportedCurrencies: List<String>,
    val allowedProviders: List<PaymentProviderType>,
    val isTaxIncluded: Boolean = false,
    val vatRatePercentage: Double = 0.0,
    val maxTransactionLimit: Double = 10000.0
)

data class LegacyPaymentTransaction(
    val transactionId: String = UUID.randomUUID().toString(),
    val orderOrBookingId: String,
    val customerUid: String,
    val amount: Double,
    val currency: String,
    val countryCode: String,
    val providerType: PaymentProviderType,
    val platformFee: Double,
    val providerFee: Double,
    val netMerchantPayout: Double,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Country-aware Payment Abstraction Layer.
 * Decouples client checkout logic from backend payment gateway SDKs.
 */
class PaymentAbstractionService {

    private val countryConfigs = mutableMapOf(
        "US" to CountryPaymentConfiguration(
            countryCode = "US",
            defaultCurrency = "USD",
            supportedCurrencies = listOf("USD"),
            allowedProviders = listOf(PaymentProviderType.STRIPE, PaymentProviderType.APPLE_PAY, PaymentProviderType.GOOGLE_PAY),
            vatRatePercentage = 0.0
        ),
        "GB" to CountryPaymentConfiguration(
            countryCode = "GB",
            defaultCurrency = "GBP",
            supportedCurrencies = listOf("GBP", "EUR"),
            allowedProviders = listOf(PaymentProviderType.STRIPE, PaymentProviderType.APPLE_PAY, PaymentProviderType.GOOGLE_PAY),
            isTaxIncluded = true,
            vatRatePercentage = 20.0
        ),
        "AE" to CountryPaymentConfiguration(
            countryCode = "AE",
            defaultCurrency = "AED",
            supportedCurrencies = listOf("AED", "USD"),
            allowedProviders = listOf(PaymentProviderType.STRIPE, PaymentProviderType.APPLE_PAY),
            isTaxIncluded = true,
            vatRatePercentage = 5.0
        ),
        "SA" to CountryPaymentConfiguration(
            countryCode = "SA",
            defaultCurrency = "SAR",
            supportedCurrencies = listOf("SAR"),
            allowedProviders = listOf(PaymentProviderType.MADA_SAUDI, PaymentProviderType.APPLE_PAY, PaymentProviderType.STRIPE),
            isTaxIncluded = true,
            vatRatePercentage = 15.0
        )
    )

    fun getCountryConfiguration(countryCode: String): CountryPaymentConfiguration {
        return countryConfigs[countryCode.uppercase()] ?: CountryPaymentConfiguration(
            countryCode = countryCode,
            defaultCurrency = "USD",
            supportedCurrencies = listOf("USD"),
            allowedProviders = listOf(PaymentProviderType.STRIPE, PaymentProviderType.GOOGLE_PAY)
        )
    }

    /**
     * Calculates fees and splits for marketplace orders or appointments.
     */
    fun calculateSplit(amount: Double, commissionRate: Double = 0.05): Triple<Double, Double, Double> {
        val platformFee = amount * commissionRate
        val providerFee = (amount * 0.029) + 0.30 // Standard interchange estimate
        val netMerchant = amount - (platformFee + providerFee)
        return Triple(platformFee, providerFee, maxOf(0.0, netMerchant))
    }
}
