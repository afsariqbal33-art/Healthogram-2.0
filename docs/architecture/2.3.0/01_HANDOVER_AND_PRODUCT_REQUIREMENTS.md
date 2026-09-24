# HEALTHOGRAM 2.3: HANDOVER & PRODUCT REQUIREMENTS DOCUMENT (PRD)

**Document ID:** HGM-2.3-ARCH-01-PRD  
**Phase:** Step 48 Architecture & Planning  
**Target Release:** Healthogram Version `2.3.0`  
**Git Branch:** `develop/healthogram-2-3`  
**Timestamp:** 2026-09-22T06:15:00Z  
**Governing Roles:** Senior Product Architect, Principal Software Architect, Healthcare Interoperability Lead, Chief Security Officer  

---

## 1. Step 47 → Step 48 Handover Report

### Production Baseline & Evidence Ingestion
Healthogram Version `2.2.0` concluded Step 47 with **STABILIZATION COMPLETE**. The production operational baseline established in Step 47 provides the direct empirical foundation for Version 2.3 planning:
* **Release Baseline:** Version `2.2.0` (`versionCode 20201`), compiled against Android 16 (API 36).
* **Rollout Posture:** Stage B (5.0% Staged Rollout on Google Play) across conservative wave (`US, CA, GB, SA, AE, EG, IN`).
* **Incident Status:** 0 P0 / 0 P1 incidents.
* **Bug Register:** 4 non-critical issues triaged (scheduled for patch `2.2.1` independently):
  * `BUG-001` (RTL Arabic bill layout spacing)
  * `BUG-002` (Translation repository cache composite key)
  * `BUG-003` (Egypt SMS carrier gateway latency)
  * `BUG-004` (ExoPlayer reels memory pool retain count)
* **Technical Debt Register:** 8 items cataloged (`DEBT-01` through `DEBT-08`), incorporated directly into Phase 1 of the 2.3 roadmap.
* **Core Invariants Verified in Production:**
  * Zero raw PHI in QR tokens; 60s dynamic TTL.
  * `international_marketplace_enabled = false` strictly held (100% domestic isolation).
  * Main account categories locked to exactly 5.
  * Maximum 4 concurrent device logins with FIFO eviction.
  * Double-entry ledger invariant holds unconditionally: $\sum \text{Debits} - \sum \text{Credits} == \$0.00$.

---

## 2. Healthogram 2.3 Guiding Architectural Principles

All 2.3 planning and implementation must adhere to the 11 locked platform principles:
1. **SECURITY FIRST:** Privacy by default; health data strictly isolated from social and public datastores.
2. **USER CONTROL:** Patients possess absolute sovereign control over their clinical records, consent grants, and session lifecycles.
3. **MODULARITY:** New capabilities (Appointments, FHIR, Health Connect) implemented as isolated, decoupled feature modules.
4. **BACKWARD COMPATIBILITY:** Existing 2.2 clients must continue operating without interruption.
5. **FEATURE FLAGS:** Every major 2.3 enhancement deployed behind Remote Config multi-state feature flags (`OFF`, `BETA`, `ON`, `MAINTENANCE`).
6. **COUNTRY AWARENESS:** Regulatory, payment, and delivery logic parameterized by country configuration rather than hardcoded logic.
7. **AUDITABILITY:** Immutable, write-only audit logs for all clinical access, ledger transactions, and administrative changes.
8. **SCALABILITY:** Asynchronous event queues and cursor pagination to support high-throughput operations.
9. **COST CONTROL:** Strict FinOps budgeting with upfront compute and egress cost modeling for every new service.
10. **OBSERVABILITY:** Distributed tracing, error budgets, and SLA/SLO metrics on all client and server endpoints.
11. **ROLLBACK CAPABILITY:** Automated circuit breakers and immediate rollback runbooks for every deployment artifact.

---

## 3. Locked Account & Marketplace Model

The Healthogram platform identity model is structurally locked to prevent clinical conflicts of interest and regulatory licensing violations:

### A. Main Account Categories (Strictly 5 Only)
1. **Individual:** General citizen, patient, content creator, healthcare consumer. Owns a sovereign Health Passport.
2. **Doctor:** Verified licensed clinical practitioner (generalist or specialist).
3. **Clinic:** Verified outpatient clinical facility or polyclinic group.
4. **Hospital:** Verified inpatient medical center, emergency care, or multi-department healthcare system.
5. **Laboratory:** Accredited medical diagnostic center, pathology laboratory, or imaging facility. (Note: Laboratories upload authorized reports; they **do not** have their own Health Passport).

*Forbidden Categories:* Under no circumstances will account types such as "Pharmacy", "Medical Store", "Medicine Company", "Wholesale/Supplier", or "Medical Equipment Manufacturer" be added as main platform categories.

### B. Marketplace Roles (Strictly Decoupled)
The marketplace operates under a strictly separate role hierarchy:
1. **Customer:** Any verified platform user purchasing domestic healthcare items.
2. **Seller:** Verified commercial entity selling approved healthcare goods.
   * **Individual Seller:** Independent licensed professional (e.g., licensed private pharmacist).
   * **Business Seller:** Corporate medical distributor, licensed pharmacy chain, or verified medical device vendor.

---

## 4. Product Scope (The 17 Integrated Subsystems)

Healthogram 2.3 maintains its comprehensive healthcare super-app footprint:
1. **Secure Health Passport:** Sovereign patient medical records, single-use 60s QR vault, granular consent engine.
2. **Healthcare Provider Ecosystem:** Accredited profiles, credentials verification, doctor discovery.
3. **Social / Creator Platform:** Health education, verified clinical thought-leadership, video reels, stories, posts.
4. **Healthcare Marketplace:** Domestic-only catalog, e-commerce cart, prescription orders, escrow payments.
5. **AI Studio:** Contextual creator tools, product description assist, image cleanup, zero PHI ingestion.
6. **Messaging:** Signal Protocol end-to-end encrypted (E2EE) direct messaging with offline queueing.
7. **Audio Calling:** Real-time P2P WebRTC audio teleconsultation with zero default recording.
8. **Video Calling:** High-definition WebRTC video teleconsultation with dynamic bandwidth adaptation.
9. **Multilingual Translation:** Side-by-side bilingual display, on-device ML Kit, cached medical glossaries.
10. **Notifications:** Privacy-preserving FCM channels omitting diagnostic details from lock screens.
11. **Country-Wise Payments:** Domestic payment gateways (Stripe, HyperPay, Apple/Google Pay) with integer currency accounting.
12. **Delivery & Logistics:** Domestic courier adapters, cold-chain tracking, two-factor OTP proof of delivery.
13. **Admin Control Panel:** Granular RBAC, practitioner license verification, content moderation, audit review.
14. **Owner Control Panel:** Emergency kill switches, platform commission controls, country policy toggles.
15. **Owner Earnings:** Immutable double-entry financial ledger, payout reconciliation, real-time fee tracing.
16. **Healthcare Interoperability:** HL7 FHIR R4 standardized export/import, Android Health Connect sync.
17. **Analytics & Monitoring:** Privacy-first telemetry, Crashlytics scrubbers, Google Play Vitals tracking.

---

## 5. High-Level Technical Requirements

* **Target Runtime:** Android 16 (API level 36), `minSdk = 26` (Android 8.0 Oreo).
* **UI Architecture:** 100% Jetpack Compose with Material Design 3 (M3), full Arabic RTL bidirectional support.
* **Client Database:** Room with SQLCipher 256-bit encryption for sensitive local health data.
* **Cloud Infrastructure:** Google Cloud Platform (Europe/Middle East/US regions), Firebase Auth, Firestore, Cloud Functions (Node.js 20 / TypeScript), Cloud Run for media workers.
* **Integrity & Security:** Play Integrity API (`MEETS_DEVICE_INTEGRITY`), Firebase App Check, Customer-Managed Encryption Keys (CMEK) on Cloud Storage.
* **API Standards:** REST / JSON with OpenAPI 3.1 specifications, gRPC for real-time messaging sync.
