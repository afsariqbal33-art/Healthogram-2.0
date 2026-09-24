# HEALTHOGRAM — DATA INVENTORY & MAPPING MATRIX

**Classification:** Internal Compliance & Data Governance  
**Effective Date:** September 16, 2026  
**Data Protection Officer Contact:** privacy@healthogram.app  

---

## 1. Complete Enterprise Data Inventory

| Data Domain | Specific Data Elements | Source | Purpose | Storage Location | Access Controls | Third-Party Sharing | Retention Schedule | Deletion Mechanism | Encryption |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Account Identity** | Full name, email, phone number, hashed password, profile avatar, role type. | User Registration | User authentication, account recovery, session security. | Firebase Auth, `users/{uid}`, `public_profiles/{uid}` | Owner, authenticated users for public info. | None. | Account lifecycle + 30 days grace. | User triggered or support request. | TLS 1.3 in transit, AES-256 at rest. |
| **Health Passport (Core)** | Allergies, chronic conditions, medications, emergency contacts, blood type. | Patient input & verified doctors | Personal emergency health profile, clinical record keeping. | `health_profiles/{uid}`, `health_allergies/`, `health_medications/` | Patient and explicitly authorized healthcare providers. | None without explicit single-use grant. | Active user account duration. | Self-service deletion from vault. | End-to-end encrypted in transit & AES-256 at rest. |
| **Clinical Records** | Lab reports, diagnoses, prescriptions, doctor visit notes, clinical attachments. | Verified Doctors, Clinics, Hospitals, Laboratories | Medical history review, continuity of care. | `health_diagnoses/`, `health_lab_reports/`, `health_vault/{uid}/` | Patient and attending healthcare provider with active grant. | None. | Retained until patient requests purge. | Granular deletion per record or full export purge. | Private storage bucket, signed time-limited URLs. |
| **Audit Logs** | Timestamp, accessor UID, patient UID, grant ID, records accessed. | Server backend (Cloud Functions) | HIPAA/GDPR legal compliance, patient access transparency. | `health_access_logs/{logId}` | Patient (read-only), Compliance Auditor. | None. | 7 years (statutory medical audit requirement). | Immutable; system automated purge after 7 years. | Server-side write-only, AES-256 at rest. |
| **Marketplace Data** | Cart items, shipping address, order history, tracking numbers. | Customer checkout | Order fulfillment, logistics tracking, returns management. | `marketplace_orders/{orderId}` | Buyer, Seller, Delivery Driver, Support Admin. | Shipping carriers (address/name only). | 7 years for commercial audit compliance. | Anonymized upon account deletion; financial records archived. | Encrypted in transit & at rest. |
| **Financial & Ledger** | Transaction amount, seller commission, tax, escrow status, payout ID. | Payment Gateway webhooks | Double-entry accounting, seller payouts, tax reporting. | `payment_transactions/`, `platform_revenue_records/` | Buyer, Seller, Finance Admin, Platform Owner. | Payment processor (Stripe/Bank). | 10 years (statutory financial regulation). | Retained for statutory period; user linkage anonymized. | TLS 1.3, tokenized tokens, zero raw card data. |
| **Communications** | Text messages, voice notes, media attachments. | User chat | Direct patient-doctor & user communication. | `chat_threads/`, `messages/{msgId}` | Chat participants only. | None. | 1 year rolling or user deletion. | Manual delete-for-everyone or delete-for-me. | TLS 1.3 in transit, AES-256 at rest. |
| **Teleconsultation Calls** | WebRTC signaling tokens, call duration, timestamp. | Call initiation | Live audio/video consultation. | Ephemeral Firestore signaling docs | Call participants only. | None. Audio/video streams are peer-to-peer. | Ephemeral (signaling docs deleted on call end). | Immediate deletion upon call termination. | DTLS-SRTP end-to-end encryption. |
| **AI Studio Prompts** | Caption generation prompt, image tags. | User input | Generative creator tool assistance. | AI temporary job cache | Prompt author only. | Google Gemini API (stateless processing). | 24 hours job cache. | Automatic cache expiration. | TLS 1.3, zero PHI forwarding policy. |
| **Device & Telemetry** | OS version, device model, crash stack traces, app version. | Android OS / Crashlytics SDK | Stability diagnostics, bug fixing, session limit enforcement. | Firebase Crashlytics, `users/{uid}/devices/{id}` | User (own devices), App Engineering. | Google Firebase Crashlytics. | 90 days retention window. | Automatic rolling deletion. | HTTPS / TLS 1.3. |

---

## 2. Special Category: Health Passport Isolation

Health data is classified under the highest sensitivity tier (Tier-1 Protected Health Information). It is logically separated from social and marketplace databases, forbidden from being used for advertising or marketing profiling, and excluded from generative AI model ingestion.
