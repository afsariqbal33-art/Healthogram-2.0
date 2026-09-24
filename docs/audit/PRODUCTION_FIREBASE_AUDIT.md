# HEALTHOGRAM — PRODUCTION FIREBASE AUDIT

**Environment:** Production (`healthogram-prod`)  
**Audit Date:** September 16, 2026  
**Auditor:** Firebase Security Engineer / DevOps Lead  
**Firebase CLI / SDK Level:** Firebase Android BoM 33.x+ / Firebase Functions Node 20  

---

## 1. Project Topology & Resource Matrix

| Service | Environment Project ID | Region / Zone | Security Posture | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Authentication** | `healthogram-prod` | Global | Identity Platform, Phone OTP + Email link, Multi-factor enabled | **VERIFIED** |
| **Cloud Firestore** | `healthogram-prod` | `nam5` (multi-region) | Strict rules v2, Deny-all baseline, Zero public medical paths | **VERIFIED** |
| **Cloud Storage** | `healthogram-prod.appspot.com` | Multi-region | Non-public buckets for medical records, signed upload tokens | **VERIFIED** |
| **Cloud Functions** | `healthogram-prod` | `us-central1` | Node 20 runtime, Min instances 1 for low cold-start, App Check verified | **VERIFIED** |
| **Cloud Messaging (FCM)** | `healthogram-prod` | Global | V1 HTTP API, OAuth2 scoped tokens, Zero PHI payload policy | **VERIFIED** |
| **Firebase App Check** | `healthogram-prod` | Global | Play Integrity API enforced for Android, debug token isolated to testing | **VERIFIED** |
| **Firebase Crashlytics** | `healthogram-prod` | Global | PII scrubbers active, custom keys restricted to non-identifying state | **VERIFIED** |
| **Google Analytics** | `healthogram-prod` | Global | Consent Mode v2 active, demographic logging restricted | **VERIFIED** |
| **Remote Config** | `healthogram-prod` | Global | Server-side kill switches for marketplace, calls, translation, maintenance | **VERIFIED** |

---

## 2. Cloud Firestore Rules Breakdown

### A. Root Lockdown
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if false; // Deny by default
    }
  }
}
```

### B. Health Passport Collections
- `health_profiles/{patientUid}`: Read allowed only to owner or verified healthcare scanner with active session grant.
- `health_conditions/{recordId}`: Read allowed only to patient or verified doctor/clinic/hospital.
- `health_allergies/{recordId}`: Same strict healthcare scoping.
- `health_medications/{recordId}`: Read allowed only to patient or verified provider.
- `health_visits/{recordId}`: Scoped to patient or attending provider.
- `health_diagnoses/{recordId}`: Creation restricted strictly to verified doctors/clinics/hospitals.
- `health_tests/{recordId}`: Creation allowed to verified healthcare accounts; updates restricted.
- `health_lab_reports/{recordId}`: Creation strictly isolated to verified `laboratory` accounts.
- `health_prescriptions/{recordId}`: Creation strictly isolated to verified `doctor` accounts.
- `health_documents/{recordId}`: Owner and attending provider access only; private storage pointers.
- `health_bills/{recordId}`: Patient and provider access only; isolated from general marketplace.
- `health_notes/{recordId}`: Patient-only private vault.
- `health_access_grants/{grantId}`: Granular permissions with expiration timestamps.
- `health_access_logs/{logId}`: Read-only for patient; client write **strictly forbidden** (backend creation only).
- `health_qr_sessions/{sessionId}`: Ephemeral access tokens; raw medical data never placed in session token.

---

## 3. Storage Security Rules Breakdown

- **Avatars**: `users/{uid}/avatar/{fileName}`: Authenticated read; write restricted to authenticated owner (`request.auth.uid == uid`), file size <= 5MB, image content type only.
- **Social Media**: `posts/{postId}/{fileName}`, `reels/{reelId}/{fileName}`: Authenticated read; write restricted to post author.
- **Medical Vault**: `health_vault/{patientUid}/{documentId}`:
  - Public read: **STRICTLY FORBIDDEN**.
  - Direct client download: Restricted to active grant holders via signed Cloud Function URLs.
  - File constraints: PDF, JPEG, PNG, DICOM only; file size <= 25MB.

---

## 4. App Check & Play Integrity Enforcement

1. **Android Attestation Provider**: `PlayIntegrityAppCheckProviderFactory` configured for production flavor.
2. **Device Integrity**: Fails verification on rooted devices or untrusted app installations.
3. **Backend Validation**: Cloud Functions verify `context.app != null` before executing sensitive mutations (payout releases, healthcare verification approvals).

---

## 5. Firebase Production Verdict

**PRODUCTION FIREBASE STATUS: VERIFIED & COMPLIANT**  
Zero exposed secrets. Zero open collections. Least-privilege rules validated.
