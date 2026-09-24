# Healthogram Dependency Audit & Vulnerability Assessment

## Version 1.0.0 Release Evaluation

This document provides a pre-release audit of all direct dependencies and Gradle plugins bundled in Healthogram v1.0.0.

---

## 1. Direct Dependency Catalog

| Dependency / Library | Group / Artifact | Category | Status | Security Rating |
| :--- | :--- | :--- | :--- | :--- |
| **AndroidX Activity Compose** | `androidx.activity:activity-compose` | Core UI | Active | Stable / Low Risk |
| **Compose BOM** | `androidx.compose:compose-bom` | UI Architecture | Active | Stable / Low Risk |
| **Material 3** | `androidx.compose.material3:material3` | Design System | Active | Stable / Low Risk |
| **Material Icons Extended** | `androidx.compose.material:material-icons-extended` | UI Icons | Active | Stable / Low Risk |
| **AndroidX Core KTX** | `androidx.core:core-ktx` | Android Platform | Active | Stable / Low Risk |
| **Lifecycle & ViewModel** | `androidx.lifecycle:lifecycle-viewmodel-compose` | State Management | Active | Stable / Low Risk |
| **Navigation Compose** | `androidx.navigation:navigation-compose` | App Routing | Active | Stable / Low Risk |
| **Room KTX & Runtime** | `androidx.room:room-runtime`, `androidx.room:room-ktx` | Local Persistence | Active | Stable / Low Risk |
| **Firebase BOM** | `com.google.firebase:firebase-bom` | Backend Platform | Active | Stable / Low Risk |
| **Firebase Auth** | `com.google.firebase:firebase-auth` | Identity & Access | Active | Stable / Low Risk |
| **Firebase Firestore** | `com.google.firebase:firebase-firestore` | Cloud Database | Active | Stable / Low Risk |
| **Firebase Storage** | `com.google.firebase:firebase-storage` | Object Storage | Active | Stable / Low Risk |
| **Firebase App Check** | `firebase-appcheck-playintegrity` / `recaptcha` | Anti-Abuse | Active | Stable / Low Risk |
| **Google Credentials / ID** | `androidx.credentials`, `com.google.android.libraries.identity.googleid` | Modern Identity | Active | Stable / Low Risk |
| **Coroutines Android** | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Asynchronous Runtime | Active | Stable / Low Risk |
| **Retrofit & Moshi** | `com.squareup.retrofit2:retrofit`, `converter-moshi` | Network Client | Active | Stable / Low Risk |
| **OkHttp & Logging Interceptor** | `com.squareup.okhttp3:okhttp`, `logging-interceptor` | Transport Security | Active | Stable / Low Risk |

---

## 2. Unused & Commented Dependencies (APK Size Optimization)

In compliance with Android platform build rules, unused dependencies have been actively commented out to minimize DEX file size and optimize cold start times:
- `accompanist.permissions` (migrated to standard Compose Activity contracts)
- `camera2`, `camera.core`, `camera.lifecycle`, `camera.view` (clean photo picker used)
- `datastore.preferences` (Room database used for local state)
- `play.services.location` (granular on-demand service)

---

## 3. Vulnerability & CVE Clearance
- **Transitive Dependency Tree**: Inspected via Gradle dependencies graph. No critical severity CVEs identified.
- **Dynamic Code Loading (DCL)**: Zero dynamic code loading plugins or executable DEX injectors. Fully compliant with Google Play System Integrity policies.
- **Broad Storage Permissions**: Zero usage of `READ_EXTERNAL_STORAGE` or `MANAGE_EXTERNAL_STORAGE`. Zero-permission Android Photo Picker contract is enforced.
- **Audit Decision**: **APPROVED FOR PRODUCTION RELEASE v1.0.0**.
