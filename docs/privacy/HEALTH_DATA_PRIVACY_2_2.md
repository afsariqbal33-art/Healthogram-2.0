# HEALTHOGRAM — HEALTH DATA PRIVACY & CLINICAL SAFEGUARDS SPECIFICATION 2.2

**Document Version:** 2.2.0  
**Classification:** Regulatory Healthcare Privacy Standard  
**Effective Date:** September 20, 2026  
**Audience:** Clinical Partners, Security Engineers, Healthcare Compliance Auditors  
**Owner:** Healthcare Security Architect & Chief Privacy Officer (privacy@healthogram.app)

---

## 1. Regulatory Framework & Standards

Healthogram Health Passport 2.2 is engineered to comply with the most stringent global healthcare data privacy standards:
- **HIPAA Security & Privacy Rules** (45 CFR Parts 160 and 164)
- **HITECH Act** (Health Information Technology for Economic and Clinical Health Act)
- **GDPR Special Category Data** (Article 9 — Processing of data concerning health)
- **HL7 FHIR Release 4 (R4)** (Fast Healthcare Interoperability Resources)
- **Android Health Connect** API privacy and security guidelines
- **FDA / IMDRF Digital Health Software Standards** for mobile medical record viewers

---

## 2. Zero-Trust Access Architecture for Health Passport

Access to any Health Passport record requires satisfying four simultaneous cryptographic and authorization criteria:

```text
       [Doctor / Clinic Request]
                  │
                  ▼
      ┌───────────────────────┐
      │ 1. Identity Check     │ Authenticated user with verified healthcare badge
      └───────────┬───────────┘
                  │
                  ▼
      ┌───────────────────────┐
      │ 2. Role Eligibility   │ Role in: ['doctor', 'clinic', 'hospital', 'laboratory']
      └───────────┬───────────┘
                  │
                  ▼
      ┌───────────────────────┐
      │ 3. Active Consent     │ Unexpired, approved HealthAccessGrant issued by Patient
      └───────────┬───────────┘
                  │
                  ▼
      ┌───────────────────────┐
      │ 4. Granular Scope     │ Target collection in grant.scopes (e.g. 'allergies')
      └───────────┬───────────┘
                  │
                  ▼
        [AUTHORIZED: Access Logged]
```

---

## 3. Scoped Granular Consent Engine

Healthogram rejects coarse "all-or-nothing" health data sharing. The patient maintains granular control over eleven distinct clinical scopes:

| Scope Identifier | Clinical Category | Data Elements Included | Eligible Accessing Roles | Default Status |
| :--- | :--- | :--- | :--- | :--- |
| `profile` | Demographics & Emergency Card | Blood group, emergency contacts, primary language, organ donor status | Doctor, Clinic, Hospital, Laboratory | Optional |
| `conditions` | Chronic & Acute Conditions | ICD-10 diagnosis codes, condition onset date, clinical status | Doctor, Clinic, Hospital | Denied until granted |
| `allergies` | Allergies & Adverse Reactions | Allergen substance, criticality level, manifestation reaction | Doctor, Clinic, Hospital | Denied until granted |
| `medications` | Pharmacotherapy & Dosages | Active medication name, dosage form, frequency, prescriber | Doctor, Clinic, Hospital | Denied until granted |
| `visits` | Clinical Encounters | Visit date, attending physician, chief complaint, follow-up date | Doctor, Clinic, Hospital | Denied until granted |
| `diagnoses` | Formal Diagnostic Assessments | Clinical summary, ICD-10 assessment, differential notes | Doctor, Clinic, Hospital | Denied until granted |
| `tests` | Diagnostic Orders | Ordered lab tests, radiology requests, pathology requests | Doctor, Clinic, Hospital, Laboratory | Denied until granted |
| `lab_reports` | Laboratory Test Results | Numerical test values, reference intervals, specimen timestamp | Doctor, Clinic, Hospital, Laboratory | Denied until granted |
| `prescriptions` | Medical Prescriptions | Prescription ID, drug regimen, refills authorized | Doctor, Clinic, Hospital | Denied until granted |
| `documents` | Clinical Files & Scans | Radiographs, ECG recordings, discharge summaries (PDF) | Doctor, Clinic, Hospital | Denied until granted |
| `bills` | Medical Invoices | Encounters fees, insurance co-pays | Doctor, Clinic, Hospital | Denied until granted |

---

## 4. Health QR Code Privacy Architecture

The Healthogram Health Passport QR generator implements strict privacy safeguards:

1. **Opaque Token Representation:**
   - The generated QR code encodes exclusively a temporary opaque session token (UUID v4) and cryptographic nonce:
     `healthogram://health-session/v2?token=7f9b8c2d-e4a1-4389-bc82-0192837465ae&exp=1726852800`
   - The QR code contains **ZERO** medical data, diagnosis strings, medication lists, or patient identifiers.
2. **Single-Use and Ephemeral Lifetime:**
   - QR tokens expire automatically after **10 minutes** if unconsumed.
   - Upon first valid scan by an authorized clinician, the token state transitions to `isConsumed = true`. Any subsequent scan is rejected immediately.
3. **Multi-Factor Clinical Verification:**
   - Scanning the QR code requires the attending clinician to be logged in with a verified institutional account and biometric authentication active on their mobile device.
   - The clinician must explicitly submit clinical purpose and requested scopes. The patient immediately receives an on-screen consent prompt detailing the request before any data is unlocked.

---

## 5. Immutable Access Logging & Patient Transparency

Every read, export, or print interaction with Health Passport data generates an immutable audit record in `health_access_logs`:
- **Accessor Information:** Doctor name, professional license number, institution UID, device ID, IP reference.
- **Grant Metadata:** Grant ID, purpose of consultation, authorized scopes accessed.
- **Timestamp:** High-precision server timestamp.
- **Patient Dashboard:** The patient can review the real-time "Who Accessed My Records" feed in their Health Passport settings, with instant one-tap revocation capability.
