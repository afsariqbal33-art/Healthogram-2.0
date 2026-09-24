# 24 — PRODUCTION DEPLOYMENT & CI/CD PIPELINE

## 1. Toolchain & Build Environment
* **Language & SDK:** Kotlin 2.2.10 / OpenJDK 21.0.12+8-LTS
* **Build System:** Gradle 9.3.1 / Android Gradle Plugin (AGP) 9.1.1
* **UI Framework:** Jetpack Compose Multiplatform / Kotlin Compose Compiler
* **Android Target:** API 36 (Android 16 Mandatory Forward Compliance)
* **Compile SDK:** 36 (minorApiLevel = 1) | **Min SDK:** 24 (Android 7.0+)

## 2. Automated Pipeline Steps
1. **Lint & Static Analysis:** Checks syntax, unused resources, and accessibility semantics.
2. **Automated Unit & Robolectric Tests:** Executes full 472-test regression suite.
3. **Secret Scan:** Scans repository for exposed API keys, private keys, or credentials.
4. **Release Bundle Compilation:** Executes `gradle :app:bundleRelease` to produce `Healthogram-2.3.0-release.aab`.
5. **Cryptographic Checksumming:** Generates SHA-256 hash manifest.
6. **Artifact Archiving:** Packages clean source archive and release binaries.
