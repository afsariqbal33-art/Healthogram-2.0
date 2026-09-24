# Proof of Delivery (POD) & Contactless OTP

## 1. Multi-Modal Verification Types
To guarantee verified physical receipt of sensitive wellness and pharmaceutical orders, Healthogram mandates one of the following recipient confirmation mechanisms:
- **`OTP` (One-Time Password)**: 6-digit dynamic PIN sent to customer device upon out-for-delivery event. Verified by courier before package release.
- **`SIGNATURE`**: Digital signature captured on courier terminal.
- **`PHOTO`**: Geotagged photo of package placed at doorstep.
- **`PICKUP_CONFIRMATION`**: QR code scan at clinic storefront counter.

## 2. Delivery OTP Engine
1. When a shipment transitions to `OUT_FOR_DELIVERY`, `DeliveryEngine` generates a cryptographically random 6-digit numeric OTP.
2. The OTP is salted and hashed using SHA-256 before storage in `delivery_otps/{otpId}`.
3. The courier terminal transmits the entered code to `DeliveryEngine.verifyDeliveryOtp()`.
4. Max 5 verification attempts are allowed before security lockout. Upon successful match, `ProofOfDelivery` is permanently recorded and order status transitions to `DELIVERED`.

## 3. POD Record Schema (`proof_of_delivery/{podId}`)
```kotlin
data class ProofOfDelivery(
    val podId: String,
    val shipmentId: String,
    val orderId: String,
    val deliveryMethod: String,
    val recipientConfirmationType: String,
    val recipientName: String,
    val confirmationTimestamp: Long,
    val photoReference: String?,
    val signatureReference: String?,
    val otpVerified: Boolean,
    val notes: String?
)
```
