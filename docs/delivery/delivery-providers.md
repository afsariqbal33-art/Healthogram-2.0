# Delivery Provider Abstraction & Adapters

## 1. Provider Adapter Interface
The `DeliveryProviderAdapter` interface shields Healthogram core logic from proprietary logistics protocols:

```kotlin
interface DeliveryProviderAdapter {
    val providerId: String
    val providerName: String

    fun checkServiceability(originCountry: String, destinationCountry: String, originPostalCode: String, destinationPostalCode: String): ProviderServiceabilityResult
    fun calculateShippingRate(originAddress: CustomerDeliveryAddress, destinationAddress: CustomerDeliveryAddress, weightGrams: Int, dimensions: String, serviceType: DeliveryServiceType): ProviderRateCalculationResult
    fun createShipment(shipment: Shipment, packages: List<Package>, originAddress: CustomerDeliveryAddress, destinationAddress: CustomerDeliveryAddress): ProviderShipmentCreationResult
    fun cancelShipment(providerShipmentId: String, reason: String): Boolean
    fun assignShipment(providerShipmentId: String, driverId: String?): Boolean
    fun getShipment(providerShipmentId: String): Shipment?
    fun getTracking(trackingNumber: String): List<ShipmentTrackingEvent>
    fun generateLabel(providerShipmentId: String): ProviderLabelResult
    fun generateManifest(shipmentIds: List<String>): ProviderManifestResult
    fun schedulePickup(providerShipmentId: String, pickupAddress: CustomerDeliveryAddress, requestedTimestamp: Long): Boolean
    fun reschedulePickup(providerShipmentId: String, newTimestamp: Long, reason: String): Boolean
    fun trackShipment(trackingNumber: String): ShipmentStatus
    fun handleWebhook(payload: String, signatureHeader: String, signingSecret: String): ProviderWebhookResult
    fun createReturnShipment(originalShipmentId: String, pickupAddress: CustomerDeliveryAddress, sellerAddress: CustomerDeliveryAddress, reason: String): ProviderShipmentCreationResult
    fun cancelReturnShipment(returnShipmentId: String): Boolean
    fun getDeliveryEstimate(originCountry: String, destinationCountry: String, serviceType: DeliveryServiceType): DeliveryEstimate
}
```

## 2. Concrete Adapters
- **`InternalDeliveryAdapter` (`internal_fleet`)**: Healthogram's dedicated temperature-controlled vans and motorcycle couriers. Supports same-day 4-6 hour delivery, active OTP verification, and live driver tracking.
- **`ThirdPartyDeliveryAdapter` (`aramex`, `smsa`, `dhl`, `fedex`)**: Standardized 3PL integration providing API rate calculation, barcode PDF label generation, electronic shipment manifests, and webhook event handling.
- **`SellerDeliveryAdapter` (`seller_managed`)**: For direct clinic/pharmacy counter self-pickup and seller-executed localized delivery.
