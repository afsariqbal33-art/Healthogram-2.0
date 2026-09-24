# HEALTHOGRAM 2.3.0 PRODUCTION CONFIGURATION MANIFEST

**Version:** 2.3.0  
**Version Code:** 23000  
**Environment:** `production`  
**Configuration Freeze Ref:** `v2.3.0-rc1`  
**Generated At:** 2026-09-23T14:30:00Z  

---

## 1. Firebase Production Infrastructure & Rules
* **Project ID:** `healthogram-prod-2026`
* **Firestore Rules:** `firestore.rules.v2.3` (Zero-trust, client-vault isolation, role-based access control, session validation)
* **Storage Rules:** `storage.rules.v2.3` (MIME validation, 25MB document limit, private patient vaults)
* **Cloud Functions:** Node.js 20 / v2 HTTPS callable & event triggers deployed to `europe-west1` and `us-central1`
* **Firebase App Check:** Play Integrity API attestation enforced for client verification
* **Firebase Cloud Messaging (FCM):** High-priority data payloads with masked user-visible notifications

---

## 2. Remote Config Snapshot Reference
* **Active Snapshot File:** `PRODUCTION_REMOTE_CONFIG_v2.3.0.json`
* **Emergency Kill Switches:**
  - `emergency_maintenance_mode`: `false`
  - `killswitch_all_traffic`: `false`
  - `marketplace_killswitch`: `false`
  - `webrtc_calling_killswitch`: `false`
  - `ai_generation_killswitch`: `false`
  - `payments_processing_killswitch`: `false`
  - `fhir_sync_killswitch`: `false`
  - `health_connect_killswitch`: `false`
  - `delivery_dispatch_killswitch`: `false`
  - `seller_payouts_killswitch`: `false`

---

## 3. Account Roles & Governance Invariants
* **Locked Healthcare Account Categories:**
  - `INDIVIDUAL`
  - `DOCTOR`
  - `CLINIC`
  - `HOSPITAL`
  - `LABORATORY`
* **Prohibited Healthcare Account Categories:**
  - `PHARMACY`, `MEDICAL_STORE`, `MEDICINE_COMPANY`, `WHOLESALE`, `SUPPLIER`, `EQUIPMENT_MANUFACTURER`, `EQUIPMENT_SUPPLIER` (Permanently rejected by validation layer)
* **Independent Marketplace Roles:**
  - `CUSTOMER`, `SELLER` (`INDIVIDUAL_SELLER`, `BUSINESS_SELLER`)
* **Session Management Limits:**
  - Maximum concurrent device sessions: 4
  - Session idle timeout: 15 minutes
  - QR Consent Token TTL: 60 seconds
