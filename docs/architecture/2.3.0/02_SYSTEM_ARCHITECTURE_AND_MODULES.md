# HEALTHOGRAM 2.3: SYSTEM ARCHITECTURE, MODULES, DATABASE & API BLUEPRINT

**Document ID:** HGM-2.3-ARCH-02-SYS  
**Phase:** Step 48 Architecture & Planning  
**Target Release:** Healthogram Version `2.3.0`  
**Git Branch:** `develop/healthogram-2-3`  
**Timestamp:** 2026-09-22T06:20:00Z  

---

## 1. System Architecture Blueprint

Healthogram 2.3 adopts a clean, layered hexagonal architecture designed to preserve the stability of 2.2 core engines while enabling isolated feature evolution.

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    HEALTHOGRAM 2.3 SYSTEM TOPOLOGY                         │
├────────────────────────────────────────────────────────────────────────────┤
│                       ANDROID CLIENT PRESENTATION                          │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ Jetpack Compose UI • Material 3 • AutoMirrored RTL • Dynamic Sizing  │  │
│  │  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐ ┌────────┐ │  │
│  │  │Health Passport │ │Marketplace/Cart│ │Social & Creator│ │Calling │ │  │
│  │  │(FHIR / Vault)  │ │& Appointments  │ │Feed & Reels    │ │(WebRTC)│ │  │
│  │  └────────────────┘ └────────────────┘ └────────────────┘ └────────┘ │  │
│  └──────────────────────────────────┬───────────────────────────────────┘  │
├─────────────────────────────────────┼──────────────────────────────────────┤
│                                     ▼                                      │
│                       APPLICATION & DOMAIN LAYER                           │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ ViewModel StateFlow • Coroutines Flow • Clean UseCases               │  │
│  │ Session Manager (4-Device FIFO) • Double-Entry Ledger Engine         │  │
│  │ Cryptographic Vault Manager (AES-GCM-256) • Signal E2EE Engine       │  │
│  └──────────────────────────────────┬───────────────────────────────────┘  │
├─────────────────────────────────────┼──────────────────────────────────────┤
│                                     ▼                                      │
│                       DATA & PERSISTENCE LAYER                             │
│  ┌───────────────────────────────┐      ┌───────────────────────────────┐  │
│  │ Local Persistence             │      │ Remote Network (Retrofit/gRPC)│  │
│  │ • Room Database (SQLCipher)   │      │ • API Gateway v1 / v2 Routes  │  │
│  │ • DataStore Preferences       │      │ • Firebase SDK (Auth, FCM)    │  │
│  │ • Android Health Connect SDK  │      │ • Cloud Functions (Idempotent)│  │
│  └───────────────────────────────┘      └───────────────────────────────┘  │
├────────────────────────────────────────────────────────────────────────────┤
│                    GOOGLE CLOUD PRODUCTION BACKEND                         │
│  • Cloud Firestore (Sharded, CMEK Encrypted, Cursor Paginated)             │
│  • Google Cloud Functions (Node.js 20, Min Instances = 1, Idempotent)     │
│  • Cloud Storage (Signed URLs, 7-year WORM compliance retention)           │
│  • Play Integrity & Firebase App Check Attestation Layer                   │
│  • Redis In-Memory Session Cache & Distributed Distributed Lock Store      │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Multi-Module Project Architecture

To eliminate tight coupling, reduce incremental build times, and isolate failure domains, the codebase transitions to dynamic feature modules:

```
healthogram/
├── app/                                # Application shell, Dagger/Hilt, Navigation host
├── core/
│   ├── common/                         # Coroutine dispatchers, result types, extensions
│   ├── designsystem/                   # Compose theme, typography, shapes, M3 tokens
│   ├── database/                       # Room DB, SQLCipher encryption, DAOs, migrations
│   ├── network/                        # Retrofit client, gRPC stubs, interceptors, auth headers
│   ├── security/                       # Keystore manager, AES-GCM-256 cipher, Play Integrity
│   └── model/                          # Shared domain entities (User, AccountType, Money)
└── features/
    ├── healthpassport/                 # Vault, QR scanner, consent UI, medical timeline
    ├── fhir/                           # HL7 FHIR R4 parser, export/import, bundle validator
    ├── healthconnect/                  # Android Health Connect sync manager
    ├── appointments/                   # Smart booking, slot reservation, doctor calendar
    ├── marketplace/                    # Catalog, search, cart, domestic checkout, orders
    ├── payments/                       # Payment abstraction, Google/Apple Pay, Stripe/HyperPay
    ├── ledger/                         # Double-entry ledger journal, owner earnings
    ├── delivery/                       # Courier adapter, dispatch tracking, OTP handover
    ├── social/                         # Feed, posts, reels player, creator tools
    ├── messaging/                      # Signal protocol E2EE chat, attachments, sync
    ├── calling/                        # WebRTC audio/video teleconsultation engine
    ├── aistudio/                       # Contextual creator assist, description generator
    ├── translation/                    # On-device ML Kit, side-by-side clinical renderer
    └── admin/                          # Administrative inspection, license verification
```

---

## 3. Database Change Plan & Schema Evolution

Firestore and Room schemas will evolve with strict backward compatibility using additive changes and explicit version tracking.

### A. Schema Versioning Standard
All Firestore collections and local Room entities will include an explicit `schema_version` integer:
* `schema_version = 1`: Healthogram 2.1 legacy baseline
* `schema_version = 2`: Healthogram 2.2 production release baseline
* `schema_version = 3`: Healthogram 2.3 release additions

### B. New & Modified Collections in 2.3
| Collection Path | Operation | `schema_version` | Description & Purpose | New Indexes Required |
| :--- | :---: | :---: | :--- | :--- |
| `/appointments/{id}` | **NEW** | `3` | Stores consultation bookings between patients and clinicians | `(doctorUid, slotTime ASC)`, `(patientUid, status ASC)` |
| `/doctor_schedules/{uid}/slots/{slotId}` | **NEW** | `3` | Doctor working hours and availability calendar | `(startTime ASC, isBooked ASC)` |
| `/fhir_exports/{exportId}` | **NEW** | `3` | Audit metadata for FHIR clinical exports (zero clinical data) | `(ownerUid, timestamp DESC)` |
| `/health_connect_sync/{uid}` | **NEW** | `3` | Sync state, high-water mark, and permissions for Health Connect | `(uid, lastSyncTimestamp ASC)` |
| `/marketplace_orders/{id}` | **MODIFIED** | `3` | Added `paymentMethod: GOOGLE_PAY \| APPLE_PAY \| CARD` | Existing indexes retained |
| `/users/{uid}/health_passport/` | **MODIFIED** | `3` | Added `emergencyAccessAllowed: Boolean` (opt-in toggle) | Existing subcollections retained |
| `/delivery_shipments/{id}` | **MODIFIED** | `3` | Added `temperatureMonitored: Boolean`, `tempRange: [min, max]` | `(courierId, status ASC)` |

### C. Zero-Downtime Migration & Rollback Guard
1. **Additive Evolution:** New fields are optional (`nullable` or provided with default values). Legacy 2.2 clients reading 2.3 documents safely ignore unknown fields.
2. **Backfill Protocol:** If backfilling is required, a Cloud Run background worker processes batches of 500 documents using cursor pagination with dry-run verification.
3. **Rollback Strategy:** Firestore rules maintain read/write permissions for both `schema_version: 2` and `schema_version: 3`.

---

## 4. API Change Plan (Versioned Architecture)

Healthogram 2.3 introduces structured API versioning (`/api/v1` and `/api/v2`) to prevent breaking legacy production clients:

```
[CLIENT INCOMING REQUEST]
       │
       ▼
[CLOUD ARMOR / API GATEWAY]
       │
       ├─ /api/v1/* ──► [V1 Legacy Handlers] (Fully backward-compatible with 2.2)
       │
       └─ /api/v2/* ──► [V2 Enhanced Handlers] (Supports FHIR, Appointments, Google Pay)
```

### Key API Changes for 2.3
* **Appointments API (`/api/v2/appointments`):**
  * `GET /api/v2/doctors/{doctorId}/slots?date=YYYY-MM-DD` (Rate limit: 60/min)
  * `POST /api/v2/appointments/reserve` (Idempotent, acquires 5-minute Redis distributed lock)
  * `POST /api/v2/appointments/confirm` (Verifies payment intent, commits booking)
  * `POST /api/v2/appointments/cancel` (Processes refund according to cancellation policy)
* **FHIR Interoperability API (`/api/v2/health/fhir`):**
  * `POST /api/v2/health/fhir/validate-bundle` (Validates client-generated FHIR JSON against HL7 schema)
  * `POST /api/v2/health/fhir/export-audit` (Logs authenticated export event for compliance)
* **Payments API (`/api/v2/payments`):**
  * `POST /api/v2/payments/create-intent` (Supports `payment_method_types: ['card', 'google_pay']`)
  * `POST /api/v2/payments/webhook` (Idempotent webhook dispatcher with signature verification)
