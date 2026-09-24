# 01 — PRODUCT OVERVIEW

## 1. Product Vision & Mission
Healthogram 2.3 is an enterprise-grade digital healthcare and wellness super-platform that unites sovereign personal health record management with accredited medical care discovery, verified wellness commerce, and private teleconsultations.

## 2. Core Account Model
Healthogram enforces strict domain boundaries with exactly five primary healthcare account categories:
1. **Individual:** Patient or wellness user with an encrypted personal Health Passport.
2. **Doctor:** Licensed medical practitioner managing clinical appointments and consented patient record reviews.
3. **Clinic:** Outpatient healthcare facility coordinating multiple doctors, specialties, and clinical slots.
4. **Hospital:** Inpatient/outpatient medical center managing comprehensive clinical departments and medical staff.
5. **Laboratory:** Diagnostic testing center managing laboratory catalog, specimen intake, and cryptographically signed result reporting.

*Prohibited Account Categories:* The platform strictly rejects `PHARMACY`, `MEDICAL_STORE`, `MEDICINE_COMPANY`, `WHOLESALE`, `SUPPLIER`, `EQUIPMENT_MANUFACTURER`, and `EQUIPMENT_SUPPLIER` from the primary healthcare taxonomy.

## 3. Marketplace Model
Marketplace operates under independent commercial roles:
* **Customer:** Browses healthcare and wellness items, places orders, tracks delivery.
* **Seller:** Onboarded as either an `Individual Seller` or a `Business Seller` with KYC verification and escrow accounting.

## 4. Key Platform Capabilities
* **Sovereign Health Passport:** Client-side AES-GCM-256 envelope encryption.
* **One-Time QR Consent:** 60-second TTL single-use tokens; zero raw medical data in the QR matrix.
* **Clinical Interoperability:** HL7 FHIR R4 schema mapping and Android Health Connect on-device sync.
* **Double-Entry Financial Ledger:** Zero unbacked balance generation; strict 7-day seller escrow holds.
* **Encrypted Teleconsultations:** Peer-to-peer WebRTC video/audio with strict no-auto-record policy.
* **Android 16 Native:** Jetpack Compose Material 3 with adaptive layouts and native Arabic RTL support.
