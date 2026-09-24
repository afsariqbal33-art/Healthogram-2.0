# Healthogram 2.2 — Architecture Diagrams & Data Flow Models

**Document:** `docs/architecture/HEALTHOGRAM_2_2_ARCHITECTURE_DIAGRAM.md`  
**System Version:** Healthogram 2.2.0  
**Authority:** Principal Software Architect & Lead Security Engineer  
**Classification:** ARCHITECTURAL SPECIFICATION & SCHEMATICS  

---

## 1. High-Level Multi-Tier Architecture Diagram

```
===================================================================================================
                                      CLIENT TIER (Android 16 / API 36)
===================================================================================================
  [Jetpack Compose UI] <---> [ViewModels / MVI State] <---> [Room DB (AES-256 Encrypted Cache)]
                                    │                │
            [Health Connect SDK] ───┘                └─── [Firebase App Check / Play Integrity]
                                    │
                                    ▼ (HTTPS / TLS 1.3 / Certificate Pinning)
===================================================================================================
                                    API GATEWAY & SECURITY TIER
===================================================================================================
                     [Cloud Load Balancer / Google Cloud Armor DDoS Protection]
                                                │
                 ┌──────────────────────────────┴──────────────────────────────┐
                 ▼                                                             ▼
    [App Check Attestation Validator]                            [Firebase Auth Session Engine]
    (Rejects unauthorized bots & spoofing)                      (Enforces Max 4 Active Devices)
                                                │
                                                ▼
===================================================================================================
                               APPLICATION MICROSERVICES TIER (Node 20)
===================================================================================================
  ┌─────────────────────────┬─────────────────────────┬─────────────────────────┬─────────────────┐
  │ Clinical Services       │ Social & Messaging      │ Commerce & Finance      │ External Proxy  │
  ├─────────────────────────┼─────────────────────────┼─────────────────────────┼─────────────────┤
  │ • Health Passport Core  │ • Direct Messaging      │ • Marketplace Engine    │ • FHIR R4 Engine│
  │ • Consent Evaluator     │ • WebRTC Signaling     │ • Double-Entry Ledger   │ • Vertex AI Adpt│
  │ • Ephemeral QR Manager  │ • Social Feed Engine    │ • Payout Reconciler     │ • Courier Broker│
  │ • Appointment Scheduler │ • Content Moderation    │ • Tax/Fee Calculator    │ • Payment Hooks │
  └───────────┬─────────────┴───────────┬─────────────┴───────────┬─────────────┴────────┬────────┘
              │                         │                         │                      │
===================================================================================================
                                     PERSISTENCE & CLOUD STORAGE
===================================================================================================
              ▼                         ▼                         ▼                      ▼
  ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────────────┐ ┌────────────────┐
  │ Cloud Firestore       │ │ Cloud Storage (GCS)   │ │ Realtime DB / Redis   │ │ External APIs  │
  │ (Multi-Region eur3)   │ │ (Private Buckets)     │ │ (WebRTC / Presence)   │ │ (Hospital EHR, │
  │ • health_records      │ │ • /clinical_docs/     │ │ • active_calls        │ │ Stripe, Twilio,│
  │ • appointments        │ │ • /social_media/      │ │ • typing_indicators   │ │ Translation)   │
  │ • financial_ledger    │ │ • /partner_bundles/   │ │ • live_sessions       │ │                │
  └───────────────────────┘ └───────────────────────┘ └───────────────────────┘ └────────────────┘
```

---

## 2. Strict Data-Boundary & Airgap Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                 HEALTH DATA ENCLAVE (STRICT ZERO-TRUST)                         │
│                                                                                                 │
│   [Patient Health Passport] ──(Granular Consent)──> [Verified Doctor / Hospital / Lab]          │
│              │                                                      │                           │
│              ▼                                                      ▼                           │
│   [Encrypted Storage Bucket]                              [FHIR R4 Ingestion Node]              │
│   (Presigned URL Exp: 15m)                                (mTLS 1.3 Partner Gateway)            │
└────────────────────────────────────────────────┬────────────────────────────────────────────────┘
                                                 │
                                                 │  STRICT AIRGAP: Zero cross-querying allowed
                                                 │  Security Rules deny any joins across domains
                                                 │
┌────────────────────────────────────────────────▼────────────────────────────────────────────────┐
│                               PUBLIC & COMMERCIAL APPLICATION DOMAINS                           │
│                                                                                                 │
│   ┌────────────────────────────────────────┐       ┌────────────────────────────────────────┐   │
│   │         Social & Community             │       │         Marketplace & Payments         │   │
│   ├────────────────────────────────────────┤       ├────────────────────────────────────────┤   │
│   │ • User Profiles & Creators             │       │ • Customers & Verified Sellers         │   │
│   │ • Public Feeds, Reels, Stories         │       │ • Product Catalog (Wellness / Fitness) │   │
│   │ • Comments, Follows, Sharing           │       │ • Double-Entry Ledger (Minor Units)    │   │
│   │ • Zero Clinical Data Ingestion         │       │ • Regional Escrow & Payout Automation  │   │
│   └────────────────────────────────────────┘       └────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. HL7 FHIR R4 ServiceRequest Bi-Directional Ingestion Pipeline

```
  [Clinic / Doctor]
         │
         │ 1. Issues Diagnostic Order (ServiceRequest)
         ▼
  [Healthogram FHIR Gateway]
         │
         │ 2. Routes to accredited partner via mTLS
         ▼
  [Partner Diagnostic Laboratory] (Level 6 Certified)
         │
         │ 3. Sample Collected -> Analysis Complete -> Generates DiagnosticReport Bundle
         ▼
  [Healthogram Ingestion Validator]
         │
         ├─ A. Schema Validation (16 Normative R4 Models)
         ├─ B. Digital Signature & Provenance Verification
         ├─ C. Deterministic Deduplication Hash Check
         └─ D. Patient Active Consent Verification
         │
         ▼ (Validation Passed)
  [Encrypted Patient Health Passport]
         │
         │ 4. FCM Push Notification (Sanitized: "New lab report available")
         ▼
  [Patient Android Client] (Decrypted via Keystore AES-256)
```

---

## 4. Mobile Client Internal Architecture (Android 16)

```
  ┌────────────────────────────────────────────────────────────────────────┐
  │                      JETPACK COMPOSE PRESENTATION                      │
  │   HealthPassportScreen │ AppointmentScreen │ MarketplaceScreen │ Feed  │
  └───────────────────────────────────┬────────────────────────────────────┘
                                      │
  ┌───────────────────────────────────▼────────────────────────────────────┐
  │                  VIEWMODELS & UNIDIRECTIONAL DATA FLOW                 │
  │       StateFlow<UiState> ──(User Actions)──> ViewModel.handleIntent()  │
  └───────────────────────────────────┬────────────────────────────────────┘
                                      │
  ┌───────────────────────────────────▼────────────────────────────────────┐
  │                         CORE REPOSITORY LAYER                          │
  │   HealthRepository │ AppointmentRepository │ CommerceRepository        │
  └─────────────────┬───────────────────────────────────┬──────────────────┘
                    │                                   │
        ┌───────────▼───────────┐           ┌───────────▼───────────┐
        │  Local Cache (Room)   │           │ Remote Network Engine │
        │  • SQLCipher AES-256  │           │ • Ktor / Retrofit     │
        │  • Offline Sync Delta │           │ • Firebase Firestore  │
        │  • Room 2.6+ Entities │           │ • App Check Token Int │
        └───────────────────────┘           └───────────────────────┘
```

---

## 5. Double-Entry Financial Ledger State Machine

```
  [Checkout Initiated] 
           │
           ▼
  [PAYMENT_PENDING] ──(Webhook Failed / Cancelled)──> [PAYMENT_FAILED]
           │
           ▼ (Webhook Verified HMAC-SHA256)
  [ESCROW_HELD] ──(Order Cancelled Prior to Dispatch)──> [REFUNDED]
           │
           ▼ (Merchant Dispatches Goods)
  [IN_TRANSIT]
           │
           ▼ (Customer Confirms Delivery via OTP)
  [DELIVERED]
           │
           ▼ (7-Day Return Window Expires Cleanly)
  [AVAILABLE_FOR_PAYOUT]
           │
           ▼ (Owner Approves Transfer with Hardware PIN)
  [PAYOUT_COMPLETED]
```
