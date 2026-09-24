# Order Fulfillment State Machine

## 1. 18 Authoritative Fulfillment States
Every suborder fulfillment transitions across deterministic states:

```
[PENDING]
   │
   ▼
[CONFIRMED]
   │
   ▼
[PROCESSING] ─── (Preserving, inspecting, thermal preparation)
   │
   ▼
[PACKED] ─────── (Boxed with tamper-evident seal and barcode label)
   │
   ├──────────────────────────────┐ (Self-Pickup Route)
   ▼ (Carrier Route)              ▼
[READY_FOR_PICKUP]       [CUSTOMER_PICKUP_READY]
   │                              │
   ▼                              ▼
[PICKUP_SCHEDULED]       [CUSTOMER_PICKED_UP] ──► [COMPLETED]
   │
   ▼
[PICKED_UP]
   │
   ▼
[IN_TRANSIT]
   │
   ▼
[OUT_FOR_DELIVERY]
   │
   ├──────────────────────────────┬───────────────────────────┐
   ▼                              ▼                           ▼
[DELIVERED]              [DELIVERY_FAILED]           [DAMAGED / LOST]
(OTP verified)                    │                           │
                                  ▼                           ▼
                         [RETURNED_TO_SELLER]            [DISPUTED]
```

## 2. Fulfillment Invariants
1. An order cannot transition to `OUT_FOR_DELIVERY` without an assigned carrier tracking number and valid OTP hash.
2. `DELIVERED` requires verified proof of delivery (contactless OTP match or recipient signature).
3. Failed delivery attempts record an immutable `DeliveryFailure` document with reason codes (`customer_unavailable`, `inaccessible_location`, `package_damaged`, etc.).
