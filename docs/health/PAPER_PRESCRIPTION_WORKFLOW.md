# Paper Prescription & Document Intelligence Workflow

## 1. Safety Directive & Non-Authoritative OCR Invariant
Under Healthogram Safety Architecture, **Optical Character Recognition (OCR) and multimodal AI models must NEVER silently convert unverified text into an authoritative medical fact.** 

Machine extractions are strictly categorized as **Candidate Fields** until an explicit human confirmation gate is passed.

---

## 2. Seven-Stage Ingestion Pipeline

```
[1. Secure Upload & AV] 
       │
       ▼
[2. Cryptographic Hashing] ─── (SHA-256 integrity check)
       │
       ▼
[3. OCR / Multimodal Extraction] ─── (Candidates + Confidence Scores)
       │
       ▼
[4. Mandatory Human Review UI] ─── (Side-by-side verification)
       │
       ▼
[5. Medical Disclaimer Gate] ─── (Explicit acknowledgment)
       │
       ▼
[6. Provenance Attribution] ─── (Source: DIGITIZED_FROM_PAPER)
       │
       ▼
[7. Health Timeline Persistence] ─── (Immutable audit log)
```

### Stage 1: Secure Upload & AV Scanning
- Files accepted: `image/jpeg`, `image/png`, `image/webp`, `application/pdf`.
- File size limit: 15MB.
- Scanned for malicious payload before storage in private clinical Google Cloud Storage bucket.

### Stage 2: Cryptographic Hashing
- Computes SHA-256 digest of original uploaded document to detect tampering or duplicate submissions.

### Stage 3: Candidate Field Extraction
- Extracts non-authoritative candidates:
  - Doctor Name candidate
  - Clinic/Hospital candidate
  - Prescription Date candidate
  - Medication name, dosage, frequency, route, duration
  - Numerical confidence score (0.0 to 1.0)
- Stored as `PaperPrescriptionDigitization` in status `PENDING_REVIEW`.

### Stage 4 & 5: Side-by-Side Review & Disclaimer Gate
- The mobile UI renders the original paper document alongside interactive editable fields.
- User or clinician verifies each dosage and frequency.
- The user must check the explicit medical disclaimer:
  *"I confirm that I have reviewed the extracted medications against the original paper document. This digital record does not replace physical consultations."*

### Stage 6 & 7: Provenance & Persistence
- Record is committed to `HealthTimelineService` with `RecordSourceStatus.DIGITIZED_FROM_PAPER`.
- Verification status tagged as `PATIENT_VERIFIED` or `CLINICIAN_CONFIRMED`.
- Audit record emitted to `PaperPrescriptionAuditLog`.
