# Healthogram Country-wise Delivery + Shipping + Order Fulfillment Architecture

## 1. Overview
The Healthogram Delivery System orchestrates post-checkout order fulfillment across distinct international jurisdictions while strictly enforcing sovereign domestic boundaries.

The core architecture connects:
```
Customer
  ↓
Marketplace Order
  ↓
Seller Suborders (Multi-Seller Cart Split)
  ↓
Fulfillment State Machine
  ↓
Delivery Provider Adapter (Internal Fleet / 3PL)
  ↓
Courier Pickup & Thermal Handover
  ↓
Shipment & Climate-Controlled Tracking
  ↓
Out For Delivery
  ↓
Contactless Proof of Delivery (OTP / Signature)
  ↓
Return / Dispute Workflow (if needed)
  ↓
Seller Settlement & Double-Entry Ledger
```

## 2. Core Architectural Principles
1. **Separation of Concerns**: Payment (Step 14) and Delivery (Step 15) are decoupled micro-architectures. An order is never marked as fulfilled simply because payment succeeded.
2. **Server-Authoritative Calculation**: Shipping rates are computed strictly on the backend (`DeliveryEngine.calculateShippingRate`) based on validated country codes, weight tiers, and zone rules. FlutterFlow or client-side pricing is never trusted.
3. **Provider Abstraction**: A unified `DeliveryProviderAdapter` interface encapsulates third-party carriers (Aramex, SMSA, DHL, FedEx) and the platform's proprietary temperature-controlled vehicle fleet.
4. **Health Privacy Protection (HIPAA / GDPR)**: Carrier shipping manifests and package labels are strictly sanitized (`DeliveryCustomActions.sanitizePackageLabel`). Sensitive prescription names, diagnosis codes, and patient records are never leaked on external labels.
5. **Local / Domestic Boundary by Default**: Cross-border international shipping is disabled at launch (`internationalDeliveryEnabled = false`) to satisfy medical import/export compliance.
