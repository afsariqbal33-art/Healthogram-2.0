# HEALTHOGRAM 2.3: CANDIDATE REGISTER & ARCHITECTURAL BLUEPRINT

**Document ID:** HGM-STAB-2.3-PLAN  
**Planning Cycle:** Healthogram Version `2.3.0`  
**Standard:** ISO/IEC/IEEE 42010 (Systems and software engineering — Architecture description)  
**Timestamp:** 2026-09-22T06:40:00Z  
**Governing Roles:** Principal Systems Architect, Chief Product Officer, Principal Security Architect  

---

## 1. Healthogram 2.3 Feature Candidate Discovery & Classification

Based on production stabilization evidence gathered in Step 47, candidate features for Version 2.3 have been evaluated and classified into five standard product discovery tiers:
* `MUST HAVE`
* `SHOULD CONSIDER`
* `OPTIONAL`
* `FUTURE`
* `NOT RECOMMENDED FOR 2.3`

### High-Level Candidate Disposition Matrix
| Subsystem / Domain | Proposed 2.3 Feature Candidate | Classification | Strategic Justification |
| :--- | :--- | :--- | :--- |
| **Health Passport** | FHIR R4 Standardized Clinical Export | `MUST HAVE` | Enables seamless interoperability with national electronic health record (EHR) systems in US/EU/SA. |
| **Healthcare / Consultations** | Multi-Specialty Smart Appointment Booking | `MUST HAVE` | Closes the loop from doctor discovery in social feed to clinical consultation and prescription creation. |
| **Marketplace** | Batch Prescription Refill & Auto-Dispatch | `SHOULD CONSIDER` | High convenience for chronic medication patients; drives recurring domestic marketplace revenue. |
| **Seller Tools** | Bulk Inventory CSV Import & Barcode Scanner | `SHOULD CONSIDER` | Simplifies onboarding for high-volume pharmacies and medical supply businesses. |
| **Payments** | Apple Pay & Google Pay One-Tap Domestic Checkout | `MUST HAVE` | Lowers cart abandonment in domestic markets while preserving banking-grade 3D Secure verification. |
| **Delivery** | Cold-Chain Temperature Sensor Telemetry Logging | `SHOULD CONSIDER` | Verifies temperature-sensitive insulin and vaccine shipments remain between 2°C–8°C during transit. |
| **Social / Creator** | Accredited CME (Continuing Medical Education) Video Reels | `OPTIONAL` | Allows verified doctors to publish certified clinical education content with peer-reviewed badges. |
| **Messaging** | Voice-to-Text Transcription for E2EE Clinical Notes | `OPTIONAL` | Facilitates rapid dictation for busy clinicians during consultations. |
| **Calling** | Low-Bandwidth Adaptive AV1 Video Calling Codec | `SHOULD CONSIDER` | Enhances video consultation quality in rural areas with fluctuating 3G/4G connectivity. |
| **AI Studio** | Medical Terminology Plain-Language Explanation Assist | `SHOULD CONSIDER` | Patient-facing contextual tool explaining complex lab jargon with strict clinical disclaimer. |
| **Security** | Hardware Security Key (FIDO2 / WebAuthn) Login Support | `MUST HAVE` | Enterprise-grade login protection for Hospital and Clinic admin consoles. |
| **Accessibility** | Dynamic High-Contrast Mode & Screen Reader Optimizations | `MUST HAVE` | Guarantees full digital accessibility for visually impaired and elderly patients. |
| **International Marketplace**| Cross-Border International Healthcare Marketplace | `NOT RECOMMENDED FOR 2.3` | **Strictly Rejected:** Cross-border pharmaceutical shipping creates severe regulatory and customs liability. |
| **Unsolicited AI Diagnosis** | Autonomous AI Medical Diagnosis & Autonomous Prescribing | `NOT RECOMMENDED FOR 2.3` | **Strictly Rejected:** Violates medical safety regulations and patient sovereignty mandates. |

---

## 2. Formal 18-Point Requirement Specifications for `MUST HAVE` Candidates

In accordance with Healthogram requirement governance, every proposed 2.3 candidate feature must fulfill all 18 criteria.

### Candidate Specification 1: FHIR R4 Standardized Clinical Export
1. **Problem:** Patients moving between healthcare systems struggle to transfer structured medical histories without manual data re-entry.
2. **User:** `Individual` patient owning a Health Passport; authorized `Doctor`.
3. **Use Case:** Patient requests an authenticated export of their health passport; app generates a validated HL7 FHIR R4 JSON bundle containing conditions, observations, medications, and allergies.
4. **Business Purpose:** Positions Healthogram as an open, standard-compliant healthcare ecosystem, facilitating regulatory compliance with US 21st Century Cures Act and Saudi Seha standards.
5. **Security Impact:** Exports must be signed with patient's private key and encrypted with a user-supplied passphrase.
6. **Privacy Impact:** Zero cloud storage of export bundles; generated entirely in memory on-device and shared directly via Android ShareSheet.
7. **Data Model:** Mapping schema between Healthogram internal Room entities and `org.hl7.fhir.r4.model.Bundle`.
8. **API:** Pure client-side serialization; optional verification endpoint `POST /api/v1/health/fhir/validate`.
9. **UI:** "Export Health Records" button inside Health Passport settings with password prompt and format selector (FHIR JSON or Encrypted PDF).
10. **Backend:** Cloud Function for schema validation assistance if complex clinical attachments are present.
11. **Firebase:** Download logged to `/users/{uid}/health_passport_audit/{auditId}` for legal compliance.
12. **Testing:** Automated schema validation test against official HL7 FHIR validator test suites.
13. **Analytics:** Anonymous event `event_fhir_export_generated` (strictly zero payload metadata).
14. **Cost:** Negligible (on-device processing, zero external API costs).
15. **Dependencies:** `hapi-fhir-base` Android-optimized lightweight parser.
16. **Rollback:** Disable export button via Remote Config flag `fhir_export_enabled = false`.
17. **Feature Flag:** `feature_fhir_export_v2_3`.
18. **Country Requirements:** Standard compliant globally (`US, CA, GB, SA, AE, EG, IN`).

---

### Candidate Specification 2: Multi-Specialty Smart Appointment Booking
1. **Problem:** Patients discover verified doctors and clinics on Healthogram social feeds but must leave the app or call manually to schedule consultations.
2. **User:** `Individual` (patient), `Doctor`, `Clinic`, `Hospital`.
3. **Use Case:** Patient navigates to doctor's verified profile, selects "Book Consultation", picks an available 30-minute slot, chooses Teleconsultation or In-Clinic, and completes domestic deposit payment.
4. **Business Purpose:** Generates high-margin consultation fee commissions and unifies the social-to-clinical patient lifecycle.
5. **Security Impact:** Clinician calendar availability exposes working hours only; patient booking reasons are encrypted with provider's public key.
6. **Privacy Impact:** Appointment records isolated in private subcollection; invisible to social feed or public profiles.
7. **Data Model:** Entity `Appointment(id, patientUid, doctorUid, slotTime, mode, status, paymentRef)`.
8. **API:** `POST /api/v1/appointments/book`, `POST /api/v1/appointments/reschedule`, `POST /api/v1/appointments/cancel`.
9. **UI:** Interactive calendar picker in doctor profile; integrated booking status in Health Passport "Visits" tab.
10. **Backend:** Cloud Functions slot reservation engine with distributed transactional locks to prevent double-booking.
11. **Firebase:** Firestore collection `/appointments/{appointmentId}`, Cloud Messaging reminders 24h and 1h prior.
12. **Testing:** Concurrency collision testing (100 simultaneous bookings on single slot) verifying exactly 1 booking succeeds.
13. **Analytics:** Funnel metrics: `appointment_view_slots` → `appointment_slot_selected` → `appointment_confirmed`.
14. **Cost:** Low Firestore read/write operations; covered by platform booking service fee.
15. **Dependencies:** Existing Stripe/HyperPay payment infrastructure.
16. **Rollback:** Emergency kill switch `appointments_module_enabled = false`.
17. **Feature Flag:** `feature_appointment_booking_v2_3`.
18. **Country Requirements:** Domestic timezone handling and local currency payment gateway mapping per country.

---

### Candidate Specification 3: Google Pay & Apple Pay One-Tap Domestic Checkout
1. **Problem:** Manual credit card entry on mobile screens causes checkout friction and cart drop-offs during domestic purchases.
2. **User:** `Customer` purchasing healthcare products on domestic Marketplace.
3. **Use Case:** Customer taps "Buy with Google Pay" on product page; Android Google Pay sheet authorizes domestic card token; order placed in single tap.
4. **Business Purpose:** Increases marketplace checkout conversion rate by an estimated 22-30% in domestic markets.
5. **Security Impact:** Employs device-bound network tokens and biometric confirmation; zero raw card numbers touch app or server.
6. **Privacy Impact:** Zero storage of financial billing addresses beyond encrypted shipping manifest.
7. **Data Model:** Existing `Order` and `PaymentIntent` entities; payment method recorded as `GOOGLE_PAY` or `APPLE_PAY`.
8. **API:** Google Pay API Client SDK integrated into checkout pipeline.
9. **UI:** Official branded Google Pay button complying with Google Brand Guidelines.
10. **Backend:** Cloud Function Stripe/HyperPay intent handler receives `paymentMethodId` derived from Google Pay token.
11. **Firebase:** Logs transaction ID to double-entry ledger.
12. **Testing:** End-to-end sandbox payment tokenization test with mock bank issuers.
13. **Analytics:** Payment method breakdown: `payment_method_selected (card | google_pay)`.
14. **Cost:** Standard gateway processing fee (no additional Google fee).
15. **Dependencies:** `com.google.android.gms:play-services-wallet:19.4.0`.
16. **Rollback:** Remote Config toggle `google_pay_checkout_enabled = false`.
17. **Feature Flag:** `feature_google_pay_checkout_v2_3`.
18. **Country Requirements:** Supported across all 7 rollout countries with local currency tokenization.

---

## 3. Healthogram 2.3 Architectural Blueprint

The 2.3 architecture builds upon the proven 2.2 foundation without destabilizing core systems.

```
┌────────────────────────────────────────────────────────────────────────┐
│                   HEALTHOGRAM 2.3 ARCHITECTURE                         │
├────────────────────────────────────────────────────────────────────────┤
│                           PRESENTATION LAYER                           │
│  ┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐   │
│  │ Health Passport   │  │ Marketplace &     │  │ Social, Reels &   │   │
│  │ (FHIR & Vault)    │  │ Appointments      │  │ Communications    │   │
│  └─────────┬─────────┘  └─────────┬─────────┘  └─────────┬─────────┘   │
├────────────┼──────────────────────┼──────────────────────┼─────────────┤
│            │             DOMAIN & SERVICE LAYER          │             │
│  ┌─────────▼─────────┐  ┌─────────▼─────────┐  ┌─────────▼─────────┐   │
│  │ Identity, RBAC &  │  │ Financial Double- │  │ Logistics & OTP   │   │
│  │ 4-Device Session  │  │ Entry Ledger      │  │ Dispatch Engine   │   │
│  └─────────┬─────────┘  └─────────┬─────────┘  └─────────┬─────────┘   │
├────────────┼──────────────────────┼──────────────────────┼─────────────┤
│            │             DATA & PERSISTENCE LAYER        │             │
│  ┌─────────▼─────────┐  ┌─────────▼─────────┐  ┌─────────▼─────────┐   │
│  │ Local Room DB     │  │ Firestore Private │  │ CMEK-Encrypted    │   │
│  │ (SQLCipher)       │  │ Sharded Datastore │  │ Cloud Storage     │   │
│  └───────────────────┘  └───────────────────┘  └───────────────────┘   │
├────────────────────────────────────────────────────────────────────────┤
│                          INFRASTRUCTURE & OPS                          │
│  • Google Cloud Functions (Idempotent, Min Instances = 1)              │
│  • Play Integrity & App Check Token Attestation                        │
│  • Firebase Remote Config Modular Kill Switches                        │
│  • Continuous Non-Destructive Backup & 14-min RTO Sandbox Restore      │
└────────────────────────────────────────────────────────────────────────┘
```

### Architectural Principles for 2.3
1. **Strict Backward Compatibility:** Version 2.3 API contracts and Firestore schemas will maintain 100% backward compatibility with 2.2.x clients.
2. **Modular Service Abstraction:** New appointment and FHIR modules will be developed as isolated feature packages (`com.example.feature.appointments`, `com.example.feature.fhir`) with zero coupling to legacy social feed code.
3. **Schema Versioning & Safe Migration:** Database migrations will use additive-only schema evolution (`version = 3`) with automated unit tests for Room data migration.
4. **Feature-Flag Driven Deployment:** Every 2.3 enhancement will deploy behind an individual Remote Config kill switch before enablement.
