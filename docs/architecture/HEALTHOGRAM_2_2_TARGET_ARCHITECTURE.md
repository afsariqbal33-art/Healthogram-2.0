# Healthogram 2.2 — Target Architecture Specification

**Document:** `docs/architecture/HEALTHOGRAM_2_2_TARGET_ARCHITECTURE.md`  
**System:** Healthogram Platform 2.2  
**Target Release:** Healthogram 2.2.0 (Build 20200)  
**Authority:** Architecture Review Board (ARB), Principal Software Architect, CISO  
**Status:** APPROVED ARCHITECTURAL SPECIFICATION  

---

## 1. Healthogram 2.2 Core Product Principles

Every architectural choice, schema modification, and engineering task in Healthogram 2.2 is governed by fifteen immutable principles:

1. **Security before feature expansion:** Security controls, encryption standards, and zero-trust validations strictly take precedence over new feature deliverables.
2. **Health data remains isolated:** Clinical information within the Health Passport is strictly airgapped from social feeds, marketing algorithms, and commercial marketplace systems.
3. **Evidence before major architecture changes:** Major technical refactors must be justified by reproducible production telemetry, not theoretical design trends.
4. **No unnecessary microservices:** Preserve clean, modular monolith Cloud Functions and client-side architecture; avoid distributed network hops without proven scaling bottlenecks.
5. **No unnecessary database migration:** Retain proven Cloud Firestore multi-region collections; do not migrate databases for novelty.
6. **No unnecessary rewrite:** Evolutionary enhancements build upon verified, high-uptime Step 35 components rather than disruptive whole-code rewrites.
7. **Backward compatibility wherever practical:** New schema revisions must maintain seamless backward compatibility with existing mobile clients and database snapshots.
8. **Country expansion must be controlled:** International deployments follow the mandatory 17-step Country Expansion Framework; no unilateral market activation.
9. **Financial integrity is mandatory:** Double-entry ledger reconciliation, minor-unit integer arithmetic, and strict idempotency govern all marketplace and owner finances.
10. **Healthcare interoperability must remain standards-based:** Adhere strictly to HL7 FHIR Release 4 (R4) normative models and AndroidX Health Connect official SDKs.
11. **AI must remain privacy-controlled:** Generative and assistive AI operate through strict data-redaction adapters with zero training retention and zero autonomous diagnostic authority.
12. **Users must control sensitive permissions:** Patients retain total sovereign control over biometric sensors, camera/microphone access, and clinical record sharing.
13. **Feature rollout must be reversible:** All high-risk capabilities must be fronted by Firebase Remote Config feature flags with instant kill-switch rollback capabilities.
14. **Every major feature requires observability:** Comprehensive structured logging, metrics, error budgets, and alerting rules must accompany any production service.
15. **Every major feature requires QA and security review:** No code reaches production without passing threat modeling, unit testing, and clinical safety verification.

---

## 2. Core Identity & Role Invariants

Healthogram 2.2 enforces an immutable multi-tenant account model:

### A. Primary Account Categories (Strictly 5 Classes)
1. **`Individual`**: General users and patients accessing personal Health Passports, appointments, messaging, social feed, and marketplace shopping.
2. **`Doctor`**: Verified healthcare practitioners conducting consultations, viewing consented patient records, and managing clinical schedules.
3. **`Clinic`**: Outpatient facilities and specialized polyclinics managing multi-practitioner rosters, clinical appointments, and diagnostic requests.
4. **`Hospital`**: Inpatient and tertiary care medical centers integrating institutional FHIR gateways and comprehensive department operations.
5. **`Laboratory`**: Accredited diagnostic centers fulfilling clinical test orders, performing pathology/radiology investigations, and publishing verified reports.

> **CRITICAL INVARIANT:** Pharmacy, Medicine Company, Medicine Distributor, Wholesale Supplier, Medical Equipment Manufacturer, and Medical Equipment Supplier accounts are **STRICTLY PROHIBITED** from existing on the platform.

### B. Marketplace Roles (Strictly 2 Roles)
1. **`Customer`**: Individual purchasing non-pharmaceutical wellness and lifestyle goods.
2. **`Seller`**: Verified merchant offering approved wellness, personal care, and fitness products.

---

## 3. High-Level Subsystems Architecture

```
                                  ┌────────────────────────────────────────┐
                                  │       Healthogram Mobile Client        │
                                  │   (Android 16 / Jetpack Compose M3)    │
                                  │   Room DB Local Cache (SQLCipher)      │
                                  └───────────────────┬────────────────────┘
                                                      │
                                                      │ Google Play Integrity / App Check
                                                      │ mTLS 1.3 / HTTPS
                                                      ▼
                                  ┌────────────────────────────────────────┐
                                  │       Firebase API Cloud Gateway       │
                                  │    Node 20 Modular Cloud Functions     │
                                  └───────┬──────────────┬──────────────┬──┘
                                          │              │              │
                    ┌─────────────────────┘              │              └─────────────────────┐
                    ▼                                    ▼                                    ▼
       ┌─────────────────────────┐          ┌─────────────────────────┐          ┌─────────────────────────┐
       │   Clinical Core Enclave │          │  Commercial & Messaging │          │   External Integrations │
       │  (Health Passport 2.2)  │          │  (Marketplace & Social) │          │  (mTLS Gateways & AI)   │
       ├─────────────────────────┤          ├─────────────────────────┤          ├─────────────────────────┤
       │ • Zero-Trust Consent    │          │ • Double-Entry Ledger   │          │ • HL7 FHIR R4 Gateways  │
       │ • FHIR R4 Engine        │          │ • Order Fulfillment     │          │ • Android Health Connect│
       │ • Ephemeral QR Tokens   │          │ • Encrypted WebRTC Turn │          │ • Vertex AI (Gemini)    │
       │ • AES-256 Storage Blobs │          │ • Social Feed Isolation │          │ • Payment Processors    │
       └─────────────────────────┘          └─────────────────────────┘          └─────────────────────────┘
```

---

## 4. Key Subsystem Specifications

### 4.1. Health Passport 2.2
- **Unified Timeline Engine**: Combines conditions, lab tests, vital signs, medications, and clinical encounters into a chronologically indexed, filtered timeline.
- **Record Deduplication & Provenance**: Enforces deterministic hashing `SHA-256(patientUid + recordType + eventTimestamp + sourceIdentifier)` to guarantee zero duplicate clinical records.
- **Document Intelligence & OCR Pipeline**: Asynchronously extracts text from paper lab reports via server-side OCR with strict human clinician confirmation before ingestion.
- **Emergency Health Information Profile**: Read-only, unencrypted emergency data subset (blood type, critical allergies, emergency contact ICE) accessible via lockscreen widget without unlocking device.
- **Granular Consent Center**: Per-category (Vitals, Lab Tests, Diagnoses, Medications) and per-practitioner grant expiration (15m, 1h, 24h, 30d, permanent) with sub-second revocation propagation.

### 4.2. Android Health Connect 2.2
- **Scoped Telemetry**: Restricted exclusively to `StepsRecord`, `HeartRateRecord`, and `BloodGlucoseRecord`.
- **Sync Architecture**: Background periodic synchronization executed via AndroidX `WorkManager` with a 6-hour battery-conservative cadence and exponential backoff.
- **Absolute Airgap**: Biometric telemetry is encrypted in a private Room database and prohibited from entering marketing, advertising, or social algorithms.

### 4.3. HL7 FHIR 2.2 Interoperability Layer
- **Standard**: Full alignment with HL7 FHIR Release 4 (R4).
- **Supported Resources**: 16 normative models (`Patient`, `Practitioner`, `Organization`, `Encounter`, `Observation`, `Condition`, `AllergyIntolerance`, `Medication`, `MedicationRequest`, `DiagnosticReport`, `Procedure`, `Immunization`, `DocumentReference`, `CarePlan`, `Appointment`, `ServiceRequest`).
- **Bi-directional ServiceRequest**: Automated workflow for diagnostic orders from Clinic -> Laboratory -> Ingestion into Patient Health Passport.

### 4.4. Healthcare Partner Platform
- **Maturity Model (Levels 0–7)**: Formal progression from Application (`Level 0`) through Sandbox (`Level 2`), Security Audit (`Level 3`), Certification (`Level 4`), Pilot (`Level 5`), Full Partner (`Level 6`), to Bi-directional Order Fulfillment (`Level 7`).
- **Partner State Machine**: `APPLIED` -> `UNDER_REVIEW` -> `VERIFIED` -> `ACTIVE` -> `SUSPENDED` -> `REVOKED` -> `EXPIRED`.
- **Security**: Mandatory mutual TLS (mTLS 1.3), OAuth2 token introspection with 1-hour TTL, and immutable transaction audit logging.

### 4.5. Appointments 2.2
- **Atomic Booking Engine**: Firestore atomic transactions execute slot reservation and conflict resolution to guarantee zero double-booking.
- **Timezone Awareness**: All schedule availability stored in UTC with localized clinic offset presentation.
- **Push Notification Privacy**: Appointment reminders contain zero clinical or diagnostic disclosures (e.g. "You have an appointment with Dr. Al-Balushi tomorrow at 10:00 AM").

### 4.6. Marketplace 2.2 & Financial Ledger
- **Scope Restriction**: Only approved wellness, fitness, and lifestyle products are permitted.
- **International Marketplace**: **Permanently disabled** (`FLAG_INTERNATIONAL_MARKETPLACE = false`) until bilateral GCC tax and customs integration is authorized by Owner Control.
- **Immutable Financial Ledger**: All monetary mutations (commissions, payouts, refunds) execute via backend Cloud Functions using integer minor units (Baisa/Cents) with zero client balance modification allowed.

### 4.7. AI Studio 2.2 & Healthcare AI
- **`AIProviderAdapter`**: Abstract interface isolating application logic from underlying LLM/multimodal providers (Vertex AI, Cloud Vision, Gemini 3.8 Flash).
- **Domain Airgap**:
  - `Generic AI Studio`: Content generation, image enhancement, captions for social/marketplace sellers.
  - `Healthcare AI`: Strictly non-diagnostic; provides patient-consented document summarization and translation with prominent clinical disclaimers.

### 4.8. Security & Session Governance
- **Session Concurrency**: Maximum 4 simultaneous active device sessions per user account. Evicts oldest session on 5th device registration.
- **Firebase App Check**: Play Integrity hardware attestation required on all mutating API calls.
- **Hardware-Backed Keystore**: Android Keystore AES-256-GCM encryption for local clinical documents and biometric cache.
