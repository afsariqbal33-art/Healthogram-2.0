# Healthogram 2.2 — Core Implementation Baseline & Matrix

**Document:** `docs/engineering/HEALTHOGRAM_2_2_IMPLEMENTATION_BASELINE.md`  
**Application Version:** 2.2.0-alpha (Build 20200)  
**Previous Stable Release:** 2.1.0-stable (Build 20100)  
**Source Revision / Branch:** `feature/healthogram-2-2-core-implementation` (Target: `release/2.2.0`)  
**Authority:** Principal Software Architect, Full-Stack Lead & QA Lead  
**Classification:** AUTHORITATIVE ENGINEERING BASELINE  

---

## 1. Environment & Build Configuration Baseline

- **Android SDK Configuration**:
  - `compileSdk`: 36 (minorApiLevel 1)
  - `targetSdk`: 36 (Android 16 / Play 2026 Target Compliance)
  - `minSdk`: 24 (Android 7.0 Nougat)
  - `versionCode`: 20200
  - `versionName`: 2.2.0-alpha
- **Firebase Environment**:
  - Project ID: `healthogram-prod` / Multi-Region `eur3` (Frankfurt)
  - Firebase Authentication: Identity Platform with Custom Claims & Multi-Factor Auth
  - Cloud Firestore: Multi-Region High-Availability with 168 (consolidating to 142) composite indexes
  - Cloud Functions: Node.js 20 Serverless Runtime
  - App Check: Play Integrity API enforcement (Hardware Device Attestation)
  - Cloud Storage: Private CMEK-encrypted buckets with 15-minute Signed URLs
- **Active Feature Flags Baseline**:
  - `FLAG_HEALTH_PASSPORT_V2`: `true` (100% rollout)
  - `FLAG_HEALTH_PASSPORT_2_1`: `true` (100% rollout)
  - `FLAG_HEALTH_PASSPORT_2_2`: `false` (Staged 2.2 Canary)
  - `FLAG_FHIR_2_2`: `false` (Staged Canary)
  - `FLAG_HEALTH_CONNECT_2_2`: `false` (Staged Canary)
  - `FLAG_APPOINTMENTS_2_2`: `false` (Staged Canary)
  - `FLAG_MARKETPLACE_2_2`: `false` (Staged Canary)
  - `FLAG_AI_2_2`: `false` (Staged Canary)
  - `FLAG_TRANSLATION_2_2`: `false` (Staged Canary)
  - `FLAG_INTERNATIONAL_MARKETPLACE`: **`false` (PERMANENTLY DISABLED in v2.2)**
- **Active External Providers**:
  - Identity & Security: Firebase Auth, Google Play Integrity, Cloud KMS
  - Payments: Stripe (Global), Thawani (Oman), PayTabs (Saudi Arabia)
  - Wearables & Health: AndroidX Health Connect Client SDK 1.1.0-alpha10
  - AI & ML: Google Cloud Vertex AI (Gemini 3.8 Flash), Cloud Document AI
  - Real-Time Communication: WebRTC Peer-to-Peer with Google Cloud TURN relay fallback

---

## 2. Technical Findings Summary (Step 36 & Step 35 Inputs)

1. **Security & Session Finding**: Session eviction currently executes on-demand during login. Requires scheduled daily cloud sweep for inactive sessions (> 90 days) and re-authentication for sensitive account mutations (DEBT-05).
2. **Health Connect Battery Finding**: Periodic WorkManager sync killed by OEM battery killers (Xiaomi/Huawei). Requires expedited requests, opportunistic foreground sync, and OEM guide (DEBT-01).
3. **Database Index Quota Finding**: 168 active composite indexes approaching 200 index quota. Needs consolidation of marketplace and appointment query indexes down to 142 (DEBT-02).
4. **FHIR Memory Spike Finding**: Large medical histories (> 200 records) buffer entire JSON bundle in function RAM. Needs NDJSON streaming directly to GCS (DEBT-03).
5. **UI Recomposition Finding**: Complex Health Passport timeline redrawing offscreen items. Needs Compose `@Immutable` model annotations and Canvas trend charts (DEBT-04).
6. **Financial Ledger Integrity**: Double-entry ledger verified at 0.00 OMR drift; requires maintaining minor-unit integer arithmetic and webhook HMAC-SHA256 idempotency.

---

## 3. Comprehensive Implementation Matrix

| ID | Requirement | Current State | Action | Priority | Dependency | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-001** | Max 4 Device Sessions & Eviction | Partially Implemented (Login-time only) | Implement `purgeZombieSessions` daily cron & `logoutAllDevices` | Priority 0 | Firebase Auth | `PARTIALLY IMPLEMENTED` |
| **REQ-002** | Re-Auth for Sensitive Operations | Not Implemented | Enforce re-auth token for withdrawals, security settings, account deletion | Priority 0 | REQ-001 | `NOT IMPLEMENTED` |
| **REQ-003** | Sensitive File Upload Restrictions | Partially Implemented | Validate MIME types, 10MB limit, and private storage isolation | Priority 0 | Cloud Storage | `PARTIALLY IMPLEMENTED` |
| **REQ-004** | Health Passport Data Separation | Implemented | Maintain separate collections (`conditions`, `allergies`, `visits`, etc.) | Priority 1 | Firestore | `IMPLEMENTED` |
| **REQ-005** | Health Passport Canvas Trend Charts | Not Implemented | Implement Jetpack Compose native Canvas vector chart engine | Priority 1 | REQ-004 | `NOT IMPLEMENTED` |
| **REQ-006** | Paper Prescription OCR Staging | Partially Implemented (Client draft only) | Create server-side Document AI staging draft & clinician signoff | Priority 1 | Cloud Storage | `PARTIALLY IMPLEMENTED` |
| **REQ-007** | Emergency Health Profile (ICE) | Implemented (In-app only) | Add lockscreen/tile emergency access protocol without full decrypt | Priority 1 | Keystore | `PARTIALLY IMPLEMENTED` |
| **REQ-008** | FHIR R4 ServiceRequest Lab Pipeline | Not Implemented | Implement bi-directional ServiceRequest order & DiagnosticReport ingest | Priority 1 | FHIR Engine | `NOT IMPLEMENTED` |
| **REQ-009** | Health Connect OEM Battery Resilience | Outdated (DEBT-01) | Add expedited WorkManager, opportunistic sync, and OEM guide | Priority 2 | WorkManager | `OUTDATED` |
| **REQ-010** | Firestore Composite Index Consolidation | Outdated (DEBT-02, 168 indexes) | Consolidate composite indexes down to 142 in `firestore.indexes.json` | Priority 2 | Firestore | `OUTDATED` |
| **REQ-011** | NDJSON Streaming for Large FHIR History | Outdated (DEBT-03) | Stream large FHIR exports directly to Cloud Storage buckets | Priority 2 | Cloud Storage | `OUTDATED` |
| **REQ-012** | Double-Entry Financial Ledger Engine | Implemented | Verify atomic transactions, integer minor units, and 0 client balance edits | Priority 3 | Payments | `IMPLEMENTED` |
| **REQ-013** | Payment Webhook Idempotency & HMAC | Implemented | Verify HMAC-SHA256 signature and duplicate event suppression | Priority 3 | Stripe / Thawani | `IMPLEMENTED` |
| **REQ-014** | Atomic Appointment Booking & Rescheduling| Implemented | Verify 0 double-booking Firestore transactions & UTC storage | Priority 3 | Firestore | `IMPLEMENTED` |
| **REQ-015** | Generic Push Notification Payload Invariant| Implemented | Verify push alerts carry zero clinical or diagnostic disclosures | Priority 3 | FCM | `IMPLEMENTED` |
| **REQ-016** | Appointment Calendar Export (.ics) | Not Implemented | Add native Android Calendar Provider intent & .ics generator | Priority 4 | Appointments | `NOT IMPLEMENTED` |
| **REQ-017** | Customer Multi-Address Delivery Book | Partially Implemented (Single address) | Implement user subcollection `/shipping_addresses` with default flag | Priority 4 | Marketplace | `PARTIALLY IMPLEMENTED` |
| **REQ-018** | AIProviderAdapter Abstraction | Implemented | Maintain interface isolating Vertex AI and enforce token accounting | Priority 4 | Vertex AI | `IMPLEMENTED` |
| **REQ-019** | On-Device ML Kit Translation Fallback | Not Implemented | Implement offline emergency clinical phrase translation | Priority 4 | ML Kit | `NOT IMPLEMENTED` |
| **REQ-020** | International Marketplace Isolation | Implemented (Disabled) | Retain `FLAG_INTERNATIONAL_MARKETPLACE = false` permanently | Priority 0 | Feature Flags | `IMPLEMENTED` |
