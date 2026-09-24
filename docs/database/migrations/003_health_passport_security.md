# DATABASE MIGRATION 003: HEALTH PASSPORT ZERO-TRUST SCHEMA

**Migration ID:** `003_health_passport_security`  
**Target Environment:** Development, Staging, Production  
**Author:** Healthogram Security & Healthcare-Data Architect  

---

## 1. Overview
Implements Zero-Trust storage for patient clinical records with AES-GCM-256 field-level encryption, dynamic single-use QR access grants, and strict isolation preventing Laboratory accounts from owning Health Passports.

## 2. Collections Created & Security Invariants
* `/health_passports/{patientUid}`: Encrypted master clinical summary record.
* `/clinical_records/{recordId}`: Granular test results, lab metrics, and doctor clinical notes (encrypted fields: `diagnosis`, `prescription_details`, `notes`).
* `/qr_access_grants/{grantId}`: Temporary scoped authorization grants linking patient and verified attending doctor with 15-minute NTP expiration.
* `/active_qr_nonces/{nonce}`: Single-use cryptographic nonces for replay prevention.

## 3. Backward Compatibility & Rollback Plan
* **Backward Compatibility**: Any unencrypted legacy records must be migrated via Cloud Function `encryptExistingPassports` prior to applying deny-unencrypted rule.
* **Rollback Procedure**: Revert to previous rules revision via `firebase deploy --only firestore:rules`.
