# Returns & Return-to-Seller (RTS)

## 1. Return Workflows
The delivery engine supports two distinct return paths:

### Path A: Carrier Return to Seller (RTS)
Triggered when delivery repeatedly fails (customer unavailable, invalid address, recipient refused).
- The courier halts delivery attempts and logs a `DeliveryFailure`.
- The system automatically issues an RTS tracking number and reverses destination routing back to the seller origin clinic/warehouse.
- Order transitions to `RETURNED_TO_SELLER`.

### Path B: Customer-Initiated Return
Triggered by the patient/customer after delivery:
- Permitted within statutory window (e.g. 14 days) for eligible medical and wellness products.
- Condition verification required: Sealed packaging, cold-chain integrity confirmation, and encrypted evidence photos.
- Upon inspection approval by the seller, the engine automatically triggers the Step 14 refund mechanism (`CustomerReturnStatus.APPROVED_FOR_REFUND` -> `PaymentTransaction.REFUNDED`).

## 2. Customer Return Schema (`customer_returns/{returnId}`)
```kotlin
data class CustomerReturn(
    val returnId: String,
    val orderId: String,
    val suborderId: String,
    val customerUid: String,
    val sellerUid: String,
    val productIds: List<String>,
    val reason: String,
    val condition: String,
    val evidenceReference: String,
    val returnMethod: String,
    val returnShipmentId: String?,
    val status: CustomerReturnStatus,
    val requestedAt: Long,
    val approvedAt: Long?,
    val receivedAt: Long?,
    val inspectedAt: Long?,
    val refundStatus: String
)
```
