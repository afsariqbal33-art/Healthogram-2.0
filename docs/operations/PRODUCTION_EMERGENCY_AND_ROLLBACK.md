# Healthogram Production Emergency Response & Rollback Playbook

## Version 1.0.0 Disaster Recovery & Incident Protocol

This playbook guides engineering, security, and release operations during high-severity production incidents or catastrophic regressions.

---

## 1. Incident Classification & Response Levels

| Severity | Definition | Response Time | Action Owner |
| :--- | :--- | :--- | :--- |
| **SEV-0 (Critical)** | Data breach, Health Passport exposure, payment ledger imbalance, widespread crash on launch | < 15 minutes | Lead Security Engineer, Owner, Release Lead |
| **SEV-1 (High)** | Major subsystem failure (Calls failing, orders blocked, push notifications down) | < 30 minutes | Subsystem Owner, Android Release Lead |
| **SEV-2 (Medium)** | Non-blocking feature degradation (Translation timeout, AI suggestion delay) | < 2 hours | Feature Engineering Team |

---

## 2. Emergency Response Protocol (The "4-Step Triage")

```text
[Step 1: Incident Detection]
           ↓
[Step 2: Containment via Feature Flag / Kill-Switch]
           ↓
[Step 3: Root Cause Analysis & Hotfix Branch]
           ↓
[Step 4: CI Verification & Play Console Deployment]
```

### 1. Instant Containment (No Binary Rollout Required)
- **Global Maintenance Mode**: In Owner Control Panel (`OwnerDashboardPage`), activate `Maintenance Mode` to display a branded, graceful maintenance screen while preserving database consistency.
- **Granular Subsystem Kill-Switch**: Disable specific feature flags remotely:
  - `marketplace_enabled = false`
  - `calling_enabled = false`
  - `ai_studio_enabled = false`
  - `translation_enabled = false`
- **Security Quarantine**: If unauthorized access is detected, revoke token session secrets server-side immediately.

---

## 3. Google Play Rollback Mechanics

Google Play **does not support direct binary downgrades** on client devices. A user with version `1.0.0 (code 1)` installed cannot be downgraded to `0.9.0 (code 0)` via the store.

Therefore, Rollback is executed via:
1. **Halt Staged Rollout**: If the release was at a phased rollout percentage (e.g. 10% or 20%), immediately click **Halt Rollout** in Google Play Console to prevent new users from downloading the flawed version.
2. **Rapid Hotfix Deployment (`hotfix/1.0.1`)**:
   ```bash
   git checkout main
   git checkout -b hotfix/1.0.1
   # Apply minimal surgical patch
   # Bump versionCode = 2, versionName = "1.0.1" in app/build.gradle.kts
   gradle :app:testDebugUnitTest
   git commit -m "fix(hotfix): resolve SEV-1 production issue"
   git tag -a v1.0.1 -m "Hotfix release 1.0.1"
   gradle :app:bundleRelease
   ```
3. **Expedited Play Review**: Upload `Healthogram-v1.0.1-release.aab` directly to Production track and submit for expedited review.

---

## 4. Backend Cloud Functions & Rules Rollback

If the incident stems from a Cloud Function or security rule regression:
```bash
# Revert to known-good Git commit
git checkout v1.0.0 -- firestore.rules storage.rules functions/
firebase deploy --only firestore:rules,storage --project healthogram-prod
firebase deploy --only functions --project healthogram-prod
```
Cloud Functions and Security Rules take effect within 60 seconds globally without requiring client app updates.
