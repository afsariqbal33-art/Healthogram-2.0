# Healthogram Android Build Architecture

## Executive Summary

| Attribute | Specification |
| :--- | :--- |
| **Development Platform** | Gemini 3.8 Flash AI Assistant |
| **Source of Truth** | GitHub Git Repository (`main` / `release/*` branches) |
| **Project Type** | Native Android (Kotlin + Jetpack Compose) |
| **UI Framework** | Jetpack Compose + Material Design 3 (M3) |
| **Build System** | Gradle (Kotlin DSL - `.gradle.kts`) |
| **Application ID** | `com.aistudio.healthogram.hkqvpm` (FROZEN) |
| **Package Name / Namespace** | `com.example` |
| **Application Name** | `Healthogram` |
| **Compile SDK** | 36 (minorApiLevel 1) |
| **Target SDK** | 36 |
| **Minimum SDK** | 24 (Android 7.0 Nougat) |
| **Java / JVM Compatibility** | Java 11 bytecode compatibility, OpenJDK 21/17 toolchain |
| **Version Name** | `1.0.0` |
| **Version Code** | `1` |

---

## 1. Project Type Verification

An exhaustive repository inspection confirms the project architecture:

- `pubspec.yaml` is **absent** (No Flutter dependencies or FlutterFlow frameworks).
- `package.json` in root is **absent** (No React Native or web-hybrid wrapper).
- `settings.gradle.kts` defines `rootProject.name = "Healthogram"` and modules `:app`.
- `app/build.gradle.kts` configures the Android Application plugin (`com.android.application`), Compose compiler plugin, Kotlin Symbol Processing (`ksp`), and Secrets Gradle Plugin.
- All application user interfaces, domain engines, state management, and screens are written in idiomatic Kotlin with Jetpack Compose.

---

## 2. Gemini 3.8 Flash Development Workflow

```text
[Developer Instructions]
         ↓
[Gemini 3.8 Flash Assistant]
         ↓
[Generate / Update Kotlin Source & Resources]
         ↓
[Local Validation via compile_applet / unit tests]
         ↓
[Git Diff Code Review & Security Sanity Check]
         ↓
[Git Commit to feature/* or develop]
         ↓
[GitHub Repository (Canonical Source of Truth)]
         ↓
[GitHub Actions CI/CD Pipeline]
         ↓
[Android Release Artifacts: APK / AAB]
         ↓
[Google Play Console (Internal → Closed → Production)]
```

### Development Mandates
1. **GitHub is the Source of Truth**: Gemini generated suggestions must be committed to Git. No ephemeral uncommitted code is treated as production.
2. **Strict Scope Discipline**: Never remove existing modules or features.
3. **Security Safeguards**: Gemini must never expose API keys, bypass App Check, weaken Firestore rules, or leak Health Passport medical records.
4. **Account Categories & Roles Preserved**:
   - Account Categories: `Individual`, `Doctor`, `Clinic`, `Hospital`, `Laboratory`
   - Marketplace Roles: `Customer`, `Seller`

---

## 3. Environment Separation Matrix

| Configuration | Development | Staging | Production |
| :--- | :--- | :--- | :--- |
| **Target Artifact** | `Healthogram-debug.apk` | `Healthogram-staging.apk` | `Healthogram-v1.0.0-release.aab` |
| **Firebase Project** | `healthogram-dev` | `healthogram-staging` | `healthogram-prod` |
| **Signing Key** | Android Debug Keystore | Staging Keystore / Debug Config | Play Store Upload Key + Google Play App Signing |
| **App Check** | Debug Provider with test token | SafetyNet / Play Integrity (Test mode) | Play Integrity Provider (Enforced) |
| **Payments Mode** | Mock / Sandboxed Gateway | Payment Sandbox / Test Cards | Production Gateway (Server-side webhooks) |
| **Delivery Mode** | Simulated test rates | Sandboxed Carrier Webhooks | Live Shipping Carrier APIs |
| **AI Studio** | Dev Quotas & Test Models | Staging Quotas | Dedicated Production Quota (Tier 1) |

---

## 4. Build Artifact Output Locations

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release AAB (Primary Play Store Target)**: `app/build/outputs/bundle/release/app-release.aab`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk` or signed `app-release.apk`
