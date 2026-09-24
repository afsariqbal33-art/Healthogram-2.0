# HEALTHOGRAM — DELIVERY ARCHITECTURE

## 1. Localized Logistics Integration
- Delivery partners are assigned dynamically based on destination country and package classification:
  - International: DHL Express, FedEx, Aramex.
  - National & Local Express: Certified regional cold-chain and medical logistics carriers.

## 2. Tracking Lifecycle
`ORDER_CONFIRMED` -> `DISPATCH_PENDING` -> `PICKED_UP` -> `IN_TRANSIT` -> `OUT_FOR_DELIVERY` -> `DELIVERED` (with digital Proof-of-Delivery).
