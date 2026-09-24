# HEALTHOGRAM — PRODUCTION ROLLBACK PROCEDURES

**Target Version:** 1.0.0 (Production)  
**Classification:** Operational Safety & Release Management  

---

## 1. Rollback Philosophy & Constraints

In mobile software distribution via Google Play, **binary rollbacks are forward-only**. Once a user downloads version `1.0.0`, the device cannot be forcibly downgraded to an older APK by the Google Play Store without publishing an updated APK with a higher `versionCode`.

Therefore, Healthogram relies on a 3-tier defense-in-depth rollback strategy:
1. **Tier 1 (Instant — 0 seconds):** Remote Config Dynamic Feature Disabling.
2. **Tier 2 (Fast — 5 minutes):** Firebase Backend Rules & Cloud Functions Reversion.
3. **Tier 3 (Emergency Binary — 2 hours):** Accelerated Forward-Rollout Hotfix (`1.0.1` / `versionCode = 2`).

---

## 2. Tier 1: Dynamic Client Mitigation (Remote Config)

If a regression is discovered in a specific feature module after launch:

| Regressed Feature | Remote Config Parameter to Set | Impact |
| :--- | :--- | :--- |
| **Marketplace Checkout** | `"marketplace_checkout_enabled": false` | Shows friendly "Checkout temporarily undergoing maintenance" banner; prevents broken orders. |
| **AI Studio Tools** | `"ai_studio_enabled": false` | Hides AI generation FAB; reverts editor to standard image picker. |
| **WebRTC Video Calls** | `"video_calling_enabled": false` | Hides video call option; defaults consultations to audio-only or in-app chat. |
| **Translation Engine** | `"translation_engine_enabled": false` | Disables real-time overlay; communication remains active in native language. |
| **Health Passport QR** | `"emergency_disable_health_qr_sharing": true` | Prevents new QR handshake generation while existing patient profile remains intact. |

---

## 3. Tier 2: Backend Cloud Functions & Rules Rollback

If an error is introduced in Cloud Functions or Firestore Security Rules:
```bash
# View recent Firebase deployment revisions
firebase hosting:channel:list # or inspect git deployment tags

# Rollback Firestore Security Rules to known good commit (Step 25 / v1.0.0 baseline)
git checkout v1.0.0 -- firestore.rules storage.rules
firebase deploy --only firestore:rules,storage:rules

# Rollback Cloud Functions
git checkout v1.0.0 -- functions/
cd functions && npm install
firebase deploy --only functions
```

---

## 4. Tier 3: Forward Hotfix Binary Release

If the client binary contains a critical native crash that cannot be mitigated via server-side configuration:
1. Immediately halt any active Google Play Staged Rollout in Google Play Console -> **Release** -> **Production** -> **Halt Rollout**.
2. Create hotfix branch: `git checkout -b hotfix/v1.0.1`.
3. Increment `versionCode` to `2` and `versionName` to `"1.0.1"` in `app/build.gradle.kts`.
4. Apply the targeted fix and run test verification:
   ```bash
   gradle :app:testDebugUnitTest
   ```
5. Build production bundle:
   ```bash
   gradle :app:bundleRelease
   ```
6. Submit `1.0.1` to Google Play Console with **Expedited Review Request** for critical crash mitigation.
