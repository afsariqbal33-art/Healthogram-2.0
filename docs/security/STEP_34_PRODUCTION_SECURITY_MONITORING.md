# Step 34 — Production Security & Health Passport Monitoring Specification

**Document:** `docs/security/STEP_34_PRODUCTION_SECURITY_MONITORING.md`  
**Security Framework:** HIPAA Security Rule (§164.312), Zero-Trust Architecture, GDPR Art 32  
**Monitoring Platform:** Google Cloud Security Command Center, Firebase App Check, Cloud Audit Logs  
**Status:** PRODUCTION ACTIVE  

---

## 1. Absolute Privacy Invariant for Operational Dashboards

> **MANDATORY DIRECTIVE:** Operational, security, and SRE dashboards **MUST NEVER** display, log, or index clinical or medical payload content (such as diagnoses, medication names, lab results, or physician notes). Metrics track structural event metadata only: Actor UID, Target Patient UID, Action Enum, Result Status, Timestamp, IP Hash, and Latency.

---

## 2. Health Passport Security Monitoring Matrix

| Event Type | Monitored Metric | Baseline Expectation | Anomaly Trigger Threshold | Automated Security Action |
| :--- | :--- | :--- | :--- | :--- |
| **Access Requests** | `health_access_requests_rate` | 0.5 - 2.0 / min / active doctor | > 15 requests / min / doctor | Rate limit doctor account; alert SOC |
| **Consent Grants** | `health_consent_grants_total` | Monitored volume | Sudden 10x spike across unrelated accounts | Flag for manual compliance review |
| **Consent Revocations** | `health_consent_revocations_total` | Real-time immediate | > 50 / hour | Investigate patient UI UX or sync glitch |
| **Failed Authorization** | `health_authz_denied_events` | < 0.1% of requests | > 1.0% or 5 repeated fails for single actor | Temporarily freeze actor session (15m) |
| **Ephemeral QR Sessions** | `health_qr_scans_rate` | 1 scan per 15-minute token | Duplicate scan attempt on consumed token | Token rejected; security incident logged |
| **Emergency Break-Glass** | `health_emergency_override_events` | Rare (0 - 5 / month globally) | Any single override event | Mandatory SMS alert to patient; audit log |
| **Bulk Health Exports** | `health_export_volume_mb` | < 10 MB / user / day | > 50 MB / user or > 5 exports / hr | Throttle export; prompt biometric re-auth |
| **Cross-Tenant Queries** | `health_unrelated_patient_queries` | 0 (Zero) | > 0 queries without active consent | Revoke partner token; initiate P0 breach audit |

---

## 3. Firebase App Check & Play Integrity Enforcement Protocol

To eliminate unauthorized backend traffic, bot networks, and modified APKs:

1. **Phase 1 (Monitoring Mode - Weeks 1-2):**
   - App Check is configured with Play Integrity provider.
   - Cloud Functions and Firestore enforce `AppCheck: Monitoring`.
   - Attestation failure metrics are collected to confirm zero false positives among legitimate Android 16/15 devices.
2. **Phase 2 (Gradual Enforcement - Weeks 3-4):**
   - Enforce App Check on non-critical endpoints (`search`, `translation`).
   - Monitor customer support logs for legacy device rejection.
3. **Phase 3 (Full Strict Enforcement - Production):**
   - Health Passport endpoints (`consumeHealthQrSession`, `fhirExportBundle`, `updateHealthRecord`) require valid Play Integrity attestation tokens.
   - Unauthenticated or rooted client requests receive HTTP 401 Unauthorized.

---

## 4. Administrative & Owner Security Guards

- **Owner PIN Verification:** Server-side bcrypt comparison required before writing to `emergency_controls/current`.
- **Administrative Immutability:** Admins cannot read or decrypt private patient health records.
- **Concurrent Session Ceiling:** Strict 4-device concurrent session limit enforced across individual accounts with automated eviction of oldest sessions.
