# HEALTHOGRAM 2.3: MIGRATION, ROADMAP, GOVERNANCE & RISK BLUEPRINT

**Document ID:** HGM-2.3-ARCH-07-ROADMAP  
**Phase:** Step 48 Architecture & Planning  
**Target Release:** Healthogram Version `2.3.0`  
**Git Branch:** `develop/healthogram-2-3`  
**Timestamp:** 2026-09-22T06:45:00Z  
**Governing Standards:** DORA Operational Framework, ITIL v4 Release Management, ISO/IEC 27005 (Risk Management)  

---

## 1. Zero-Downtime Data Migration Plan

Healthogram 2.3 enforces zero-downtime database evolution without data loss or service disruption.

```
[PHASE A: ADDITIVE SCHEMA] ──► [PHASE B: DUAL-WRITE HOOK] ──► [PHASE C: ASYNC BACKFILL] ──► [PHASE D: CUTOVER]
(Nullable 2.3 fields added)    (Writes update both models)    (Cursor batches of 500)      (V2 readers promoted)
```

### Migration Execution Plan
1. **Old Schema Baseline:** Version 2.2 documents with `schema_version = 2`.
2. **New Schema Target:** Version 2.3 documents with `schema_version = 3`.
3. **Deployment Order:**
   * Step 1: Deploy Firestore Security Rules supporting both `schema_version: 2` and `schema_version: 3`.
   * Step 2: Deploy Cloud Functions handling dual-version data models.
   * Step 3: Run Cloud Run asynchronous cursor backfill worker on historical records.
   * Step 4: Promote mobile client APKs/AABs via Google Play Staged Rollout.
4. **Validation & Verification:** Automated integrity verification script checks 100% of sample documents before releasing version constraints.

---

## 2. Rollback Strategy & Emergency Circuit Breakers

Every Version 2.3 feature includes a documented, automated rollback mechanism:
* **Client-Side Kill Switches:** Configured in Remote Config (`feature_fhir_export_v2_3 = false`, `feature_appointment_booking_v2_3 = false`, `feature_google_pay_checkout_v2_3 = false`). Disabling a flag instantly reverts the UI to the proven 2.2 stable baseline within 15 minutes.
* **Server-Side API Circuit Breakers:** Cloud Functions endpoints wrapped in circuit breaker middleware: If error rates exceed 2.0% over a 5-minute window, the endpoint trips to maintenance mode with graceful fallback messaging.
* **Database Rollback:** Because all migrations are strictly additive, rolling back application code does not require restoring or downgrading Firestore databases.

---

## 3. Remote Config Feature Flag Matrix

| Feature Flag Key | Description | Supported States | Target Countries | Target Account Categories | Default State |
| :--- | :--- | :---: | :---: | :---: | :---: |
| `feature_fhir_export_v2_3` | HL7 FHIR R4 standardized export | `OFF`, `BETA`, `ON` | All 7 countries | `Individual` | `OFF` |
| `feature_appointment_booking_v2_3`| Multi-specialty clinical booking | `OFF`, `BETA`, `ON` | `US, SA, AE` | `Individual`, `Doctor`, `Clinic`| `OFF` |
| `feature_google_pay_checkout_v2_3`| One-tap Google Pay wallet | `OFF`, `BETA`, `ON` | All 7 countries | `Customer` | `OFF` |
| `feature_health_connect_sync_v2_3`| Android Health Connect sync | `OFF`, `BETA`, `ON` | All 7 countries | `Individual` | `OFF` |
| `feature_emergency_access_v2_3` | Opt-in hospital break-glass access | `OFF`, `BETA`, `ON` | `US, SA, AE` | `Individual`, `Hospital` | `OFF` |
| `international_marketplace_enabled`| Cross-border commerce | `OFF` (Locked) | None | None | `OFF` (Locked) |

---

## 4. Subsystem Dependency Graph

```
[CORE SECURITY & IDENTITY (4-Device Limit)]
       │
       ├─► [HEALTH PASSPORT VAULT] ──► [HL7 FHIR R4 EXPORT MODULE]
       │                                     │
       │                                     ▼
       ├─► [APPOINTMENTS MODULE] ◄─── [ANDROID HEALTH CONNECT]
       │            │
       │            ▼
       ├─► [PAYMENT SERVICE & GOOGLE PAY] ──► [DOUBLE-ENTRY LEDGER]
       │            │
       │            ▼
       └─► [DOMESTIC MARKETPLACE & DELIVERY] ──► [OTP HANDOVER]
```
*Blocking Constraints:*
* FHIR Export depends on Health Passport client vault stability.
* Appointment Booking depends on Payment Service and Double-Entry Ledger.
* Delivery Handover depends on domestic order checkout and OTP generation.

---

## 5. Technical Debt Integration Plan (Step 47 Debt Retirement)

The 8 technical debt items cataloged during Step 47 are integrated directly into Version 2.3 engineering:

| Debt ID | Subsystem | Description | Target Phase in 2.3 | Resolution Strategy |
| :--- | :--- | :--- | :---: | :--- |
| **DEBT-01** | `Social/Reels` | ExoPlayer surface caching holds up to 10 instances | Phase 1 | Clamp pool to 3; call `clearMediaItems()` on detach |
| **DEBT-02** | `Translation` | Composite key in translation cache misses termId | Phase 1 | Update composite key to `(termId, lang, hash)` |
| **DEBT-03** | `Firestore` | Index definitions require synchronization | Phase 1 | Deploy updated `firestore.indexes.json` in CI |
| **DEBT-04** | `HealthPassport`| Fixed spacing in Arabic medical bill card | Phase 1 | Wrap with directional layout padding in Compose |
| **DEBT-05** | `CloudFunctions`| Cold start for health passport auth functions | Phase 1 | Configure `minInstances = 1` in target regions |
| **DEBT-06** | `Testing` | Mock WebSocket for automated multi-device sync | Phase 6 | Build local in-memory WebSocket mock harness |
| **DEBT-07** | `Security` | App Check debug token for web preview | Phase 1 | Document and standardize container debug tokens |
| **DEBT-08** | `Operations` | Ledger reconciliation check runs nightly | Phase 1 | Promote reconciliation job to hourly cron |

---

## 6. Healthogram 2.3 Comprehensive Risk Register

| Risk ID | Category | Description | Probability | Impact | Mitigation Strategy | Owner | Status |
| :--- | :--- | :--- | :---: | :---: | :--- | :--- | :---: |
| **RSK-01** | `Security` | Key leakage during FHIR export | Low | High | Bundles generated purely in-memory; zero cloud storage | CSO | `MITIGATED` |
| **RSK-02** | `Healthcare` | Mistranslation of critical prescriptions | Low | High | Dual-pane side-by-side display with non-certification warning | Clinical Lead | `MITIGATED` |
| **RSK-03** | `Financial` | Duplicate payment webhook commits | Low | High | Idempotency keys enforced in Redis with 24h TTL | FinOps Lead | `MITIGATED` |
| **RSK-04** | `Operations` | Appointment slot double-booking race | Medium | High | Distributed Redis locks (5m TTL) + Firestore transactions | Backend Lead | `MITIGATED` |
| **RSK-05** | `Regulatory` | Cross-border medical shipment attempt | Low | High | `international_marketplace_enabled=false` strictly locked | Compliance Lead| `MITIGATED` |
| **RSK-06** | `Third-party`| SMS OTP gateway delay during peak traffic | Medium | Medium | Multi-carrier routing fallback + WhatsApp OTP channel | Identity Lead | `IN_PROGRESS`|
| **RSK-07** | `Performance`| High memory pressure in video reels | Low | Medium | Clamped ExoPlayer pool size (max 3 instances) | Android Lead | `MITIGATED` |

---

## 7. Healthogram 2.3 Phased Development Roadmap

To ensure engineering discipline without arbitrary calendar dates, Version 2.3 progresses through 10 sequential milestones:

* **PHASE 1: Core Architecture & Technical Debt Retirement**
  * Resolve `DEBT-01` through `DEBT-05` and `DEBT-07` / `DEBT-08`.
  * Establish multi-module codebase structure and versioned API routing (`/api/v2`).
* **PHASE 2: Security & Health Passport Hardening**
  * Implement granular clinical scope presets (`SCOPE_EMERGENCY_SUMMARY`, `SCOPE_LAB_REPORTS_ONLY`).
  * Implement opt-in Emergency Access break-glass workflow with real-time alerts.
* **PHASE 3: Healthcare Interoperability**
  * Integrate HL7 FHIR R4 export engine with on-device cryptographic signing.
  * Integrate optional Android Health Connect synchronization manager.
* **PHASE 4: Appointments, Marketplace & Payments**
  * Build Multi-Specialty Smart Appointment Booking with distributed Redis locking.
  * Integrate Google Pay and Apple Pay one-tap native checkout sheets.
  * Implement cold-chain temperature telemetry monitoring for sensitive medical shipments.
* **PHASE 5: Communications, Creator & AI Studio Enhancements**
  * Unified `+` creation tray (Post, Reel, Video, Story, Live).
  * Professional Creator dashboard analytics.
  * Contextual AI Studio listing assistance with zero PHI ingestion.
* **PHASE 6: Performance, FinOps & Testing Harness**
  * Generate updated Baseline Profiles for Android 16.
  * Implement automated hourly double-entry ledger reconciliation worker.
  * Complete full Robolectric, Roborazzi, and FHIR automated test suites.
* **PHASE 7: Comprehensive QA & Security Penetration Testing**
  * Full regression testing across 7 rollout countries in localized languages.
  * Zero-trust security audit on all API endpoints and Firestore rules.
* **PHASE 8: Release Candidate & Staging Validation**
  * Build production Release Candidate AAB with verified cryptographic SHA-256 hash.
  * Deploy to staging environment for end-to-end user acceptance testing.
* **PHASE 9: Controlled Production Rollout**
  * Staged rollout on Google Play Production Track (1% → 5% → 15% → 50% → 100%).
  * Telemetry monitoring across Crashlytics, Play Vitals, and financial journals.
* **PHASE 10: Production Operations & Stabilization**
  * Post-launch monitoring and continuous operational handover.

---

## 8. Release Strategy & Governance

Version 2.3 deployment follows the proven controlled pipeline:
$$\text{Develop (develop/healthogram-2-3)} \longrightarrow \text{QA} \longrightarrow \text{Staging} \longrightarrow \text{Release Candidate (v2.3.0)} \longrightarrow \text{Play Staged Rollout} \longrightarrow \text{Main}$$
* **No Direct Uncontrolled Production Deploys:** Every release artifact requires verified commit traceability, signed cryptographic hashes, and green CI/CD pipeline results.

---

## 9. Final 2.3 Architecture Approval Checklist

* [x] Step 47 evidence inspected and ingested.
* [x] Product requirements defined across all 17 subsystems.
* [x] Account model locked to exactly 5 healthcare categories.
* [x] Marketplace roles locked to Customer and Seller.
* [x] Health Passport 2.3 granular scopes and emergency workflow modeled.
* [x] Healthcare organization profiles and verification framework defined.
* [x] HL7 FHIR R4 interoperability architecture specified.
* [x] Android Health Connect sync architecture specified.
* [x] Appointment booking distributed locking architecture designed.
* [x] Marketplace healthcare-only restrictions and domestic isolation preserved.
* [x] Payment service abstraction and Google/Apple Pay modeled.
* [x] Owner Earnings double-entry ledger invariant verified.
* [x] Delivery adapter architecture and cold-chain monitoring specified.
* [x] Social unified `+` entry and creator system defined.
* [x] Messaging E2EE and calling provider abstraction designed.
* [x] Translation side-by-side medical validation specified.
* [x] AI Studio contextual isolation and zero PHI ingestion verified.
* [x] Notification channel privacy modeled.
* [x] Admin and Owner control panels with emergency kill switches designed.
* [x] Database changes and versioned APIs (`/api/v2`) documented.
* [x] Security, privacy, performance, and cost models defined.
* [x] Testing strategy spanning Robolectric, Roborazzi, and FHIR suites defined.
* [x] Zero-downtime migration plan and automated rollback runbooks authored.
* [x] Remote Config feature flag matrix established.
* [x] Subsystem dependency graph and risk register documented.
* [x] Step 47 technical debt integrated into Phase 1 roadmap.
* [x] 10-phase development roadmap structured.
* [x] All 37 required Step 48 deliverables completed.
