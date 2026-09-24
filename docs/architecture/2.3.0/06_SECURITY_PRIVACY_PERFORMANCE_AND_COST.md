# HEALTHOGRAM 2.3: SECURITY, PRIVACY, PERFORMANCE, COST & TESTING BLUEPRINT

**Document ID:** HGM-2.3-ARCH-06-SEC-PERF  
**Phase:** Step 48 Architecture & Planning  
**Target Release:** Healthogram Version `2.3.0`  
**Git Branch:** `develop/healthogram-2-3`  
**Timestamp:** 2026-09-22T06:40:00Z  
**Governing Standards:** NIST SP 800-53, ISO/IEC 27001, OWASP Mobile Top 10, WCAG 2.2 AA  

---

## 1. Security Architecture 2.3 & Threat Modeling

Healthogram 2.3 enforces zero-trust architecture across all client-server interactions.

```
[MOBILE CLIENT] 
  │ (Hardware Attestation: Play Integrity 'MEETS_DEVICE_INTEGRITY')
  │ (App Attestation: Firebase App Check)
  │
  ▼
[CLOUD ARMOR / API GATEWAY] ── (Rate Limits: 60 req/min, DDoS Shield)
  │
  ▼
[CLOUD FUNCTIONS AUTH MIDDLEWARE] ── (Custom Claims & RBAC Validation)
  │
  ▼
[FIRESTORE / STORAGE SECURITY RULES] ── (Granular Data Boundaries)
  │
  ▼
[CUSTOMER-MANAGED ENCRYPTION (CMEK)] ── (Google Cloud KMS)
```

### Threat Modeling & Mitigations for Version 2.3 Additions
1. **Threat: Unauthorized FHIR Clinical Export Manipulation**
   * *Mitigation:* Bundles generated strictly client-side in RAM, digitally signed with patient's hardware Keystore key.
2. **Threat: Appointment Double-Booking & DoS Exhaustion**
   * *Mitigation:* Atomic distributed Redis locks (5-minute TTL) + Firestore transactional commit verification.
3. **Threat: Google Pay Payment Token Interception**
   * *Mitigation:* Device-bound cryptographic network tokens verified directly by payment gateway backends; zero raw card data touches application servers.
4. **Threat: Emergency Access Privilege Escalation**
   * *Mitigation:* Restricted to verified `Hospital` accounts with MFA; mandatory real-time SMS/FCM alert to patient and emergency contacts.

---

## 2. Privacy Architecture & Data Minimization Framework

Every newly introduced data field in Version 2.3 is evaluated under the 9-point Privacy Governance Rule:

| Evaluated Dimension | Appointment Booking Record | FHIR Export Audit Record | Health Connect Sync Record |
| :--- | :--- | :--- | :--- |
| **1. Purpose** | Facilitates clinician consultation scheduling | Regulatory audit compliance | Personal fitness tracking |
| **2. Who Can Access?** | Patient UID and designated Doctor UID only | Patient UID only | Patient UID only |
| **3. Retention Period** | 7 years (statutory medical encounter rule)| 1 year compliance audit retention | User-configured (or immediate deletion)|
| **4. Storage Location** | Encrypted Firestore collection | Private Firestore subcollection | Encrypted local Room database |
| **5. User Deletion?** | Subject to clinical statutory retention | Yes | Yes (one-tap local wipe) |
| **6. User Export?** | Yes (included in Health Passport export) | Yes | Yes (via Health Connect API) |
| **7. Third-Party Sharing?**| Zero third-party sharing | Zero third-party sharing | Zero third-party sharing |
| **8. Consent Required?** | Explicit booking consent | Explicit export authorization | Explicit Android OS runtime consent |
| **9. Data Sensitivity** | High (PHI / Clinical) | Medium (Audit metadata only) | High (Vitals / Biometrics) |

---

## 3. Performance Architecture & Target SLAs

Performance budgets are established across core subsystems to guarantee responsiveness on mid-tier Android hardware:

| Architectural Vector | Production SLA Target (P95) | Architecture & Concurrency Strategy |
| :--- | :---: | :--- |
| **App Cold Startup** | $< 1,200\text{ ms}$ | Baseline Profiles generated; deferred SDK initialization |
| **Social Feed Scroll** | $\ge 58\text{ fps}$ | `LazyColumn` item keying; remembered lambdas; bounded media cache |
| **Health Passport Decryption** | $< 400\text{ ms}$ | SQLCipher encrypted SQLite cache with memory cipher instances |
| **Dynamic QR Generation** | $< 150\text{ ms}$ | Pre-warmed ECC key buffers; optimized QR matrix rendering |
| **Appointment Slot Query** | $< 350\text{ ms}$ | Pre-indexed Firestore queries with composite range indexes |
| **E2EE Message Delivery** | $< 250\text{ ms}$ | Persistent WebSocket connection with FCM fallback |
| **WebRTC Telehealth Connect** | $< 1,500\text{ ms}$ | Parallel ICE candidate gathering; local STUN/TURN pooling |

---

## 4. Cost Architecture & FinOps Projections

To prevent runaway infrastructure expenses, every Version 2.3 capability includes an architectural cost model:

| Cost Vector | Estimated Unit Cost | Cost Driver | Architectural FinOps Mitigation |
| :--- | :--- | :--- | :--- |
| **Firestore Operations** | $\$0.06 / 100\text{k reads}$ | Appointment slot queries | Cursor pagination (15 items/page); 60s Redis cache |
| **Cloud Storage** | $\$0.02 / \text{GB/month}$ | Medical scans & bills | WebP compression; GCS Nearline lifecycle archiving |
| **Cloud Functions** | $\$0.40 / 1\text{M invocations}$| Booking & payment hooks | Concurrency tuned to 80; memory clamped to 256MB |
| **AI Studio (Vertex)** | `DATA NOT AVAILABLE` | Product caption generation | Daily user quotas (10/day standard, 50/day business) |
| **Translation Engine** | $\$0.00$ (on-device) | Clinical bilingual cards | Pre-packaged ML Kit models; Room cached glossary |
| **WebRTC Relays** | $\$0.01 / \text{GB}$ | Video teleconsultations | Peer-to-peer prioritized; TURN relay fallback only |

---

## 5. Testing Architecture & Quality Assurance Matrix

Testing spans unit, integration, security, and specialized domain suites:

```
[TESTING MATRIX]
  │
  ├─ 1. Client Unit & Local JVM: Robolectric testing for CUJs without emulator
  ├─ 2. Visual Regression: Roborazzi screenshot verification for LTR & RTL layouts
  ├─ 3. Healthcare Integrity:
  │     ├─ HL7 FHIR R4 schema validation suite
  │     ├─ Zero-raw-PHI QR code payload regex tests
  │     └─ Consent revocation and expiration boundary tests
  ├─ 4. Financial Integrity:
  │     ├─ Double-entry ledger invariant tests (sum(debits) - sum(credits) == 0)
  │     ├─ Payment webhook idempotency tests
  │     └─ Simulated gateway refund and chargeback reconciliations
  ├─ 5. Security & Threat Testing:
  │     ├─ Play Integrity token validation rejects tampered devices
  │     ├─ App Check rejects unauthorized Cloud Functions invocations
  │     └─ 4-device session limit FIFO eviction tests
  └─ 6. Accessibility & Localization:
        ├─ WCAG 2.2 AA 48dp touch target linting
        └─ Arabic RTL layout mirroring screenshot tests
```
