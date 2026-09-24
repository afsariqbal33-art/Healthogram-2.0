# HEALTHOGRAM 2.2 — ENTERPRISE SECURITY THREAT MODEL & STRIDE ASSESSMENT

**Document Version:** 2.2.0  
**Classification:** Enterprise Threat Modeling Standard  
**Effective Date:** September 20, 2026  
**Methodology:** STRIDE (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege) + DREAD Scoring  
**Owner:** Chief Information Security Architect

---

## 1. System Architecture & Trust Boundaries

The Healthogram platform defines five distinct security boundaries:
1. **Untrusted External Environment:** Public Internet, compromised networks, third-party web clients.
2. **Android Client Boundary:** Protected by Android Keystore, App Check attestation, Play Integrity API, ProGuard obfuscation, and runtime permission sandboxes.
3. **Cloud Edge & Network Gateway:** Cloudflare DDoS mitigation, Firebase Hosting, TLS 1.3 termination, App Check verification.
4. **Cloud Compute & Microservices:** Google Cloud Functions (Gen 2), Cloud Run, strict service-account IAM least privilege.
5. **Data & Storage Perimeter:** Cloud Firestore, Firebase Storage, Cloud Secret Manager, Cloud KMS.

```text
[Untrusted Client / Network]
             │
   (App Check / TLS 1.3)
             ▼
┌────────────────────────────────────────────────┐
│             Firebase Security Perimeter        │
│  ├── App Check & Token Verification            │
│  ├── Firestore Security Rules (93 collections) │
│  └── Storage Security Rules (10 namespaces)    │
└──────────────────────┬─────────────────────────┘
                       │
       (IAM Least Privilege / Service Account)
                       ▼
┌────────────────────────────────────────────────┐
│        Serverless Compute (Cloud Functions)    │
│  ├── Auth Triggers & Webhook Processors        │
│  ├── Health Passport Consent Verification      │
│  ├── Double-Entry Ledger & Commission Engine   │
│  └── AI Studio Moderator & Token Accounting    │
└────────────────────────────────────────────────┘
```

---

## 2. STRIDE Threat Analysis Matrix

| STRIDE Category | Threat ID | Threat Vector & Description | DREAD Score (1-10) | Countermeasures Implemented in Healthogram 2.2 | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | **THR-S01** | Attacker spoofs a verified physician or clinic to gain unauthorized access to patient health data. | **9.2 (High)** | Verification badges cannot be written by clients; verified badge requires multi-document review by compliance reviewers; custom auth claims signed by backend. | **LOW** |
| **Spoofing** | **THR-S02** | Credential stuffing or automated bot brute-forcing user passwords. | **8.4 (High)** | Adaptive rate limiting (5 attempts per minute); mandatory Firebase App Check; re-authentication challenges for sensitive operations. | **LOW** |
| **Tampering** | **THR-T01** | Buyer tampers with product price or cart total in HTTP request payload during checkout. | **9.5 (Critical)** | Client cart prices are untrusted; order creation calculates totals server-side directly from active seller catalog database; Firestore rules prohibit client order updates. | **NEGLIGIBLE** |
| **Tampering** | **THR-T02** | User attempts to self-elevate account role or grant themselves admin/owner privileges. | **9.8 (Critical)** | Firestore rules explicitly forbid `adminRole` and `ownerRole` keys in `users/{uid}` and `public_profiles/{uid}`; admin roles are isolated in server-only `admin_roles/`. | **NEGLIGIBLE** |
| **Repudiation** | **THR-R01** | Doctor denies having accessed or altered a patient's medical records. | **7.5 (Medium)** | Write-once, read-only `health_access_logs` capture attending doctor UID, institution ID, exact scopes accessed, and immutable server timestamp. | **NEGLIGIBLE** |
| **Repudiation** | **THR-R02** | Seller disputes payout disbursement or platform commission deduction. | **8.1 (High)** | Immutable double-entry financial ledger records every transaction, fee breakdown, and bank transfer with cryptographic reference IDs. | **NEGLIGIBLE** |
| **Information Disclosure** | **THR-I01** | Eavesdropper reads sensitive medical diagnostics via insecure QR code transmission. | **9.6 (Critical)** | Health Passport QR code contains zero medical records; encodes only an ephemeral, single-use, 10-minute opaque token; token resolves exclusively over TLS 1.3 with authorized consent. | **NEGLIGIBLE** |
| **Information Disclosure** | **THR-I02** | Android screen capture or task switcher exposing patient prescriptions to spyware. | **8.3 (High)** | All Health Passport and financial view activities set `FLAG_SECURE` in Android Window Manager to block external screen recording and screenshots. | **LOW** |
| **Denial of Service** | **THR-D01** | Malicious client flooding AI Studio or messaging endpoints with excessive requests. | **7.8 (Medium)** | Token bucket rate limiting enforced in `SecurityHardeningEngine` and Cloud Functions; daily AI token quotas tracked server-side in `ai_usage_summary`. | **LOW** |
| **Elevation of Privilege** | **THR-E01** | Insecure Direct Object Reference (IDOR) allowing user to read another user's private vault files. | **9.7 (Critical)** | Storage Security Rules enforce `request.auth.uid == userId` for all private buckets (`health_private/`, `messages_private/`, `ai_private/`). | **NEGLIGIBLE** |

---

## 3. Threat Model Governance

This threat model is reviewed continuously upon any architectural modification or major release candidate milestone. Automated security regression suites validate that zero identified threats regress into unmitigated states.
