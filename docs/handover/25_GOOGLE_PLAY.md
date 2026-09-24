# 25 — GOOGLE PLAY OPERATIONS & POLICY COMPLIANCE

## 1. Compliance Declarations
* **Health Apps Declaration:** Complete and verified across 3 categories:
  1. Diseases and Conditions Management
  2. Medication and Treatment Management
  3. Healthcare Services and Management
* **Medical Device Disclaimer:** Non-SaMD status explicitly declared in store metadata and in-app settings.
* **Data Safety:** Full survey responses verified in `DATA_SAFETY_DECLARATION.md`. Zero data sharing with third parties without consent; client-side encryption at rest.

## 2. Privacy Policy & Account Deletion
* **Public URL:** Publicly accessible at `https://healthogram.app/privacy`.
* **In-App Deletion:** Transparent deletion workflow at `Settings -> Privacy & Security -> Delete Account`.
* **Web Deletion Portal:** Fallback web deletion at `https://healthogram.app/account/delete`.

## 3. Staged Rollout Strategy
* **Phase A (Canary):** 5% rollout to evaluate crash and ANR signals via Android Vitals and Crashlytics.
* **Phase B (Expansion):** 10% -> 25% -> 50% progressive expansion upon satisfaction of stability gates.
* **Phase C (General Availability):** 100% rollout upon executive sign-off.
