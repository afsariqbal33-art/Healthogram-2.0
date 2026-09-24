# HEALTHOGRAM 2.3.0 GOOGLE PLAY POLICY & COMPLIANCE DECLARATIONS

**Release Version:** 2.3.0 (versionCode 23000)  
**Target API:** 36 (Android 16 Mandatory Forward Compliance)  
**Date:** 2026-09-23  
**Status:** FULLY COMPLIANT & VERIFIED  

---

## 1. Google Play Health Apps Declaration (Play Console -> Policy -> App Content -> Health Apps)

### Applicable Categories Selected
Healthogram accurately declares the actual health features implemented in the application:
1. **Diseases and Conditions Management:** Patient-controlled tracking of chronic and acute health conditions, allergies, and diagnoses under licensed clinical consultation.
2. **Medication and Treatment Management:** Active medication schedules, dosages, treatment plans, and doctor-prescribed therapy reminders.
3. **Healthcare Services and Management:** Patient-to-provider appointment booking, clinical consultations with verified doctors, clinics, hospitals, and laboratories, and patient-consented clinical health record exchange.

### Explicit Medical Disclaimers & Exclusions (NO Medical Device Claims)
* **Non-Medical Device (SaMD):** Healthogram is **NOT** a Software as a Medical Device (SaMD) and is not intended to diagnose, treat, cure, or prevent any disease without clinician evaluation.
* **No Automated Clinical Diagnoses:** The app does not make autonomous clinical diagnostic assertions. All medical recommendations require human clinical review by a licensed healthcare professional.
* **No Emergency Medical Service:** Healthogram is not an emergency response or 911 dispatch service. A prominent notice instructs users to contact emergency services in acute emergencies.
* **No Unsupported Medical Claims:** Marketing, store copy, and in-app text avoid promotional buzzwords ("100% cure", "miracle treatment", "guaranteed outcome").

---

## 2. Privacy Policy & Public Accessibility

* **Public Web Privacy Policy URL:** `https://healthogram.app/privacy`
* **In-App Direct Access:** `Settings -> Privacy & Security -> Privacy Policy`
* **Accessibility Characteristics:**
  - Accessible via standard HTTPS web browser without authentication, paywall, or geoblocking.
  - Rendered in clean, responsive HTML (not PDF-only).
  - Explicit disclosures covering:
    - Personal data collection and identity verification
    - Zero-trust Health Passport storage and AES-GCM-256 client-side envelope encryption
    - Scoped, time-bound consent management
    - Health Connect and HL7 FHIR R4 interoperability boundaries
    - In-app marketplace purchases, seller payouts, and escrow hold policies
    - Zero PHI sharing with third-party advertisers or AI model training pipelines
    - Account deletion rights and data retention requirements

---

## 3. Google Play Data Safety Declaration

Healthogram collects and processes only the data strictly necessary for application functionality, always with patient/user consent.

| Data Category | Specific Data Types | Purpose | Collected / Shared | Encrypted in Transit & at Rest | Deletion Available |
| :--- | :--- | :--- | :---: | :---: | :---: |
| **Health & Fitness** | Health records, conditions, medications, vitals, allergies | App functionality (Health Passport, clinical care) | Collected (User-entered, Consent-gated) / Never Shared with 3rd parties | YES (TLS 1.3 / AES-GCM-256) | YES |
| **Personal Info** | Name, email address, phone number, account category | Account management, authentication, provider verification | Collected / Never sold | YES (TLS 1.3) | YES |
| **Financial Info** | Payment transaction IDs, seller bank accounts | In-app purchases, seller payouts, tax compliance | Collected (Tokenized via PCI-DSS gateways) / Not Shared | YES (TLS 1.3 / PCI-DSS Level 1) | Financial records retained per legal audit rules |
| **Messages** | In-app doctor-patient and peer-to-peer chats | In-app communication & teleconsultation | Collected / Not Shared | YES (TLS 1.3) | YES |
| **Photos & Videos** | Medical document uploads, profile photos, social posts | App functionality & user social feed | Collected (User-uploaded) / Not Shared | YES (TLS 1.3) | YES |
| **Audio** | Voice notes, teleconsultation audio | Real-time calling & voice messages | Processed ephemeral in WebRTC / Not recorded without notice | YES (DTLS / SRTP) | YES |
| **App Info & Performance** | Crash logs, ANR diagnostics, app interactions | App stability & crash reduction | Collected (Zero PHI, anonymized) | YES (TLS 1.3) | Retained 90 days max |
| **Device IDs** | Firebase installation ID, FCM push token | Push notifications & device session ceiling (max 4) | Collected / Not Shared | YES (TLS 1.3) | Cleared on logout / account deletion |

---

## 4. Google Play Account Deletion Compliance

* **In-App Direct Deletion:** Located at `Settings -> Privacy & Security -> Account Management -> Delete Account`.
* **Web Deletion Request URL:** `https://healthogram.app/account/delete`
* **Deletion Execution Flow:**
  1. User re-authenticates via password or sovereign MFA PIN.
  2. The application explains data deletion consequences: all profile data, personal media, chat history, and Health Passport cryptographic keys will be permanently destroyed.
  3. System terminates and revokes all active device sessions across all devices.
  4. Firebase Authentication record is deleted.
  5. Cloud Firestore user profiles, social posts, reels, and Health Passport vaults are cryptographically shredded.
  6. Financial ledger entries and consent audit trails are scrubbed of personal identifiers and preserved in immutable anonymized format strictly to satisfy statutory tax, medical audit, and anti-money laundering (AML) regulatory retention mandates.

---

## 5. Google Play Permissions & System Integrity Compliance

* **Zero-Permission Photo Picker:** Healthogram uses Android’s Photo Picker (`ActivityResultContracts.PickVisualMedia`) for document and media selection. The broad `READ_EXTERNAL_STORAGE` and `READ_MEDIA_IMAGES` permissions are **NOT** requested.
* **Least-Privilege Hardware:** `CAMERA`, `RECORD_AUDIO`, and `POST_NOTIFICATIONS` are requested at runtime with clear, contextual user rationales. All hardware features are marked `android:required="false"` to prevent restricting app availability.
* **No Dynamic Code Loading (DCL):** Healthogram contains zero runtime-loaded `.dex`, `.jar`, or `.so` files from remote servers, fully complying with Google Play system integrity policies.
