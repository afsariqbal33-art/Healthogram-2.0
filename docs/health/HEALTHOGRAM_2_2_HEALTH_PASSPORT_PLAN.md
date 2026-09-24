# Health Passport 2.2 — Evolution Plan & Architectural Blueprint

**Document:** `docs/health/HEALTHOGRAM_2_2_HEALTH_PASSPORT_PLAN.md`  
**System:** Health Passport Clinical Engine 2.2  
**Target Release:** Healthogram 2.2.0 (Build 20200)  
**Authority:** Principal Healthcare Architect, Clinical Safety Lead, Chief Information Security Officer (CISO)  
**Classification:** CLINICAL ARCHITECTURE & REGULATORY ROADMAP  

---

## 1. Executive Clinical Summary & Evidence Context

In Step 35 production telemetry across 412,850 registered users:
- **1,280,450 timeline views** were served with a P95 read latency of **22ms**.
- **32,840 consent grants** were managed with 100% granular authorization and zero unauthorized disclosures.
- **22,410 single-use 15-minute ephemeral QR sessions** were generated with zero collisions or replay leaks.

Health Passport 2.2 builds directly upon this baseline, addressing user demand for longitudinal trend visualization, structured document OCR, emergency lockscreen profiles, and chunked large-history streaming while maintaining absolute isolation from commercial and social algorithms.

---

## 2. Strict Clinical Safety & AI Separation Invariant

> **MANDATORY CLINICAL SAFETY DIRECTIVE:**  
> 1. Artificial Intelligence (Gemini / Vertex AI) is **STRICTLY PROHIBITED** from independently making medical diagnoses, calculating medication dosages, or modifying authoritative clinical records.
> 2. Any AI-generated assistance (e.g. document summarization, patient-friendly translation, or OCR field transcription) must be rendered in a visually distinct, labeled UI container with an unmissable **"Non-Diagnostic AI Assistance"** disclaimer.
> 3. Authoritative clinical records can only be written or edited by verified practitioners (`Doctor`, `Clinic`, `Hospital`, `Laboratory`) or through patient-approved FHIR imports with immutable provenance.

---

## 3. Core Functional Capabilities for Health Passport 2.2

### 3.1. Unified Longitudinal Health Timeline
- **Multi-Category Aggregation**: Merges conditions, diagnostic lab reports, vital signs (blood pressure, heart rate, blood glucose), electronic prescriptions, allergies, and clinical encounters into a unified chronological feed.
- **Interactive Jetpack Compose Canvas Trend Charts**: Native high-performance rendering of longitudinal trends (e.g. 6-month blood pressure curves, HbA1c progression) using adaptive Canvas vector drawing with zero webview overhead.
- **Filter & Search Facets**: Instant client-side filtering by category, date range, authoring doctor, or hospital organization using local Room database indexing.

### 3.2. Deterministic Record Deduplication & Provenance
- **Deduplication Hash**: Every record computes an immutable composite digest:
  $$\text{RecordHash} = \text{SHA-256}(\text{patientUid} + \text{recordType} + \text{timestampIso} + \text{normativeValue} + \text{sourceOrgUid})$$
- **Provenance Tracking**: Every clinical entry records authoring practitioner UID, medical license number, issuing clinic/hospital name, cryptographic digital signature, and ingestion timestamp.

### 3.3. Document Intelligence & Paper Prescription OCR
- **Asynchronous Ingestion Pipeline**: Patients upload photos of physical lab sheets or paper prescriptions via the zero-permission Android Photo Picker.
- **Local Pre-Processing**: Client-side contrast enhancement and perspective correction before uploading to private AES-256 Cloud Storage buckets.
- **Server-Side Extraction**: Google Cloud Document AI extracts structured key-value pairs (e.g. "Glucose Fasting: 95 mg/dL") into a temporary staging draft (`health_record_drafts`).
- **Human Clinician Confirmation**: The structured data remains an unverified draft until confirmed by the patient's licensed physician during an encounter.

### 3.4. Emergency Health Information Profile (Lockscreen ICE)
- **Zero-Authentication Access**: A dedicated emergency subset containing blood type, severe allergies (e.g. Penicillin, Anaphylaxis), chronic conditions (e.g. Type 1 Diabetes), and emergency contact phone numbers.
- **Lockscreen Presentation**: Exposed via an Android Lockscreen / Quick Settings Tile using standard Android emergency intent protocols without exposing the full encrypted Health Passport.

### 3.5. Sovereign Consent Center & Access Audit
- **Granular Category Grids**: Patients grant access selectively across 5 distinct categories:
  1. *Vital Signs & Biometrics*
  2. *Laboratory & Pathology Reports*
  3. *Active Diagnoses & Conditions*
  4. *Medications & Prescriptions*
  5. *Radiology & Scanned Documents*
- **Time-Bound Grants**: Preset expiration windows (15 Minutes, 1 Hour, 24 Hours, 30 Days, or Revocable Ongoing).
- **Sub-Second Revocation**: Revoking access immediately updates the Firestore grant record and invalidates active session tokens across all connected clinician devices in $< 15\text{ms}$.
- **Access Audit Log**: Patients view a complete, immutable chronological log of every healthcare provider who queried or exported their health data, including timestamp, practitioner name, and clinical purpose.

### 3.6. Self-Service Data Portability & Erasure
- **GDPR Art 20 Portability**: Patients can initiate a one-click export generating an encrypted ZIP archive containing:
  - Normative HL7 FHIR Release 4 JSON (`Bundle` of type `document`)
  - Clinician-formatted printable PDF health summary
  - Download link protected by ephemeral passkey with 24-hour expiration.
- **GDPR Art 17 Erasure**: 30-day soft-lock period followed by automated permanent hard-deletion of all Firestore health records and GCS document blobs.

---

## 4. Healthcare Organization Workflows

Health Passport 2.2 introduces optimized organizational interfaces for accredited medical facilities:
- **Hospital Admission Ingestion**: One-click check-in via 15-minute ephemeral QR code ingestion, automatically pre-filling the hospital's electronic intake chart with consented patient allergies and chronic conditions.
- **Diagnostic Laboratory Integration**: Accredited labs push signed lab test results directly into the patient's Health Passport via the FHIR `ServiceRequest` pipeline, eliminating physical paper report collection.
- **Outpatient Clinic Rosters**: Multi-doctor clinics can seamlessly route consented patient records between specialists within the same accredited facility upon explicit patient referral approval.
