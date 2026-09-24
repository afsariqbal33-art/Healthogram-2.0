# HEALTHOGRAM 2.0 TARGET ARCHITECTURE SPECIFICATION

**Document Version:** 2.0.0-PROPOSAL  
**Classification:** Enterprise Systems & Distributed Platform Architecture  
**Status:** Architecture Specification & Evolution Roadmap  
**Target Scale:** Millions of concurrent users, high media throughput, multi-region healthcare compliance  

---

## 1. Executive Summary & Architectural Philosophy

Healthogram 2.0 is the architectural evolution of the production-tested Healthogram v1.0 platform. Rather than naively fracturing into an unmanageable microservice sprawl, Healthogram 2.0 adopts a **Modular Distributed Domain Architecture**. 

### Core Architectural Invariants:
1. **Sovereign Health Passport Isolation:** Patient medical vaults remain cryptographically air-gapped from social feeds, advertising engines, generic analytics, and third-party AI models.
2. **Double-Entry Financial Immutability:** Marketplace escrows, seller disbursements, courier fees, and Owner earnings are computed strictly server-side using double-entry ledger semantics.
3. **Deterministic Account Taxonomy:** Primary healthcare categories are strictly constrained to 5 entities: `Individual`, `Doctor`, `Clinic`, `Hospital`, and `Laboratory`. Marketplace roles are strictly `Customer` and `Seller`.
4. **Resilient Simplicity:** Retain serverless and managed primitives (Cloud Functions v2, Cloud Firestore, Cloud Storage, Cloud Tasks) where latency and durability are proven, extracting asynchronous workers or dedicated search engines only where justified by quantifiable bottlenecks.

---

## 2. Multi-Layer Platform Architecture

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                CLIENT APPLICATION LAYER                                │
│  • Native Android (Jetpack Compose, Kotlin 2.1, Target SDK 36, Min SDK 24)             │
│  • Adaptive Window Form Factors: Mobile, Tablet (Dual-Pane), Foldable, ChromeOS Desktop│
│  • Local Storage: Room (SQLCipher encrypted cache), Jetpack DataStore                  │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ TLS 1.3 / App Check (Play Integrity)
                                            ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              API GATEWAY & SECURITY LAYER                              │
│  • Firebase App Check Token Attestation (Play Integrity cryptographic hardware attestation)
│  • Cloud Armor WAF & Adaptive Rate Limiting (DDoS, brute-force OTP protection)        │
│  • Firebase Auth & Custom Claims Engine (Role-based & attribute-based authorization)  │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ Authenticated & Scoped RPC / REST
                                            ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              DOMAIN SERVICES (ISOLATED BOUNDARIES)                     │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌────────────────────────────────┐  │
│  │   IDENTITY & AUTH   │  │   PROFILES & ORGS   │  │ HEALTH PASSPORT (ZERO-TRUST)   │  │
│  │ • Phone OTP Engine  │  │ • 5 Allowed Roles   │  │ • Cryptographic Vault          │  │
│  │ • 4-Device Session  │  │ • Org Affiliations  │  │ • Ephemeral QR Tokens (15m NTP)│  │
│  │ • Biometric Binding │  │ • Verification Gate │  │ • Immutable Audit Trail        │  │
│  └─────────────────────┘  └─────────────────────┘  └────────────────────────────────┘  │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌────────────────────────────────┐  │
│  │   SOCIAL & CREATOR  │  │   MARKETPLACE CORE  │  │ PAYMENTS & FINANCIAL LEDGER    │  │
│  │ • Hybrid Feed Engine│  │ • Catalog & Search  │  │ • Server-Side Double Entry     │  │
│  │ • Reels & Stories   │  │ • Cart & Inventory  │  │ • Marketplace Escrow Engine    │  │
│  │ • Creator Analytics │  │ • Multi-Seller Order│  │ • Owner Earnings & Payouts     │  │
│  └─────────────────────┘  └─────────────────────┘  └────────────────────────────────┘  │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌────────────────────────────────┐  │
│  │ REALTIME COMMS      │  │ AI STUDIO PLATFORM  │  │ SOVEREIGN TRANSLATION          │  │
│  │ • E2EE Direct Chat  │  │ • Provider Router   │  │ • Localized Dictionaries       │  │
│  │ • WebRTC P2P Video  │  │ • Air-Gapped Prompts│  │ • Realtime Voice / Text        │  │
│  │ • Ephemeral Presence│  │ • Safety Moderation │  │ • Non-Blocking Degradation     │  │
│  └─────────────────────┘  └─────────────────────┘  └────────────────────────────────┘  │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌────────────────────────────────┐  │
│  │ DELIVERY LOGISTICS  │  │ NOTIFICATION ENGINE │  │ PLATFORM GOVERNANCE            │  │
│  │ • 11-State Machine  │  │ • FCM Deduplication │  │ • Owner Control Panel          │  │
│  │ • Courier OTP Proof │  │ • Zero-PHI Payloads │  │ • Emergency Kill Switches      │  │
│  │ • Geofence & Rates  │  │ • Preferences Check │  │ • Country Configurations       │  │
│  └─────────────────────┘  └─────────────────────┘  └────────────────────────────────┘  │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │
                     ┌──────────────────────┴──────────────────────┐
                     ▼                                             ▼
┌──────────────────────────────────────────┐  ┌──────────────────────────────────────────┐
│      PERSISTENCE & DATA STORAGE LAYER    │  │        ASYNCHRONOUS EVENT ENGINE         │
│ • Cloud Firestore (Multi-Region nam5)    │  │ • Cloud Tasks (Retry backoff & ratelimits│
│ • Cloud Storage (Private Medical Vaults) │  │ • Google Cloud Pub/Sub & Eventarc        │
│ • Dedicated Search Engine (Algolia/ES)   │  │ • Async Media Transcoding & Compression  │
│ • Cloud KMS (Field-Level Data Encryption)│  │ • Nightly Ledger Reconciliation Workers  │
└──────────────────────────────────────────┘  └──────────────────────────────────────────┘
                     │                                             │
                     └──────────────────────┬──────────────────────┘
                                            ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                               EXTERNAL PROVIDER INTEGRATIONS                           │
│  • Payment Gateways: Stripe, Local Payment Rails (Apple Pay, Google Pay, Mada)        │
│  • Telecommunications: WebRTC STUN/TURN (Twilio / CoTurn), Twilio Verify SMS           │
│  • Generative AI & Safety: Gemini 1.5/2.0 API, Cloud Vision Moderation                │
│  • Delivery Couriers: Local Country Couriers, SMS OTP Gateways                         │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Domain Decomposition & Service Boundaries

Each domain operates as a distinct modular boundary with encapsulated data schemas, strict access contracts, and zero shared database write privileges.

### 1. Identity & Session Domain
- **Responsibilities:** Phone authentication, SMS OTP verification, biometrics credential registration, active session enforcement (hard cap of 4 concurrent devices per UID).
- **Data Collections:** `users`, `user_sessions`, `auth_rate_limits`.
- **Security Rule:** Self-update restricted to user metadata; session array mutations controlled exclusively via Cloud Functions.

### 2. Healthcare Profiles & Verification Domain
- **Responsibilities:** Management of the 5 canonical account types (`Individual`, `Doctor`, `Clinic`, `Hospital`, `Laboratory`). Clinical license verification, facility accreditation inspection, state medical board attestation.
- **Data Collections:** `profiles`, `doctor_profiles`, `clinic_profiles`, `facility_profiles`, `verification_requests`.
- **Security Rule:** Public profile reads allowed; verification badge and professional status writable ONLY by authorized Admin/Owner verification roles.

### 3. Health Passport Sovereign Vault Domain
- **Responsibilities:** Patient-centric sovereign health record management, clinical encounters, diagnostic reports, laboratory results, allergy rosters, prescription logs, and time-bound ephemeral QR code access grants.
- **Data Collections:** `health_passports`, `health_conditions`, `health_allergies`, `health_medications`, `health_visits`, `health_diagnoses`, `health_lab_reports`, `health_prescriptions`, `health_documents`, `health_access_grants`, `health_access_logs`, `health_qr_sessions`.
- **Storage Vault:** `gs://healthogram-vault/clinical_records/{patientUid}/` (Strict private ACL, AES-256 field encryption).
- **Air-Gap Rule:** ZERO read or write access granted to social feeds, marketplace search, or external AI models.

### 4. Social & Creator Media Domain
- **Responsibilities:** Social post publishing, video reels, ephemeral stories, comments, mentions, hashtags, and creator performance analytics.
- **Data Collections:** `posts`, `reels`, `stories`, `comments`, `likes_shards`, `creator_analytics`.
- **Architecture Pattern:** Hybrid Feed Architecture (fan-out on write for standard followers, fan-out on read with Redis/MemoryStore caching for high-volume creators).

### 5. Marketplace Core & Inventory Domain
- **Responsibilities:** Product catalog browsing, full-text token search, inventory locking, shopping cart aggregation, checkout validation, and multi-seller order routing.
- **Data Collections:** `products`, `categories`, `cart_sessions`, `orders`, `sub_orders`, `seller_inventory`.
- **Integrity Rule:** Product prices, tax calculations, and inventory deductions are authoritative ONLY on the backend server. Direct client writes to order totals are rejected.

### 6. Payments & Financial Ledger Domain
- **Responsibilities:** Multi-currency payment processing, checkout intents, webhook idempotency, marketplace escrow custody, automated dispute resolutions, double-entry financial reconciliation, and Owner platform earnings.
- **Data Collections:** `payment_intents`, `payment_transactions`, `financial_ledger`, `settlement_records`, `payout_requests`, `idempotency_locks`.
- **Ledger Invariant:** Every financial movement creates two offsetting ledger entries (Debit and Credit). Total platform debits equal total credits at all times.

### 7. Delivery & Fulfillment Logistics Domain
- **Responsibilities:** 11-step finite state machine tracking package fulfillment, shipping rate engine, courier dispatching, real-time GPS telemetry, and cryptographic proof-of-delivery (6-digit OTP).
- **Data Collections:** `delivery_orders`, `courier_profiles`, `shipment_tracking`, `delivery_zones`.
- **Fulfillment States:** `PENDING` -> `ASSIGNED` -> `PICKED_UP` -> `IN_TRANSIT` -> `OUT_FOR_DELIVERY` -> `DELIVERY_ATTEMPTED` -> `DELIVERED` (or `RETURNED` / `DISPUTED`).

### 8. Real-Time Communications (Messaging & WebRTC) Domain
- **Responsibilities:** Direct 1-on-1 and group messaging, ephemeral presence, and peer-to-peer audio/video teleconsultations.
- **Signaling Layer:** Cloud Firestore for signaling handshakes (SDP offer/answer, ICE candidates) with automated TTL cleanup (1-hour expiry).
- **Media Layer:** Direct P2P WebRTC data streams via TURN servers. Absolute zero automated server-side call recording.

### 9. AI Studio Domain (Air-Gapped & Governed)
- **Responsibilities:** Creator caption generation, title suggestions, seller product marketing copy, and multi-modal image enhancement.
- **Provider Router:** Abstracted provider interface (`AIProviderAdapter`) supporting Gemini Flash, Gemini Pro, and alternate fallback engines.
- **Air-Gap Rule:** Explicit firewall blocking any ingestion of Health Passport clinical collections into AI prompt contexts.

### 10. Country Configuration & Governance Domain
- **Responsibilities:** Master platform settings, emergency kill switches, country-specific feature enablement, currency formats, tax rules, and commission schedules.
- **Data Collections:** `platform_config`, `country_configs/{countryCode}`, `feature_flags`.
- **International Commerce Rule:** `international_marketplace_enabled` defaults to `false` until authorized by the platform Owner.

---

## 4. Scalability Strategy & Technical Evolution

| Workload Area | Healthogram v1.0 (Current) | Healthogram 2.0 (Target Evolution) | Justification & Triggers |
| :--- | :--- | :--- | :--- |
| **Feed Distribution** | Direct Firestore collection query with pagination | Hybrid Fan-Out with Cloud CDN & Memcache | High-volume creators (> 100k followers) causing query read hotspots. |
| **Catalog Search** | Firestore prefix queries (`startAt`/`endAt`) | Dedicated Search Engine (Algolia or Cloud Search) | Faceted filtering, typo tolerance, multi-attribute relevance ranking. |
| **Asynchronous Jobs** | Direct Cloud Functions invocation | Google Cloud Tasks with exponential backoff & dead-letter queues | Decoupling request/response latency from media transcoding and push fan-out. |
| **Media Delivery** | Firebase Storage direct download URLs | Cloud Storage + Cloud CDN with signed cookie URL tokens | Lower bandwidth costs, reduced latency for global video streaming. |
| **Telemetry & Presence**| Firestore presence documents | Realtime Database or WebRTC ephemeral heartbeat | Sub-second latency, zero Firestore write exhaustion on typing indicators. |
| **Database Tiering** | Single Multi-Region Firestore instance | Multi-Region Firestore + BigQuery coldline analytics export | Offloading heavy analytical reporting and financial auditing from transactional database. |
