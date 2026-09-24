# Shipping Rate Engine & Server Calculation

## 1. Zero Trust Architecture
Client-side applications (FlutterFlow/web apps) are strictly prohibited from determining shipping fees. The server-authoritative engine calculates all pricing via:

$$\text{Total Shipping Minor} = \max(\text{Minimum Fee}, \text{Base Fee} + (\Delta \text{Weight kg} \times \text{Per-Kg Fee}) + \text{Handling}) + \text{VAT}$$

- Free shipping thresholds are applied when the suborder total qualifies (`subtotalMinor >= freeShippingThresholdMinor`).
- Integer minor units (cents / halalas) eliminate IEEE 754 floating point imprecisions.

## 2. Shipping Rate Config Schema (`shipping_rate_configs/{rateId}`)
```kotlin
data class ShippingRateConfig(
    val rateId: String,
    val countryCode: String,
    val zoneId: String,
    val deliveryProvider: String,
    val deliveryMode: DeliveryMode,
    val serviceType: DeliveryServiceType,
    val currencyCode: String,
    val baseFeeMinor: Long,
    val perKmFeeMinor: Long,
    val perKgFeeMinor: Long,
    val handlingFeeMinor: Long,
    val minimumFeeMinor: Long,
    val maximumFeeMinor: Long,
    val freeShippingThresholdMinor: Long,
    val estimatedMinDays: Int,
    val estimatedMaxDays: Int,
    val active: Boolean
)
```

## 3. Rate Quote Lifecycle (`shipping_quotes/{quoteId}`)
- Created with an exact 30-minute validity timestamp (`expiresAt = now + 1800000ms`).
- At checkout submission, the server re-validates the quote status. Expired quotes trigger an automated recalculation before payment capture.
