# HEALTHOGRAM — GOOGLE PLAY COMPLIANCE & POLICY AUDIT

**Audit Date:** September 16, 2026  
**Auditor:** Google Play Release & Policy Compliance Engineer  
**Reference Guidelines:** Google Play Developer Program Policies (2026 Edition)  

---

## 1. Android Target API Level Compliance

- **Google Play Requirement (Effective August 31, 2026):** New apps and updates must target API level 36 (`targetSdkVersion = 36`) or higher.
- **Healthogram Target SDK:** `targetSdk = 36` (Configured in `app/build.gradle.kts`).
- **Healthogram Compile SDK:** `compileSdk = 36` with minor release `1`.
- **Status:** **PASS (COMPLIANT)**

---

## 2. Google Play Health Apps Declaration

Google Play requires developers of health-related applications to complete the dedicated Health Apps Declaration in the Play Console.

### Health Category Declarations for Healthogram:
1. **Health Records & Management:**
   - *Declared Functionality:* Digital personal health records storage and authorized sharing (Health Passport).
   - *Clinical Disclaimer:* Explicitly stated in app and store listing that Healthogram is an organizational tool and not a replacement for professional medical diagnosis or treatment.
2. **Telehealth & Virtual Consultations:**
   - *Declared Functionality:* Secure WebRTC audio and video communication between patients and licensed healthcare professionals.
3. **Medical Supply & Wellness Marketplace:**
   - *Declared Functionality:* Marketplace for verified health wellness items. Prohibited pharmaceuticals and controlled substances are strictly excluded.
4. **Android Health Connect Integration:**
   - *Decision:* **NOT INTEGRATED in v1.0.0.** No Health Connect permissions (`androidx.health.connect.client.permission`) are requested. Health Connect policy requirements are not applicable for v1.0.0 release.

---

## 3. Google Play Permissions Audit

| Permission | Category | Justification | Policy Compliance Status |
| :--- | :--- | :--- | :--- |
| `android.permission.INTERNET` | Normal | Core network communication with Firebase and API services. | **COMPLIANT** |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Connection monitoring for offline mode and media caching. | **COMPLIANT** |
| `android.permission.CAMERA` | Runtime | QR scanning for Health Passport, teleconsultation video calls, profile photos. | **COMPLIANT** (Requested in-context) |
| `android.permission.RECORD_AUDIO` | Runtime | Voice messaging, audio/video teleconsultations. | **COMPLIANT** (Requested in-context) |
| `android.permission.POST_NOTIFICATIONS` | Runtime | Order status updates, appointment reminders, access request alerts. | **COMPLIANT** (Requested in-context) |
| Broad Storage (`READ_EXTERNAL_STORAGE`) | **REMOVED** | Zero broad storage permissions requested. Uses Android Photo Picker API. | **COMPLIANT (ZERO PERMISSION)** |

---

## 4. Google Play Data Safety Form Mapping

| Data Category | Data Element | Collected? | Shared? | Purpose | Ephemeral / Encrypted? | Deletion Supported? |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Personal Info** | Name, Email, Phone Number | Yes | No | Account management, authentication | Encrypted in transit | Yes |
| **Financial Info** | Purchase history, Transaction IDs | Yes | Payment Processor | Order processing, seller payouts | Encrypted in transit & at rest | Retained per tax law |
| **Health Info** | Health Passport records, allergies | Yes (Optional) | Explicitly Granted Healthcare Accounts | Personal health records management | Encrypted in transit & at rest | Yes |
| **Messages** | In-app direct messages | Yes | Intended recipient only | User-to-user communication | Encrypted in transit | Yes |
| **Photos & Videos** | Avatars, post media, medical docs | Yes (User uploaded) | Designated viewers | Social sharing, health record vault | Encrypted in transit & at rest | Yes |
| **App Activity** | Feature interactions, navigation | Yes | No | Analytics, app improvement | Encrypted in transit | Yes |
| **Diagnostics** | Crash logs, ANR stack traces | Yes | Firebase Crashlytics | Performance and bug fixing | Encrypted in transit (No PII) | Automatic 90-day expiry |

---

## 5. Play Policy Compliance Verdict

**GOOGLE PLAY COMPLIANCE STATUS: FULLY COMPLIANT**  
Target SDK 36 verified, zero forbidden permissions, comprehensive health declarations prepared.
