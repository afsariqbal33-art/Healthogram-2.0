# HEALTHOGRAM — THIRD-PARTY DATA SHARING & SUBPROCESSOR DIRECTORY

**Document Version:** 2.2.0  
**Classification:** Enterprise Privacy & Transparency Standard  
**Effective Date:** September 20, 2026  
**Audience:** Public, Users, Regulators, and Security Auditors  
**Owner:** Data Protection Officer (privacy@healthogram.app)

---

## 1. Zero Health Data Sharing Commitment

Healthogram maintains an uncompromising policy regarding Protected Health Information (PHI) and clinical records:

> **CORE PRINCIPLE:**  
> **Healthogram NEVER sells, rents, monetizes, or shares user health data, medical conditions, medications, diagnoses, biometric measurements, or Health Passport contents with ANY third-party advertising network, data broker, analytics vendor, or external marketing organization.**

Health Passport records are accessed exclusively by the patient owner and attending verified healthcare providers who have received an explicit, time-bounded, scoped cryptographic grant authorized directly by the patient.

---

## 2. Authorized Infrastructure Subprocessors

Healthogram engages carefully vetted cloud infrastructure and technology providers under strict Business Associate Agreements (BAA) and Data Processing Addendums (DPA) incorporating Standard Contractual Clauses (SCCs).

| Subprocessor Name | Corporate Entity | Service Provided | Data Categories Shared | Data Transfer Safeguards & Certifications | PHI Ingestion Permitted? |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Google Cloud Platform (GCP)** | Google LLC (USA/EU) | Core cloud computing, Google Kubernetes Engine, Cloud KMS, Cloud Functions | Encrypted application state, database backups, application logs | HIPAA BAA executed, ISO 27001, SOC 2 Type II, EU Model Clauses, CMEK encryption | **YES** (Covered under signed HIPAA Business Associate Agreement) |
| **Firebase Services** | Google LLC (USA/EU) | Authentication, Cloud Firestore, Cloud Storage, Cloud Messaging (FCM) | User authentication IDs, encrypted Firestore documents, storage media assets, device tokens | HIPAA BAA executed, SOC 2 Type II, ISO 27017, ISO 27018, EU GDPR compliant | **YES** (Covered under signed HIPAA Business Associate Agreement) |
| **Google AI Studio / Gemini API** | Google LLC (USA) | Generative AI assistant features (post caption generation, hashtag suggestions, translation) | Ephemeral creator prompts, social text snippets, translation source phrases | Stateless processing API, Zero data retention for model retraining, TLS 1.3 | **STRICTLY NO.** Application filters reject any request containing health identifiers. |
| **Stripe, Inc.** | Stripe, Inc. (USA/EU) | Payment gateway processing, merchant payouts, fraud screening | Buyer billing name, card tokens (PCI Level 1), payment amounts, seller payout bank accounts | PCI-DSS Level 1 Service Provider, SOC 2 Type II, TLS 1.3 | **NO.** Zero clinical data transmitted. Only commercial invoice amounts. |
| **Google Play In-App Billing** | Google LLC (USA) | Mobile digital subscriptions and creator tip processing | Google account obfuscated transaction ID, subscription tier status | PCI-DSS Level 1, Google Play Developer Terms | **NO.** Zero clinical data transmitted. |
| **Shipment Carriers (USPS, FedEx, DHL, Local Couriers)** | Designated delivery partners | Physical order fulfillment and parcel delivery | Customer delivery recipient name, shipping street address, contact phone number, package weight | Delivery Service Agreements, Carrier API token authorization | **NO.** Only physical marketplace order shipping labels. Zero health or medical passport data. |
| **Firebase Crashlytics** | Google LLC (USA) | Android client application crash and stability reporting | Stack traces, device OS version, hardware model, anonymous crash session ID | Google Privacy Terms, ISO 27001 | **NO.** Medical records and user PII are strictly filtered out of crash keys. |

---

## 3. Data Flow & Boundary Enforcement

```text
┌─────────────────────────────────────────────────────────────┐
│                 HEALTHOGRAM APPLICATION CORE               │
├──────────────────────────────┬──────────────────────────────┤
│    HEALTH PASSPORT (PHI)     │   SOCIAL & MARKETPLACE DATA  │
│  • Conditions & Diagnoses    │  • Public Posts & Comments   │
│  • Medications & Prescriptions│ • Product Listings & Carts   │
│  • Lab Reports & Encounters  │  • Creator Captions & Tips   │
└──────────────┬───────────────┴──────────────┬───────────────┘
               │                              │
     [HIPAA BAA Vault]              [Standard Commercial APIs]
               │                              │
               ▼                              ▼
    ┌────────────────────┐         ┌────────────────────┐
    │ Google Cloud /     │         │ Stripe / Carriers  │
    │ Firebase HIPAA BAA │         │ Gemini API (Social)│
    │ (Encrypted Storage)│         │ Crashlytics Logs   │
    └────────────────────┘         └────────────────────┘
               │                              │
       NO THIRD-PARTY                NON-PHI OPERATIONAL
       ADVERTISING OR                PROCESSING EXCLUSIVELY
       DATA BROKERS
```

---

## 4. Subprocessor Governance & Security Reviews

1. **Vendor Security Auditing:** Every third-party vendor undergoes an annual security posture review evaluating SOC 2 Type II compliance, vulnerability management, and data handling policies.
2. **Breach Notification:** All subprocessor agreements obligate the vendor to notify Healthogram's Security Incident Response Team within **24 hours** of discovering any security incident affecting platform data.
3. **Right to Audit:** Contracts preserve Healthogram's right to audit vendor compliance or inspect third-party certification reports on an annual basis.
