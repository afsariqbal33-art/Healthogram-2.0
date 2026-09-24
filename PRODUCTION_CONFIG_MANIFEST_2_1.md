# Healthogram 2.1 Production Configuration Manifest

**Document:** `PRODUCTION_CONFIG_MANIFEST_2_1.md`  
**System:** Healthogram 2.1.0 (Build 20100)  
**Security Classification:** Highly Confidential / Operational Non-Secret  
**Target Architecture:** Google Cloud Platform, Firebase Multi-Region, Android 16 (API 36)  
**Last Verified:** September 2026  

---

## 1. System & Platform Identity

| Configuration Key | Production Value | Verification Source |
| :--- | :--- | :--- |
| **Android Application ID** | `com.aistudio.healthogram.hkqvpm` | `app/build.gradle.kts` |
| **Android Version Name** | `2.1.0` | `app/build.gradle.kts` |
| **Android Version Code** | `20100` | `app/build.gradle.kts` |
| **Target SDK / Min SDK** | `Target 36 (Android 16)` / `Min 24 (Android 7.0)` | Google Play 2026 Mandate |
| **Firebase Project ID** | `healthogram-prod-global` (Isolated VPC) | GCP Project Directory |
| **Firestore Database Mode** | Multi-Region (`eur3` Primary, `us-central1` Replicated) | Google Cloud Console |
| **Remote Config Template Version**| `v2.1.0-rc1.14` | Firebase Remote Config API |
| **Cloud Functions Engine** | Node.js 20 LTS (ES Modules & CommonJS Modular) | `functions/package.json` |
| **Database Migration Version** | `005_notification_schema.md` / `003_health_passport_security` | `docs/database/migrations/` |
| **Target Cloud Infrastructure** | Google Cloud Artifact Registry + Cloud Tasks + Secrets Manager | Terraform IaC |

---

## 2. Regional & Internationalization Configuration

| Domain | Configuration Specification | Notes |
| :--- | :--- | :--- |
| **Active Launch Countries** | `OM` (Oman), `SA` (Saudi Arabia), `AE` (UAE), `US` (United States) | Phased rollouts governed by CountryConfigService |
| **Supported Currencies** | `OMR` (3 decimals), `SAR` (2 decimals), `AED` (2 decimals), `USD` (2 decimals) | Minor-unit integer financial ledger |
| **Supported Locales** | `en-US` (Primary LTR), `ar-OM` / `ar-SA` (Native RTL Arabic) | Bi-directional layout engine verified |
| **Data Residency Pinning** | GCC Data strictly pinned to Middle East Regional Clusters | Invariant enforced in firestore.rules |

---

## 3. Account Hierarchy & Boundaries

### Non-Negotiable Core Accounts:
1. **Individual** (`INDIVIDUAL`): Primary health consumer, patient, and timeline owner.
2. **Doctor** (`DOCTOR`): Verified practitioner with scoped, patient-consented record access.
3. **Clinic** (`CLINIC`): Multi-provider ambulatory organization with delegated staff roles.
4. **Hospital** (`HOSPITAL`): Multi-department enterprise institution with role compartmentalization.
5. **Laboratory** (`LABORATORY`): Diagnostic test order fulfillment and signed result publishing.

### Non-Negotiable Marketplace Roles:
1. **Customer**: General marketplace buyer.
2. **Seller**: Verified vendor of compliant lifestyle and health-support items.

*Prohibited Account Invariant:* **Zero** pharmacy, medicine company, wholesale medicine distributor, or medical equipment manufacturer accounts exist.

---

## 4. Integration Providers & Capabilities (Non-Secret)

| Service Domain | Approved Provider | Integration Mode | Production Security State |
| :--- | :--- | :--- | :--- |
| **Healthcare Interop (FHIR)** | Epic FHIR R4, Cerner FHIR R4, LabLink R4 | Scoped REST / OAuth2 Bearer | Mapped via `FHIRMappingService`; Sandbox separated |
| **Android Health Connect** | AndroidX Health Connect SDK 1.1.0 | On-Device Encrypted Local IPC | Opt-In permission gate; Instant revocation |
| **Appointment Provider** | Multi-Provider Healthogram Scheduler 2.1 | Internal Cloud Engine + Cloud Tasks | Conflict prevention; zero diagnostic notifications |
| **AI Diagnostics Guard** | Gemini 3.8 Flash (Vertex AI Enterprise Endpoint) | Server-side VPC-proxied REST API | Non-diagnostic disclaimer; physical airgap |
| **Translation Engine** | Google Cloud Translation Enterprise | Asynchronous Batch API | Clinical terms scrubbed; zero data retention |
| **Global Payment Gateways** | Stripe (US/Intl), Thawani (OM), PayTabs (GCC) | Webhook HMAC SHA-256 Signatures | PCI-DSS Tokenized; zero card numbers stored |
| **Fulfillment / Delivery** | DHL Express, Oman Post, Local Courier Network | Webhook Event Stream | Verification via OTP proof-of-delivery |

---

## 5. Security Enclaves & Integrity Safeguards

- **Firebase App Check**: Play Integrity attestation actively required in production for all client-to-backend invocations.
- **Biometric Security Engine**: BiometricPrompt with AES-256-GCM hardware AndroidKeyStore keystore.
- **Concurrent Session Cap**: 4 simultaneous device session limit per individual profile with automated oldest-session eviction.
- **Owner Control Enclave**: Immutable hardware-backed PIN required for destructive operational switches.
