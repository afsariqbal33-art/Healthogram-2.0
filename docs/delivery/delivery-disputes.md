# Delivery Disputes & Resolution

## 1. Supported Dispute Categories
Customers and sellers can raise formal disputes regarding physical fulfillment:
1. `not_received`: Package marked as delivered but not physically received.
2. `damaged`: Medication box or supplement bottle crushed, leaked, or unsealed.
3. `wrong_delivery`: Delivered to incorrect address or neighbor without permission.
4. `missing_item`: Suborder arrived missing one or more ordered products.
5. `late_delivery`: Exceeded promised carrier delivery SLA window.
6. `delivery_proof_dispute`: Disputing signature or OTP handoff authenticity.

## 2. Dispute Schema (`delivery_disputes/{disputeId}`)
```kotlin
data class DeliveryDispute(
    val disputeId: String,
    val orderId: String,
    val shipmentId: String,
    val customerUid: String,
    val sellerUid: String,
    val provider: String,
    val disputeType: String,
    val description: String,
    val evidence: String,
    val status: String,
    val assignedAdmin: String?,
    val resolution: String?,
    val createdAt: Long,
    val updatedAt: Long
)
```

## 3. Resolution Protocol
1. Platform compliance admin reviews GPS logs, driver notes, and proof-of-delivery records.
2. If carrier fault is established, carrier claim is filed and customer receives an immediate replacement or refund.
3. If seller packaging defect is established, seller absorbs return shipping fee.
