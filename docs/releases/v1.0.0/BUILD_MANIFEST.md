# HEALTHOGRAM — PRODUCTION BUILD MANIFEST v1.0.0

**Release Tag:** `v1.0.0`  
**Base Commit SHA:** `cb96ef814d77ff83ce7e26b8df2aa2ac2ff0fa60`  
**Build Date:** September 16, 2026  
**Target Environment:** Production (`healthogram-prod`)  
**Development Platform:** Gemini 3.8 Flash  

---

## 1. Build Specifications

| Parameter | Value |
| :--- | :--- |
| **Application Name** | Healthogram |
| **Application ID** | `com.aistudio.healthogram.hkqvpm` |
| **Package / Namespace** | `com.example` |
| **Version Name** | `1.0.0` |
| **Version Code** | `1` |
| **Target SDK Version** | `36` (Android 16 / Vanilla Ice Cream) |
| **Compile SDK Version** | `36` (Release minorApiLevel 1) |
| **Minimum SDK Version** | `24` (Android 7.0 Nougat) |
| **Kotlin Version** | `2.1.0` |
| **Gradle Version** | `8.9` |
| **Android Gradle Plugin**| `8.7.0` |
| **JDK Version** | OpenJDK 17.0.12 |
| **Architecture** | Native Android (Kotlin + Jetpack Compose + M3) |

---

## 2. Release Artifacts

| Artifact Type | File Path | Build Command | Purpose |
| :--- | :--- | :--- | :--- |
| **Android App Bundle (AAB)** | `app/build/outputs/bundle/release/app-release.aab` | `gradle :app:bundleRelease` | Primary Google Play submission artifact |
| **Release APK** | `app/build/outputs/apk/release/app-release.apk` | `gradle :app:assembleRelease` | Direct distribution & sideload QA testing |
| **Debug APK** | `app/build/outputs/apk/debug/app-debug.apk` | `gradle :app:assembleDebug` | Internal emulator and dev testing |

---

## 3. Cryptographic Signing & Integrity

- **Signing Scheme:** APK Signature Scheme v2 + v3 enabled.
- **Key Alias:** `upload` (Google Play App Signing active).
- **App Check Provider:** Play Integrity API enabled with release SHA-256 fingerprint.
- **ProGuard / R8:** Rule configuration active (`proguard-rules.pro`), shrinking and obfuscation enabled for release builds.
