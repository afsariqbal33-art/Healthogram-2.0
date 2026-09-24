# 28 — INCIDENT CLASSIFICATION & RESPONSE RUNBOOK

## 1. Severity Classifications & SLAs
* **P0 (Critical Emergency):** Data breach, Health Passport PHI leak, ledger imbalance, crash rate > 1.09%, total system outage.
  - *Triage:* < 5 minutes | *Mitigation:* < 1 hour.
  - *Action:* Halt Play rollout immediately, activate Remote Config kill-switch.
* **P1 (Major Outage):** Core feature failure (checkout broken, calls failing, push notifications dropped).
  - *Triage:* < 15 minutes | *Mitigation:* < 4 hours.
* **P2 (Moderate Defect):** Non-blocking UI glitch, minor translation defect.
  - *Triage:* < 1 hour | *Mitigation:* < 24 hours.
* **P3 (Minor Issue):** Cosmetic styling or minor label typo.
  - *Triage:* < 4 hours | Handled in standard sprint cycle.

## 2. Containment Protocol
1. **Declare Incident:** Alert incident team in `#incident-healthogram-prod`.
2. **Rollout Control:** Click **Halt Rollout** in Google Play Console if binary defect.
3. **Emergency Circuit Breaker:** Toggle corresponding kill switch in `PRODUCTION_REMOTE_CONFIG_v2.3.0.json`.
4. **Forensic Preservation:** Snapshot logs and audit trails without modifying production data.
5. **Hotfix Deployment:** Apply minimal fix on `hotfix/2.3.x`, run full test suite, compile signed AAB, submit to Play Console with expedited review request.
