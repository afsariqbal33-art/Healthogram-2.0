# HEALTHOGRAM — INCIDENT RESPONSE PLAN & ESCALATION MATRIX

**Classification:** Critical Production Security & Reliability  
**Target Version:** 1.0.0 (Production)  
**Effective Date:** September 16, 2026  
**Incident Commander Role:** On-Duty Site Reliability Engineer  

---

## 1. Incident Severity Definitions

| Severity | Definition | Target Response (Ack) | Target Resolution (Mitigate) | Examples |
| :--- | :--- | :--- | :--- | :--- |
| **SEV-0 (Critical Catastrophe)** | Uncontrolled PHI/Health Passport data leakage, active credential breach, financial ledger compromise, complete service outage. | Immediate (< 5 mins) | < 30 minutes | Public medical records exposed, payment gateway private keys leaked, complete login failure. |
| **SEV-1 (Major Disruption)** | Primary service component outage affecting substantial user base (> 10%), checkout failure, teleconsultation connection failure. | < 15 minutes | < 2 hours | Marketplace checkout down, QR session grants failing universally, FCM push failure. |
| **SEV-2 (Moderate Degradation)** | Non-critical feature failure, latency regression, minor device-specific rendering bug, isolated seller payout delay. | < 1 hour | < 8 hours | AI caption tool timeout, translation latency increase, background media upload retry loop. |
| **SEV-3 (Minor / Cosmetic)** | Minor UI inconsistency, typo in localized copy, edge-case cosmetic anomaly. | Next business day | Scheduled release | Text padding mismatch, non-blocking icon alignment. |

---

## 2. SEV-0 / SEV-1 Incident Workflow

```text
[Incident Detected] (PagerDuty / Crashlytics / Firestore Alert)
        ↓
1. ACKNOWLEDGE: On-Call Engineer claims incident within 5 minutes.
        ↓
2. TRIAGE & CONTAIN:
   ├── Health Data Exposure? ──> Trip Remote Config: emergency_health_passport_lock = true
   ├── Payment Vulnerability? ──> Trip Remote Config: emergency_disable_marketplace = true
   └── Global Compromise?    ──> Trip Remote Config: emergency_maintenance_mode = true
        ↓
3. ISOLATE & PRESERVE:
   - Revoke compromised tokens/sessions via Firebase Admin SDK.
   - Snapshot audit logs and Cloud Firestore point-in-time state.
        ↓
4. ROOT CAUSE & HOTFIX:
   - Branch from main: `hotfix/<issue-name>`.
   - Implement surgical fix, run regression tests (`gradle :app:testDebugUnitTest`).
        ↓
5. DEPLOY & VERIFY:
   - Deploy backend rules/functions or submit urgent Play Console release.
        ↓
6. POST-MORTEM:
   - Publish blameless Post-Mortem within 48 hours.
```

---

## 3. Remote Config Emergency Kill Switches

Healthogram maintains instant, client-side kill switches controlled via Firebase Remote Config:

```json
{
  "emergency_maintenance_mode": false,
  "emergency_disable_marketplace": false,
  "emergency_disable_payments": false,
  "emergency_disable_seller_onboarding": false,
  "emergency_disable_ai_studio": false,
  "emergency_disable_translation": false,
  "emergency_disable_calling": false,
  "emergency_disable_messaging": false,
  "emergency_disable_new_registrations": false,
  "emergency_disable_health_qr_sharing": false,
  "emergency_health_passport_lock": false
}
```

*Rule on Health Passport Emergency Lock:* When `emergency_health_passport_lock` is set to `true`, existing patient records remain secure and encrypted; creation of new external access grants and QR handshakes are paused until explicitly cleared by the Health Security Admin.

---

## 4. Escalation Contacts & On-Call Roster

- **Incident Commander (Primary):** `oncall-lead@healthogram.app` (+1-800-555-HLTH-1)
- **Firebase Security Engineer:** `security@healthogram.app`
- **Health Compliance Officer (HIPAA/GDPR):** `compliance@healthogram.app`
- **Owner / Executive Escalation:** `owner@healthogram.app`
- **Google Play Release Liaison:** `play-release@healthogram.app`
