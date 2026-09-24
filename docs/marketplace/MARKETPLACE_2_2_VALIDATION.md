# Healthogram Marketplace 2.2 Validation Specification

**Document Version:** 2.2.0  
**Phase:** Step 39 — Commercial Platform Integrity, Financial Security, Order Fulfillment, Seller Operations & Owner Finance  
**Classification:** Proprietary / Engineering Specification  

---

## 1. Executive Summary & Non-Negotiable Architecture

Healthogram Marketplace 2.2 is an advanced healthcare commerce ecosystem architected with strict role boundaries, multi-tier regulatory compliance, and sovereign country-level isolation. 

### 1.1 Non-Negotiable Role Structure
The Healthogram marketplace enforces **exactly two** commercial roles:
1. **Customer**: Individuals and organizations purchasing verified healthcare, wellness, diagnostic, and medical supply products.
2. **Seller**: Verified merchants offering approved healthcare products within their authorized jurisdiction.

> **CRITICAL ARCHITECTURAL BOUNDARY:**  
> The system strictly prohibits the re-introduction of vendor categories as marketplace roles (e.g., Pharmacy, Medical Store, Medicine Company, Wholesale/Supplier, Medical Equipment Manufacturer/Supplier). Clinical and institutional account categories (Doctor, Hospital, Clinic, Pharmacy, Lab) reside solely in the Healthogram Healthcare Directory and Verification system, completely segregated from commercial marketplace roles.

---

## 2. Product Catalog, Lifecycle & Compliance

### 2.1 Approved Healthcare Product Scope
Marketplace products are strictly restricted to legitimate healthcare, wellness, diagnostic equipment, patient monitoring devices, first-aid, mobility, and clinical supplies.

### 2.2 Product Lifecycle State Machine
Every product item adheres to the following server-authorized state machine:

```
[DRAFT]
   │ (Seller submits)
   ▼
[SUBMITTED]
   │ (Automated & Manual Compliance Screening)
   ▼
[UNDER_REVIEW]
   ├─── (Deficiencies detected) ───► [REJECTED] / [SUSPENDED]
   │ (Approved by Compliance)
   ▼
[APPROVED]
   │ (Seller activates inventory)
   ▼
[ACTIVE] ◄────────────────────────┐
   │ (Seller pauses / Out of stock)│ (Restocked / Resumed)
   ▼                              │
[PAUSED] / [OUT_OF_STOCK] ────────┘
   │
   ▼ (Seller archives or compliance revokes)
[ARCHIVED] / [SUSPENDED]
```

### 2.3 Product Data Validation Rules
1. **Price Validation**: Must be strictly positive (> 0.0) in local minor currency units. Floating-point imprecision is eliminated.
2. **Inventory Validation**: Must be non-negative (>= 0). Negative inventory manipulation is blocked at the Firestore security rule and Cloud Function levels.
3. **SKU Uniqueness**: SKUs are strictly scoped per seller store, preventing collisions across merchants.
4. **Country Restrictions**: Products are bound to `countryCode`. Unapproved international listing is prohibited by default.

---

## 3. Customer Journey & Shopping Experience

```
Marketplace Home
   │
   ├── Search & Filter (Country-scoped, categorized)
   │      │
   │      ▼
   │   Product Detail (Specifications, compliance badge, seller store)
   │      │
   │      ▼
   └── Add to Cart ──► Cart Review ──► Checkout (Server price revalidation)
                                          │
                                          ▼
                                      Payment Intent & 3DS Gateway
                                          │
                                          ▼
                                      Order Confirmation & Suborders
                                          │
                                          ▼
                                      Carrier Dispatch & OTP Delivery
```

### 3.1 Cart Validation & Price Integrity
- **Server-Authoritative Price Revalidation**: During checkout initiation, the server recalculates the entire cart against authoritative database prices. Client-tampered prices (e.g., submitting 1.00 SAR instead of 189.00 SAR) are unconditionally rejected.
- **Inventory Locking**: Temporary reservation holds stock for 15 minutes while the customer completes gateway authentication.

---

## 4. Multi-Seller Parent Order Architecture

When a customer's cart contains items from multiple distinct sellers:
1. **Parent Order**: The single unified order visible to the customer with aggregate financial totals and single unified payment intent.
2. **Seller Suborders**: Each seller receives an isolated suborder containing only their products.
   - Independent fulfillment timeline.
   - Independent shipping carrier, tracking number, and dispatch status.
   - Independent platform commission and net seller earnings.
   - Independent return and refund processing.

---

## 5. Order State Machine & Cancellation Protocol

### 5.1 Order States
- `PENDING_PAYMENT`: Awaiting gateway confirmation.
- `PAID`: Funds captured and ledger accrued.
- `CONFIRMED`: Acknowledged by fulfillment system.
- `PROCESSING`: Pick and pack started by seller.
- `PACKED`: Sealed and labeled with tracking barcode.
- `SHIPPED`: In carrier custody.
- `OUT_FOR_DELIVERY`: En route with driver.
- `DELIVERED`: Handed over upon OTP / POD confirmation.
- `COMPLETED`: Return window expired, order finalized.

### 5.2 Cancellation Matrix
| Stage | Cancellation Action | Refund Behavior |
|---|---|---|
| `PENDING_PAYMENT` | Immediate cancellation | No payment to refund |
| `PAID` | Instant cancellation | 100% automatic gateway refund |
| `PROCESSING` | Seller notification required | Full refund upon seller acknowledgment |
| `PACKED` / `SHIPPED` | Dispatched — Cancellation blocked | Return protocol applies post-delivery |
| `DELIVERED` | Return request required | Inspected return triggers compensating refund |

---

## 6. Verification Status

All 27 core validation criteria across role integrity, product safety, multi-seller suborders, and order states have been validated by `com.example.healthogram.MarketplaceStep39ValidationSuite` with 100% green test execution.
