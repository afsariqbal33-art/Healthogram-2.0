# Healthogram Release Configuration

This centralized document tracks the operational release configuration for Healthogram production builds.

## 1. Release Identification

| Parameter | Configuration Value |
| :--- | :--- |
| **Application Name** | `Healthogram` |
| **Application ID** | `com.aistudio.healthogram.hkqvpm` |
| **Package Name / Namespace** | `com.example` |
| **Release Version (versionName)** | `1.0.0` |
| **Internal Build Code (versionCode)** | `1` |
| **Release Tag** | `v1.0.0` |
| **Git Release Branch** | `release/1.0.0` |
| **Commit Target** | HEAD of `release/1.0.0` |

---

## 2. Platform & Toolchain Specifications

| Component | Target Version |
| :--- | :--- |
| **Operating System** | Android 7.0 (API 24) to Android 15/16 (API 36) |
| **Minimum SDK (`minSdk`)** | `24` |
| **Target SDK (`targetSdk`)** | `36` |
| **Compile SDK (`compileSdk`)** | `36` (minorApiLevel 1) |
| **Build System** | Gradle (Kotlin DSL) |
| **Java Bytecode Target** | Java 11 (`JavaVersion.VERSION_11`) |
| **Java JDK Runtime** | OpenJDK 21 LTS / 17 LTS |
| **Kotlin Version** | 2.0+ (Compose Compiler Plugin aligned) |
| **Gradle Plugins** | Android Application, Compose Compiler, KSP, Secrets, Roborazzi, Google Services |

---

## 3. Signing Architecture

### Google Play App Signing Integration
Healthogram adheres to the official Google Play App Signing model:
1. **Upload Keystore**: Used by developers / CI to sign artifacts (`.aab`) before transmission to the Google Play Console.
   - Keystore path: `${KEYSTORE_PATH}` or GitHub Actions secret `ANDROID_KEYSTORE_BASE64`
   - Key alias: `upload`
   - Algorithm: RSA 4096-bit or 2048-bit with SHA-256
2. **Play App Signing Key**: Maintained in Google's secure Cloud Key Management Service (KMS). Google Play strips the upload signature, verifies its provenance, and signs the user-facing APKs delivered to devices.
3. **Emergency Key Reset**: Documented in Google Play Console under **Setup > App Integrity > Request key upgrade / upload key reset**.

---

## 4. Production Firebase Target

- **Project ID**: `healthogram-prod`
- **Configuration File**: `app/google-services.json` (Production profile)
- **App Check Provider**: Play Integrity API
- **FCM Server**: High-priority push notifications with medical privacy sanitization
- **Database Rules**: Zero-trust Firestore rules (`firestore.rules`)
- **Storage Rules**: Healthcare authorization scoped Storage rules (`storage.rules`)

---

## 5. Build Artifact Targets

| Artifact | Gradle Command | Output Path | Usage |
| :--- | :--- | :--- | :--- |
| **Production AAB** | `gradle :app:bundleRelease` | `app/build/outputs/bundle/release/app-release.aab` | Google Play Console (Internal, Closed, Production) |
| **Release APK** | `gradle :app:assembleRelease` | `app/build/outputs/apk/release/app-release.apk` | Enterprise sideload / Private QA testing |
| **Debug APK** | `gradle :app:assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` | Day-to-day developer debugging & test logs |
