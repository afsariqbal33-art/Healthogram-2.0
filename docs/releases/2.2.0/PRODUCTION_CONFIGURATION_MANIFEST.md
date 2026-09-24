# HEALTHOGRAM 2.2.0 PRODUCTION CONFIGURATION MANIFEST

**Version:** 2.2.0  
**Version Code:** 20201  
**Environment:** `production`  
**Configuration Freeze Ref:** `v2.2.0`  

---

## 1. Firebase Production Project Endpoints
* **Firebase Project ID:** `healthogram-prod-2026`
* **Firestore Rules Version:** 2 (Strict RBAC, AES Vault, no public access)
* **Storage Rules Version:** 2 (Isolated medical vaults, document size limits, mime checks)
* **Realtime Database:** Realtime presence & ephemeral signaling (`healthogram-prod-default-rtdb.firebaseio.com`)
* **Cloud Functions Region:** `europe-west1` / `us-central1` (Production v2 Cloud Functions)
* **Firebase App Check:** Active with Play Integrity attestation provider
* **Firebase Crashlytics & Analytics:** Enabled for release monitoring

---

## 2. Platform Feature Flags & Emergency Kill Switches
* `emergency_maintenance_mode`: `false`
* `health_passport_v2_enabled`: `true`
* `fhir_r4_ingestion_enabled`: `true`
* `health_connect_sync_enabled`: `false` (Remote Config guarded for stage 1)
* `international_marketplace_enabled`: `false` (Strictly restricted to authorized local jurisdictions)
* `webrtc_teleconsultation_enabled`: `true`
* `webrtc_auto_record_enabled`: `false` (Strict HIPAA constraint)
* `ai_studio_creative_tools_enabled`: `true` (Air-gapped from patient clinical schemas)
