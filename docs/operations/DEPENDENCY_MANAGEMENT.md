# HEALTHOGRAM — DEPENDENCY MANAGEMENT & SECURITY HYGIENE

**Classification:** Build Engineering & Supply Chain Security  
**Target:** Gradle Version Catalog (`gradle/libs.versions.toml`)  

---

## 1. Version Catalog Standards

All dependencies, plugins, and libraries must be declared centrally in `gradle/libs.versions.toml`. Hardcoding dependency version strings in `app/build.gradle.kts` is strictly forbidden.

### Core Dependency Stacks:
- **AndroidX & Jetpack Compose:** Compose BOM, Navigation Compose, Lifecycle ViewModel Compose, Core KTX.
- **Kotlin & Tooling:** Kotlin 2.1.0, KSP, Coroutines 1.9.0.
- **Firebase Enterprise:** Firebase BOM 33.7.0, Auth, Firestore, Storage, Functions, Messaging, Crashlytics, Analytics, Remote Config, App Check Play Integrity.
- **Image & UI Loading:** Coil 2.7.0.
- **Local Database:** Room 2.6.1 (with KSP code generation).
- **Testing & Quality:** JUnit 4.13.2, Robolectric 4.14.1, Roborazzi 1.39.0.

---

## 2. Monthly Dependency Update Protocol

1. **Step 1 — Vulnerability Review:**
   Review open-source security advisories (CVEs) affecting existing dependencies.
2. **Step 2 — Branch Creation:**
   Create dependency review branch: `git checkout -b chore/dependency-update-YYYYMM`.
3. **Step 3 — Surgical Updates:**
   Update compatible patch/minor versions in `gradle/libs.versions.toml`. Never bump all dependencies at once.
4. **Step 4 — Automated Test Suite Execution:**
   ```bash
   gradle :app:testDebugUnitTest
   ```
5. **Step 5 — Smoke Test Verification:**
   ```bash
   gradle :app:testDebugUnitTest --tests com.example.healthogram.qa.ProductionSmokeTestSuiteTest
   ```
6. **Step 6 — Production Build Verification:**
   ```bash
   gradle :app:bundleRelease
   ```
7. **Step 7 — Merge & Deploy:**
   Merge into `develop` for internal testing.
