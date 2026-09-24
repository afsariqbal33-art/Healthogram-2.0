# HEALTHOGRAM v2.0.0 PRODUCTION RELEASE READINESS & DEPLOYMENT CHECKLIST

**Release Version:** v2.0.0-global-readiness  
**Target Date:** 2026-09-17  
**Target Platforms:** Android 16 (API Level 36), Target SDK 36, Min SDK 24  
**Release Manager:** Principal Software Architect & Release Engineering Lead  

---

## 1. Google Play Store Compliance Verification (Android 16 Mandate)

- [x] **Target SDK 36:** Confirmed `targetSdk = 36` and `compileSdk = 36` in `app/build.gradle.kts`.
- [x] **Predictive Back Navigation:** Enabled and verified with Compose M3 standard scaffolds.
- [x] **Zero Broad Storage Permissions:** Android Photo Picker (`PickVisualMedia`) utilized for user avatars, social posts, and prescription document selection. Zero `READ_EXTERNAL_STORAGE` or `READ_MEDIA_IMAGES` permissions declared in `AndroidManifest.xml`.
- [x] **Granular Notification Permissions:** Runtime `POST_NOTIFICATIONS` requested gracefully with contextual prompt.
- [x] **Adaptive Layouts & Large Screens:** Verified on Phone, Foldable, and Tablet form factors using Window Size Classes.
- [x] **App Check & Play Integrity:** Production attestation provider configured; debug tokens disabled in release builds.

---

## 2. Release Artifact Verification

- **Target Bundle:** `Healthogram-v2.0.0-scale-release.aab`
- **Signing Config:** Production Google Play App Signing key configured via secure container secrets.
- **ProGuard / R8:** Shrinking and optimization active (`proguard-android-optimize.txt`).
- **Reproducible Build Hash:** Documented in `BUILD_MANIFEST.md`.

---

## 3. Staged Rollout Strategy

1. **Internal Dogfooding (Day 1):** 100% internal engineering and QA team members.
2. **Staged Beta (Day 2 - 3):** 5% rollout in primary market (Oman). Continuous monitoring of Crashlytics and SRE error budgets.
3. **Regional Production (Day 4 - 5):** 25% rollout expanding to GCC markets (Saudi Arabia, UAE).
4. **Full Production (Day 7):** 100% public general availability upon verification of zero SEV-0 / SEV-1 incidents.
