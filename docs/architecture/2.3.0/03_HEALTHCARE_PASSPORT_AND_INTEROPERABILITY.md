# HEALTHOGRAM 2.3: HEALTH PASSPORT, HEALTHCARE ECOSYSTEM & INTEROPERABILITY

**Document ID:** HGM-2.3-ARCH-03-HEALTH  
**Phase:** Step 48 Architecture & Planning  
**Target Release:** Healthogram Version `2.3.0`  
**Git Branch:** `develop/healthogram-2-3`  
**Timestamp:** 2026-09-22T06:25:00Z  
**Governing Standards:** HL7 FHIR Release 4, Android Health Connect API, HIPAA Privacy & Security Rules, GDPR Article 9  

---

## 1. Health Passport 2.3 Architectural Plan

Health Passport in Version 2.3 strengthens patient sovereignty while introducing granular clinical scopes and an optional, strictly regulated Emergency Access workflow.

### A. Granular Clinical Scope Presets
Rather than granting broad "all or nothing" access, Version 2.3 allows patients to select explicit clinical scopes during QR consent challenge:
* `SCOPE_EMERGENCY_SUMMARY` (Allergies, blood type, active medications, emergency contacts only)
* `SCOPE_LAB_REPORTS_ONLY` (Diagnostic pathology and imaging reports)
* `SCOPE_PRESCRIPTIONS_ONLY` (Active prescriptions and dispensing logs)
* `SCOPE_CONSULTATION_VISIT` (Encounter notes, diagnoses, and vitals for current visit)
* `SCOPE_FULL_CLINICAL_TIMELINE` (Complete encrypted medical history)

### B. Dynamic QR Token & Single-Use Session Lifecycle
* **Generation:** Generated on device using a client-side ephemeral keypair.
* **Payload:** Contains zero raw PHI:
  ```json
  {
    "token": "qr_vault_sess_2a8b9c",
    "fingerprint": "sha256_e4c3b2...",
    "expiresAt": 1790076060,
    "nonce": "7f8a9b0c1d2e"
  }
  ```
* **Strict TTL:** 60 seconds maximum lifetime. Single-use only.
* **Revocation:** Instantaneous via patient UI. Server invalidates session token in Redis and writes an immutable revocation event to `/health_access_logs/`.

### C. Emergency Access Workflow (Break-Glass Protocol)
* **Legal & Consent Model:** Emergency access is **disabled by default**. It can only be activated if the patient explicitly toggles "Allow Emergency Access" in their Health Passport settings and signs a digital consent directive.
* **Access Criteria:** Limited exclusively to accredited `Hospital` or `Emergency Clinic` accounts with active hardware verification.
* **Scope Constraint:** Strictly restricted to `SCOPE_EMERGENCY_SUMMARY` (Allergies, chronic conditions, blood group, emergency contact). Mental health records and billing records are completely inaccessible.
* **Audit & Immediate Notification:** Any emergency access immediately generates a maximum-priority push notification and SMS to the patient and their designated emergency contacts. An immutable audit record is logged with the attending physician's national medical license ID.

---

## 2. Healthcare Organization & Verification Architecture

Healthogram maintains strict separation between clinical institutions and commercial entities:

### A. Clinical Institutional Roles
1. **Doctor:** Verified practitioner profile; manages calendar availability, teleconsultations, and signs digital prescriptions with PKI cryptographic keys.
2. **Clinic:** Verified outpatient facility; manages multi-doctor rosters, shared clinical rooms, and outpatient scheduling.
3. **Hospital:** Inpatient health system; manages clinical departments, emergency triage units, and multi-specialty teams.
4. **Laboratory:** Diagnostic testing center; receives patient test orders and uploads authorized, signed digital lab reports directly to patient vaults. (Note: Laboratories do not have personal Health Passports).

### B. Country-Aware Verification Framework
Verification is platform verification, not a substitute for government licensing:
* **United States (US):** NPI (National Provider Identifier) registry validation + State Medical Board license check.
* **Saudi Arabia (SA):** SCFHS (Saudi Commission for Health Specialties) registration + Seha accreditation.
* **United Arab Emirates (AE):** DHA / DOH / MOHAP medical license verification.
* **Egypt (EG):** Egyptian Medical Syndicate registration verification.
* **India (IN):** NMC (National Medical Commission) registration + state council certificate.
* **United Kingdom (GB):** GMC (General Medical Council) reference number lookup.
* **Canada (CA):** Provincial College of Physicians and Surgeons registration.

---

## 3. HL7 FHIR Interoperability Architecture

Version 2.3 introduces standardized clinical export and import complying with **HL7 FHIR Release 4 (R4)**:

### A. Supported FHIR R4 Resources
| Internal Healthogram Entity | Mapped FHIR R4 Resource | Mapping Cardinality | Encryption & Privacy Treatment |
| :--- | :--- | :---: | :--- |
| `UserProfile` (Clinical subset) | `Patient` | 1:1 | De-identified; exports only patient-consented demographic fields |
| `DoctorProfile` | `Practitioner` | 1:1 | Public professional registry attributes |
| `Clinic` / `Hospital` | `Organization` | 1:1 | Accreditation metadata and national facility identifier |
| `ClinicalEncounter` | `Encounter` | 1:1 | Encounter class (AMB/EMER), service provider reference |
| `PatientCondition` | `Condition` | 1:1 | SNOMED-CT / ICD-10 clinical coding |
| `PatientAllergy` | `AllergyIntolerance` | 1:1 | Criticality, verification status, allergen substance code |
| `Prescription` | `MedicationRequest` | 1:1 | RxNorm / ATC medication coding, dosage instructions |
| `VitalSigns` | `Observation` | 1:Many | LOINC coded observations (heart rate, blood pressure, SpO2) |
| `LabReport` | `DiagnosticReport` | 1:1 | LOINC diagnostic code with encapsulated signed PDF document |
| `MedicalDocument` | `DocumentReference` | 1:1 | MIME type, attachment hash, CMEK storage reference |
| `AppointmentBooking` | `Appointment` | 1:1 | Start/end time, participant references, appointment status |

### B. FHIR Processing Pipeline
1. **On-Device Export:** The export bundle is assembled entirely in-memory using Android-optimized `hapi-fhir-base`. Zero unencrypted clinical data touches the server during export.
2. **Provenance & Cryptographic Signing:** Each exported bundle contains a `Provenance` resource containing the SHA-256 digest of the clinical records, digitally signed with the patient's local Keystore key.
3. **Rate Limiting:** Export operations are limited to 3 exports per 24 hours per account to prevent data exfiltration abuse.

---

## 4. Android Health Connect Integration

Healthogram 2.3 integrates with the official **Android Health Connect API** as an optional, patient-controlled health data bridge:

### A. Supported Data Types
* `StepsRecord` & `DistanceRecord` (Physical activity)
* `HeartRateRecord` & `RestingHeartRateRecord` (Cardiovascular vitals)
* `SleepSessionRecord` (Sleep staging metrics)
* `BloodGlucoseRecord` (Diabetic health tracking)
* `BloodPressureRecord` (Hypertension monitoring)
* `OxygenSaturationRecord` (SpO2 levels)

### B. Privacy & Synchronization Controls
* **Granular Permission Handshake:** Health Connect permissions are requested via the standard Android system consent dialog. Users can permit reading steps while revoking blood glucose access.
* **Deduplication Engine:** Health Connect records are ingested using source package and client record ID (`record.metadata.clientRecordId`) to eliminate duplicate ingestion.
* **Zero Cloud Leakage Without Consent:** Ingested Health Connect metrics reside strictly in the local encrypted Room database. They are only backed up to the patient's private Firestore vault if the user explicitly toggles "Cloud Vault Sync".

---

## 5. Multi-Specialty Smart Appointment Booking Architecture

The appointments subsystem connects patient discovery to clinical consultations:

```
[PATIENT SELECTS DOCTOR] ──► [FETCH AVAILABLE SLOTS]
                                     │
                                     ▼
                          [SELECT 30-MIN TIME SLOT]
                                     │
                                     ▼
                          [ACQUIRE REDIS LOCK (5m TTL)]
                                     │
                                     ▼
                          [PAY DEPOSIT / AUTHORIZE]
                                     │
                                     ▼
                          [COMMIT TRANSACTION IN FIRESTORE]
                                     │
                                     ▼
                          [DISPATCH FCM REMINDER HOOKS]
```

### Key Concurrency & Reliability Controls
* **Distributed Slot Locking:** When a patient selects a time slot, the backend acquires an atomic distributed lock in Redis (`lock:doctor_{uid}_slot_{timestamp}`) with a 300-second TTL. This completely prevents double-booking race conditions during high traffic.
* **Double-Booking Prevention:** Firestore transactional write enforces:
  `transaction.get(slotRef).isBooked == false`. If another write occurred, transaction aborts gracefully with a user-friendly retry prompt.
* **Timezone Normalization:** All appointment timestamps are stored in UTC (`ISO 8601`) and converted to provider and patient local timezones at display time.
* **Cancellation & Refund Automation:** Cancellations made > 24 hours prior to appointment time trigger automated 100% refund of deposit via the double-entry ledger refund hook.
