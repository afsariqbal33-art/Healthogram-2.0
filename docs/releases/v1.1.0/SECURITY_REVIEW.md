# HEALTHOGRAM — VERSION 1.1.0 PRE-FLIGHT SECURITY REVIEW

**Classification:** Application Security Architecture & Threat Analysis  
**Milestone:** v1.1.0  
**Status:** APPROVED FOR STAGE 1 DEVELOPMENT  

---

## 1. Security Scope of Planned v1.1 Changes

The planned enhancements for Version 1.1 have undergone formal threat modeling:

| Feature Addition | Security Threat Identified | Required Mitigation / Invariant |
| :--- | :--- | :--- |
| **Biometric Auth for QR** | Biometric bypass or spoofing on compromised/rooted devices. | Uses Android Keystore hardware-backed keys (`BIOMETRIC_STRONG`); requires user authentication for cryptographic key usage. |
| **Seller Coupons** | Coupon abuse, negative cart totals, discount stacking exploitation. | Discount calculated server-side in Cloud Function; minimum cart total validation; coupon expiration validated against atomic transaction timestamp. |
| **Creator Analytics v2** | Exposure of private follower activity, tracking leaks. | Aggregated, k-anonymized metrics; queries filter out individual user UIDs; no exportable tracking identifiers. |
| **Offline Drafts** | Storage of unencrypted drafts on shared/rooted storage. | Local Room database encrypted with SQLCipher via Android Keystore master key; drafts isolated per user UID. |
| **Tablet Multi-Pane** | Accidental shoulder surfing or screen capture of medical records. | Applies `FLAG_SECURE` to Health Passport window panes when backgrounded or exposed in recents task switcher. |

---

## 2. Invariant Rules Retained

- **Health Passport Air-Gap:** Patient medical records remain strictly excluded from any generative AI or third-party analytics pipelines.
- **Ledger Invariant:** Gross transaction totals must balance to zero discrepancy after applying seller promotional discounts.
- **Role Isolation:** Zero expansion of account types. Prohibited categories (pharmacy, medicine broker, manufacturer) remain permanently excluded.
