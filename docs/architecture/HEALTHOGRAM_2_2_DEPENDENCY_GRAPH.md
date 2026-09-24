# Healthogram 2.2 — Architectural & Subsystem Dependency Graph

**Document:** `docs/architecture/HEALTHOGRAM_2_2_DEPENDENCY_GRAPH.md`  
**System:** Healthogram Platform 2.2  
**Authority:** Principal Software Architect & DevOps Lead  
**Classification:** ARCHITECTURAL SPECIFICATION & DEPENDENCY MAP  

---

## 1. High-Level Subsystem Dependency Chains

```
[Level 0: Foundation]
  Security & App Check ───────► Data Governance & Privacy Policies
         │                                   │
         ▼                                   ▼
[Level 1: Enclave Platforms]
  Cloud Infrastructure ───────► Health Passport Core ───────► HL7 FHIR Interoperability
         │                                   │                         │
         ▼                                   ▼                         ▼
[Level 2: Integrations]
  Identity & Verification ────► Healthcare Partners ────────► Android Health Connect
         │                                                             │
         ▼                                                             ▼
[Level 3: Applications]
  Payments & Ledger ──────────► Marketplace ────────────────► Owner Earnings
         │                           │
         ▼                           ▼
  Notification Engine ────────► Appointments & Messaging ───► Social Platform
         │
         ▼
[Level 4: Intelligence & Global]
  AI Infrastructure ──────────► AI Studio & Healthcare AI ──► Globalization & Country Configs
```

---

## 2. Detailed Dependency Vectors

### A. Security & Clinical Data Vector
```
  [App Check & Play Integrity]
           │
           ▼
  [Session Concurrency (Max 4)]
           │
           ▼
  [Firestore Security Rules]
           │
           ▼
  [Patient Sovereign Consent Engine]
           │
           ├───────────────────────────────┬───────────────────────────────┐
           ▼                               ▼                               ▼
  [Health Passport Timeline]      [HL7 FHIR R4 Gateways]         [Health Connect Ingestion]
           │                               │                               │
           ▼                               ▼                               ▼
  [Longitudinal Trend Graphs]     [ServiceRequest Lab Orders]    [Local Encrypted Biometrics]
```
- **Constraint:** Health Passport, FHIR, and Health Connect strictly require sovereign patient consent and verified App Check attestation before any data can be ingested or exported.

### B. Financial & Commercial Vector
```
  [Country Currency Configuration]
           │
           ▼
  [Payment Gateway Adapters (Stripe / Thawani / PayTabs)]
           │
           ▼
  [Double-Entry Financial Ledger (Minor Units)]
           │
           ├───────────────────────────────┐
           ▼                               ▼
  [Marketplace Checkout & Escrow]  [Direct Appointment Fees]
           │                               │
           ▼                               ▼
  [Courier Delivery OTP Proof]     [Doctor Consultation Slot]
           │                               │
           ▼                               ▼
  [7-Day Return Escrow Release]   [Consultation Completion]
           │                               │
           └───────────────┬───────────────┘
                           ▼
               [Owner Platform Commissions]
                           │
                           ▼
               [Hardware-PIN Payout Release]
```
- **Constraint:** Zero client-side balance mutations. Marketplace orders cannot release seller payouts until courier OTP delivery proof is registered and the return period expires cleanly.

### C. Partner Interoperability & Identity Vector
```
  [Medical License & Institution Identity Verification]
           │
           ▼
  [Healthcare Partner Status: VERIFIED]
           │
           ▼
  [Mutual TLS (mTLS 1.3) Handshake & Scoped OAuth2 Token]
           │
           ▼
  [FHIR R4 Diagnostic Order Pipeline (ServiceRequest)]
           │
           ▼
  [Automated Transaction Audit Logging]
```
- **Constraint:** External hospital or laboratory endpoints cannot receive or push FHIR bundles without Level 4+ Certification and active patient consent.

### D. AI Intelligence Vector
```
  [Vertex AI Enterprise Gateway (Zero-Retention DPA)]
           │
           ▼
  [AIProviderAdapter Abstraction Interface]
           │
           ├───────────────────────────────┐
           ▼                               ▼
  [Generic AI Studio Engine]      [Healthcare AI Non-Diagnostic Engine]
  • Caption generation            • Document OCR & translation
  • Image enhancement             • Clinical term redaction
  • Seller product copy           • Prominent medical disclaimers
```
- **Constraint:** `Generic AI Studio` never accesses Health Passport documents. `Healthcare AI` has zero autonomous diagnostic or prescriptive authority.

---

## 3. Critical Path & Blocker Analysis for Release 2.2

1. **Security & Session Concurrency**: Completed in Step 35; provides foundational integrity.
2. **Technical Debt Burndown (DEBT-01 & DEBT-02)**: Must execute in Sprint 1 to prevent WorkManager battery kills on OEM devices and Firestore index limits.
3. **FHIR `ServiceRequest` Engine**: Blocked on Level 6 partner sandbox test harness completion.
4. **International Marketplace**: Deliberately **held outside the critical path** (`FLAG_INTERNATIONAL_MARKETPLACE = false`).
