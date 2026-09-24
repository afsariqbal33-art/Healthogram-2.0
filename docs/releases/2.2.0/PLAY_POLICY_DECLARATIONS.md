# HEALTHOGRAM 2.2.0 GOOGLE PLAY POLICY & COMPLIANCE DECLARATIONS

**Release Version:** 2.2.0 (versionCode 20201)  
**Target API:** 36 (Android 16)  
**Date:** 2026-09-21  

---

## 1. Health Apps Declaration (Play Console -> Policy -> App Content -> Health Apps)
* **Designated Health Categories:**
  - `Healthcare services and management` (Provider appointments, clinic consultations)
  - `Medical records and documents` (Health Passport personal health record storage)
  - `Medication and treatment management` (Prescription reminders, dosage logs)
* **Categories Explicitly Excluded (NOT claimed):**
  - Regulated Software as a Medical Device (SaMD) / Diagnostic device claims: `NONE`
  - Emergency 911 dispatch service: `NONE`
* **Health Declarations Verification:**
  - All health functionality is patient-consented and provider-verified.
  - Zero medical diagnosis algorithms claiming diagnostic certainty without licensed clinician review.

---

## 2. Privacy Policy & In-App Access
* **Public URL:** `https://healthogram.app/privacy`
* **In-App Navigation:** `Settings -> Privacy Settings -> View Privacy Policy`
* **Characteristics:** Publicly accessible, responsive HTML (not PDF-only), no login barrier, zero geofencing.
* **Scope Covered:** Health Passport, FHIR R4 interoperability, Health Connect, marketplace payments, social feeds, WebRTC calls, AI Studio isolation, and complete data deletion.

---

## 3. Google Play Data Safety Audit
| Data Type | Purpose | Collected / Shared | Encrypted | Deletion |
| :--- | :--- | :--- | :---: | :---: |
| **Health Info (Medical Records)** | App Functionality (Health Passport) | Collected (Optional) / Not Shared | AES-256 / TLS | Yes |
| **Personal Info (Name, Email, Phone)** | Account Mgmt / Authentication | Collected (Required) / Not Shared | TLS | Yes |
| **Financial Info (Payment / History)** | In-App Purchases / Marketplace | Collected (Required) / Tokenized | PCI-DSS / TLS | Legal Retention |
| **Messages & Communications** | Patient-Doctor Chat | Collected (Optional) / Not Shared | TLS | Yes |
| **Photos, Videos & Audio** | Profile, Teleconsultation | Collected (Optional) / Not Shared | TLS | Yes |
| **App Performance & Crash Logs** | Analytics & Crashlytics | Collected / Diagnostics | TLS | 90-day TTL |

---

## 4. Account Deletion Fulfillment (Play Policy Compliance)
* **In-App Deletion Path:** `Settings -> Account Management -> Delete Account`
* **Web Deletion Request URL:** `https://healthogram.app/account/delete`
* **Deletion Lifecycle:**
  1. Re-authentication with password/OTP.
  2. Immediate revocation of active sessions and Firebase Auth deletion.
  3. Immediate cryptographic erasure of Health Passport AES keys and documents.
  4. Anonymization of financial ledger transaction records to preserve statutory tax/audit double-entry integrity.
