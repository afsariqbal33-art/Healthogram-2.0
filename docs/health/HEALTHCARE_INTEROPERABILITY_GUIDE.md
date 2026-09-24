# Healthcare Interoperability & FHIR R4 Integration Guide

**Version:** 2.2.0  
**Domain:** FHIR Interoperability & Medical Exchange  
**Standard:** HL7 FHIR Release 4.0.1  

---

## 1. Architectural Scope & Principles

Healthogram 2.2 incorporates full bidirectional FHIR R4 mapping across primary clinical resources. The integration layer adheres strictly to the following invariants:
1. **Unbroken Provenance:** Every imported or exported FHIR resource encapsulates origin metadata (`sourceSystem`, `author`, `verificationStatus`).
2. **Conflict Detection (Non-Destructive Ingestion):** External observations never overwrite existing timeline data silently. Conflicting clinical records trigger a `HealthRecordConflict` alert requiring patient or clinician reconciliation.
3. **Strict Terminology Binding:** Codes are validated against standardized normative coding systems (LOINC for observations/labs, SNOMED-CT for conditions/allergies, RxNorm for medications, and ICD-10 for diagnostic billing).

---

## 2. Resource Mapping Schema

| Healthogram Model | Normative FHIR Resource | Primary Coding System | Validation Constraints |
| :--- | :--- | :--- | :--- |
| `HealthProfile` | `Patient` | ISO 3166 / Internal HealthID | Valid birthDate, active boolean |
| `HealthObservation` | `Observation` | LOINC (http://loinc.org) | status='final', subject reference, valueQuantity/String |
| `HealthCondition` | `Condition` | SNOMED-CT (http://snomed.info/sct) | clinicalStatus, verificationStatus, onsetDateTime |
| `HealthAllergy` | `AllergyIntolerance` | SNOMED-CT / RxNorm | criticality, category, manifestation |
| `HealthMedication` | `MedicationStatement` | RxNorm (http://www.nlm.nih.gov/research/umls/rxnorm) | dosage, frequency, status='active' |
| `DiagnosticReportRecord` | `DiagnosticReport` | LOINC | issued date, performer reference, results array |
| `PaperPrescriptionDraft` | `MedicationRequest` | RxNorm | intent='order', requester, authoredOn |
| `HealthEncounterSummary`| `Encounter` | CPT / SNOMED | status='finished', class, period |

---

## 3. Ingestion & Conflict Resolution Workflow

When an external FHIR resource arrives via partner adapter or patient import:

```
[Incoming FHIR Resource]
         │
         ▼
[Schema Validation] ───────────► (Fails) ──► Ingestion Rejected (HTTP 422)
         │
         ▼ (Valid)
[Duplicate / Conflict Check]
   ├─ Matching record & timestamp? ────────► Flag as DUPLICATE_SKIPPED
   ├─ Matching type, close time, diff val? ─► Register HealthRecordConflict
   └─ Clean record ────────────────────────► Ingest with IMPORTED_UNVERIFIED status
```

### 3.1 Conflict Resolution Options
When a conflict is detected, it enters `activeConflicts` with status `UNRESOLVED`. The patient or authorized clinician can resolve via:
- `RESOLVED_USE_EXISTING`: Retain Healthogram's current authoritative value.
- `RESOLVED_USE_INCOMING`: Accept incoming external provider value.
- `RESOLVED_KEEP_BOTH`: Annotate both records in timeline with explanatory clinical note.

---

## 4. Export Bundles & Portability

Patients have full sovereignty to export their complete clinical history or scoped subsets in three formats:
1. **FHIR R4 Bundle (`FHIR_R4_BUNDLE`):** Normative JSON collection containing all resources with intact provenance references.
2. **Structured JSON (`STRUCTURED_JSON`):** Developer-friendly normalized payload.
3. **Clinical PDF Summary (`PDF_SUMMARY`):** Human-readable clinical document formatted for presentation to non-digital care providers.

All exports generate an audited, signed record with a strict **24-hour expiration window** before transient download links are permanently invalidated.
