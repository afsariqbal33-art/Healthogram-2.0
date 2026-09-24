# HEALTHOGRAM 2.3.0 INCIDENT RESPONSE & ESCALATION RUNBOOK

**Target Release:** 2.3.0 (versionCode 23000)  
**Document ID:** HGM-IR-RUNBOOK-230  
**Effective Date:** September 2026  

---

## 1. On-Call Escalation Matrix & Roles
* **Incident Commander (Lead Technical Operations / DevOps):** Overall decision-making, operational triage, rollout halt commands.
* **Healthcare Security Architect:** PHI/PII leakage triage, cryptographic key evaluation, HIPAA/GDPR breach protocol.
* **Senior Android Release Engineer:** Crashlytics/ANR triage, Play Console staged rollout controls, hotfix build deployment.
* **Financial Ledger QA Lead:** Stripe/PCI-DSS payment errors, ledger imbalance investigation, escrow release freezes.
* **Firebase / Cloud Architect:** Firestore rules, Cloud Functions latency, rate limiting, and App Check validation.

---

## 2. Severity Classifications & Service Level Agreements (SLAs)

| Severity | Definition & Examples | Response SLA | Resolution SLA | Mandatory Actions |
| :--- | :--- | :---: | :---: | :--- |
| **P0 (Critical)** | Health Passport PHI exposure, QR token forgery, financial ledger corruption, total auth failure, crash rate > 1.09% | **< 5 min** | **< 1 hour** | Immediate rollout halt in Play Console; activate Remote Config kill-switch; isolate affected collections |
| **P1 (Major)** | Checkout broken, appointment booking failure, WebRTC calls failing, FCM notifications dropped across all users | **< 15 min** | **< 4 hours** | Rollout frozen at current percentage; targeted kill-switch activation; start forward-fix hotfix |
| **P2 (Moderate)**| Localized translation defect, courier map delay, non-blocking UI styling glitch, minor social feed lag | **< 1 hour** | **< 24 hours** | Rollout continues under observation; scheduled patch in hotfix wave |
| **P3 (Minor)** | Minor padding mismatch, cosmetic badge label typo, non-critical analytics event dropped | **< 4 hours** | Next Sprint | Logged in Jira/GitHub issue tracker |

---

## 3. Incident Execution Lifecycle
1. **Detection:** Automated alert fired by Crashlytics, Firebase Cloud Monitoring, or Android Vitals webhooks.
2. **Declaration:** Incident declared in `#incident-healthogram-prod` channel. Incident Commander appointed.
3. **Containment:** 
   - Rollout expansion frozen or halted in Google Play Console.
   - Remote Config emergency switches activated in `PRODUCTION_REMOTE_CONFIG_v2.3.0.json`.
4. **Data Protection:** Zero log sanitization that could destroy forensics; zero disclosure of user PHI in public status updates.
5. **Remediation & Hotfix:** Root cause isolated, patched in `hotfix/2.3.x`, regression tested, signed, and uploaded to Play Console.
6. **Post-Mortem Review:** Complete blameless retrospective published within 48 hours of resolution.
