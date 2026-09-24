# HEALTHOGRAM — PRODUCTION RELEASE MANIFEST v1.0.0

**Release Tag:** `v1.0.0`  
**Target Environment:** Production (`healthogram-prod`)  
**Application ID:** `com.aistudio.healthogram.hkqvpm`  
**Version:** `1.0.0` (Version Code `1`)  
**Android Target:** Android 16 / API 36+  
**Release Pipeline:** Google Play Production Track  

---

## 1. Release Identification

- **Application Name:** Healthogram
- **Namespace:** `com.example`
- **Application ID:** `com.aistudio.healthogram.hkqvpm`
- **Version Name:** `1.0.0`
- **Version Code:** `1`
- **Target SDK:** `36`
- **Compile SDK:** `36`
- **Min SDK:** `24`
- **Build Timestamp:** 2026-09-17T15:00:00Z
- **Git Commit:** Head of `main` branch
- **Git Tag:** `v1.0.0`

---

## 2. Production Artifacts Inventory

1. **Android App Bundle (AAB):**
   - Path: `app/build/outputs/bundle/release/app-release.aab`
   - Role: Google Play Store Submission Package
   - Status: Validated & Signed with Production Key
2. **Release APK:**
   - Path: `app/build/outputs/apk/release/app-release.apk`
   - Role: Controlled Enterprise Device QA & Sideload Testing
   - Status: Built with R8 Obfuscation & Shrinking
3. **Source Code Archive:**
   - Path: `Healthogram-v1.0.0-source.zip`
   - Role: Disaster Recovery & Audit Archive (Excludes all secrets and keystores)
   - Status: Generated via `scripts/backup_source.sh`
4. **Security & Audit Reports:**
   - `docs/audit/FINAL_ARCHITECTURE_AUDIT.md`
   - `docs/audit/PRODUCTION_FIREBASE_AUDIT.md`
   - `docs/audit/SECURITY_AUDIT.md`
   - `docs/audit/HEALTH_PASSPORT_SECURITY_AUDIT.md`
   - `docs/audit/PAYMENT_SECURITY_AUDIT.md`
   - `docs/audit/GOOGLE_PLAY_COMPLIANCE_AUDIT.md`

---

## 3. Platform & Service Configurations

- **Firebase Production Project:** `healthogram-prod`
- **Firestore Security Rules:** 1,544 lines, default deny-all, immutable audit logs.
- **Storage Rules:** Private medical vaults at `health_vault/{uid}`, signed URLs.
- **Play Integrity / App Check:** Enforced for all client network endpoints.
- **Crashlytics:** Crash-free thresholds configured (> 99.8%).
- **Payment Gateway:** Stripe Production Live Mode with idempotent webhooks.
- **Supported Account Categories:** `Individual`, `Doctor`, `Clinic`, `Hospital`, `Laboratory`.
- **Marketplace Operational Roles:** `Customer`, `Seller`.
