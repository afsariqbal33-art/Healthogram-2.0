# Healthogram Marketplace & Financial Security Matrix

**Document Version:** 2.2.0  
**Phase:** Step 39 — Commercial Security, Threat Mitigation & Threat Matrix  
**Classification:** Proprietary / Engineering Specification  

---

## 1. Threat Modeling & Mitigation Matrix

| Threat ID | Threat Vector | Risk Level | Mitigation Strategy | Enforcement Mechanism |
|---|---|---|---|---|
| **SEC-01** | **Client Price Tampering** (Modifying price in request payload) | **CRITICAL (P0)** | Authoritative server price revalidation; client prices ignored during checkout. | Backend recalculates cart subtotal strictly from product database snapshot. |
| **SEC-02** | **Negative Inventory Exploit** (Purchasing more stock than available) | **CRITICAL (P0)** | Atomic stock reservation and decrement via database transactions. | Firestore transaction verifies $stock \ge requested$ before decrements. |
| **SEC-03** | **Unauthorized Role Elevation** (Customer or Seller assuming Admin or Doctor) | **CRITICAL (P0)** | Immutable token claims; strict custom claim verification in Cloud Functions. | Role checking in security rules; clinical and marketplace roles strictly partitioned. |
| **SEC-04** | **Cross-Seller Resource Access** (Seller A viewing/updating Seller B's data) | **HIGH (P1)** | Multi-tenant tenant ID filtering on all queries; Firestore security rules. | `request.auth.uid == resource.data.sellerUid`. |
| **SEC-05** | **Webhook Forgery / Replay** (Fake payment confirmation webhooks) | **CRITICAL (P0)** | HMAC-SHA256 signature verification; idempotency cache of processed webhook IDs. | `verifyHmac(signature, secret)`; rejected if hash mismatch or ID already seen. |
| **SEC-06** | **Double-Click Payment Race** (Concurrent checkout triggering 2 charges) | **HIGH (P1)** | Unique idempotency key (UUIDv4) sent with checkout intent; backend mutex. | Idempotency registry rejects duplicate keys within 24-hour TTL. |
| **SEC-07** | **Direct Balance Mutation** (Client updating owner or seller balance) | **CRITICAL (P0)** | Balances are computed server-side from immutable ledger entries only. | Firestore rules block all client `write` operations on balances and ledgers. |
| **SEC-08** | **Patient Clinical Data Leakage** (Exposing diagnosis/prescriptions to sellers) | **CRITICAL (P0)** | Data minimization on order items; suborders receive only commercial SKUs and shipping address. | Healthcare records, prescriptions, and Health Passport isolated from marketplace collections. |
| **SEC-09** | **Unauthorized Seller Payouts** (Triggering payout without review or verification) | **HIGH (P1)** | Seller verification status check; mandatory reserve hold period (7 days). | Cloud Function enforces `status == VERIFIED` and clears funds only after hold window. |
| **SEC-10** | **Owner Account Takeover Draining** (Malicious withdrawal of platform revenue) | **CRITICAL (P0)** | Mandatory 2FA/biometric re-auth; withdrawal rate limits; dual-approval on large sums. | Server verifies time-based OTP and enforces velocity and threshold limits. |
| **SEC-11** | **Review Fraud / Astroturfing** (Fake reviews by non-purchasers) | **MEDIUM (P2)** | Verified purchase gatekeeper; customer must have delivered order containing item. | Backend query verifies order state == `DELIVERED` before allowing review submission. |
| **SEC-12** | **International Regulatory Breach** (Cross-border selling without sovereign approval) | **HIGH (P1)** | International marketplace disabled by default; country-level feature flags. | `countryCode` matching between seller, customer, and catalog; international flag OFF. |

---

## 2. Cryptographic Security Standards

1. **Transport Layer**: Strict TLS 1.3 encryption for all mobile-to-cloud communications.
2. **Payload Signatures**: Webhook signatures computed using SHA256 HMAC with rotating shared secrets stored in Cloud Secret Manager.
3. **Data at Rest**: AES-256 encryption on all database storage, bucket volumes, and document archives.
4. **Tokenization**: Card credentials tokenized via PCI-DSS certified gateway before hitting network endpoints.
