# HEALTHOGRAM 2.3: MARKETPLACE, PAYMENTS, FINANCIALS & LOGISTICS ARCHITECTURE

**Document ID:** HGM-2.3-ARCH-04-COMMERCE  
**Phase:** Step 48 Architecture & Planning  
**Target Release:** Healthogram Version `2.3.0`  
**Git Branch:** `develop/healthogram-2-3`  
**Timestamp:** 2026-09-22T06:30:00Z  
**Governing Standards:** PCI-DSS v4.0 Level 1, ISO 20022 Financial Messaging, Good Distribution Practice (GDP) for Medical Products  

---

## 1. Marketplace 2.3 Subsystem Blueprint

Healthogram Marketplace operates exclusively as a specialized domestic healthcare and wellness marketplace.

### A. Role Boundaries & Strict Separation
* **Platform Account Category:** Stays strictly restricted to the 5 primary healthcare categories (`Individual`, `Doctor`, `Clinic`, `Hospital`, `Laboratory`).
* **Marketplace Role:** Decoupled into `Customer` and `Seller`.
* **Seller Classifications:**
  * `Individual Seller`: Licensed independent pharmacists or certified medical practitioners offering clinical products.
  * `Business Seller`: Licensed pharmaceutical distributors, verified medical supply corporations, or healthcare equipment vendors.
* **Verification Mandate:** Sellers must submit verifiable business registration and pharmaceutical trade licenses before publishing products.

### B. Domestic Marketplace Boundary (`international_marketplace_enabled = false`)
* **Policy:** Cross-border sales remain **100% disabled**.
* **Enforcement:** Cart and checkout pipelines enforce `buyerCountry == sellerCountry == deliveryCountry`. Any cross-border cart combination is blocked at the data validation layer.
* **Independent Domestic Markets:** Supported in `US`, `CA`, `GB`, `SA`, `AE`, `EG`, `IN`.

### C. Healthcare Product Categorization & Catalog Controls
Products are strictly restricted to healthcare taxonomies:
1. `PHARMACEUTICALS` (Prescription drugs requiring signed e-prescription; Over-the-counter wellness)
2. `MEDICAL_DEVICES` (Glucometers, blood pressure monitors, nebulizers, pulse oximeters)
3. `ORTHOPEDIC_SUPPORTS` (Wheelchairs, braces, crutches, post-surgical mobility aids)
4. `CLINICAL_CONSUMABLES` (Syringes, bandages, surgical gloves, antiseptics)
5. `NUTRITION_SUPPLEMENTS` (Vitamins, minerals, clinical nutrition formulas)

*Automated Ingestion Gate:* Any product listing submitted outside these approved healthcare categories is automatically rejected by the listing validation pipeline.

---

## 2. Country-Wise Parameterized Commerce Architecture

Rather than hardcoded country logic, Version 2.3 models regional commerce rules via a dynamic configuration registry:

```json
{
  "countryCode": "SA",
  "currency": "SAR",
  "minorUnits": 2,
  "vatRate": 0.15,
  "paymentGateways": ["HYPERPAY", "APPLE_PAY", "MADA"],
  "deliveryPartners": ["ARAMEX", "SMSA"],
  "prescriptionUploadMandatory": true,
  "maxDeliveryWindowHours": 48,
  "temperatureMonitoringRequired": true
}
```

---

## 3. Payment Architecture 2.3 & Unified Gateway Abstraction

Version 2.3 decouples payment providers behind a resilient, unified payment adapter service:

```
[CUSTOMER CHECKOUT]
        │
        ▼
[HEALTHOGRAM PAYMENT SERVICE] ── (Idempotency Key & Nonce Validation)
        │
        ├─► [STRIPE ADAPTER]     (US, CA, GB)
        ├─► [HYPERPAY ADAPTER]   (SA, AE, EG - mada/Benefit/KNET)
        ├─► [RAZORPAY ADAPTER]   (IN - UPI/NetBanking)
        ├─► [GOOGLE PAY ADAPTER] (Android Native Tokenization)
        └─► [APPLE PAY ADAPTER]  (iOS Native Tokenization)
        │
        ▼
[SERVER-SIDE WEBHOOK DISPATCHER] ── (HMAC Signature Verification)
        │
        ▼
[DOUBLE-ENTRY FINANCIAL LEDGER] ── (Balanced Journal Postings)
        │
        ▼
[ORDER COMMIT & SELLER ESCROW]
```

### Core Financial Safeguards
1. **End-to-End Idempotency:** Checkout and webhook operations require unique client-generated UUID v4 idempotency keys. Duplicate submissions return the cached transaction receipt without re-charging.
2. **One-Tap Google Pay & Apple Pay Integration:** Implements native wallet sheets, reducing checkout friction while maintaining 3D-Secure banking authentication.
3. **Refund & Chargeback Isolation:** Automated dispute hooks immediately hold the disputed transaction balance in an escrow liability account, preventing platform balance leakage.

---

## 4. Owner Earnings & Double-Entry Ledger Verification

Platform earnings and commission remittances are backed by an immutable double-entry journal complying with GAAP.

### Double-Entry Invariant
Every transaction records equal debit and credit journal entries:
$$\sum \text{Debits} - \sum \text{Credits} = \$0.00$$

### Financial Transaction Lifecycle
$$\text{Customer Charge} \longrightarrow \text{Gateway Deposit} \longrightarrow \text{Platform Fee (Revenue)} + \text{Seller Escrow (Liability)}$$
$$\text{Delivery Confirmed} \longrightarrow \text{Seller Escrow Released} \longrightarrow \text{Seller Payable} \longrightarrow \text{Bank Payout}$$

* **Traceability:** Every ledger entry links directly to its source `orderId`, `paymentIntentId`, and external gateway transfer reference.
* **Audit Trail:** Nightly automated reconciliation workers compare gateway merchant balances against platform ledger balances. Any variance $> \$0.00$ triggers a P1 financial alert.

---

## 5. Logistics & Delivery 2.3 Subsystem (Adapter Architecture)

Delivery ensures the physical integrity, timeliness, and regulatory custody transfer of clinical goods.

### A. Provider Adapter Architecture
Logistics providers are integrated via standardized service interfaces (`DeliveryPartnerAdapter`):
* `FedExAdapter` (US, CA)
* `AramexAdapter` (SA, AE, EG)
* `DelhiveryAdapter` (IN)
* `RoyalMailAdapter` (GB)

### B. Cold-Chain Temperature Sensor Telemetry
* Temperature-sensitive items (insulin, biologics, vaccines) are flagged with `temperatureMonitored: true`.
* Participating courier fleets record ambient temperatures at departure, transit hubs, and handover.
* If recorded transit temperature exceeds the allowed threshold ($2^\circ\text{C} - 8^\circ\text{C}$), the shipment is automatically flagged for clinical inspection prior to customer delivery.

### C. Secure Two-Factor Delivery Handover (OTP)
* Recipient generates a secure 4-digit Delivery OTP inside their app upon courier arrival.
* Courier inputs the OTP into their mobile terminal; cryptographic verification commits the status transition to `DELIVERED` and releases seller escrow funds.
* Contactless drop-offs are strictly forbidden for scheduled medications.
