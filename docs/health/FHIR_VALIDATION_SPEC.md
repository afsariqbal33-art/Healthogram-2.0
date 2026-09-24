# FHIR Validation Specification (FHIRValidationService)

## 1. Validation Architecture

The `FHIRValidationService` executes multi-layer validation before any external FHIR R4 resource is permitted into the Healthogram ingestion pipeline:

```
[Inbound FHIR Resource]
           │
           ▼
 [Layer 1: Structural Schema] ─── (JSON/Class schema, FHIR R4 core compliance)
           │
           ▼
 [Layer 2: Required Fields] ─── (id, status, subject/patient, code)
           │
           ▼
 [Layer 3: Reference Integrity] ─── (Subject must resolve to Patient/{id})
           │
           ▼
 [Layer 4: Terminology Bindings] ─── (LOINC, SNOMED CT, RxNorm, ICD-10, CVX)
           │
           ▼
 [Layer 5: Provenance & Version] ─── (R4 4.0.1 compliance, meta.versionId)
```

---

## 2. Validation Issue Classifications

Reports are categorized into three severities:
- `ERROR`: Halts ingestion immediately. The resource cannot be saved to the database. Examples: Missing `subject.reference`, missing `resource.id`, invalid administrative gender code.
- `WARNING`: Resource is ingested, but flagged for human clinician review. Examples: Observation without standard LOINC coding, Condition with non-standard clinical status string.
- `INFORMATION`: Informational notice regarding optional extensions or non-critical metadata.

---

## 3. Supported Terminology Systems

| Standard | URI Authority | Primary Use Case |
| :--- | :--- | :--- |
| **LOINC** | `http://loinc.org` | Laboratory test codes, vital sign observations |
| **SNOMED CT** | `http://snomed.info/sct` | Clinical diagnoses, findings, procedures, anatomical sites |
| **ICD-10-CM** | `http://hl7.org/fhir/sid/icd-10-cm` | Diagnostic billing and clinical conditions |
| **RxNorm** | `http://www.nlm.nih.gov/research/umls/rxnorm` | Standardized clinical drug identifiers and formulations |
| **CVX** | `http://hl7.org/fhir/sid/cvx` | Vaccine administered codes |
| **UCUM** | `http://unitsofmeasure.org` | Unified Code for Units of Measure (e.g. `mmol/L`, `mg/dL`, `kg`) |
