# HEALTHOGRAM — DATA CLASSIFICATION & HANDLING STANDARD

**Document Version:** 2.2.0  
**Classification:** Enterprise Security & Privacy Policy  
**Effective Date:** September 20, 2026  
**Audience:** All Engineering, Product, Security, Compliance, and Operations Personnel  
**Owner:** Chief Information Security Architect & Data Protection Officer (privacy@healthogram.app)

---

## 1. Executive Summary & Objective

Healthogram operates a dual-paradigm architecture providing high-throughput social/creator engagement alongside strictly regulated clinical digital health records (Health Passport), multi-party communications, verified healthcare operations, and commercial marketplace transactions. 

This Data Classification Standard establishes mandatory data sensitivity classifications, permissible handling procedures, isolation boundaries, encryption standards, and destruction protocols across the entire lifecycle of all data ingested, processed, or persisted by Healthogram.

---

## 2. Classification Hierarchy & Sensitivity Tiers

Healthogram defines six distinct classification levels. Every Firestore collection, Storage bucket directory, memory buffer, cache entry, and API payload is mapped directly to exactly one sensitivity tier.

```
┌─────────────────────────────────────────────────────────────────┐
│ Tier 6: CRITICAL SECURITY & CRYPTOGRAPHIC SECRETS              │
├─────────────────────────────────────────────────────────────────┤
│ Tier 5: RESTRICTED PROTECTED HEALTH INFORMATION (PHI / E-PHI)   │
├─────────────────────────────────────────────────────────────────┤
│ Tier 4: HIGHLY CONFIDENTIAL FINANCIAL & LEGAL DATA             │
├─────────────────────────────────────────────────────────────────┤
│ Tier 3: CONFIDENTIAL PERSONAL IDENTITY & SENSITIVE USER DATA    │
├─────────────────────────────────────────────────────────────────┤
│ Tier 2: INTERNAL OPERATIONAL & SYSTEM TELEMETRY                 │
├─────────────────────────────────────────────────────────────────┤
│ Tier 1: PUBLIC & SOCIAL CONTENT                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Detailed Data Classification Matrix

| Classification Tier | Description & Scope | Concrete Data Types & Field Examples | Firestore / Storage Locations | Access Control Standard | In-Transit Encryption | At-Rest Encryption | Secondary Use / AI Ingestion |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Tier 6: CRITICAL** | Cryptographic private keys, root secrets, HMAC signing keys, webhook secret tokens, owner bypass credentials. | Firebase service account credentials, KMS root keys, Stripe webhook signing secrets, JWT private keys, owner emergency recovery phrase. | Cloud Secret Manager, Hardware Keystore, Never stored in client code or Firestore. | Zero client access. Cloud Functions execution identity only via IAM least privilege. | TLS 1.3 with Perfect Forward Secrecy (PFS). | Hardware HSM / Cloud KMS AES-256 GCM. | **STRICTLY PROHIBITED.** |
| **Tier 5: RESTRICTED (PHI)** | Protected Health Information, medical records, diagnostic logs, clinical encounters, lab results, prescriptions. | ICD-10 conditions, drug names, dosage schedules, allergies, blood type, physician clinical notes, lab test values, diagnostic PDFs, DICOM scans, single-use QR tokens. | `health_profiles/`, `health_conditions/`, `health_allergies/`, `health_medications/`, `health_visits/`, `health_diagnoses/`, `health_tests/`, `health_lab_reports/`, `health_prescriptions/`, `health_documents/`, `health_notes/`, `health_private/{uid}/**`. | Patient owner exclusive read/write. Attending verified clinicians require patient-approved, time-limited, scoped cryptographic access grants. | TLS 1.3 enforced. Client-side field level encryption prior to persistence. | AES-256-GCM + Customer Managed Key envelope encryption. | **STRICTLY PROHIBITED.** Zero AI training, zero ad targeting, zero cross-service aggregation. |
| **Tier 4: HIGHLY CONFIDENTIAL** | Financial ledgers, payment transactions, bank accounts, payout IBANs, tax IDs, double-entry ledgers, chargebacks. | Masked IBAN/SWIFT, tax identifiers, seller payout schedules, escrow holds, revenue shares, payment intents, chargeback claims. | `financial_ledger_entries/`, `marketplace_seller_balances/`, `marketplace_seller_payout_accounts/`, `owner_financial_accounts/`, `payment_transactions/`, `financial_private/{reportId}/**`. | Account owner, server webhook listeners, Platform Owner, certified Financial Auditor role. | TLS 1.3 mandatory. | AES-256 + PCI-DSS compliant vaulting. Zero raw card storage. | **STRICTLY PROHIBITED.** Only aggregated anonymized financial reporting allowed. |
| **Tier 3: CONFIDENTIAL** | Personal Identifiable Information (PII), verification documents, private messages, user safety blocklists. | National IDs, medical licenses, passport scans, private delivery street addresses, 1:1 chat messages, voice transcripts, user block records. | `private_profiles/`, `verifications/`, `verification_documents/`, `marketplace_addresses/`, `messages/`, `voice_transcripts/`, `verification_private/{uid}/**`, `messages_private/{convId}/**`. | User owner, authenticated conversation participants, vetted verification review staff. | TLS 1.3 mandatory; DTLS-SRTP for WebRTC media streams. | AES-256 at rest in Firestore and Cloud Storage. | User-consented features only (e.g. real-time speech translation). Never retained for model training. |
| **Tier 2: INTERNAL** | Operational logs, audit trails, device registries, rate limit counters, system telemetry, performance counters. | Access timestamps, device fingerprints, IP references, App Check attestations, HTTP status codes, trace metrics, feature flag evaluations. | `security_audit_logs/`, `health_access_logs/`, `admin_audit_logs/`, `user_devices/`, `login_security_events/`, `data_integrity_reports/`. | Security Operations, Auditing Roles, automated monitoring bots. Client read-only for own records. | TLS 1.3. | AES-256 at rest. | Operational anomaly detection and fraud prevention only. |
| **Tier 1: PUBLIC** | Public social profiles, published social posts, public reels, stories, marketplace product listings, catalog prices. | Display names, handles, post captions, public videos, product titles, descriptions, catalog prices, seller badges. | `public_profiles/`, `posts/`, `reels/`, `stories/`, `marketplace_products/`, `public_social/**`, `public_marketplace/**`. | Public / Authenticated read. Author-only update/delete. | TLS 1.3. | AES-256 at rest. | Permitted for creator discovery, search indexing, and AI Studio post caption generation. |

---

## 4. Architectural Isolation Boundaries

Healthogram implements strict logical and network isolation separating Tier 5 (Health Passport) from all Tier 1 (Social) and Tier 4 (Marketplace) systems:

1. **Database-Level Isolation:**
   - Health Passport collections (`health_*`) exist strictly in dedicated Firestore namespaces.
   - Social posts and comments (`posts/*`, `reels/*`) are structurally prevented via Firestore Security Rules from referencing or embedding medical identifiers (`healthRecordId`, `biometricId`, `prescriptionId`).
   - Marketplace collections (`marketplace_*`) have zero read access to `health_*` collections.
2. **Storage Bucket Segregation:**
   - Medical assets are isolated in `health_private/{patientUid}/**` with zero public URL generation.
   - Verification documents reside in `verification_private/{userId}/**` with restricted reviewer roles.
   - Public social and marketplace assets reside in separate namespaces with strict MIME and size checks.
3. **QR Code Security Model:**
   - QR codes generated by the client contain exclusively opaque, single-use, cryptographically random session tokens (`opaqueToken`).
   - QR codes **NEVER** contain raw clinical data, medical conditions, allergies, or patient demographics.
   - Scanned QR tokens require server-side exchange through verified healthcare provider credentials with active biometric authentication.

---

## 5. Handling & Sanitization Rules

1. **Logging Hygiene:**
   - Application and server logs must **NEVER** log Tier 5 (PHI), Tier 6 (Secrets), or Tier 4 (Financial credentials).
   - In logging statements, patient UIDs are replaced with cryptographic hashes (HMAC-SHA256) and medical identifiers are redacted.
2. **Crash Reporting:**
   - Firebase Crashlytics custom keys are strictly prohibited from storing medical record contents or user address fields.
3. **Screen Capture & Android Leak Prevention:**
   - Screens rendering Tier 5 Health Passport or Tier 4 Financial Balances enforce `FLAG_SECURE` in Android Window Manager to block screenshots and task switcher previews.
4. **Data Minimization:**
   - APIs returning health records must return only the specific fields requested by the authorized scope (e.g. `conditions` scope returns only conditions, never medications).

---

## 6. Audit & Enforcement

Failure to adhere to this classification standard constitutes a critical security incident. The Platform Compliance Engine runs continuous automated scans verifying Firestore rules and Storage rules against this matrix.
