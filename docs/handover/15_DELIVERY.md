# 15 — LOGISTICS & COURIER DELIVERY MANAGEMENT

## 1. Multi-Zone Courier Dispatch Engine
* **Delivery Zones:** Mapped by country, city, and postal district with dedicated rate tables and estimated delivery times (ETA).
* **Fulfillment States:** `PENDING` -> `ASSIGNED` -> `PICKED_UP` -> `IN_TRANSIT` -> `DELIVERED` -> `RETURNED`.

## 2. Real-Time Tracking & Telemetry Throttling
* **5-Second Location Throttle:** Live courier location updates are strictly throttled to a minimum interval of 5 seconds to prevent network thrashing, excessive battery drain, and redundant Firestore writes.
* **Proof of Delivery PIN:** For high-value medical hardware orders, the recipient must provide a 4-digit one-time PIN to the courier to complete delivery.

## 3. Carrier Adapter Abstraction
* **Internal Courier Mode:** Uses native driver app with location sharing.
* **External Commercial Couriers:** Uses standardized adapter interfaces for third-party commercial APIs (e.g. DHL, Aramex, FedEx) with status webhook ingestion.
