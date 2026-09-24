# HEALTHOGRAM — HEALTHCARE MARKETPLACE

## 1. Dual-Role Isolation
Marketplace roles exist completely separately from primary account categories:
- **CUSTOMER**: Can explore, search, filter, purchase, track shipments, and request returns.
- **SELLER**: Can list products, manage inventory, execute promotions, analyze sales, and request payouts.

> **CRITICAL SECURITY GUARANTEE**:
> Seller permissions MUST NEVER equal:
> - Admin permissions
> - Healthcare permissions
> - Health Passport permissions
> - Owner financial permissions
>
> Marketplace has ONLY: **1. Customer** and **2. Seller**.
> Do NOT create: Pharmacy, Medicine Company, Wholesale / Supplier, Medical Equipment Manufacturer roles.
>
> Seller accounts can ONLY be:
> - `individual_seller`
> - `business_seller`

---

## 2. Permitted Product Categories & Compliance
- Medical Equipment & Diagnostic Devices (e.g. BP monitors, pulse oximeters, glucometers)
- Daily Wellness & Nutritional Supplements (vitamins, minerals)
- Personal Care & Hygiene
- Physical Rehabilitation & Mobility (braces, crutches, compression wear)
- First Aid & Emergency Home Supplies
- Connected Health Wearables & Smart Monitors

### Prohibited Medical Claims
Products containing unsubstantiated curative claims are automatically blocked from listing:
- "cures cancer"
- "guaranteed treatment"
- "100% cures disease"
- "miracle cure"
- "cures diabetes"
- "replaces chemotherapy"
- "reverses all aging"

---

## 3. Seller Lifecycle & Verification States
1. `not_started`
2. `draft`
3. `submitted`
4. `under_review`
5. `additional_information_required`
6. `verified`
7. `rejected`
8. `suspended`
9. `expired`
10. `revoked`

Sensitive identity documents (National ID, CR, Tax Certificates) are stored exclusively in private storage paths:
`marketplace_private/{sellerUid}/verification/{documentId}`
and are NEVER exposed via public URLs.

---

## 4. Financial Ledger & Server-Authoritative Balances
- **Append-Oriented Ledger**: `marketplace_seller_ledger/{ledgerId}`
  Records: `sale`, `commission`, `payment_fee`, `refund`, `adjustment`, `tax`, `payout`, `chargeback`.
  Read-only for sellers; strictly server-authoritative creation and updates.
- **Aggregated Balances**: `marketplace_seller_balances/{sellerUid}`
  `gross_sales`, `refunds`, `fees`, `taxes`, `available_balance`.
  Direct client writes are prohibited.

---

## 5. Payout Lifecycle & Rules
1. `requested`
2. `under_review`
3. `approved`
4. `processing`
5. `paid`
6. `failed`
7. `cancelled`

**Guardrails**:
- Payout amount must not exceed `available_balance`.
- Minimum payout threshold: 100 SAR.
- Payout account must be verified.
- Seller must have active, non-suspended status.

---

## 6. Inventory Reservation System
- Available vs. Reserved stock separation.
- Checkouts reserve inventory with atomic Firestore transactions (`reserveInventory`).
- Timeout or cancellation releases reserved stock (`releaseInventory`).
- Prevents overselling and negative inventory values.

---

## 7. Cloud Functions Ecosystem (Step 09)
Located in `/cloud_functions/marketplace_seller.js`:
- `createSellerApplication`
- `submitSellerApplication`
- `validateSellerApplication`
- `approveSeller`
- `rejectSeller`
- `suspendSeller`
- `createSellerProduct`
- `submitProductForReview`
- `approveProduct`
- `rejectProduct`
- `validateProductCompliance`
- `updateInventory`
- `reserveInventory`
- `releaseInventory`
- `calculateSellerCommission`
- `createSellerLedgerEntry`
- `updateSellerBalance`
- `createSellerPayoutRequest`
- `validateSellerPayout`
- `processSellerPayout`
- `handleSellerPayoutWebhook`
- `processSellerReturn`
- `processSellerRefund`
- `generateSellerInvoice`
- `generateSellerReport`
- `updateSellerAnalytics`
- `sendSellerNotification`

