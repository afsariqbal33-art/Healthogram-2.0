# HEALTHOGRAM 2.3.0 CONTROLLED ROLLBACK PLAN & EMERGENCY PROTOCOL

**Document ID:** HGM-OPS-ROLLBACK-230  
**Effective Date:** 2026-09-23  
**Target Release:** 2.3.0 (versionCode 23000)  
**Previous Stable Production Release:** 2.2.0 (versionCode 20201)  
**Authority Matrix:** Senior Release Engineer, Technical Operations Lead, Lead Security Architect  

---

## 1. Rollback Trigger Criteria (P0 / P1 Severity)
Rollback or emergency halt must be triggered immediately upon detection of:
* **Health Passport Security Compromise:** Plaintext exposure of medical records, QR key derivation vulnerabilities, or consent bypass.
* **Financial Ledger Imbalance:** Inconsistent double-entry debits vs credits, unbacked token issuance, or automated payout overdraws.
* **Android Vitals Threshold Exceeded:** User-perceived crash rate > 1.09% or ANR rate > 0.47% (Google Play bad behavior threshold).
* **Mass Authentication / Session Outage:** Systemic login failures, erroneous account lockouts, or session ceiling corruption.
* **Database Inconsistency:** Data corruption in Firestore collections or breaking schema deserialization.

---

## 2. Multi-Tiered Containment & Rollback Actions

### Tier 1: Immediate Rollout Halting (Play Console - 0 to 5 minutes)
1. Navigate to **Google Play Console -> Production -> Releases -> Release Dashboard**.
2. Click **Halt Rollout** on version 2.3.0.
   - Halting immediately halts further distribution of the 2.3.0 APK/AAB to users who have not yet updated.
   - Existing users on 2.3.0 remain on 2.3.0; operational protection moves to Tier 2.
3. Notify the Incident Response Team and record the halt event timestamp.

### Tier 2: Remote Config Emergency Mitigation (0 to 2 minutes)
If the issue is isolated to a specific platform subsystem, selectively activate sub-system kill switches in `PRODUCTION_REMOTE_CONFIG_v2.3.0.json`:
* Marketplace issues: Set `marketplace_killswitch = true` and `payments_processing_killswitch = true`.
* Calling issues: Set `webrtc_calling_killswitch = true`.
* AI issues: Set `ai_generation_killswitch = true`.
* Interoperability issues: Set `fhir_sync_killswitch = true` and `health_connect_killswitch = true`.
* Critical catastrophic outage: Set `emergency_maintenance_mode = true` or `killswitch_all_traffic = true`.
Changes propagate to mobile clients within seconds via Firebase Remote Config real-time listeners.

### Tier 3: Backward-Compatible Forward-Fix (Hotfix 2.3.1)
Because Google Play does not allow re-uploading a lower version code to downgrade users who have already updated, the canonical recovery for client-side crashes is a rapid forward hotfix:
1. Branch from `release/2.3.0` -> `hotfix/2.3.1`.
2. Apply minimal targeted patch.
3. Bump `versionCode = 23001`, `versionName = "2.3.1"`.
4. Run regression suite (`compile_applet` / tests).
5. Compile `Healthogram-2.3.1-release.aab`, compute SHA-256, and upload to Google Play Console with expedited review request.

### Tier 4: Disaster Recovery Database Restore
1. If data corruption occurred, isolate the affected collections in Firestore rules: `allow write: if false;`.
2. Restore corrupted documents from point-in-time backup snapshot `gs://healthogram-prod-backups-2026/2.3.0-pre-release/`.
3. Re-verify cryptographic signatures of all Health Passport vaults.
4. Run double-entry reconciliation script to confirm ledger integrity.
