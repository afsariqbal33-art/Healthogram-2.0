package com.example.healthogram.delivery

import java.util.UUID

/**
 * HEALTHOGRAM — STEP 15: COUNTRY-WISE DELIVERY + SHIPPING + ORDER FULFILLMENT SYSTEM
 * Domain Models, Enums, State Machines, and Ledger Data Structures.
 *
 * Strict Architectural Separation:
 * - Payment confirms money (Step 14).
 * - Delivery confirms physical fulfillment (Step 15).
 * - Integer minor units (halalas/cents) for all financial shipping amounts.
 * - Zero exposure of customer Health Passport or private medical records on labels/packages.
 */

// ==========================================
// 1. DELIVERY MODES & SERVICE TYPES
// ==========================================

enum class DeliveryMode(val wireValue: String, val label: String) {
    SELLER_MANAGED("seller_managed", "Seller Delivery"),
    PLATFORM_MANAGED("platform_managed", "Healthogram Express Fleet"),
    THIRD_PARTY("third_party", "Third-Party Logistics Partner"),
    CUSTOMER_PICKUP("customer_pickup", "Customer Clinic/Store Pickup"),
    SCHEDULED("scheduled", "Scheduled Window Delivery")
}

enum class DeliveryServiceType(val wireValue: String, val label: String) {
    STANDARD("standard", "Standard Delivery"),
    EXPRESS("express", "Express Delivery"),
    SAME_DAY("same_day", "Same-Day Delivery"),
    SCHEDULED("scheduled", "Scheduled Slot"),
    PICKUP("pickup", "Self-Pickup")
}

enum class CountryDeliveryStatus {
    ACTIVE,
    INACTIVE,
    MAINTENANCE,
    BETA,
    COMING_SOON
}

// ==========================================
// 2. COUNTRY DELIVERY CONFIGURATION
// ==========================================

data class CountryDeliveryConfig(
    val countryCode: String,                       // e.g. "SA", "AE", "US", "GB", "DE"
    val countryName: String,                       // e.g. "Saudi Arabia", "United Arab Emirates"
    val deliveryEnabled: Boolean = true,
    val marketplaceDeliveryEnabled: Boolean = true,
    val sellerDeliveryEnabled: Boolean = true,
    val platformDeliveryEnabled: Boolean = true,
    val thirdPartyDeliveryEnabled: Boolean = true,
    val customerPickupEnabled: Boolean = true,
    val scheduledDeliveryEnabled: Boolean = true,
    val sameDayEnabled: Boolean = true,
    val expressDeliveryEnabled: Boolean = true,
    val standardDeliveryEnabled: Boolean = true,
    val returnDeliveryEnabled: Boolean = true,
    val defaultCurrency: String = "SAR",
    val defaultTimezone: String = "Asia/Riyadh",
    val supportedDeliveryProviders: List<String> = listOf("aramex", "smsa", "dhl", "internal_fleet"),
    val defaultDeliveryProvider: String = "aramex",
    val fallbackDeliveryProvider: String = "internal_fleet",
    val maxDeliveryDistanceKm: Double = 150.0,
    val maxPackageWeightKg: Double = 30.0,
    val maxPackageDimensions: String = "120x80x80 cm",
    val deliveryZonesEnabled: Boolean = true,
    val addressValidationRequired: Boolean = true,
    val phoneValidationRequired: Boolean = true,
    val proofOfDeliveryRequired: Boolean = true,
    val cashOnDeliveryEnabled: Boolean = false,
    val internationalDeliveryEnabled: Boolean = false, // MUST remain FALSE initially
    val status: CountryDeliveryStatus = CountryDeliveryStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "system_admin"
)

// ==========================================
// 3. DELIVERY ZONES
// ==========================================

data class DeliveryZone(
    val zoneId: String,
    val countryCode: String,
    val zoneName: String,
    val zoneCode: String,
    val city: String,
    val region: String,
    val postalCodes: List<String> = emptyList(),
    val geographicBoundaries: String = "Metropolitan Circle",
    val active: Boolean = true,
    val standardDeliveryDays: Int = 2,
    val expressDeliveryAvailable: Boolean = true,
    val sameDayAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 4. SHIPPING RATE CONFIGURATION
// ==========================================

data class ShippingRateConfig(
    val rateId: String = UUID.randomUUID().toString(),
    val countryCode: String,
    val zoneId: String,
    val deliveryProvider: String,
    val deliveryMode: DeliveryMode,
    val serviceType: DeliveryServiceType,
    val currencyCode: String,
    val baseFeeMinor: Long,                        // e.g. 1500 = SAR 15.00
    val perKmFeeMinor: Long = 0L,
    val perKgFeeMinor: Long = 200L,                // e.g. 200 = SAR 2.00 / kg above 1kg
    val handlingFeeMinor: Long = 0L,
    val minimumFeeMinor: Long = 1000L,
    val maximumFeeMinor: Long = 15000L,
    val freeShippingThresholdMinor: Long = 20000L, // e.g. Free shipping over SAR 200.00
    val estimatedMinDays: Int = 1,
    val estimatedMaxDays: Int = 3,
    val active: Boolean = true,
    val effectiveFrom: Long = 0L,
    val effectiveUntil: Long = Long.MAX_VALUE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 5. SHIPPING QUOTES
// ==========================================

enum class ShippingQuoteStatus {
    ACTIVE,
    EXPIRED,
    SELECTED,
    CANCELLED
}

data class ShippingQuote(
    val quoteId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val orderId: String,
    val sellerUid: String,
    val countryCode: String,
    val currencyCode: String,
    val deliveryMode: DeliveryMode,
    val serviceType: DeliveryServiceType,
    val provider: String,
    val shippingFeeMinor: Long,
    val taxMinor: Long,
    val totalShippingMinor: Long,
    val estimatedPickupAt: Long,
    val estimatedDeliveryAt: Long,
    val expiresAt: Long = System.currentTimeMillis() + (30 * 60 * 1000L), // 30 minutes validity
    val status: ShippingQuoteStatus = ShippingQuoteStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
}

// ==========================================
// 6. MULTI-SELLER CART & SUBORDERS
// ==========================================

data class SuborderProductItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPriceMinor: Long,
    val weightGrams: Int = 250,
    val lengthCm: Double = 15.0,
    val widthCm: Double = 10.0,
    val heightCm: Double = 5.0,
    val fragile: Boolean = false,
    val temperatureSensitive: Boolean = false
)

data class OrderSuborder(
    val subOrderId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val sellerUid: String,
    val sellerName: String,
    val products: List<SuborderProductItem>,
    val subtotalMinor: Long,
    val shippingFeeMinor: Long,
    val taxMinor: Long,
    val fulfillmentStatus: FulfillmentStatus = FulfillmentStatus.PENDING,
    val shipmentIds: List<String> = emptyList()
)

// ==========================================
// 7. ORDER FULFILLMENT & STATE MACHINE
// ==========================================

enum class FulfillmentStatus(val label: String, val stepOrder: Int) {
    PENDING("Pending Confirmation", 0),
    CONFIRMED("Confirmed", 1),
    PROCESSING("Processing & Preserving", 2),
    PACKED("Packed & Sealed", 3),
    READY_FOR_PICKUP("Ready for Carrier", 4),
    PICKUP_SCHEDULED("Pickup Scheduled", 5),
    PICKED_UP("Picked Up by Courier", 6),
    IN_TRANSIT("In Transit to Destination", 7),
    OUT_FOR_DELIVERY("Out for Delivery", 8),
    DELIVERED("Delivered", 9),
    CUSTOMER_PICKUP_READY("Ready for Customer Pickup", 4),
    CUSTOMER_PICKED_UP("Customer Picked Up", 9),
    CANCELLED("Cancelled", -1),
    DELIVERY_FAILED("Delivery Failed", -2),
    RETURNED_TO_SELLER("Returned to Seller", -3),
    LOST("Lost in Transit", -4),
    DAMAGED("Package Damaged", -5),
    DISPUTED("In Dispute", -6)
}

data class OrderFulfillment(
    val fulfillmentId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val suborderId: String,
    val sellerUid: String,
    val customerUid: String,
    val fulfillmentType: DeliveryMode,
    val fulfillmentStatus: FulfillmentStatus = FulfillmentStatus.PENDING,
    val warehouseReference: String? = null,
    val pickupAddressReference: String,
    val deliveryAddressReference: String,
    val packageCount: Int = 1,
    val totalWeightGrams: Int = 500,
    val packageDimensions: String = "20x15x10 cm",
    val deliveryProvider: String = "internal_fleet",
    val shipmentId: String? = null,
    val expectedPickupAt: Long? = null,
    val expectedDeliveryAt: Long? = null,
    val actualPickupAt: Long? = null,
    val actualDeliveryAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 8. SHIPMENT & PACKAGES
// ==========================================

enum class ShipmentStatus(val label: String) {
    CREATED("Shipment Created"),
    PICKUP_SCHEDULED("Pickup Scheduled"),
    PICKED_UP("Picked Up"),
    IN_TRANSIT("In Transit"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    FAILED("Delivery Failed"),
    RETURNED_TO_SELLER("Returned to Seller"),
    CANCELLED("Cancelled"),
    LOST("Lost in Transit"),
    DAMAGED("Damaged in Transit")
}

data class Shipment(
    val shipmentId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val suborderId: String,
    val fulfillmentId: String,
    val sellerUid: String,
    val customerUid: String,
    val countryCode: String,
    val originZoneId: String,
    val destinationZoneId: String,
    val deliveryProvider: String,
    val providerShipmentId: String,
    val trackingNumber: String,
    val serviceType: DeliveryServiceType,
    val deliveryMode: DeliveryMode,
    val packageCount: Int = 1,
    val weightGrams: Int = 500,
    val dimensions: String = "20x15x10 cm",
    val shippingFeeMinor: Long,
    val currencyCode: String,
    val status: ShipmentStatus = ShipmentStatus.CREATED,
    val estimatedPickupAt: Long,
    val estimatedDeliveryAt: Long,
    val actualPickupAt: Long? = null,
    val actualDeliveryAt: Long? = null,
    val isDelayed: Boolean = false,
    val delayReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Package(
    val packageId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val orderId: String,
    val packageNumber: Int = 1,
    val weightGrams: Int = 500,
    val lengthCm: Double = 20.0,
    val widthCm: Double = 15.0,
    val heightCm: Double = 10.0,
    val barcode: String = "PKG-${System.currentTimeMillis().toString().takeLast(8)}",
    val trackingNumber: String,
    val contentsSummary: String,                   // Generic label, e.g., "Health & Wellness Products" (NEVER detailed medical conditions/drugs)
    val fragile: Boolean = false,
    val temperatureSensitive: Boolean = false,
    val specialHandling: String? = null,
    val status: String = "SEALED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 9. PRODUCT SHIPPING ATTRIBUTES & RESTRICTIONS
// ==========================================

data class ProductShippingAttributes(
    val weightGrams: Int = 250,
    val lengthCm: Double = 15.0,
    val widthCm: Double = 10.0,
    val heightCm: Double = 5.0,
    val packageType: String = "BOX",
    val fragile: Boolean = false,
    val temperatureSensitive: Boolean = false,
    val shippingRestrictions: List<String> = emptyList(),
    val deliveryCategory: String = "HEALTHCARE_STANDARD",
    val maxQuantityPerOrder: Int = 10,
    val requiresSpecialHandling: Boolean = false
)

data class ShippingRestriction(
    val restrictionId: String = UUID.randomUUID().toString(),
    val countryCode: String,
    val productCategory: String,
    val productType: String,
    val prohibited: Boolean = false,
    val restricted: Boolean = false,
    val requiresDocumentation: Boolean = false,
    val requiresSpecialCarrier: Boolean = false,
    val requiresTemperatureControl: Boolean = false,
    val maxQuantity: Int = 5,
    val active: Boolean = true,
    val effectiveFrom: Long = 0L,
    val effectiveUntil: Long = Long.MAX_VALUE
)

// ==========================================
// 10. CUSTOMER DELIVERY ADDRESS & VALIDATION
// ==========================================

data class CustomerDeliveryAddress(
    val addressId: String = UUID.randomUUID().toString(),
    val customerUid: String,
    val countryCode: String,                       // e.g. "SA", "AE", "US"
    val fullName: String,
    val phoneReference: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val region: String,
    val postalCode: String,
    val district: String = "",
    val latitudeApprox: Double? = null,
    val longitudeApprox: Double? = null,
    val deliveryInstructions: String? = null,
    val addressType: String = "home",              // "home", "work", "other"
    val isDefault: Boolean = false,
    val validated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AddressValidationResult(
    val isValid: Boolean,
    val countryMatch: Boolean,
    val zoneId: String?,
    val isServiceable: Boolean,
    val errors: List<String> = emptyList(),
    val standardizedAddress: CustomerDeliveryAddress? = null
)

// ==========================================
// 11. DELIVERY ESTIMATES
// ==========================================

data class DeliveryEstimate(
    val estimateId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val sellerUid: String,
    val countryCode: String,
    val serviceType: DeliveryServiceType,
    val estimatedPickupDate: String,
    val estimatedDeliveryDate: String,
    val timezone: String = "Asia/Riyadh",
    val confidence: Double = 0.95,
    val generatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L
)

// ==========================================
// 12. TRACKING EVENTS & WEBHOOKS
// ==========================================

data class ShipmentTrackingEvent(
    val trackingEventId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val trackingNumber: String,
    val provider: String,
    val status: String,
    val statusCode: String,
    val locationText: String,
    val eventTime: Long = System.currentTimeMillis(),
    val timezone: String = "Asia/Riyadh",
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class DeliveryWebhookEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val provider: String,
    val providerEventId: String,
    val shipmentId: String,
    val eventType: String,
    val signatureVerified: Boolean = true,
    val payloadHash: String,
    val receivedAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val processingStatus: String = "PROCESSED",     // PROCESSED, DUPLICATE_SKIPPED, FAILED
    val retryCount: Int = 0,
    val errorCode: String? = null
)

data class DeliveryTrackingSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val driverReference: String,
    val trackingEnabled: Boolean = true,
    val locationPrecision: String = "APPROXIMATE_500M",
    val latitudeApprox: Double = 24.7136,
    val longitudeApprox: Double = 46.6753,
    val startedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 7200000L, // 2 hours
    val status: String = "ACTIVE"
)

// ==========================================
// 13. DELIVERY PARTNERS, DRIVERS & ASSIGNMENT
// ==========================================

data class DeliveryPartnerRecord(
    val partnerId: String,
    val partnerName: String,
    val countryCode: String,
    val serviceRegions: List<String>,
    val supportedServices: List<DeliveryServiceType>,
    val active: Boolean = true,
    val integrationType: String = "API_ADAPTER",   // API_ADAPTER, FLEET_INTERNAL, MANUAL
    val apiReference: String = "adapter_v1",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DeliveryDriver(
    val driverId: String = UUID.randomUUID().toString(),
    val partnerId: String,
    val displayName: String,
    val status: String = "AVAILABLE",              // AVAILABLE, EN_ROUTE, OFFLINE
    val serviceZone: String,
    val vehicleType: String = "VAN",               // VAN, MOTORCYCLE, SEDAN, REFRIGERATED_VAN
    val active: Boolean = true,
    val currentAssignmentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DeliveryAssignment(
    val assignmentId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val partnerId: String,
    val driverId: String?,
    val assignedAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val pickupAt: Long? = null,
    val completedAt: Long? = null,
    val status: String = "unassigned",             // unassigned, assigned, accepted, rejected, picked_up, completed, cancelled
    val assignmentSource: String = "AUTO_ALGORITHM",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 14. PROOF OF DELIVERY & OTP
// ==========================================

data class ProofOfDelivery(
    val podId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val orderId: String,
    val deliveryMethod: String = "CONTACTLESS_OTP",
    val recipientConfirmationType: String = "OTP", // OTP, SIGNATURE, PHOTO, RECIPIENT_CONFIRMATION, PICKUP_CONFIRMATION
    val recipientName: String,
    val confirmationTimestamp: Long = System.currentTimeMillis(),
    val photoReference: String? = null,
    val signatureReference: String? = null,
    val otpVerified: Boolean = true,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class DeliveryOtp(
    val otpId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val orderId: String,
    val hashedOtp: String,                         // SHA-256 hashed OTP for verification
    val expiresAt: Long = System.currentTimeMillis() + 86400000L,
    val attempts: Int = 0,
    val verified: Boolean = false,
    val verifiedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// 15. FAILURES, RESCHEDULING, RTS & RETURNS
// ==========================================

data class DeliveryFailure(
    val failureId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val orderId: String,
    val reasonCode: String,                        // customer_unavailable, incorrect_address, inaccessible_location, provider_issue, weather_disruption, package_damaged, recipient_refused, restricted_item
    val description: String,
    val attemptedAt: Long = System.currentTimeMillis(),
    val retryAllowed: Boolean = true,
    val nextAttemptAt: Long? = System.currentTimeMillis() + 86400000L,
    val customerActionRequired: Boolean = false,
    val sellerActionRequired: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class DeliveryReschedule(
    val rescheduleId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val oldDate: String,
    val newDate: String,
    val reason: String,
    val requestedBy: String = "CUSTOMER",          // CUSTOMER, SELLER, CARRIER
    val approved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class ReturnToSeller(
    val rtsId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val orderId: String,
    val sellerUid: String,
    val reason: String,
    val initiatedAt: Long = System.currentTimeMillis(),
    val pickedUpAt: Long? = null,
    val receivedAt: Long? = null,
    val status: String = "INITIATED",              // INITIATED, IN_TRANSIT, RECEIVED, INSPECTED
    val returnShipmentId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CustomerReturnStatus(val label: String) {
    REQUESTED("Return Requested"),
    APPROVED("Return Approved"),
    REJECTED("Return Rejected"),
    RETURN_IN_TRANSIT("Return In Transit"),
    RECEIVED("Received by Seller"),
    INSPECTING("Inspecting Condition"),
    APPROVED_FOR_REFUND("Approved for Refund"),
    REFUNDED("Refund Processed"),
    CLOSED("Closed")
}

data class CustomerReturn(
    val returnId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val suborderId: String,
    val customerUid: String,
    val sellerUid: String,
    val productIds: List<String>,
    val reason: String,
    val condition: String,                         // UNOPENED, SEAL_INTACT, DEFECTIVE, DAMAGED
    val evidenceReference: String,                 // Private media storage reference
    val returnMethod: String = "COURIER_PICKUP",   // COURIER_PICKUP, DROP_OFF
    val returnShipmentId: String? = null,
    val status: CustomerReturnStatus = CustomerReturnStatus.REQUESTED,
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val receivedAt: Long? = null,
    val inspectedAt: Long? = null,
    val refundStatus: String = "PENDING_INSPECTION",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DeliveryDispute(
    val disputeId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val shipmentId: String,
    val customerUid: String,
    val sellerUid: String,
    val provider: String,
    val disputeType: String,                       // not_received, damaged, wrong_delivery, missing_item, late_delivery, address_issue, delivery_proof_dispute
    val description: String,
    val evidence: String,
    val status: String = "OPEN",                   // OPEN, UNDER_REVIEW, RESOLVED, REJECTED
    val assignedAdmin: String? = null,
    val resolution: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 16. DELIVERY FINANCIAL LEDGER & COST CALCULATION
// ==========================================

enum class DeliveryLedgerEntryType {
    SHIPPING_CHARGE,
    DELIVERY_PROVIDER_COST,
    SELLER_DELIVERY_FEE,
    DELIVERY_ADJUSTMENT,
    DELIVERY_REFUND,
    FAILED_DELIVERY_FEE,
    RETURN_SHIPPING,
    PLATFORM_DELIVERY_REVENUE
}

data class DeliveryFinancialLedgerEntry(
    val entryId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val orderId: String,
    val sellerUid: String,
    val provider: String,
    val countryCode: String,
    val currencyCode: String,
    val entryType: DeliveryLedgerEntryType,
    val amountMinor: Long,
    val status: String = "POSTED",
    val referenceId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val immutable: Boolean = true
)

data class CashCollectionRecord(
    val recordId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val orderId: String,
    val amountMinor: Long,
    val currencyCode: String,
    val collectorReference: String,
    val collectionStatus: String = "PENDING",       // PENDING, COLLECTED, FAILED, RECONCILED, DISPUTED
    val collectedAt: Long? = null,
    val reconciledAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// 17. DELIVERY CAPACITY & SLOTS
// ==========================================

data class DeliverySlot(
    val slotId: String = UUID.randomUUID().toString(),
    val countryCode: String,
    val zoneId: String,
    val provider: String,
    val date: String,                              // "2026-09-15"
    val startTime: String,                         // "10:00"
    val endTime: String,                           // "13:00"
    val capacity: Int = 50,
    val reservedCapacity: Int = 12,
    val availableCapacity: Int = 38,
    val active: Boolean = true
) {
    fun isAvailable(): Boolean = active && availableCapacity > 0
}

// ==========================================
// 18. AUDIT LOGS, RECONCILIATION & REVIEWS
// ==========================================

data class DeliveryAuditLog(
    val logId: String = UUID.randomUUID().toString(),
    val actorUid: String,
    val actorRole: String,
    val action: String,
    val orderId: String,
    val shipmentId: String,
    val previousStatus: String? = null,
    val newStatus: String? = null,
    val provider: String,
    val requestId: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
)

data class DeliveryReconciliationReport(
    val reportId: String = UUID.randomUUID().toString(),
    val provider: String,
    val periodStart: Long,
    val periodEnd: Long,
    val totalShipmentsChecked: Int,
    val matchedCount: Int,
    val discrepancyCount: Int,
    val discrepancyDetails: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class DeliveryReview(
    val reviewId: String = UUID.randomUUID().toString(),
    val shipmentId: String,
    val orderId: String,
    val customerUid: String,
    val provider: String,
    val speedRating: Int,                          // 1 to 5
    val experienceRating: Int,                     // 1 to 5
    val conditionRating: Int,                      // 1 to 5
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// 19. FEATURE FLAGS & EMERGENCY KILL SWITCHES
// ==========================================

data class DeliveryFeatureFlags(
    val emergencyDeliveryStop: Boolean = false,
    val emergencyShipmentCreationStop: Boolean = false,
    val emergencyPayoutHold: Boolean = false,
    val internationalDeliveryEnabled: Boolean = false, // MUST remain FALSE initially
    val sellerDeliveryEnabled: Boolean = true,
    val platformDeliveryEnabled: Boolean = true,
    val thirdPartyDeliveryEnabled: Boolean = true,
    val customerPickupEnabled: Boolean = true,
    val sameDayEnabled: Boolean = true,
    val expressEnabled: Boolean = true,
    val scheduledDeliveryEnabled: Boolean = true,
    val returnDeliveryEnabled: Boolean = true,
    val codEnabled: Boolean = false,
    val otpRequirementEnabled: Boolean = true
)
