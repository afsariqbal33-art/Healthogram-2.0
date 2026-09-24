# HEALTHOGRAM 2.2.0 RELEASE CANDIDATE ROLLBACK PLAN

**Document ID:** HGM-OPS-ROLLBACK-220  
**Effective Date:** 2026-09-21  
**Target Build:** 2.2.0 (versionCode 20201)  
**Previous Stable Build:** 2.1.0 (versionCode 20100)  

---

## 1. Rollback Trigger Criteria (P0 / P1)
- Data corruption in Health Passport or clinical vault.
- Unhandled cryptographic key failure or patient QR disclosure incident.
- Double-entry ledger imbalance or rogue financial webhook processing.
- Google Play Android Vitals crash rate spike > 1.09% or ANR > 0.47%.
- Widespread authentication lockouts or cloud function invocation timeouts.

---

## 2. Fast Rollback Execution Procedures
### Tier 1: Staged Rollout Halting (0–5 minutes)
1. Navigate to Google Play Console -> Production -> Releases -> Release Dashboard.
2. Click **Halt Rollout** on version 2.2.0. Halting immediately stops serving the 2.2.0 bundle to new users.
3. Verify rollout percentage freeze.

### Tier 2: Remote Config Emergency Mitigation (0–2 minutes)
1. Execute Firebase Remote Config freeze / kill-switch activation:
   - Set `emergency_maintenance_mode = true` or selectively isolate faulty module:
   - `marketplace_killswitch = true`
   - `webrtc_calling_killswitch = true`
   - `ai_generation_killswitch = true`
2. Publish Remote Config changes instantly.

### Tier 3: Binary Rollback (5–15 minutes)
1. Re-promote previous validated stable release (2.1.0, versionCode 20100) to production track, or submit rapid hotfix build 2.2.1 if rollback requires forward versioning on Google Play.
