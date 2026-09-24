# HEALTHOGRAM

> **Next-Generation Global Healthcare Super-Platform**  
> High-security, scalable, international healthcare super-app unifying patient medical agency, verified medical organization operations, engaging social feeds/reels, certified healthcare marketplace, AI Studio, and real-time communications.

**Current Version:** `2.3.0` (Build `23000`)  
**Package / Application ID:** `com.aistudio.healthogram.hkqvpm`  
**Target & Compile SDK:** Android 16 (API 36) | **Min SDK:** 24 (Android 7.0 Nougat)

---

## 1. Project Overview

Healthogram is an enterprise-grade mobile application designed to bridge the gap between individual health ownership and healthcare provider operations. Built with modern Android (Kotlin, Jetpack Compose, Material 3) and a resilient Firebase serverless backend, Healthogram provides a private, zero-trust architecture for healthcare without sacrificing consumer usability.

### Five Non-Negotiable Account Categories
Healthogram strictly enforces five distinct primary account types:
1. **Individual**: Patients, consumers, creators, and wellness enthusiasts.
2. **Doctor**: Licensed and verified healthcare practitioners with temporary, patient-authorized QR clinical access.
3. **Clinic**: Outpatient clinics and specialized centers with departmental desk management.
4. **Hospital**: Multi-department inpatient medical complexes with institutional oversight.
5. **Laboratory**: Certified diagnostic testing centers authorized to upload specific lab reports (strictly prohibited from owning personal Health Passports).

*Note: Commercial pharmacy entities (`PHARMACY`, `MEDICAL_STORE`, `WHOLESALE`, `SUPPLIER`, `EQUIPMENT_MANUFACTURER`) are strictly prohibited and hard-blocked in the domain layer.*

### Marketplace Roles
1. **Customer**: Verified buyers browsing approved wellness and home medical devices.
2. **Seller**: Licensed distributors operating under isolated vendor stores (`INDIVIDUAL_SELLER`, `BUSINESS_SELLER`) with escrow holds and automated platform commission deductions.

---

## 2. Technology Stack

* **Platform / OS**: Android (minSdk 24, targetSdk 36, compileSdk 36).
* **Language & Runtime**: Kotlin 2.2.21 running on Java 11 bytecode compatibility (Temurin 21 JDK LTS).
* **UI Framework**: Jetpack Compose with Material Design 3 (M3) and edge-to-edge support.
* **Architecture Pattern**: Clean Architecture / MVVM with Kotlin Coroutines & StateFlow.
* **Local Persistence**: Jetpack Room with SQLite & secure Android Keystore caching.
* **Backend Infrastructure**: Google Cloud & Firebase.
* **Database**: Cloud Firestore with field-level encryption (AES-GCM-256) & Realtime Database for presence.
* **Storage**: Firebase Cloud Storage with strict role-based access rules.
* **Perimeter Defense**: Android Play Integrity via Firebase App Check.
* **Realtime Communication**: WebRTC signaling with end-to-end encrypted audio/video channels.
* **Build System**: Gradle 9.3.1 with Android Gradle Plugin (AGP) 8.8.0 and Version Catalog (`gradle/libs.versions.toml`).

---

## 3. Project Structure

```text
healthogram/
├── app/
│   ├── build.gradle.kts             # Module build configuration, dependencies & signing
│   ├── proguard-rules.pro           # R8 / ProGuard optimization rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # Manifest declarations & least-privilege permissions
│       │   ├── java/com/example/healthogram/
│       │   │   ├── admin/           # Platform administration & moderation controls
│       │   │   ├── aistudio/        # AI Studio creative media proxy (zero PHI)
│       │   │   ├── audit/           # Clinical access & immutable audit logging
│       │   │   ├── auth/            # Authentication, session manager, sovereign PIN
│       │   │   ├── communication/   # Messaging & WebRTC teleconsultation
│       │   │   ├── core/            # AccountType, invariants & platform models
│       │   │   ├── delivery/        # Dispatch state machine & proof-of-delivery
│       │   │   ├── designsystem/    # M3 Themes, typography, components
│       │   │   ├── devices/         # Medical device integration & Bluetooth LE
│       │   │   ├── finance/         # Escrow ledger & Owner Earnings engine
│       │   │   ├── healthpassport/  # Encrypted health records & QR access tickets
│       │   │   ├── integration/     # FHIR R4 schema mapper & Health Connect sync
│       │   │   ├── marketplace/     # Catalog, cart, order management, seller stores
│       │   │   ├── notification/    # Push notification dispatch (zero PHI payloads)
│       │   │   ├── organization/    # Clinic, hospital & laboratory desk workflows
│       │   │   ├── owner/           # Owner control panel & governance
│       │   │   ├── payments/        # Checkout tokenization & ledger balance
│       │   │   ├── performance/     # Telemetry & performance monitoring
│       │   │   ├── profile/         # User & institutional profile management
│       │   │   ├── security/        # Play Integrity App Check & crypto vault
│       │   │   ├── social/          # Posts, reels, stories, feed (air-gapped from PHI)
│       │   │   ├── translation/     # Dual English/Arabic bidirectional text & RTL
│       │   │   ├── ui/              # Compose screens & navigation graphs
│       │   │   └── verification/    # Doctor, clinic, hospital license KYC
│       │   └── res/                 # Vector drawables, strings, theme XMLs
│       └── test/                    # Robolectric & JUnit unit test suites (37 suites)
├── gradle/
│   └── libs.versions.toml           # Gradle Version Catalog
├── scripts/                         # Build, CI, environment validation scripts
├── firestore.rules.v2.3             # Production Cloud Firestore security rules
├── storage.rules.v2.3               # Production Cloud Storage security rules
├── PRODUCTION_REMOTE_CONFIG_v2.3.0.json # Remote Config parameter defaults
├── release-manifest-2.3.0.json      # Official release metadata & invariants
├── .gitignore                       # Repository ignore rules (secrets & build outputs)
├── build.gradle.kts                 # Root Gradle build script
├── settings.gradle.kts              # Root project settings
└── README.md                        # Project documentation
```

---

## 4. Firebase Backend Architecture

1. **Authentication**:
   * Firebase Auth with phone SMS, email link, and multi-factor authentication.
   * Multi-device session cap: maximum 4 concurrent active sessions with real-time push eviction.
   * 15-minute idle session timeout.

2. **Cloud Firestore**:
   * Isolated `health_passports/{patientUid}` collection protected by role-based rules (`firestore.rules.v2.3`).
   * Read access requires valid ephemeral ticket (`health_access_tickets/{ticketId}`) signed by the patient.
   * Marketplace collections (`products`, `orders`, `escrow_ledger`) enforce vendor-isolation rules.

3. **Cloud Storage**:
   * Strict 25MB upload ceiling per file.
   * Medical documents stored under encrypted, patient-permissioned paths (`storage.rules.v2.3`).

4. **App Check**:
   * Enforces Google Play Integrity attestation for all production backend traffic.

5. **Cloud Messaging (FCM)**:
   * System notifications strictly contain generic preview text.
   * Zero raw personal health information (PHI) is ever transmitted via FCM payloads.

---

## 5. Build Instructions

### Prerequisites
* JDK 17 or JDK 21 (Temurin LTS recommended)
* Android SDK 36 (Build Tools 36.0.0)
* Gradle 9.3.1 (or use `./gradlew`)

### Quick Validation & Local Build
```bash
# 1. Validate environment
./scripts/verify_environment.sh

# 2. Run test suite
gradle :app:testDebugUnitTest

# 3. Build Debug APK
gradle :app:assembleDebug
```

---

## 6. Android Production Build (APK & AAB)

### Environment Variables for Signing
For production signing, export the following environment variables:
```bash
export KEYSTORE_PATH="/path/to/production.keystore"
export STORE_PASSWORD="your-keystore-password"
export KEY_PASSWORD="your-key-password"
export KEY_ALIAS="healthogram-release"
```

### Compiling Production Release Artifacts
```bash
# Compile signed production APK
gradle :app:assembleRelease

# Compile signed production Android App Bundle (AAB)
gradle :app:bundleRelease
```

Generated outputs:
* APK: `app/build/outputs/apk/release/app-release.apk`
* AAB: `app/build/outputs/bundle/release/app-release.aab`

### Verifying APK Signature
```bash
/opt/android/sdk/build-tools/36.0.0/apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
```

---

## 7. Environments: Development, Staging & Production

| Environment | Purpose | Database / Config | App Check |
| :--- | :--- | :--- | :--- |
| **Development** | Feature branches & local emulation | `healthogram-dev` | Debug Provider |
| **Staging** | Release candidates & internal testing | `healthogram-staging` | Custom / Debug |
| **Production** | Live Google Play deployment | `healthogram-prod-2026` | Play Integrity |

---

## 8. Security & Privacy Notes

* **Client Envelope Encryption**: Diagnostic notes, prescriptions, and lab records are encrypted using AES-GCM-256 before write operations.
* **Ephemeral Dynamic QR Access**: QR codes display time-bound cryptographic tokens with a 60-second refresh and 15-minute validity window.
* **Zero Storage Permissions**: Fully compliant with Google Play Developer policies by utilizing the zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
* **Teleconsultation Policy**: Peer-to-peer WebRTC video/audio calls with strict auto-record prohibition.
* **Escrow Ledger**: Marketplace transactions feature a 7-day delivery escrow hold prior to seller payout.

---

## 9. Google Play Release Instructions

1. Log in to the [Google Play Console](https://play.google.com/console).
2. Select **Healthogram** > **Production** (or **Closed Testing**).
3. Click **Create new release**.
4. Upload `app-release.aab` (`Healthogram-2.3.0-release.aab`).
5. Verify version code (`23000`) and target API (`36`).
6. Apply release notes from `Healthogram-v2.3.0-release-notes.md`.
7. Configure rollout percentage (recommended 5.0% canary rollout).
8. Submit release for review.

---

## 10. License & Copyright

Copyright © 2026 Healthogram Inc. All rights reserved.  
Confidential and Proprietary.
