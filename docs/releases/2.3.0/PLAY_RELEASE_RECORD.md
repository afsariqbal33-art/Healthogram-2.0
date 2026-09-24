# HEALTHOGRAM 2.3.0 GOOGLE PLAY RELEASE RECORD

**Release Version:** `2.3.0`  
**Version Code:** `23000`  
**Application ID:** `com.aistudio.healthogram.hkqvpm`  
**Target API:** `36` (Android 16 Mandatory Forward Compliance)  
**Min SDK:** `24`  
**Compile SDK:** `36`  
**Base Candidate:** `v2.3.0-rc1`  
**Target Release Tag:** `v2.3.0`  
**Submission Timestamp:** `2026-09-23T15:30:00Z`  
**Artifact Uploaded:** `Healthogram-2.3.0-release.aab`  
**AAB SHA-256 Checksum:** `b003a490b32dfffb7d2cf2abc47153b81cca235a4add1db698c523305323d96e`  

---

## 1. Google Play Console Operational Status
* **Submission Status:** `STAGED_ROLLOUT_PREPARED` (Configured for 5.0% initial staged rollout)
* **Google Play Review Status:** `REQUIRES_VALIDATION` (Actual Play Console review queue awaits processing by Google Play reviewers)
* **Google Play Approval Status:** `REQUIRES_VALIDATION` (*Notice: In accordance with production policy, approval is never claimed or assumed until confirmed by live Google Play Developer Console signals*)
* **Android Vitals Status:** `DATA NOT AVAILABLE` (Awaiting production traffic on Google Play)
* **Crashlytics Release Status:** `CONFIGURED / ACTIVE`
* **Google Play App Signing:** Enforced (Google-managed release key; local upload certificate verified)

---

## 2. Release Track & Rollout Wave Parameters
* **Target Track:** Production (Staged Rollout)
* **Initial Staged Rollout Fraction:** 5.0% (`0.05`)
* **Authorized Phase Progression:**
  - **Phase A (Initial):** 5% rollout to evaluate crash rate and ANR signals
  - **Phase B (Verification):** 10% rollout following 24 hours of stable Android Vitals
  - **Phase C (Expansion):** 25% rollout after 48-hour payment & ledger reconciliation
  - **Phase D (Majority):** 50% rollout after 72 hours
  - **Phase E (General Availability):** 100% full rollout upon executive sign-off
* **Geographic Staging (Wave 1 Countries):**
  - Saudi Arabia (`SA`)
  - United Arab Emirates (`AE`)
  - United States (`US`)
  - United Kingdom (`GB`)
  - Egypt (`EG`)
  - India (`IN`)
* **Restricted Countries:** All other jurisdictions set to `MAINTENANCE` or `COMING_SOON` in `PRODUCTION_REMOTE_CONFIG_v2.3.0.json`.

---

## 3. Play Policy & Declaration Audit
* **Health Apps Declaration:** Complete and verified (`Diseases & conditions management`, `Medication & treatment management`, `Healthcare services & records`).
* **Medical Disclaimer:** Non-SaMD disclaimer verified in store copy and in-app settings.
* **Data Safety Section:** Complete (`docs/releases/2.3.0/DATA_SAFETY_DECLARATION.md`).
* **Public Privacy Policy:** Verified accessible at `https://healthogram.app/privacy`.
* **Account Deletion:** Verified in-app at `Settings -> Privacy & Security -> Delete Account` and web at `https://healthogram.app/account/delete`.
* **Play Reviewer Access Accounts:** Provisioned with synthetic sandbox credentials (`docs/releases/2.3.0/PLAY_REVIEWER_ACCESS_FINAL.md`).
