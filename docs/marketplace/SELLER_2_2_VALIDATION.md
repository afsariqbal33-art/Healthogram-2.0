# Healthogram Seller 2.2 Operations & Compliance Validation

**Document Version:** 2.2.0  
**Phase:** Step 39 — Seller Operations, Identity Verification & Isolation  
**Classification:** Proprietary / Engineering Specification  

---

## 1. Seller Onboarding Architecture

Healthogram Seller Operations provide verified healthcare merchants and individual medical practitioners with an isolated, compliant commerce interface.

### 1.1 Seller Types
The platform supports **two distinct seller classifications**:
1. **Individual Seller**:
   - Licensed individual practitioners, certified wellness providers, or verified independent distributors.
   - Requirements: National ID / Resident Identity verification, official practitioner/specialist credential check, verified mobile number and bank IBAN.
   - Strict prohibition of anonymous sales.
2. **Business Seller**:
   - Registered commercial entities, medical equipment manufacturers, and corporate healthcare suppliers.
   - Requirements: Commercial Registration (CR), Tax / VAT identification certificates, Medical Device Facility Licenses, authorized signatory verification.

---

## 2. Seller Verification State Machine

```
[DRAFT]
   │ (Seller submits documents)
   ▼
[SUBMITTED]
   │ (Automated preliminary parsing & OCR)
   ▼
[UNDER_REVIEW]
   ├─── (Information unclear / expired) ───► [ADDITIONAL_INFORMATION_REQUIRED]
   │                                                 │
   │                                                 ▼ (Resubmitted)
   │◄────────────────────────────────────────────────┘
   │ (Compliance decision)
   ├───► [REJECTED] (With explicit regulatory reason code)
   ▼
[VERIFIED]
   │ (Breach of policy / Suspicious activity)
   ▼
[SUSPENDED] / [REVOKED]
```

### 2.1 State Authorization Matrix
- Transitions from `SUBMITTED` to `VERIFIED`, `REJECTED`, or `SUSPENDED` are **strictly server-side** (Cloud Functions / authorized compliance admin endpoints).
- Clients cannot self-elevate or mutate their verification status in Firestore.

---

## 3. Document Security & Storage Isolation

1. **Storage Path Protection**:
   - Seller compliance documents are stored strictly under private Google Cloud Storage buckets:
     `marketplace_private/{sellerUid}/verification/{documentId}.pdf`
   - **Zero Public Access**: Documents are never given public read permissions or public HTTP download URLs.
2. **Access Control**:
   - Accessible only by the owning seller via signed temporary download URLs (15-minute expiration) and authorized compliance officers with role `COMPLIANCE_REVIEWER`.
3. **Data Retention & Audit**:
   - All document uploads, viewing events, and verification decisions generate append-only audit log records in `marketplace_seller_verification_audits`.

---

## 4. Cross-Seller Isolation & Patient Privacy

### 4.1 Strict Multi-Tenant Separation
- **Resource Ownership**: Every seller profile, product, inventory movement, order item, and payout request is tagged with `sellerUid`.
- **Security Rule Enforcement**: Firestore rules prohibit reading or writing across seller boundaries (`request.auth.uid == resource.data.sellerUid`).
- **Catalog Independence**: Seller A cannot view, modify, or infer the sales volume, inventory levels, or price structures of Seller B.

### 4.2 Absolute Health Passport Isolation
- **Data Minimization in Fulfillment**:
  - The Seller Order View (`SellerOrderView`) includes only recipient name, delivery city, delivery address, ordered items, and line totals.
  - **Zero Clinical Data**: Sellers and delivery personnel have zero access to customer medical history, lab results, prescriptions, diagnoses, or Health Passport QR data.
  - Medical information isolation is validated by comprehensive security tests in `SellerSecurityAttackTest` and `MarketplaceStep39ValidationSuite`.

---

## 5. Seller Payouts & Reserves

1. **Balances**:
   - `Pending Balance`: Funds from newly placed or delivered orders held during the return guarantee window (default 7 days).
   - `Available Balance`: Cleared funds ready for bank transfer payout.
   - `Reserved Balance`: Security deposit held against disputes, chargebacks, or active returns.
2. **Payout Guardrails**:
   - Payouts require `VERIFIED` seller status.
   - Minimum payout threshold: 100.00 SAR.
   - Server validates available balance before creating payout batch.
