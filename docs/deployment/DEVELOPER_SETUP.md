# HEALTHOGRAM DEVELOPER ONBOARDING & ENVIRONMENT SETUP

**Document:** `docs/deployment/DEVELOPER_SETUP.md`  
**Target Audience:** Android Engineers, Backend DevOps, QA Engineers  
**Target Architecture:** Modern Android (Kotlin / Jetpack Compose / M3) + Firebase 2nd Gen + Cloud Functions

---

## 1. Prerequisites & Required Toolchain

| Tool / Runtime | Minimum Version | Recommended Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Java Development Kit (JDK)** | OpenJDK 17 | Eclipse Temurin 17.0.10+ | Gradle compilation & Android build |
| **Android Studio** | Hedgehog (2023.1.1) | Ladybug (2024.2.1+) | Primary IDE & Compose Preview |
| **Android SDK Platform** | API 34 (Android 14) | API 34 + Build-Tools 34.0.0 | Core compilation target |
| **Node.js** | v18.0.0 | v20.12.0 LTS | Cloud Functions execution & testing |
| **npm** | v9.0.0 | v10.5.0+ | Node package management |
| **Firebase CLI** | v12.0.0 | v13.8.0+ | Security rules, functions, emulators |
| **Git** | v2.38.0 | v2.44.0+ | Source-code management & CI/CD |

---

## 2. Initial Repository Setup

1. **Clone the Private Repository:**
   ```bash
   git clone git@github.com:healthogram-enterprise/healthogram-app.git
   cd healthogram-app
   ```

2. **Configure Git Identity and Pre-commit Hooks:**
   ```bash
   git config user.name "Your Full Name"
   git config user.email "your.name@healthogram.com"
   chmod +x ./scripts/*.sh
   ```

3. **Initialize Environment Variables:**
   ```bash
   cp .env.example .env
   ```
   *Edit `.env` to supply local development configuration. NEVER commit `.env` to Git.*

---

## 3. Firebase Local Emulators Setup

1. **Install Firebase CLI Tools:**
   ```bash
   npm install -g firebase-tools
   ```

2. **Log into Authorized Developer Account:**
   ```bash
   firebase login
   ```

3. **Install Cloud Functions Dependencies:**
   ```bash
   cd functions && npm install && cd ..
   ```

4. **Start Firebase Local Emulators (Auth, Firestore, Storage, Functions):**
   ```bash
   firebase emulators:start --only firestore,auth,storage,functions
   ```

---

## 4. Building and Running the App

### Running in Android Studio
1. Open the repository root folder in **Android Studio**.
2. Wait for Gradle Sync to complete (`settings.gradle.kts` and `build.gradle.kts`).
3. Select an Android Emulator or Physical Device running Android 8.0+ (API 26+).
4. Run target: `app`.

### Command Line Build Commands

* **Compile and Validate Applet:**
  ```bash
  gradle :app:compileDebugKotlin
  ```

* **Execute Local Unit and QA Acceptance Tests:**
  ```bash
  gradle :app:testDebugUnitTest
  ```

* **Generate Development Debug APK:**
  ```bash
  gradle :app:assembleDebug
  # Output: app/build/outputs/apk/debug/app-debug.apk
  ```

* **Generate Production Signed Release AAB (Requires Keystore Vault Access):**
  ```bash
  gradle :app:bundleRelease
  # Output: app/build/outputs/bundle/release/app-release.aab
  ```

---

## 5. Security & Contribution Safeguards
- **Zero Real Health Data**: Only use synthetic test fixtures (`QA-IND-001`, `QA-DOC-001`, etc.).
- **Zero Direct Commits to `main` or `develop`**: All code must arrive via Pull Request.
- **Strict Lint Verification**: Code must pass `gradle lintDebug` with 0 errors before submitting PR.
