# Health Passport 2.1 Technical Specification

## 1. Executive Summary
Health Passport 2.1 is Healthogram's patient-centered, cryptographically attributed clinical data core. It bridges offline patient-centric control with global interoperability (HL7 FHIR R4, Android Health Connect) while preserving absolute architectural isolation from social feeds and marketplace indices.

---

## 2. Structural & Data Isolation Boundaries

Healthogram implements strict multi-tier data compartmentalization in Cloud Firestore:

| Collection Path | Accessibility | Encryption & Security Model | Invariants |
| :--- | :--- | :--- | :--- |
| `users/{uid}` | Public Social Profile | Server-side default | Contains only public bio, verification badge, and display name. **Never contains medical facts.** |
| `health_passport/{uid}` | Private Clinical Core | AES-GCM-256 field-level client encryption | Accessible only by patient or verified clinicians with active `ScopedConsentGrant`. |
| `health_documents/{docId}` | Raw Medical Artifacts | Private GCS bucket + Signed URLs | Scoped time-bound access URLs (15 min expiry). Malware/AV scanned upon upload. |
| `health_access_grants/{id}` | Clinical Consent Grants | Time-to-live indexed rules | Granular category permissions (`ALLERGIES`, `MEDICATIONS`, `CONDITIONS`, `LAB_REPORTS`, `VITALS`). |
| `health_access_logs/{id}` | Immutable Access Audit | Append-only Cloud Spanner/Firestore | Logs every viewing clinician UID, facility, category viewed, and emergency overrides. |
| `health_qr_sessions/{token}` | Ephemeral QR Handshakes | Nonce-hashed, 5-minute expiry | Single-use or short-lived cryptographic tokens for in-person clinic admission. |

---

## 3. Supported Clinical Entities & Schemas

Health Passport 2.1 provides schema definitions for:
1. **Conditions**: Active, remission, and resolved medical diagnoses with ICD-10 / SNOMED CT terminology codes.
2. **Allergies & Intolerances**: Criticalities (LOW, MODERATE, HIGH, CRITICAL), manifestation reactions, and allergen identifiers.
3. **Medications & Prescriptions**: Dosages, frequency, route of administration, prescribing doctor credentials, and refills.
4. **Laboratory & Diagnostic Reports**: Multi-analyte structured results, reference ranges (low/high), flags (NORMAL, HIGH, LOW, CRITICAL), and LOINC coding.
5. **Vitals & Observations**: Blood pressure, heart rate, oxygen saturation (SpO2), body temperature, fasting blood glucose, and respiratory rate.
6. **Encounters & Visits**: Inpatient admissions, outpatient consultations, telehealth virtual visits, and discharge summaries.
7. **Procedures**: Surgical and non-surgical clinical procedures with CPT / SNOMED CT codes.
8. **Immunizations**: Vaccines administered, lot numbers, CVX codes, dosage, and administering clinician credentials.
9. **Care Plans**: Longitudinal care objectives, dietary interventions, exercise targets, and target completion dates.
10. **Emergency Medical Cards**: Minimal, opt-in critical health indicators for first responders.

---

## 4. Provenance & Version History Model

Every record in Health Passport 2.1 contains an immutable `HealthRecordProvenance` payload:
```kotlin
data class HealthRecordProvenance(
    val recordId: String,
    val recordType: String,
    val patientUid: String,
    val createdByUid: String,
    val createdByAccountType: String, // INDIVIDUAL, DOCTOR, CLINIC, HOSPITAL, LABORATORY
    val organizationId: String? = null,
    val organizationName: String? = null,
    val sourceSystem: String, // "Healthogram Mobile v2.1", "Epic EHR", "Roche Analyzer"
    val sourceStatus: RecordSourceStatus,
    val isImported: Boolean = false,
    val externalRecordId: String? = null,
    val verificationStatus: String, // UNVERIFIED, PATIENT_VERIFIED, CLINICIAN_CONFIRMED, LAB_CERTIFIED
    val verifiedByUid: String? = null,
    val verifiedTimestamp: Long? = null,
    val version: Int = 1,
    val isSuperseded: Boolean = false,
    val supersededByRecordId: String? = null,
    val correctionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

### Invariants:
- **No Silent Overwrites**: Clinical entries are never updated in-place without incrementing `version` and marking previous versions as `isSuperseded = true`.
- **Attribution Preservation**: Inbound records from FHIR or paper digitization preserve the original creator, source system, and verification lineage permanently.
